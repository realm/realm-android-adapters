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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.model.Counter;

public class MyRecyclerViewAdapter extends RealmRecyclerViewAdapter<Counter, MyRecyclerViewAdapter.MyViewHolder> {

    private final RecyclerViewExampleActivity activity;

    public MyRecyclerViewAdapter(RecyclerViewExampleActivity activity, OrderedRealmCollection<Counter> data) {
        super(data, true);
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Counter obj = getData().get(position);
        holder.data = obj;
        holder.title.setText(obj.getCountString());
    }

    public void swap(int firstPos, int secondPos) {
        Realm realm = Realm.getDefaultInstance();
        RealmList<Counter> list = (RealmList<Counter>) getData();
        Counter counter1 = list.get(firstPos);
        Counter counter2 = list.get(secondPos);
        realm.beginTransaction();
        list.set(firstPos, counter2);
        list.set(secondPos, counter1);
        realm.commitTransaction();
        realm.beginTransaction();
        realm.cancelTransaction();
    }

    class MyViewHolder extends RecyclerView.ViewHolder /* implements View.OnLongClickListener */{
        public TextView title;
        public Counter data;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.textview);
            //view.setOnLongClickListener(this);
        }

        /*
        @Override
        public boolean onLongClick(View v) {
            activity.deleteItem(data);
            return true;
        }
        */
    }
}
