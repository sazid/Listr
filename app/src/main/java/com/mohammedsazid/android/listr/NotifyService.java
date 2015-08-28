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

public class NotifyService extends IntentService {

    NotificationManager notifMgr;
    private Notification notification;

    int id;
    long notifyTime;
    String content;

    public NotifyService() {
        super("NotifyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        id = intent.getExtras().getInt("_id", -1);

        Uri uri = ListProvider.CONTENT_URI.buildUpon().appendPath("items").build();
        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(uri, id),
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

        content = cursor.getString(cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));

        createNotification(content, id);
        unsetAlarm();

        cursor.close();
    }

    private void unsetAlarm() {
        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME, -1);
        values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, System.currentTimeMillis());
        Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), id);

        int count = getContentResolver().update(uri, values, null, null);
    }

    private void createNotification(String content, int id) {
        Intent resultIntent = new Intent(this, ChecklistItemEditorActivity.class);
        resultIntent.putExtra("_id", id);
        PendingIntent intentForActivity = PendingIntent.getActivity(this, id, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Listr")
                        .setVibrate(new long[]{400, 100, 400, 100, 400})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true)
                        .setLights(Color.RED, 3000, 1000)
                        .setContentIntent(intentForActivity)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentText(content)
                        .addAction(R.drawable.ic_done, "Done", null)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content));

        notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = builder.build();
        notifMgr.notify(id, notification);
    }
}
