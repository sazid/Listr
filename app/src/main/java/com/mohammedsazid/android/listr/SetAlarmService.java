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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

public class SetAlarmService extends IntentService {

    NotificationManager notifMgr;
    private Notification notification;

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
                        ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME,
                        ListDbContract.ChecklistItems.COLUMN_LABEL
                },
                null,
                null,
                null
        );
        cursor.moveToFirst();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        int id = -1;
        long notifyTime = -1;
        String content = "";

        do {
            if (cursor.getCount() > 0) {
                id = cursor.getInt(cursor.getColumnIndex(ListDbContract.ChecklistItems._ID));
                notifyTime = cursor.getLong(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME));
                content = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));

                if (notifyTime > -1 && notifyTime >= System.currentTimeMillis()) {
                    Intent i = new Intent(this, NotifyService.class);
                    i.putExtra("_id", id);
                    PendingIntent pendingIntent = PendingIntent.getService(this, id, i, PendingIntent.FLAG_CANCEL_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        am.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
                    } else {
                        am.set(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
                    }
                } else if (notifyTime > -1 && notifyTime < System.currentTimeMillis()) {
                    Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
                    Uri updateUri = ContentUris.withAppendedId(builder.build(), id);

                    ContentValues values = new ContentValues();
                    values.put(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME, -1);

                    getContentResolver().update(updateUri, values, null, null);

                    createNotification(content, id);
                }

                cursor.moveToNext();
            }
        } while (!cursor.isAfterLast());

        cursor.close();
    }

    private void createNotification(String content, int id) {
        Intent resultIntent = new Intent(this, ChecklistItemEditorActivity.class);
        resultIntent.putExtra("_id", id);
        PendingIntent intentForService = PendingIntent.getActivity(this, id, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

//        Intent taskCheckIntent = new Intent(this, TaskCheckService.class);
//        taskCheckIntent.putExtra("_id", id);
//        PendingIntent taskCheckPendingIntent = PendingIntent.getService(this, id, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_cross)
                        .setContentTitle("Missed task")
                        .setVibrate(new long[]{400, 100, 400, 100, 400})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true)
                        .setLights(Color.RED, 3000, 1000)
                        .setContentIntent(intentForService)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentText(content)
//                        .addAction(R.drawable.ic_done, "Done", taskCheckPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content));

        notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = builder.build();
        notifMgr.notify(id, notification);
    }
}
