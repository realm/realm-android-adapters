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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.examples.adapters.MyApplication;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.model.Counter;

class MyRecyclerViewAdapter extends RealmRecyclerViewAdapter<Counter, MyRecyclerViewAdapter.MyViewHolder> {

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<Integer>();

    MyRecyclerViewAdapter(OrderedRealmCollection<Counter> data) {
        super(data, true);
        setHasStableIds(true);
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Counter obj = getItem(position);
        holder.data = obj;
        //noinspection ConstantConditions
        holder.title.setText(obj.getCountString());
        holder.deletedCheckBox.setChecked(countersToDelete.contains(obj.getCount()));
        if (inDeletionMode) {
            holder.deletedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        countersToDelete.add(obj.getCount());
                    } else {
                        countersToDelete.remove(obj.getCount());
                    }
                }
            });
        } else {
            holder.deletedCheckBox.setOnCheckedChangeListener(null);
        }
        holder.deletedCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);

        File folder = MyApplication.context.getFilesDir();

        File imgFile = new File(folder, String.format("%s.JPG", position % 9));

        if(imgFile.exists()){
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = true;
            options.inSampleSize = 4;


            holder.bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

            holder.imageView.setImageBitmap(holder.bitmap);
        }
    }


    @Override
    public void onViewRecycled(MyViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.bitmap != null) {
            holder.bitmap.recycle();
        }
        holder.imageView.setImageBitmap(null);
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getCount();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CheckBox deletedCheckBox;
        ImageView imageView;
        Bitmap bitmap;
        public Counter data;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.textview);
            deletedCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
            imageView = (ImageView) view.findViewById(R.id.imageView);
        }
    }
}
