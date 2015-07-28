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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mohammedsazid.android.listr.data.ListDbContract;

public class ChecklistItemsRvAdapter extends CursorRecyclerAdapter<ChecklistItemsRvAdapter.ViewHolder> {

    private Context mContext;

    public ChecklistItemsRvAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {
//        int pos = cursor.getPosition();
        String label = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
        String checkedState = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE));

        boolean checked = checkedState.equals("0") ? false : true;

        holder.checklistItemLabelTv.setPaintFlags(holder.checklistItemLabelTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        if (checked) {
            holder.checklistItemLabelTv.setPaintFlags(holder.checklistItemLabelTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.checklistItemLabelTv.setText(label);
        holder.checklistItemCheckBox.setChecked(checked);
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
