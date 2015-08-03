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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class SetAlarmService extends IntentService {

    public SetAlarmService() {
        super("SetAlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri uri = ListProvider.CONTENT_URI.buildUpon().appendPath("items").build();
        Cursor cursor = getContentResolver().query(
                uri,
                new String[]{
                        ListDbContract.ChecklistItems._ID,
                        ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME
                },
                null,
                null,
                null
        );
        cursor.moveToFirst();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        int id;
        long notifyTime;

        do {
            if (cursor.getCount() > 0) {
                id = cursor.getInt(cursor.getColumnIndex(ListDbContract.ChecklistItems._ID));
                notifyTime = cursor.getLong(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME));

                if (notifyTime > -1 && notifyTime > System.currentTimeMillis()) {
                    Intent i = new Intent(this, NotifyActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, id, i, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        am.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
                    } else {
                        am.set(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
                    }

                }

                cursor.moveToNext();
            }
        } while (!cursor.isAfterLast());

        cursor.close();
    }
}
