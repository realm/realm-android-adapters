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
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.adapter.ListViewTestAdapter;
import io.realm.entity.AllJavaTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmRecyclerViewAdapterTests {
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    private final static int TEST_DATA_SIZE = 47;

    private Context context;
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
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void testAdapterParameterExceptions() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        try {
            new ListViewTestAdapter(null, resultList);
            fail("Should throw exception if context is null");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void testUpdateRealmResultInAdapter() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        resultList.sort(AllJavaTypes.FIELD_STRING);
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        assertEquals(resultList.first().getFieldString(), realmAdapter.getItem(0).getFieldString());
        assertEquals(resultList.size(), realmAdapter.getCount());

        realm.beginTransaction();
        AllJavaTypes allTypes = realm.createObject(AllJavaTypes.class, TEST_DATA_SIZE);
        allTypes.setFieldString("test data " + TEST_DATA_SIZE);
        realm.commitTransaction();
        assertEquals(resultList.last().getFieldString(), realmAdapter.getItem(realmAdapter.getCount() - 1).getFieldString());
        assertEquals(resultList.size(), realmAdapter.getCount());

        RealmResults<AllJavaTypes> emptyResultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_STRING, "Not there").findAll();
        realmAdapter.updateData(emptyResultList);
        assertEquals(emptyResultList.size(), realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testClearFromAdapter() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);

        realm.beginTransaction();
        resultList.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(0, realmAdapter.getCount());
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void testRemoveFromAdapter() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);

        realm.beginTransaction();
        resultList.deleteFromRealm(0);
        realm.commitTransaction();
        assertEquals(TEST_DATA_SIZE - 1, realmAdapter.getCount());

        resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_STRING, "test data 0").findAll();
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void testSortWithAdapter() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        resultList.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        assertEquals(resultList.first().getFieldString(), realmAdapter.getItem(0).getFieldString());
        assertEquals(resultList.size(), realmAdapter.getCount());

        resultList.sort(AllJavaTypes.FIELD_STRING);

        assertEquals(resultList.last().getFieldString(), realmAdapter.getItem(resultList.size() - 1).getFieldString());
        assertEquals(resultList.get(TEST_DATA_SIZE / 2).getFieldString(), realmAdapter.getItem(TEST_DATA_SIZE / 2).getFieldString());
        assertEquals(resultList.size(), realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testEmptyRealmResult() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_STRING, "Not there").findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        assertEquals(0, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testGetItem() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);

        assertEquals(resultList.get(0).getFieldString(), realmAdapter.getItem(0).getFieldString());
        assertEquals(resultList.size(), realmAdapter.getCount());
        assertEquals(resultList.last().getFieldString(), realmAdapter.getItem(resultList.size() - 1).getFieldString());
    }

    @Test
    @UiThreadTest
    public void testGetItemId() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    @Test
    @UiThreadTest
    public void testGetCount() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testGetView() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        View view = realmAdapter.getView(0, null, null);

        TextView name = (TextView) view.findViewById(android.R.id.text1);

        assertNotNull(view);
        assertNotNull(name);
        assertEquals(resultList.get(0).getFieldString(), name.getText());
    }

    @Test
    public void testNullResults() {
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, null);

        assertEquals(0, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testNonNullToNullResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, resultList);
        realmAdapter.updateData(null);

        assertEquals(0, realmAdapter.getCount());
    }

    @Test
    @UiThreadTest
    public void testNullToNonNullResults() {
        RealmResults<AllJavaTypes> resultList = realm.where(AllJavaTypes.class).findAll();
        ListViewTestAdapter realmAdapter = new ListViewTestAdapter(context, null);
        realmAdapter.updateData(resultList);

        assertEquals(TEST_DATA_SIZE, realmAdapter.getCount());
    }

}
