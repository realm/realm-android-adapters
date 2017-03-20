package io.realm.examples.adapters.ui.databinding;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.model.DataHelper;
import io.realm.examples.adapters.model.Owner;
import io.realm.examples.adapters.ui.DividerItemDecoration;

public class OwnerListActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_list);
        realm = Realm.getDefaultInstance();

        setUpRecyclerView((RecyclerView) findViewById(R.id.recycler_view));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.owner_list_view_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                DataHelper.addOwnerAsync(realm, 1);
                return true;
            default:
                break;
        }
        return false;
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
        RealmResults<Owner> results = realm.where(Owner.class).findAllAsync();
        OwnerListViewAdapter adapter = new OwnerListViewAdapter(results);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }
}
