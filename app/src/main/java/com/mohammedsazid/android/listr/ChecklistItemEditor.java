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

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class ChecklistItemEditor extends Fragment {

    EditText checklistItemContentEt;

    public ChecklistItemEditor() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checklist_item_editor, container, false);

        bindViews(view);

        return view;
    }

    private void bindViews(View rootView) {
        checklistItemContentEt = (EditText) rootView.findViewById(R.id.checklist_item_content);
    }

    @Override
    public void onDestroy() {
        String content = checklistItemContentEt.getText().toString();

        if (!TextUtils.isEmpty(content)) {
            Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
            Uri uri = builder.build();

            ContentValues values = new ContentValues();
            values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
            values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, "0");

            Uri insertUri = getActivity().getContentResolver().insert(uri, values);

            Log.v("LOG_URI", "onDestroy() inside CheckListItemEditor\n" + insertUri);
        }

        super.onDestroy();
    }
}
