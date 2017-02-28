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
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.model.Counter;
import io.realm.examples.adapters.ui.DividerItemDecoration;

public class RecyclerViewExampleActivity extends AppCompatActivity {

    private Realm realm;
    private RecyclerView recyclerView;
    private Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        realm = Realm.getDefaultInstance();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        setUpRecyclerView();

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
        getMenuInflater().inflate(R.menu.listview_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add:
                addItem();
                return true;
            case R.id.action_random:
                randomEditItem();
                return true;
            case R.id.action_delete_all:
                deleteAllItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyRecyclerViewAdapter(
                this, realm.where(Counter.class).findAllSortedAsync(Counter.FIELD_COUNT)));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    // Randomly duplicate/delete/create some objects in the Realm.
    private void randomEditItem() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Counter> results = realm.where(Counter.class).findAllSorted(Counter.FIELD_COUNT);
                int maxEdits = 3;

                // Duplicate some existing entries.
                int countToDup = results.size() > maxEdits ? maxEdits : results.size();
                for (int i = 0; i < countToDup; i++) {
                    int nextValue = results.get(rand.nextInt((results.size()))).getCount();
                    realm.createObject(Counter.class).setCount(nextValue);
                }

                int countToDelete = results.size() > maxEdits ? maxEdits : results.size();
                for (int i = 0; i < countToDelete; i++) {
                    results.get(rand.nextInt((results.size()))).deleteFromRealm();
                }

                for (int i = 0; i < maxEdits; i++) {
                    realm.createObject(Counter.class).increment();
                }
            }
        });
    }

    private void addItem() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(Counter.class).increment();
            }
        });
    }

    private void deleteAllItems() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }

    public void deleteItem(Counter item) {
        final int id = Integer.valueOf(item.getCountString());
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Counter.class).equalTo(Counter.FIELD_COUNT, id)
                        .findAll()
                        .deleteAllFromRealm();
            }
        });
    }
}
