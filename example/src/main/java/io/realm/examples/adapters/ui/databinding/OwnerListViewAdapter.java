package io.realm.examples.adapters.ui.databinding;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.examples.adapters.R;
import io.realm.examples.adapters.model.Owner;


class OwnerListViewAdapter extends RealmRecyclerViewAdapter<Owner, OwnerListViewAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        private Owner owner;
        private TextView textView;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OwnerDetailsActivity.class);
                intent.putExtra(OwnerDetailsActivity.EXTRA_DATA_OWNER_ID, owner.getId());
                v.getContext().startActivity(intent);
            }
        };

        ViewHolder(final View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.owner_name_view);
            itemView.setOnClickListener(onClickListener);
        }
    }

    public OwnerListViewAdapter(@Nullable OrderedRealmCollection<Owner> data) {
        super(data, true);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.owner_list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.owner = getItem(position);
        holder.textView.setText(holder.owner.getName());
    }
}
