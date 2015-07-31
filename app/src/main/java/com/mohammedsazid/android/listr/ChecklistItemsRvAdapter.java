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
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class ChecklistItemsRvAdapter extends CursorRecyclerAdapter<ChecklistItemsRvAdapter.ViewHolder> {

    private FragmentActivity mActivity;

    public ChecklistItemsRvAdapter(FragmentActivity activity, Cursor cursor) {
        super(cursor);
        mActivity = activity;
    }

    @Override
    public void onBindViewHolderCursor(final ViewHolder holder, final Cursor cursor) {
        final String label = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
        String checkedState = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE));

        final boolean checked = !checkedState.equals("0");

        if (checked) {
            holder.checklistItemLabelTv.setPaintFlags(holder.checklistItemLabelTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.checklistItemLabelTv.setTextColor(Color.DKGRAY);
        } else {
            holder.checklistItemLabelTv.setPaintFlags(holder.checklistItemLabelTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.checklistItemLabelTv.setTextColor(Color.BLACK);
        }

        holder.checklistItemLabelTv.setText(label);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                cursor.moveToPosition(position);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));

                ChecklistItemEditorFragment fragment = new ChecklistItemEditorFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("ID", _id);
                fragment.setArguments(bundle);

                mActivity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out)
//                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack("editor")
                        .commit();

                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        });

        holder.checklistItemCheckBox.setOnCheckedChangeListener(null);
        holder.checklistItemCheckBox.setChecked(checked);
        holder.checklistItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = holder.getAdapterPosition();
                cursor.moveToPosition(position);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));

                if (isChecked) {
                    holder.checklistItemLabelTv.setPaintFlags(holder.checklistItemLabelTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.checklistItemLabelTv.setTextColor(Color.DKGRAY);
                } else {
                    holder.checklistItemLabelTv.setPaintFlags(holder.checklistItemLabelTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.checklistItemLabelTv.setTextColor(Color.BLACK);
                }

                ContentValues values = new ContentValues();
                values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, isChecked);
                Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), _id);

                int count = mActivity.getContentResolver().update(uri, values, null, null);

//                Log.v("Count",
//                        ".\nID: " + _id
//                                + "\nLabel: " + label
//                                + "\nUpdate count: " + String.valueOf(count)
//                                + "\nIs checked: " + String.valueOf(isChecked)
//                                + "\nPosition: " + holder.getPosition()
//                );
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.rv_checklist_item, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView checklistItemLabelTv;
        CheckBox checklistItemCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);

            checklistItemCheckBox = (CheckBox) itemView.findViewById(R.id.checklist_item_checkBox);
            checklistItemLabelTv = (TextView) itemView.findViewById(R.id.checklist_item_label);
        }

    }

}
