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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import io.realm.adapter.RealmRecyclerAdapter;
import io.realm.entity.AllJavaTypes;
import io.realm.entity.UnsupportedCollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmRecyclerAdapterTests {

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    private Context context;

    private static final int TEST_DATA_SIZE = 47;
    private static final boolean AUTOMATIC_UPDATE = true;

    private Realm realm;

    @Before
    public void setUp() throws Exception {
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
    public void tearDown() throws Exception {
        realm.close();
    }

    @Test
    public void constructor_testRecyclerAdapterParameterExceptions() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        try {
            new RealmRecyclerAdapter(null, resultList, AUTOMATIC_UPDATE);
            fail("Should throw exception if context is null");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void clear() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
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
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, false);
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
    public void updateData_realmUnsupportedCollectionInAdapter() {
        try {
            RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
            realmAdapter.updateData(new UnsupportedCollection<AllJavaTypes>());
            fail("Should throw exception if there is unsupported collection");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_emptyRealmResult() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_STRING, "Not there").findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        assertEquals(0, resultList.size());
        assertEquals(0, realmAdapter.getData().size());
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingData() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);

        assertEquals(resultList.first().getFieldString(), realmAdapter.getItem(0).getFieldString());
        assertEquals(resultList.size(), realmAdapter.getData().size());
        assertEquals(resultList.last().getFieldString(), realmAdapter.getData().last().getFieldString());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingNullData() {
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
        assertNull(realmAdapter.getItem(0));
    }

    @Test
    @UiThreadTest
    public void getItemId_testGetItemId() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_testGetCount() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullResults() {
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNotValidResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);

        realm.close();
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNonNullToNullResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);
        realmAdapter.updateData(null);

        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullToNonNullResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, AUTOMATIC_UPDATE);
        assertEquals(0, realmAdapter.getItemCount());

        realmAdapter.updateData(resultList);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void viewHolderTestForSimpleView() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, AUTOMATIC_UPDATE);

        RealmRecyclerAdapter.ViewHolder holder = realmAdapter.onCreateViewHolder(new FrameLayout(context), 0);
        assertNotNull(holder.textView);

        realmAdapter.onBindViewHolder(holder, 0);
        assertEquals(resultList.first().getFieldString(), holder.textView.getText());
    }
}
