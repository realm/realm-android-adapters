package io.realm.examples.adapters.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Parent extends RealmObject {
    RealmList<Counter> counters;

    public RealmList<Counter> getCounters() {
        return counters;
    }
}
