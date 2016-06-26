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
import android.test.AndroidTestCase;

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entity.AllJavaTypes;

public class RealmCursorTest extends AndroidTestCase {

    private static final int SIZE = 10;
    public static final int NOT_FOUND = -1;

    private Realm realm;
    private RealmCursor<AllJavaTypes> cursor;

    private enum CursorGetter {
        STRING, SHORT, INT, LONG, FLOAT, DOUBLE, BLOB;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).modules(new RealmTestModule()).build();
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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (realm != null) {
            realm.close();
        }
    }

    public void testGetCount() {
        assertEquals(AllJavaTypes.COL_COUNT, cursor.getColumnCount());
    }

    public void testGetPosition() {
        assertEquals(-1, cursor.getPosition());
        cursor.moveToFirst();
        assertEquals(0, cursor.getPosition());
        cursor.moveToLast();
        assertEquals(SIZE - 1, cursor.getPosition());
    }

    public void testMoveOffsetValid() {
        assertTrue(cursor.move(SIZE / 2));
    }

    public void testMoveOffsetInvalid() {
        cursor.moveToFirst();
        assertFalse(cursor.move(SIZE * 2));
        assertFalse(cursor.move(SIZE * -2));
        assertFalse(cursor.move(-1));
        assertFalse(cursor.move(SIZE + 2));
    }

    public void testMoveToPositionCapAtStart() {
        cursor.move(SIZE / 2);
        assertFalse(cursor.move(-SIZE));
        assertTrue(cursor.isBeforeFirst());
    }

    public void testMoveToPositionCapAtEnd() {
        cursor.move(SIZE / 2);
        assertFalse(cursor.move(SIZE));
        assertTrue(cursor.isAfterLast());
    }

    public void testMoveToPositionEmptyCursor() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(AllJavaTypes.class).findAll().deleteAllFromRealm();
            }
        });
        assertTrue(cursor.moveToPosition(0));
        assertEquals(0, cursor.getPosition());
    }

    public void testMoveToPosition() {
        assertTrue(cursor.moveToPosition(SIZE / 2));
        assertEquals(SIZE / 2, cursor.getPosition());
    }

    public void testMoveToFirst() {
        assertTrue(cursor.moveToFirst());
        assertEquals(0, cursor.getPosition());
    }

    public void testMoveToLast() {
        assertTrue(cursor.moveToLast());
        assertEquals(SIZE - 1, cursor.getPosition());
    }

    public void testMoveToNext() {
        cursor.moveToFirst();
        assertTrue(cursor.moveToNext());
        assertEquals(1, cursor.getPosition());
    }

    public void testMoveToPrevious() {
        cursor.moveToLast();
        assertTrue(cursor.moveToPrevious());
        assertEquals(SIZE - 2, cursor.getPosition());
    }

    public void testIsFirstYes() {
        cursor.moveToFirst();
        assertTrue(cursor.isFirst());
    }

    public void testIsFirstNo() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isFirst());
        cursor.move(SIZE * -2);
        assertFalse(cursor.isFirst());
    }

    public void testIsLastYes() {
        cursor.moveToLast();
        assertTrue(cursor.isLast());
    }

    public void testIsLastNo() {
        cursor.moveToPosition(1);
        assertFalse(cursor.isLast());
        cursor.move(SIZE * 2);
        assertFalse(cursor.isLast());
    }

    public void testBeforeFirstYes() {
        assertTrue(cursor.isBeforeFirst());
    }

    public void testBeforeFirstNo() {
        cursor.moveToFirst();
        assertFalse(cursor.isBeforeFirst());
    }

    public void testIsAfterLastYes() {
        cursor.moveToLast();
        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());
    }

    public void testIsAfterLastNo() {
        cursor.moveToLast();
        assertFalse(cursor.isAfterLast());
    }

    public void testGetColumnIndexIdColumnNotFound() {
        assertEquals(NOT_FOUND, cursor.getColumnIndex("_id"));
    }

    public void testGetColumnIndexLinkedObjet() {
        assertEquals(-1, cursor.getColumnIndex("columnRealmObject.name"));
    }

    public void testGetColumnIndex() {
        assertEquals(7, cursor.getColumnIndex("fieldBoolean"));
    }

    public void testGetColumnIndexNotFound() {
        assertEquals(-1, cursor.getColumnIndex("foo"));
    }

    public void testGetColumnIndexOrThrow() {
        assertEquals(0, cursor.getColumnIndexOrThrow("fieldString"));
    }

    public void testGetColumnIndexOrThrowNotFoundThrows() {
        try {
            cursor.getColumnIndexOrThrow("foo");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetColumnNameInvalidIndexThrows() {
        try { cursor.getColumnName(-1);                 fail(); } catch (IndexOutOfBoundsException expected) {}
        try { cursor.getColumnName(AllJavaTypes.COL_COUNT); fail(); } catch (IndexOutOfBoundsException expected) {}
    }

    public void testGetColumnName() {
        assertEquals("fieldShort", cursor.getColumnName(1));
    }

    public void testGetColumnNames() {
        String[] names = cursor.getColumnNames();
        assertEquals(AllJavaTypes.COL_COUNT, names.length);
        assertEquals("fieldString", names[0]);
        assertEquals("fieldList", names[11]);
    }

    public void testGetColumnCount() {
        assertEquals(AllJavaTypes.COL_COUNT, cursor.getColumnCount());
    }

    // Test that all get<type> method throw IndexOutOfBounds properly
    public void testGetXXXInvalidIndexThrows() {
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
    public void testGetXXXFailWhenCursorClosed() {
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
    public void testGetXXXFailWhenIfOutOfBounds() {
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

    public void testGetString() {
        cursor.moveToFirst();
        String str = cursor.getString(0);
        assertEquals("test data 0", str);
    }

    public void testCopyStringToBufferInvalidIndexThrows() {
        try {
            cursor.moveToFirst();
            cursor.copyStringToBuffer(-1, new CharArrayBuffer(10));
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testCopyStringToBufferNullBufferThrows() {
        try {
            cursor.moveToFirst();
            cursor.copyStringToBuffer(0, null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testCopyStringToBuffer() {
        String expectedString = "test data 0";
        int expectedLength = expectedString.length();
        cursor.moveToFirst();
        CharArrayBuffer buffer = new CharArrayBuffer(expectedLength);
        cursor.copyStringToBuffer(0, buffer);
        assertEquals(expectedLength, buffer.sizeCopied);
        assertEquals(expectedLength, buffer.data.length);
        assertEquals("test data 0", new String(buffer.data));
    }

    public void testGetShort() {
        cursor.moveToFirst();
        short value = cursor.getShort(3);
        assertEquals(0, value);
    }

    public void testGetInt() {
        cursor.moveToFirst();
        int value = cursor.getInt(3);
        assertEquals(0, value);
    }

    public void testGetLong() {
        cursor.moveToFirst();
        long value = cursor.getLong(3);
        assertEquals(0, value);
    }

    public void testGetFloat() {
        cursor.moveToFirst();
        float value = cursor.getFloat(5);
        assertEquals(1.234567f, value);
    }

    public void testGetDouble() {
        cursor.moveToFirst();
        double value = cursor.getDouble(6);
        assertEquals(3.1415d, value);
    }

    public void testGetTypeInvalidIndexThrows() {
        try {
            cursor.getType(-1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testGetType() {
        assertEquals(Cursor.FIELD_TYPE_STRING, cursor.getType(0));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(3));
        assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(5));
        assertEquals(Cursor.FIELD_TYPE_FLOAT, cursor.getType(6));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(7));
        assertEquals(Cursor.FIELD_TYPE_INTEGER, cursor.getType(8));
        assertEquals(Cursor.FIELD_TYPE_NULL, cursor.getType(10));
        assertEquals(Cursor.FIELD_TYPE_NULL, cursor.getType(11));
    }

    public void testUnsupportedMethods() {
        cursor.moveToFirst();
        try { cursor.deactivate();                      fail(); } catch (UnsupportedOperationException expected) {}
        try { cursor.requery();                         fail(); } catch (UnsupportedOperationException expected) {}
        try { cursor.setNotificationUri(null, null);    fail(); } catch (UnsupportedOperationException expected) {}
        try { cursor.getNotificationUri();              fail(); } catch (UnsupportedOperationException expected) {}
    }

    public void testClose() {
        cursor.close();
        assertTrue(cursor.isClosed());
    }

    public void testIsNull() {
        cursor.moveToFirst();
        assertTrue(cursor.isNull(10));
    }

    public void testRegisterDataSetObserverNullThrows() {
        try {
            cursor.registerDataSetObserver(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRegisterDataSetObserverClosed() {
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

    public void testRegisterContentObserverRealmChanged() {
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

    public void testUnregisterContentObserver() {
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

    public void testGetWantsAllOnMoveCalls() {
        assertFalse(cursor.getWantsAllOnMoveCalls());
    }

    public void testGetExtras() {
        assertEquals(Bundle.EMPTY, cursor.getExtras());
    }

    public void testRespond() {
        assertEquals(Bundle.EMPTY, cursor.respond(new Bundle()));
    }
}
