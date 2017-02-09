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

package io.realm.examples.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Map;
import java.util.TreeMap;

import io.realm.examples.adapters.ui.listview.ListViewExampleActivity;
import io.realm.examples.adapters.ui.recyclerview.RecyclerViewExampleActivity;

public class MainActivity extends AppCompatActivity {

    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (ViewGroup) findViewById(R.id.list);
        setupButtons();
    }
    
    private void setupButtons() {
        setupButton("ListView", ListViewExampleActivity.class);
        setupButton("RecyclerView", RecyclerViewExampleActivity.class);
    }

    private void setupButton(String label, final Class<? extends Activity> activityClass) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(activityClass);
            }
        });
        container.addView(button);
    }
    
    /*package*/ void startActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
