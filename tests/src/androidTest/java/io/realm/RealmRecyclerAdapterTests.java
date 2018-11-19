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
import android.widget.FrameLayout;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.realm.adapter.RecyclerViewTestAdapter;
import io.realm.entity.AllJavaTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmRecyclerAdapterTests {

    private Context context;

    private static final int TEST_DATA_SIZE = 47;
    private static final boolean AUTOMATIC_UPDATE = true;

    private Realm realm;

    @Before
    @UiThreadTest
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).modules(new RealmTestModule()).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            AllJavaTypes allTypes = realm.createObject(AllJavaTypes.class, i);
            allTypes.setFieldString("test data " + i);
        }
        realm.commitTransaction();
    }

    @After
    @UiThreadTest
    public void tearDown() {
        realm.close();
    }

    @Test
    @UiThreadTest
    public void constructor_testRecyclerAdapterUnmanagedParameterExceptions() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmList<AllJavaTypes> unmanagedRealmList = new RealmList<>(resultList.toArray(new AllJavaTypes[0]));
        try {
            new RecyclerViewTestAdapter(context, unmanagedRealmList, true);
            fail("Should throw exception if list is un-managed");
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void clear() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);
        realm.beginTransaction();
        resultList.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(0, realmAdapter.getItemCount());
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void updateData_realmResultInAdapter() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        resultList.sort(AllJavaTypes.FIELD_STRING);
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, false);
        //noinspection ConstantConditions
        assertEquals(resultList.first().getFieldString(), realmAdapter.getData().first().getFieldString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        realm.beginTransaction();
        AllJavaTypes allTypes = realm.createObject(AllJavaTypes.class, TEST_DATA_SIZE);
        allTypes.setFieldString("test data " + TEST_DATA_SIZE);
        realm.commitTransaction();
        assertEquals(resultList.last().getFieldString(), realmAdapter.getData().last().getFieldString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        RealmResults<AllJavaTypes> emptyResultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_STRING, "Not there").findAll();
        realmAdapter.updateData(emptyResultList);
        assertEquals(emptyResultList.size(), realmAdapter.getData().size());
    }

    @Test
    @UiThreadTest
    public void updateData_replaceInvalidData() {
        // test for https://github.com/realm/realm-android-adapters/issues/58
        final RealmConfiguration configuration = realm.getConfiguration();

        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).sort(AllJavaTypes.FIELD_STRING).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, true);
        realm.close(); // to make resultList invalid

        // check precondition
        assertFalse(resultList.isValid());

        realm = Realm.getInstance(configuration);

        // create another valid RealmResults and check if updateData does not throw an exception.
        resultList = realm.where(AllJavaTypes.class).findAll();
        realmAdapter.updateData(resultList);
    }

    @Test
    @UiThreadTest
    public void updateData_realmUnsupportedCollectionInAdapter() {
        try {
            RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, null, AUTOMATIC_UPDATE);
            RealmResults<AllJavaTypes> results =
                    realm.where(AllJavaTypes.class).sort(AllJavaTypes.FIELD_STRING).findAll();
            realmAdapter.updateData(results.createSnapshot());
            fail("Should throw exception if there is unsupported collection");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_emptyRealmResult() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_STRING, "Not there").findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);
        assertEquals(0, resultList.size());
        //noinspection ConstantConditions
        assertEquals(0, realmAdapter.getData().size());
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingData() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);

        //noinspection ConstantConditions
        assertEquals(resultList.first().getFieldString(), realmAdapter.getItem(0).getFieldString());
        //noinspection ConstantConditions
        assertEquals(resultList.size(), realmAdapter.getData().size());
        //noinspection ConstantConditions
        assertEquals(resultList.last().getFieldString(), realmAdapter.getData().last().getFieldString());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingNullData() {
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, null, AUTOMATIC_UPDATE);
        assertNull(realmAdapter.getItem(0));
    }

    @Test
    @UiThreadTest
    public void getItemId_testGetItemId() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_testGetCount() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullResults() {
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, null, AUTOMATIC_UPDATE);
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNotValidResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);

        realm.close();
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNonNullToNullResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);
        realmAdapter.updateData(null);

        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullToNonNullResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, null, AUTOMATIC_UPDATE);
        assertEquals(0, realmAdapter.getItemCount());

        realmAdapter.updateData(resultList);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void viewHolderTestForSimpleView() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RecyclerViewTestAdapter realmAdapter = new RecyclerViewTestAdapter(context, resultList, AUTOMATIC_UPDATE);

        RecyclerViewTestAdapter.ViewHolder holder = realmAdapter.onCreateViewHolder(new FrameLayout(context), 0);
        assertNotNull(holder.textView);

        realmAdapter.onBindViewHolder(holder, 0);
        assertEquals(resultList.first().getFieldString(), holder.textView.getText());
    }
}
