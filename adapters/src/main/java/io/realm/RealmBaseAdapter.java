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

package io.realm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

/**
 * The RealmBaseAdapter class is an abstract utility class for binding UI elements to Realm data, much like an
 * {@link android.widget.CursorAdapter}.
 * <p>
 * This adapter will automatically handle any updates to its data and call {@link #notifyDataSetChanged()} as
 * appropriate.
 * <p>
 * The RealmAdapter will stop receiving updates if the Realm instance providing the {@link io.realm.RealmResults} is
 * closed. Trying to access Realm objects will at this point also result in a {@code IllegalStateException}.
 */
public abstract class RealmBaseAdapter<T extends RealmModel> extends BaseAdapter {

    protected LayoutInflater inflater;
    @Nullable
    protected OrderedRealmCollection<T> adapterData;
    @NonNull
    protected Context context;
    private final RealmChangeListener listener;

    public RealmBaseAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<T> data) {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.adapterData = data;
        this.inflater = LayoutInflater.from(context);
        this.listener = new RealmChangeListener<BaseRealm>() {
            @Override
            public void onChange(BaseRealm results) {
                notifyDataSetChanged();
            }
        };

        if (data != null) {
            addListener(data);
        }
    }

    private void addListener(@NonNull OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            realmList.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeListener(@NonNull OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.realm.handlerController.removeWeakChangeListener(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            realmList.realm.handlerController.removeWeakChangeListener(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    /**
     * Returns how many items are in the data set.
     *
     * @return the number of items.
     */
    @Override
    public int getCount() {
        if (adapterData == null) {
            return 0;
        }
        return adapterData.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     * data set.
     * @return The data at the specified position.
     */
    @Override
    @Nullable
    public T getItem(int position) {
        if (adapterData == null) {
            return null;
        }
        return adapterData.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list. Note that item IDs are not stable so you
     * cannot rely on the item ID being the same after {@link #notifyDataSetChanged()} or
     * {@link #updateData(OrderedRealmCollection)} has been called.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        // TODO: find better solution once we have unique IDs
        return position;
    }

    /**
     * Updates the data associated with the Adapter.
     *
     * Note that RealmResults and RealmLists are "live" views, so they will automatically be updated to reflect the
     * latest changes. This will also trigger {@code notifyDataSetChanged()} to be called on the adapter.
     *
     * This method is therefore only useful if you want to display data based on a new query without replacing the
     * adapter.
     *
     * @param data the new {@link OrderedRealmCollection} to display.
     */
    public void updateData(@Nullable OrderedRealmCollection<T> data) {
        if (listener != null) {
            if (adapterData != null) {
                removeListener(adapterData);
            }
            if (data != null) {
                addListener(data);
            }
        }

        this.adapterData = data;
        notifyDataSetChanged();
    }
}
