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

import android.annotation.TargetApi;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entity.AllJavaTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmCursorTest {

    private static final int SIZE = 10;
    public static final int NOT_FOUND = -1;

    private Realm realm;
    private RealmCursor<AllJavaTypes> cursor;

    private enum CursorGetter {
        STRING, SHORT, INT, LONG, FLOAT, DOUBLE, BLOB;
    }

    @Before
    public void setUp() throws Exception {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(InstrumentationRegistry.getContext()).modules(new RealmTestModule()).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        for (int i = 0; i < SIZE; i++) {
            AllJavaTypes allTypes = realm.createObject(AllJavaTypes.class, i);
            allTypes.setFieldString("test data " + i);
            allTypes.setFieldDouble(3.1415d);
            allTypes.setFieldFloat(1.234567f);
        }
        realm.commitTransaction();

        cursor = new RealmCursor<>(realm.where(AllJavaTypes.class).findAll());
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void getCount() {
        assertEquals(AllJavaTypes.COL_COUNT, cursor.getColumnCount());
    }

    @Test
    public void getPosition() {
        assertEquals(-1, cursor.getPosition());
        cursor.moveToFirst();
        assertEquals(0, cursor.getPosition());
        cursor.moveToLast();
        assertEquals(SIZE - 1, cursor.getPosition());
    }

    @Test
    public void moveOffset_valid() {
        assertTrue(cursor.move(SIZE / 2));
    }

    @Test
    public void moveOffset_invalid() {
        cursor.moveToFirst();
        assertFalse(cursor.move(SIZE * 2));
        assertFalse(cursor.move(SIZE * -2));
        assertFalse(cursor.move(-1));
        assertFalse(cursor.move(SIZE + 2));
    }

    @Test
    public void moveToPosition_capAtStart() {
        cursor.move(SIZE / 2);
        assertFalse(cursor.move(-SIZE));
        assertTrue(cursor.isBeforeFirst());
    }

    @Test
    public void moveToPosition_capAtEnd() {
        cursor.move(SIZE / 2);
        assertFalse(cursor.move(SIZE));
        assertTrue(cursor.isAfterLast());
    }

    @Test
    public void moveToPosition_emptyCursor() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(AllJavaTypes.class).findAll().deleteAllFromRealm();
            }
        });
        assertTrue(cursor.moveToPosition(0));
        assertEquals(0, cursor.getPosition());
    }

    @Test
    public void moveToPosition() {
        assertTrue(cursor.moveToPosition(SIZE / 2));
        assertEquals(SIZE / 2, cursor.getPosition());
    }

    @Test
    public void moveToFirst() {
        assertTrue(cursor.moveToFirst());
        assertEquals(0, cursor.getPosition());
    }

    @Test
    public void moveToLast() {
        assertTrue(cursor.moveToLast());
        assertEquals(SIZE - 1, cursor.getPosition());
    }

    @Test
    public void moveToNext() {
        cursor.moveToFirst();
        assertTrue(cursor.moveToNext());
        assertEquals(1, cursor.getPosition());
    }

    @Test
    public void moveToPrevious() {
        cursor.moveToLast();
        assertTrue(cursor.moveToPrevious());
        assertEquals(SIZE - 2, cursor.getPosition());
    }

    @Test
    public void isFirst_yes() {
        cursor.moveToFirst();
        assertTrue(cursor.isFirst());
    }

    @Test
    public void isFirst_no() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isFirst());
        cursor.move(SIZE * -2);
        assertFalse(cursor.isFirst());
    }

    @Test
    public void isLast_yes() {
        cursor.moveToLast();
        assertTrue(cursor.isLast());
    }

    @Test
    public void isLast_no() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isLast());
        cursor.move(SIZE * 2);
        assertFalse(cursor.isLast());
    }

    @Test
    public void beforeFirst_yes() {
        assertTrue(cursor.isBeforeFirst());
    }

    @Test
    public void beforeFirst_no() {
        cursor.moveToFirst();
        assertFalse(cursor.isBeforeFirst());
    }

    @Test
    public void isAfterLast_yes() {
        cursor.moveToLast();
        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());
    }

    @Test
    public void isAfterLast_no() {
        cursor.moveToLast();
        assertFalse(cursor.isAfterLast());
    }

    @Test
    public void getColumnIndex_idColumn_notFound() {
        assertEquals(NOT_FOUND, cursor.getColumnIndex("_id"));
    }

    @Test
    public void getColumnIndex_linkedObject() {
        assertEquals(-1, cursor.getColumnIndex("columnRealmObject.name"));
    }

    @Test
    public void getColumnIndex() {
        assertEquals(7, cursor.getColumnIndex("fieldBoolean"));
    }

    @Test
    public void getColumnIndex_notFound() {
        assertEquals(-1, cursor.getColumnIndex("foo"));
    }

    @Test
    public void getColumnIndexOrThrow() {
        assertEquals(0, cursor.getColumnIndexOrThrow("fieldString"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getColumnIndexOrThrow_notFoundThrows() {
        cursor.getColumnIndexOrThrow("foo");
    }

    @Test
    public void getColumnName_invalidIndexThrows() {
        try { cursor.getColumnName(-1);                 fail(); } catch (IndexOutOfBoundsException expected) {}
        try { cursor.getColumnName(AllJavaTypes.COL_COUNT); fail(); } catch (IndexOutOfBoundsException expected) {}
    }

    @Test
    public void getColumnName() {
        assertEquals("fieldShort", cursor.getColumnName(1));
    }

    @Test
    public void getColumnNames() {
        String[] names = cursor.getColumnNames();
        assertEquals(AllJavaTypes.COL_COUNT, names.length);
        assertEquals("fieldString", names[0]);
        assertEquals("fieldList", names[11]);
    }

    @Test
    public void getColumnCount() {
        assertEquals(AllJavaTypes.COL_COUNT, cursor.getColumnCount());
    }

    // Test that all get<type> method throw IndexOutOfBounds properly
    @Test
    public void getXXX_invalidIndexThrows() {
        cursor.moveToFirst();
        int[] indexes = new int[] {-1, AllJavaTypes.COL_COUNT};
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                try {
                    switch (cursorGetter) {
                        case STRING: cursor.getString(index); break;
                        case SHORT: cursor.getShort(index); break;
                        case INT: cursor.getInt(index); break;
                        case LONG: cursor.getLong(index); break;
                        case FLOAT: cursor.getFloat(index); break;
                        case DOUBLE: cursor.getDouble(index); break;
                        case BLOB: cursor.getBlob(index); break;
                    }
                    fail(String.format("%s (%s) should throw an exception", cursorGetter, i));
                } catch (IndexOutOfBoundsException expected) {
                }
            }
        }
    }

    // Test that all getters fail when the cursor is closed
    @Test
    public void getXXX_failWhenCursorClosed() {
        cursor.close();
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            try {
                callGetter(cursorGetter);
                fail(cursorGetter + " should throw an exception");
            } catch (IllegalStateException expected) {
            }
        }

        try { cursor.move(0);                               fail(); } catch (IllegalStateException expected) {}
        try { cursor.moveToFirst();                         fail(); } catch (IllegalStateException expected) {}
        try { cursor.moveToLast();                          fail(); } catch (IllegalStateException expected) {}
        try { cursor.moveToPosition(0);                     fail(); } catch (IllegalStateException expected) {}
        try { cursor.moveToPrevious();                      fail(); } catch (IllegalStateException expected) {}
        try { cursor.moveToNext();                          fail(); } catch (IllegalStateException expected) {}

        try { cursor.isAfterLast();                         fail(); } catch (IllegalStateException expected) {}
        try { cursor.isBeforeFirst();                       fail(); } catch (IllegalStateException expected) {}
        try { cursor.isFirst();                             fail(); } catch (IllegalStateException expected) {}
        try { cursor.isLast();                              fail(); } catch (IllegalStateException expected) {}

        try { cursor.getCount();                            fail(); } catch (IllegalStateException expected) {}
        try { cursor.getColumnCount();                      fail(); } catch (IllegalStateException expected) {}
        try { cursor.getColumnIndexOrThrow("columnString"); fail(); } catch (IllegalStateException expected) {}
        try { cursor.getColumnName(0);                      fail(); } catch (IllegalStateException expected) {}
        try { cursor.getColumnNames();                      fail(); } catch (IllegalStateException expected) {}
        try { cursor.getPosition();                         fail(); } catch (IllegalStateException expected) {}
        try { cursor.getType(0);                            fail(); } catch (IllegalStateException expected) {}
    }

    // Test that all getters fail when the cursor is out of bounds
    @Test
    public void getXXX_failWhenIfOutOfBounds() {
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            try {
                callGetter(cursorGetter);
                fail(cursorGetter + " should throw an exception");
            } catch (CursorIndexOutOfBoundsException expected) {
            }
        }

        cursor.moveToLast();
        cursor.moveToNext();
        for (CursorGetter cursorGetter : CursorGetter.values()) {
            try {
                callGetter(cursorGetter);
                fail(cursorGetter + " should throw an exception");
            } catch (CursorIndexOutOfBoundsException expected) {
            }
        }
    }

    private void callGetter(CursorGetter cursorGetter) {
        switch (cursorGetter) {
            case STRING: cursor.getString(1); break;
            case SHORT: cursor.getShort(0); break;
            case INT: cursor.getInt(0); break;
            case LONG: cursor.getLong(0); break;
            case FLOAT: cursor.getFloat(0); break;
            case DOUBLE: cursor.getDouble(0); break;
            case BLOB: cursor.getBlob(0); break;
        }
    }

    @Test
    public void getString() {
        cursor.moveToFirst();
        String str = cursor.getString(0);
        assertEquals("test data 0", str);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void copyStringToBuffer_invalidIndexThrows() {
        cursor.moveToFirst();
        cursor.copyStringToBuffer(-1, new CharArrayBuffer(10));
    }

    @Test(expected = NullPointerException.class)
    public void copyStringToBuffer_nullBufferThrows() {
        cursor.moveToFirst();
        cursor.copyStringToBuffer(0, null);
    }

    @Test
    public void copyStringToBuffer() {
        String expectedString = "test data 0";
        int expectedLength = expectedString.length();
        cursor.moveToFirst();
        CharArrayBuffer buffer = new CharArrayBuffer(expectedLength);
        cursor.copyStringToBuffer(0, buffer);
        assertEquals(expectedLength, buffer.sizeCopied);
        assertEquals(expectedLength, buffer.data.length);
        assertEquals("test data 0", new String(buffer.data));
    }

    @Test
    public void getShort() {
        cursor.moveToFirst();
        short value = cursor.getShort(3);
        assertEquals(0, value);
    }

    @Test
    public void getInt() {
        cursor.moveToFirst();
        int value = cursor.getInt(3);
        assertEquals(0, value);
    }

    @Test
    public void getLong() {
        cursor.moveToFirst();
        long value = cursor.getLong(3);
        assertEquals(0, value);
    }

    @Test
    public void getFloat() {
        cursor.moveToFirst();
        float value = cursor.getFloat(5);
        assertEquals(1.234567f, value, 0.0001f);
    }

    @Test
    public void getDouble() {
        cursor.moveToFirst();
        double value = cursor.getDouble(6);
        assertEquals(3.1415d, value, 0.001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getType_invalidIndexThrows() {
        cursor.getType(-1);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void getType() {
        assertEquals(Cursor.FIELD_TYPE_STRING, cursor.getType(0));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(3));
        assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(5));
        assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(6));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(7));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(8));
        assertEquals(Cursor.FIELD_TYPE_NULL, cursor.getType(10));
        assertEquals(Cursor.FIELD_TYPE_NULL, cursor.getType(11));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deactivate() {
        cursor.deactivate();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requery() {
        cursor.requery();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setNotificationUri() {
        cursor.setNotificationUri(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getNotificationUri() {
        cursor.deactivate();
    }

    @Test
    public void close() {
        cursor.close();
        assertTrue(cursor.isClosed());
    }

    @Test
    public void isNull() {
        cursor.moveToFirst();
        assertTrue(cursor.isNull(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerDataSetObserverNullThrows() {
        cursor.registerDataSetObserver(null);
    }

    @Test
    public void registerDataSetObserver_closed() {
        final AtomicBoolean success = new AtomicBoolean(false);
        cursor.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onInvalidated() {
                success.set(true);
            }
        });
        cursor.close();
        assertTrue(success.get());
    }

    @Test
    public void registerContentObserver_realmChanged() {
        RealmResults<AllJavaTypes> results = realm.where(AllJavaTypes.class).findAll();
        cursor = new RealmCursor<>(results);
        final AtomicBoolean success = new AtomicBoolean(false);
        cursor.registerContentObserver(new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                success.set(true);
            }
        });

        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, 42);
        realm.commitTransaction();
        assertTrue(success.get());
    }

    @Test
    public void unregisterContentObserver() {
        ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                fail();
            }
        };
        cursor.registerContentObserver(observer);
        cursor.unregisterContentObserver(observer);
        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, 42);
        realm.commitTransaction();
    }

    @Test
    public void getWantsAllOnMoveCalls() {
        assertFalse(cursor.getWantsAllOnMoveCalls());
    }

    @Test
    public void getExtras() {
        assertEquals(Bundle.EMPTY, cursor.getExtras());
    }

    @Test
    public void respond() {
        assertEquals(Bundle.EMPTY, cursor.respond(new Bundle()));
    }
}
