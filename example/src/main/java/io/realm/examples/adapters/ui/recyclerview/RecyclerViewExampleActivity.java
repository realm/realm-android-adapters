/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.examples.adapters.ui.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

import io.realm.Realm;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.model.Counter;
import io.realm.examples.adapters.model.DataHelper;
import io.realm.examples.adapters.model.Parent;
import io.realm.examples.adapters.ui.DividerItemDecoration;

public class RecyclerViewExampleActivity extends AppCompatActivity {

    private Realm realm;
    private RecyclerView recyclerView;
    private Menu menu;
    private MyRecyclerViewAdapter adapter;

    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        TouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            DataHelper.deleteItemAsync(realm, viewHolder.getItemId());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        realm = Realm.getDefaultInstance();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        setUpRecyclerView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    for (int i = 0; i < 3; i++) {
                        Counter.create(realm, true);
                    }
                    realm.commitTransaction();
                    /*
                    try {
                        DataHelper.randomAddItemAsync(realm);
                    } catch (RejectedExecutionException ignored){
                    }
                    */
                    realm.close();
                    /*
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Realm realm = Realm.getDefaultInstance();
                    Random rand = new Random();
                    long count = realm.where(Counter.class).count();
                    if (count == 0) {
                        realm.close();
                        return;
                    }
                    realm.beginTransaction();
                    Counter.delete(realm, rand.nextInt((int) count));
                    realm.commitTransaction();
                    /*
                    try {
                        DataHelper.deleteItemAsync(realm, rand.nextInt((int) count));
                    } catch (RejectedExecutionException ignored) {

                    }*/
                    realm.close();
                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */
                }
            }
        }).start();
    }

    /*
     * It is good practice to null the reference from the view to the adapter when it is no longer needed.
     * Because the <code>RealmRecyclerViewAdapter</code> registers itself as a <code>RealmResult.ChangeListener</code>
     * the view may still be reachable if anybody is still holding a reference to the <code>RealmResult>.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.listview_options, menu);
        menu.setGroupVisible(R.id.group_normal_mode, true);
        menu.setGroupVisible(R.id.group_delete_mode, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add:
                DataHelper.addItemAsync(realm);
                return true;
            case R.id.action_random:
                DataHelper.randomAddItemAsync(realm);
                return true;
            case R.id.action_start_delete_mode:
                adapter.enableDeletionMode(true);
                menu.setGroupVisible(R.id.group_normal_mode, false);
                menu.setGroupVisible(R.id.group_delete_mode, true);
                return true;
            case R.id.action_end_delete_mode:
                DataHelper.deleteItemsAsync(realm, adapter.getCountersToDelete());
                // Fall through
            case R.id.action_cancel_delete_mode:
                adapter.enableDeletionMode(false);
                menu.setGroupVisible(R.id.group_normal_mode, true);
                menu.setGroupVisible(R.id.group_delete_mode, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView() {
        adapter = new MyRecyclerViewAdapter(realm.where(Parent.class).findFirst().getCounterList());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

}
