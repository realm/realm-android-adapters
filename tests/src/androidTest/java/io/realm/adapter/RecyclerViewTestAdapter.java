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

package io.realm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.entity.AllJavaTypes;

public class RecyclerViewTestAdapter extends RealmRecyclerViewAdapter<AllJavaTypes, RecyclerViewTestAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        ViewHolder(final View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    private LayoutInflater inflater;

    // TODO: Remove context dependency.
    public RecyclerViewTestAdapter(final Context context, final OrderedRealmCollection<AllJavaTypes> realmResults, final boolean automaticUpdate) {
        super(realmResults, automaticUpdate);
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        AllJavaTypes item = getItem(position);
        if (item != null) {
            holder.textView.setText(item.getFieldString());
        }
    }

    @Override
    public long getItemId(int position) {
        return getData().get(position).getFieldLong();
    }
}
