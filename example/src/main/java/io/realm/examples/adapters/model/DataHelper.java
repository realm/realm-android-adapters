/*
 * Copyright 2017 Realm Inc.
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


import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

import io.realm.Realm;

public class DataHelper {

    // Create 3 counters and insert them into random place of the list.
    public static void randomAddItemAsync(Realm realm) {
        try {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (int i = 0; i < 3; i++) {
                        Counter.create(realm, true);
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {

        }
    }

    public static void addItemAsync(Realm realm) {
        try {

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Counter.create(realm);
                }
            });
        } catch (RejectedExecutionException ignored) {

        }
    }

    public static void deleteItemAsync(Realm realm, final long id) {
        try {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Counter.delete(realm, id);
                }
            });
        } catch (RejectedExecutionException ignored) {

        }
    }

    public static void deleteItemsAsync(Realm realm, Collection<Integer> ids) {
        try {
            // Create an new array to avoid concurrency problem.
            final Integer[] idsToDelete = new Integer[ids.size()];
            ids.toArray(idsToDelete);
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Integer id : idsToDelete) {
                        Counter.delete(realm, id);
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {

        }
    }
}
