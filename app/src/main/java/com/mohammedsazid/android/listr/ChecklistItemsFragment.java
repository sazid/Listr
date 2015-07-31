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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class ChecklistItemsFragment extends Fragment {

    RecyclerView checklistItemsRv;
    Cursor cursor;

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

    public ChecklistItemsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checklist_items, container, false);

        bindViews(view);

        return view;
    }

    private void getChecklistItems() {
        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = builder.build();

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

        ChecklistItemsRvAdapter checklistItemsRvAdapter = new ChecklistItemsRvAdapter(getActivity(), cursor);
        checklistItemsRvAdapter.setHasStableIds(true);

        checklistItemsRv.setAdapter(checklistItemsRvAdapter);
        checklistItemsRv.setHasFixedSize(true);
        checklistItemsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void bindViews(View rootView) {
        checklistItemsRv = (RecyclerView) rootView.findViewById(R.id.checklist_items_rv);
    }

    @Override
    public void onResume() {
        super.onResume();
        getChecklistItems();
        Log.v("Callbacks", "onResume() fragment");
    }
}
