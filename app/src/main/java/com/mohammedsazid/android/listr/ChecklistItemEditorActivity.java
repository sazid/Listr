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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class ChecklistItemEditorActivity extends AppCompatActivity {

    Toolbar toolbar;
    Cursor cursor = null;
    EditText checklistItemContentEt;
    String content;
    String previousContent;
    int id;
    boolean checkedState;
    boolean priorityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_item_editor);

        id = getIntent().getIntExtra("_id", -1);

        bindViews();
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
        }
        loadContent();
    }

    private void bindViews() {
        checklistItemContentEt = (EditText) findViewById(R.id.checklist_item_content);
        toolbar = (Toolbar) findViewById(R.id.top_toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_checklist_item_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int menuId = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (menuId == R.id.action_delete) {
            new MaterialDialog.Builder(ChecklistItemEditorActivity.this)
                    .title("Delete")
                    .content("Do you really want to delete the item?")
                    .positiveText("Ok")
                    .negativeText("Cancel")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            deleteItem(id);
                            content = "";
                            Intent intent = new Intent(ChecklistItemEditorActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
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
                            ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED
                    },
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            content = previousContent = cursor.getString(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
            checkedState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE)) != 0;
            priorityState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_PRIORITY)) != 0;

            checklistItemContentEt.setText(content);
        }
    }

    @Override
    public void onPause() {
        saveContentToDisk();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeCursor();
    }

    private void closeCursor() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }

    private void saveContentToDisk() {
        if (id > -1) {
            content = checklistItemContentEt.getText().toString();

            if (!content.equals(previousContent)) {
                updateItem(id);
            }
        } else {
            String content = checklistItemContentEt.getText().toString();

            if (!TextUtils.isEmpty(content)) {
                long currentTime = System.currentTimeMillis();

                Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
                Uri uri = builder.build();

                ContentValues values = new ContentValues();
                values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
                values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, 0);
                values.put(ListDbContract.ChecklistItems.COLUMN_PRIORITY, 0);
                values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, currentTime);

                Uri insertUri = this.getContentResolver().insert(uri, values);

                Toast.makeText(this, "New item inserted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Empty item discarded.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateItem(long id) {
        if (TextUtils.isEmpty(content)) {
            deleteItem(id);
        }

        long currentTime = System.currentTimeMillis();

        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
        values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, checkedState);
        values.put(ListDbContract.ChecklistItems.COLUMN_PRIORITY, checkedState);
        values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, currentTime);

        int count = this.getContentResolver().update(
                uri,
                values,
                null,
                null
        );

        if (count > 0) {
            Toast.makeText(this, "Item updated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteItem(long id) {
        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        int count = this.getContentResolver().delete(
                uri,
                null,
                null
        );

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Empty item discarded.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
        }
    }

}
