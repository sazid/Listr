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
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChecklistItemsRvAdapter extends CursorRecyclerAdapter<ChecklistItemsRvAdapter.ViewHolder> {

    private FragmentActivity mActivity;

    public ChecklistItemsRvAdapter(FragmentActivity activity, Cursor cursor) {
        super(cursor);
        mActivity = activity;
    }

    @Override
    public void onBindViewHolderCursor(final ViewHolder holder, final Cursor cursor) {
        final String label = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
        int checkedState = cursor.getInt(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE));
        int priorityState = cursor.getInt(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_PRIORITY));
        long notifyTime = cursor.getLong(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME));

        boolean notifyChecked = notifyTime > -1;
        final boolean checked = checkedState != 0;
        final boolean priorityChecked = priorityState != 0;

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

                Intent intent = new Intent(mActivity, ChecklistItemEditorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("_id", _id);
//                intent.putExtra("label", label);
//
//                View sharedView = holder.checklistItemLabelTv;
//                String transitionName = "item_label";
//
//                ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity, sharedView, transitionName);
                mActivity.startActivity(intent/*, transitionActivityOptions.toBundle()*/);
            }
        });

        // For the notification icon
        holder.checklistItemCheckBox.setChecked(checked);
        holder.checklistItemPriority.setChecked(priorityChecked);


        holder.checklistItemNotifyTimeTv.setText("");
        holder.checklistItemNotify.setVisibility(View.INVISIBLE);
        holder.checklistItemNotifyTimeTv.setVisibility(View.INVISIBLE);

        holder.checklistItemNotify.setChecked(notifyChecked);
        if (holder.checklistItemNotify.isChecked()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.DATE, 1);

            String alarmText = (new SimpleDateFormat("h:mm a").format(notifyTime));

            if (notifyTime > calendar.getTimeInMillis()) {
                alarmText = "Tomorrow " + alarmText;
            }

            holder.checklistItemNotifyTimeTv.setText(alarmText);

            holder.checklistItemNotify.setVisibility(View.VISIBLE);
            holder.checklistItemNotifyTimeTv.setVisibility(View.VISIBLE);
        }

        // For the checkbox
        holder.checklistItemCheckBox.setOnCheckedChangeListener(null);

        // For the priority toggle
        holder.checklistItemPriority.setOnCheckedChangeListener(null);

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

                long currentTime = System.currentTimeMillis();

                ContentValues values = new ContentValues();
                values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, isChecked);
                values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, currentTime);
                Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), _id);

                int count = mActivity.getContentResolver().update(uri, values, null, null);
            }
        });

        holder.checklistItemPriority.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = holder.getAdapterPosition();
                cursor.moveToPosition(position);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                long currentTime = System.currentTimeMillis();

                ContentValues values = new ContentValues();
                values.put(ListDbContract.ChecklistItems.COLUMN_PRIORITY, isChecked);
                values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, currentTime);
                Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), _id);

                int count = mActivity.getContentResolver().update(uri, values, null, null);
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
        TextView checklistItemNotifyTimeTv;
        CheckBox checklistItemCheckBox;
        CheckBox checklistItemPriority;
        CheckBox checklistItemNotify;

        public ViewHolder(View itemView) {
            super(itemView);

            checklistItemLabelTv = (TextView) itemView.findViewById(R.id.checklist_item_label);
            checklistItemNotifyTimeTv = (TextView) itemView.findViewById(R.id.checklist_item_notify_time);
            checklistItemCheckBox = (CheckBox) itemView.findViewById(R.id.checklist_item_checkBox);
            checklistItemPriority = (CheckBox) itemView.findViewById(R.id.checklist_item_priority);
            checklistItemNotify = (CheckBox) itemView.findViewById(R.id.checklist_item_notify);
        }

    }

}
