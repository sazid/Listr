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

package com.mohammedsazid.android.listr;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;


public class NotifyActivity extends AppCompatActivity {

    int id;
    String content;
    boolean checkedState;
    boolean priorityState;
    Cursor cursor = null;
    TextView tv;
    CheckBox priorityCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        id = getIntent().getIntExtra("_id", -1);

        loadContent();
        bindViews();

        tv.setText(content);
        priorityCb.setChecked(priorityState);
    }

    @Override
    public void onStop() {
        closeCursor();
        super.onStop();
    }

    private void bindViews() {
        tv = (TextView) findViewById(R.id.req_content);
        priorityCb = (CheckBox) findViewById(R.id.checklist_item_priority);
    }

    private void closeCursor() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }

    private void loadContent() {
        if (id > -1) {
            Uri uri = ContentUris.withAppendedId(
                    ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), id);

            closeCursor();
            cursor = this.getContentResolver().query(
                    uri,
                    new String[]{
                            ListDbContract.ChecklistItems._ID,
                            ListDbContract.ChecklistItems.COLUMN_LABEL,
                            ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE,
                            ListDbContract.ChecklistItems.COLUMN_PRIORITY,
                            ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED,
                            ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME
                    },
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            content = cursor.getString(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
            checkedState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE)) != 0;
            priorityState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_PRIORITY)) != 0;
        }
    }

    public void checkItem(View view) {
        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, checkedState);
        values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, System.currentTimeMillis());
        Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), id);

        int count = getContentResolver().update(uri, values, null, null);

        if (count > 0) {
            Toast.makeText(this, "Item checked.", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

}
