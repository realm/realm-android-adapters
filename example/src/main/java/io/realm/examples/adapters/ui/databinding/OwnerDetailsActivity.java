package io.realm.examples.adapters.ui.databinding;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.RealmObjectDataBindingListener;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.databinding.ActivityOwnerDetailsBinding;
import io.realm.examples.adapters.model.Dog;
import io.realm.examples.adapters.model.Owner;

public class OwnerDetailsActivity extends AppCompatActivity {

    public static String EXTRA_DATA_OWNER_ID = "owner_id";

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        int id = getIntent().getIntExtra(EXTRA_DATA_OWNER_ID, -1);
        Owner owner = realm.where(Owner.class).equalTo("id", id).findFirst();
        Dog dog = owner.getDog();

        final ActivityOwnerDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_owner_details);
        binding.setOwner(owner);
        binding.setDog(owner.getDog());

        owner.addChangeListener(
                new RealmObjectDataBindingListener.Builder<Owner>(binding)
                        .observe("name", R.id.owner_name_text_edit)
                        .onDeleted(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        })
                        .build());
        dog.addChangeListener(
                new RealmObjectDataBindingListener.Builder<Dog>(binding)
                        .observe("name", R.id.dog_name_text_edit)
                        .build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
