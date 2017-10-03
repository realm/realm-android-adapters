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
package io.realm.examples.adapters.model;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Counter extends RealmObject {
    public static final String FIELD_COUNT = "count";

    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    @PrimaryKey
    private int count;

    public int getCount() {
        return count;
    }

    public String getCountString() {
        return Integer.toString(count);
    }

    //  create() & delete() needs to be called inside a transaction.
    static void create(Realm realm) {
        create(realm, false);
    }

    public static void create(Realm realm, boolean randomlyInsert) {
        Parent parent = realm.where(Parent.class).findFirst();
        RealmList<Counter> counters = parent.getCounterList();
        Counter counter = realm.createObject(Counter.class, increment());
        if (randomlyInsert && counters.size() > 0) {
            Random rand = new Random();
            counters.listIterator(rand.nextInt(counters.size())).add(counter);
        } else {
            counters.add(counter);
        }
    }

    public static void delete(Realm realm, long id) {
        Counter counter = realm.where(Counter.class).equalTo(FIELD_COUNT, id).findFirst();
        // Otherwise it has been deleted already.
        if (counter != null) {
            counter.deleteFromRealm();
        }
    }

    private static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }
}
