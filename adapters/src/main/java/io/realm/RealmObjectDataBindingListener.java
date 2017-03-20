package io.realm;

import android.app.Activity;
import android.databinding.BaseObservable;

import java.util.ArrayList;
import java.util.List;

public abstract class RealmObjectDataBindingListener<T> implements RealmObjectChangeListener<T> {

    private final BaseObservable observable;
    private final List<String> fieldNames;
    private final List<Integer> fieldIds;

    private RealmObjectDataBindingListener(BaseObservable observable,
                                           List<String> fieldNames, List<Integer> fieldIds) {
        this.observable = observable;
        this.fieldNames = fieldNames;
        this.fieldIds = fieldIds;
    }

    @Override
    public void onChange(T t, ObjectChangeSet objectChangeSet) {
        if (objectChangeSet.isDeleted()) {
            onDeleted();
            return;
        }

        for (ObjectChangeSet.FieldChange fieldChange : objectChangeSet.getFieldChanges()) {
            int index = fieldNames.indexOf(fieldChange.fieldName);
            if (index != -1) {
                 observable.notifyPropertyChanged(fieldIds.get(index));
            }
        }
    }

    public abstract void onDeleted();


    public static class Builder<T> {
        private BaseObservable observable;
        private List<String> fieldNames = new ArrayList<String>();
        private List<Integer> fieldIds = new ArrayList<Integer>();
        private Runnable runnable = null;

        public Builder(BaseObservable observable) {
            this.observable = observable;
        }

        public Builder observe(String fieldName, int fieldId) {
            fieldNames.add(fieldName);
            fieldIds.add(fieldId);
            return this;
        }

        public Builder onDeleted(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public RealmObjectDataBindingListener<T> build() {
            return new RealmObjectDataBindingListener<T>(observable, fieldNames, fieldIds) {
                @Override
                public void onDeleted() {
                    if (runnable != null)  {
                        runnable.run();
                    }
                }
            };
        }
    }
}
