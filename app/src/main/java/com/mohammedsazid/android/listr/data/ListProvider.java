/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Mohammed Sazid-Al-Rashid
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.mohammedsazid.android.listr.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.mohammedsazid.android.listr.data.ListDbContract.ChecklistItems;

public class ListProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "com.mohammedsazid.android.listr.data.ListProvider";
    public static final String URL = "content://" + PROVIDER_NAME + "/checklists";
    public static final Uri CONTENT_URI = Uri.parse(URL);
    //    public static final int CHECKLISTS = 1;
    public static final int CHECKLIST_ITEM = 2;
    public static final int CHECKLIST_ITEMS = 3;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "/checklists/items", CHECKLIST_ITEMS);
        uriMatcher.addURI(PROVIDER_NAME, "/checklists/items/#", CHECKLIST_ITEM);
    }

    ListDbOpenHelper dbOpenHelper;

    public ListProvider() {
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = new ListDbOpenHelper(getContext());

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor c;

        if (sortOrder == null || sortOrder == "") {
            // By default, sort items based on their checked state
            sortOrder = ChecklistItems.COLUMN_CHECKED_STATE;
        }

        switch (uriMatcher.match(uri)) {
            case CHECKLIST_ITEM:

                String id = uri.getLastPathSegment();
                c = queryBuilder.query(
                        db,
                        projection,
                        ChecklistItems._ID + " = ?",
                        new String[] {id},
                        null,
                        null,
                        sortOrder
                );

                break;
            case CHECKLIST_ITEMS:

                c = queryBuilder.query(
                        db,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        long rowId = db.insert(ChecklistItems.TABLE_NAME, null, values);

        // If successful
        if (rowId > 0) {
            Uri _uri = ContentUris.withAppendedId(Uri.parse(CONTENT_URI.toString() + "/items/"), rowId);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

//        throw new SQLException("Failed to add a record into " + uri.toString());
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CHECKLIST_ITEM:

                String id = uri.getLastPathSegment();
                count = db.update(
                        ChecklistItems.TABLE_NAME,
                        values,
                        ChecklistItems._ID + " = ?",
                        new String[]{id}
                );

                break;
            case CHECKLIST_ITEMS:
                throw new UnsupportedOperationException("Updating multiple items not supported.");
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CHECKLIST_ITEM:

                String id = uri.getLastPathSegment();
                count = db.delete(
                        ChecklistItems.TABLE_NAME,
                        ChecklistItems._ID + " = ?",
                        new String[]{id}
                );

                break;
            case CHECKLIST_ITEMS:

                count = db.delete(ChecklistItems.TABLE_NAME, null, null);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case CHECKLIST_ITEM:
                return "vnd.android.cursor.item/" + CONTENT_URI.toString();
            case CHECKLIST_ITEMS:
                return "vnd.android.cursor.dir/" + CONTENT_URI.toString();
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

    }

}
