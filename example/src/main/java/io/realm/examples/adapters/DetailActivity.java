/*
 *
 *  * Copyright 2016 Realm Inc.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package io.realm.examples.adapters;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.examples.adapters.model.Item;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "EXTRA_ITEM";
    public static final int REQUEST_CODE = 1001;

    private ImageButton up;
    private ImageButton down;
    private TextView count;
    private Button save;

    private Item item;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        realm = Realm.getDefaultInstance();

        up = (ImageButton) findViewById(R.id.up);
        down = (ImageButton) findViewById(R.id.down);
        count = (TextView) findViewById(R.id.count);
        save = (Button) findViewById(R.id.save);

        item = getIntent().getParcelableExtra(EXTRA_ITEM);

        count.setText(String.valueOf(item.getCount()));

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.upCount();
                count.setText(String.valueOf(item.getCount()));
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.downCount();
                count.setText(String.valueOf(item.getCount()));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(item);
                    }
                });
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
