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
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.TableOrView;

public class RealmCursor<T extends RealmModel> implements Cursor {

    @Nullable
    private RealmResults<T> data;
    @NonNull
    private final CursorRealmChangeListener changeListener;
    private int position = -1;
    private boolean closed;

    @NonNull
    private final List<ContentObserver> contentObservers;
    @NonNull
    private final List<DataSetObserver> dataSetObservers;
    @NonNull
    private Bundle extras = Bundle.EMPTY;

    public RealmCursor(@Nullable RealmResults<T> data) {
        changeListener = new CursorRealmChangeListener();
        contentObservers = new ArrayList<>();
        dataSetObservers = new ArrayList<>();
        setData(data);
    }

    public void setData(@Nullable RealmResults<T> newData) {
        RealmResults<T> oldData = this.data;
        this.data = newData;
        closed = false;

        if (oldData != null) {
            oldData.removeChangeListener(changeListener);
        }
        if (newData != null && !contentObservers.isEmpty()) {
            newData.addChangeListener(changeListener);
        }

        if (newData != null) {
            synchronized (dataSetObservers) {
                for (DataSetObserver dataSetObserver : dataSetObservers) {
                    dataSetObserver.onChanged();
                }
            }
        } else {
            if (oldData != null) {
                synchronized (dataSetObservers) {
                    for (DataSetObserver dataSetObserver : dataSetObservers) {
                        dataSetObserver.onInvalidated();
                    }
                }
            }
        }
    }

    @NonNull
    private Row getCurrentRow() {
        return ((RealmObjectProxy) getCurrentObject()).realmGet$proxyState().getRow$realm();
    }

    @NonNull
    private T getCurrentObject() throws IllegalStateException, CursorIndexOutOfBoundsException {
        if (data != null && data.isValid()) {
            try {
                return data.get(getPosition());
            } catch (IndexOutOfBoundsException e) {
                CursorIndexOutOfBoundsException ce = new CursorIndexOutOfBoundsException(getPosition(), getCount());
                ce.initCause(e);
                throw ce;
            }
        } else {
            throw new IllegalStateException("data is null");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        checkClosed();
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition() {
        checkClosed();
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean move(int offset) {
        return moveToPosition(position + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToPosition(int newPosition) {
        checkClosed();
        if (newPosition < -1) {
            position = -1;
            return false;
        } else if (newPosition > getCount()) {
            position = getCount();
            return false;
        } else {
            position = newPosition;
            return true;
        }
    }

    private void checkClosed() {
        if (closed)
            throw new IllegalStateException("Cursor is closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToNext() {
        return move(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToPrevious() {
        return move(-1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirst() {
        return getCount() > 0 && getPosition() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLast() {
        int count = getCount();
        return count > 0 && position == count - 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeFirst() {
        return getCount() == 0 || position == -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterLast() {
        int count = getCount();
        return count == 0 || position == count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndex(@NonNull String columnName) {
        if (data != null) {
            int columnIndex = (int) data.getTable().getColumnIndex(columnName);
            return columnIndex == TableOrView.NO_MATCH ? -1 : columnIndex;
        } else {
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndexOrThrow(@NonNull String columnName) throws IllegalArgumentException {
        checkClosed();
        if (data == null) {
            throw new IllegalStateException("data is null");
        }

        int columnIndex = (int) data.getTable().getColumnIndex(columnName);

        if (columnIndex == TableOrView.NO_MATCH) {
            throw new IllegalArgumentException("column not found");
        }

        return columnIndex;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public String getColumnName(int columnIndex) {
        checkClosed();
        if (data != null) {
            return data.getTable().getColumnName(columnIndex);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public String[] getColumnNames() {
        checkClosed();
        String[] columnNames = new String[getColumnCount()];

        for (int i = 0; i < columnNames.length; i++) {
            if (data != null) {
                columnNames[i] = data.getTable().getColumnName(i);
            }
        }

        return columnNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        checkClosed();
        if (data != null) {
            return (int) data.getTable().getColumnCount();
        } else {
            return 0;
        }
    }

    private void checkValidColumnIndex(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= getColumnCount())
            throw new IndexOutOfBoundsException("invalid columnIndex");
    }

    private RuntimeException canNotConvert(Row row, int columnIndex, String expected) {
        String real = row.getColumnType(columnIndex).name();
        ClassCastException classCastException = new ClassCastException(real + "can not be converted to " + expected);
        return new IllegalArgumentException("Wrong type at columnIndex " + columnIndex, classCastException);
    }

    @Override
    @Nullable
    public byte[] getBlob(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        if (currentRow.isNull(columnIndex)) {
            return null;
        }

        switch (columnType) {
            case BINARY:
                return currentRow.getBinaryByteArray(columnIndex);
            default:
                throw canNotConvert(currentRow, columnIndex, "byte[]");
        }
    }

    @Override
    @Nullable
    public String getString(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        if (currentRow.isNull(columnIndex)) {
            return null;
        }

        switch (columnType) {
            case STRING:
                return currentRow.getString(columnIndex);
            case INTEGER:
                return Long.toString(currentRow.getLong(columnIndex));
            case FLOAT:
                return Float.toString(currentRow.getFloat(columnIndex));
            case DOUBLE:
                return Double.toString(currentRow.getDouble(columnIndex));
            case BOOLEAN:
                return Boolean.toString(currentRow.getBoolean(columnIndex));
            case DATE:
                return currentRow.getDate(columnIndex).toString();
            default:
                throw canNotConvert(currentRow, columnIndex, "String");
        }
    }


    @Override
    public void copyStringToBuffer(int columnIndex, @NonNull CharArrayBuffer buffer) {
        String string = getString(columnIndex);

        if (string == null) {
            buffer.sizeCopied = 0;
            return;
        }

        if (buffer.data.length < string.length()) {
            char[] chars = string.toCharArray();
            buffer.data = chars;
            buffer.sizeCopied = chars.length;
            return;
        }

        string.getChars(0, string.length(), buffer.data, 0);
        buffer.sizeCopied = string.length();
    }

    @Override
    public short getShort(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        switch (columnType) {
            case INTEGER:
                long aLong = currentRow.getLong(columnIndex);
                if (aLong > Short.MIN_VALUE && aLong < Short.MAX_VALUE) {
                    return (short) aLong;
                }
            case BOOLEAN:
                return currentRow.getBoolean(columnIndex) ? (short) 1 : (short) 0;
            default:
                throw canNotConvert(currentRow, columnIndex, "short");
        }
    }

    @Override
    public int getInt(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        switch (columnType) {
            case INTEGER:
                long aLong = currentRow.getLong(columnIndex);
                if (aLong > Integer.MIN_VALUE && aLong < Integer.MAX_VALUE) {
                    return (int) aLong;
                }
            case BOOLEAN:
                return currentRow.getBoolean(columnIndex) ? 1 : 0;
            default:
                throw canNotConvert(currentRow, columnIndex, "int");
        }
    }

    @Override
    public long getLong(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        switch (columnType) {
            case INTEGER:
                return currentRow.getLong(columnIndex);
            case DATE:
                return currentRow.getDate(columnIndex).getTime();
            case BOOLEAN:
                return currentRow.getBoolean(columnIndex) ? 1 : 0;
            default:
                throw canNotConvert(currentRow, columnIndex, "long");
        }
    }

    @Override
    public float getFloat(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        switch (columnType) {
            case FLOAT:
                return currentRow.getFloat(columnIndex);
            case DOUBLE:
                double aDouble = currentRow.getDouble(columnIndex);
                if (aDouble > Float.MIN_VALUE && aDouble < Float.MAX_VALUE) {
                    return (float) aDouble;
                }
            default:
                throw canNotConvert(currentRow, columnIndex, "float");
        }
    }

    @Override
    public double getDouble(int columnIndex) {
        Row currentRow = getCurrentRow();
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType = currentRow.getColumnType(columnIndex);

        switch (columnType) {
            case DOUBLE:
                return currentRow.getDouble(columnIndex);
            case FLOAT:
                return currentRow.getFloat(columnIndex);
            default:
                throw canNotConvert(currentRow, columnIndex, "double");
        }
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public int getType(int columnIndex) {
        checkValidColumnIndex(columnIndex);
        RealmFieldType columnType;
        if (data != null) {
            columnType = data.getTable().getColumnType(columnIndex);
        } else {
            throw new IllegalStateException("data is null");
        }

        switch (columnType) {
            case FLOAT:
            case DOUBLE:
                return Cursor.FIELD_TYPE_FLOAT;
            case STRING:
                return Cursor.FIELD_TYPE_STRING;
            case BINARY:
                return Cursor.FIELD_TYPE_BLOB;
            case INTEGER:
            case BOOLEAN:
            case DATE:
                return Cursor.FIELD_TYPE_INTEGER;
            default:
                return Cursor.FIELD_TYPE_NULL;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNull(int columnIndex) {
        return getCurrentRow().isNull(columnIndex);
    }

    /**
     * @deprecated Not implemented
     * @throws UnsupportedOperationException always
     */
    @Override
    @Deprecated
    public void deactivate() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Not implemented
     * @throws UnsupportedOperationException always
     */
    @Override
    @Deprecated
    public boolean requery() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        setData(null);
        closed = true;
        synchronized (contentObservers) {
            contentObservers.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContentObserver(@NonNull ContentObserver observer) {
        //noinspection ConstantConditions
        if (observer == null) {
            throw new IllegalArgumentException("observer is null");
        }
        synchronized (contentObservers) {
            if (data != null) {
                data.addChangeListener(changeListener);
            }
            if (!contentObservers.contains(observer))
                contentObservers.add(observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContentObserver(@NonNull ContentObserver observer) {
        //noinspection ConstantConditions
        if (observer == null) {
            throw new IllegalArgumentException("observer is null");
        }
        synchronized (contentObservers) {
            contentObservers.remove(observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDataSetObserver(@NonNull DataSetObserver observer) {
        //noinspection ConstantConditions
        if (observer == null) {
            throw new IllegalArgumentException("observer is null");
        }
        synchronized (dataSetObservers) {
            if (!dataSetObservers.contains(observer))
                dataSetObservers.add(observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
        //noinspection ConstantConditions
        if (observer == null) {
            throw new IllegalArgumentException("observer is null");
        }
        synchronized (dataSetObservers) {
            dataSetObservers.remove(observer);
        }
    }

    /**
     * @deprecated Not implemented
     * @throws UnsupportedOperationException always
     */
    @Override
    @Deprecated
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Not implemented
     * @throws UnsupportedOperationException always
     */
    @Override
    @Deprecated
    public Uri getNotificationUri() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @return false
     */
    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExtras(@Nullable Bundle extras) {
        this.extras = (extras == null) ? Bundle.EMPTY : extras;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Bundle getExtras() {
        return extras;
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@link Bundle#EMPTY}
     */
    @Override
    @NonNull
    public Bundle respond(Bundle extras) {
        return Bundle.EMPTY;
    }

    private class CursorRealmChangeListener implements RealmChangeListener<RealmResults<T>> {

        @Override
        public void onChange(RealmResults<T> element) {
            synchronized (contentObservers) {
                for (ContentObserver contentObserver : contentObservers) {
                    contentObserver.onChange(false);
                }
            }
        }

    }
}
