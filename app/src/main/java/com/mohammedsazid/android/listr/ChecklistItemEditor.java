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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class ChecklistItemEditor extends Fragment {

    Cursor cursor = null;
    EditText checklistItemContentEt;
    Bundle bundle;
    String content;
    int id;
    boolean checkedState;

    public ChecklistItemEditor() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checklist_item_editor, container, false);

        bundle = getArguments();

        if (bundle != null) {
            id = bundle.getInt("ID", -1);
        }

        bindViews(view);
        loadContent();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (menuId == R.id.action_delete) {
            new MaterialDialog.Builder(getActivity())
                    .title("Delete")
                    .content("Do you really want to delete the item?")
                    .positiveText("Ok")
                    .negativeText("Cancel")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            deleteItem(id);

                            content = "";
                            getActivity().getSupportFragmentManager().popBackStack("editor", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (bundle != null && id > -1) {
            inflater.inflate(R.menu.menu_editor, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void loadContent() {
        if (bundle != null && id > -1) {
            Uri uri = ContentUris.withAppendedId(
                    ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), id);

            closeCursor();
            cursor = getActivity().getContentResolver().query(
                    uri,
                    new String[]{
                            ListDbContract.ChecklistItems._ID,
                            ListDbContract.ChecklistItems.COLUMN_LABEL,
                            ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE
                    },
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            content = cursor.getString(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
            checkedState = !cursor.getString(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE)).equals("0");

            checklistItemContentEt.setText(content);
        }
    }

    private void bindViews(View rootView) {
        checklistItemContentEt = (EditText) rootView.findViewById(R.id.checklist_item_content);
    }

    @Override
    public void onStop() {
        saveContentToDisk();

        super.onStop();
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
        if (bundle != null && id > -1) {
            content = checklistItemContentEt.getText().toString();
            updateItem(id);
        } else {
            String content = checklistItemContentEt.getText().toString();

            if (!TextUtils.isEmpty(content)) {
                Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
                Uri uri = builder.build();

                ContentValues values = new ContentValues();
                values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
                values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, "0");

                Uri insertUri = getActivity().getContentResolver().insert(uri, values);
            } else {
                Toast.makeText(getActivity(), "Empty item discarded.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateItem(long id) {
        if (TextUtils.isEmpty(content)) {
            deleteItem(id);
        }

        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
        values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, checkedState);

        int count = getActivity().getContentResolver().update(
                uri,
                values,
                null,
                null
        );
    }

    private void deleteItem(long id) {
        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        int count = getActivity().getContentResolver().delete(
                uri,
                null,
                null
        );

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getActivity(), "Empty item discarded.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Item deleted.", Toast.LENGTH_SHORT).show();
        }
    }
}
