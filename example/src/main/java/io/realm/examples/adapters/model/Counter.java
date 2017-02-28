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

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmObject;

public class Counter extends RealmObject {
    public static final String FIELD_COUNT = "count";

    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCountString() {
        return Integer.toString(count);
    }

    public void increment() {
        this.count = INTEGER_COUNTER.getAndIncrement();
    }
}
