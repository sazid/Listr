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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

import java.text.SimpleDateFormat;


public class NotifyActivity extends AppCompatActivity {

    private static final long ALARM_TIMER = 1000 * 60 * 5;
    int id;
    String content;
    boolean checkedState;
    boolean priorityState;
    Cursor cursor = null;
    TextView reqContentTv;
    TextView reqTimeTv;
    CheckBox priorityCb;
    Ringtone ringtone;
    long notifyTime = -1;
    NotificationManager notifMgr;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        id = getIntent().getIntExtra("_id", -1);

        loadContent();
        bindViews();


        String alarmText = (new SimpleDateFormat("h:mm a").format(notifyTime));
        reqContentTv.setText(content);
        reqTimeTv.setText(alarmText);
        priorityCb.setChecked(priorityState);

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Screen On
        acquireLock();
    }

    private void sendNotification() {
//        Intent resultIntentService = new Intent(this, SetAlarmService.class);
        Intent resultIntentActivity = new Intent(this, ChecklistItemEditorActivity.class);
        resultIntentActivity.putExtra("_id", id);

//        PendingIntent intentForService = PendingIntent.getService(this, id, resultIntentService, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent intentForActivity = PendingIntent.getActivity(this, id, resultIntentActivity, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_done)
                        .setContentTitle("Listr")
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{400, 100, 400, 100, 400})
                        .setAutoCancel(true)
                        .setLights(Color.RED, 3000, 1000)
                        .setContentIntent(intentForActivity)
//                        .setDeleteIntent(intentForService)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content));

        notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = builder.build();
        notifMgr.notify(id, notification);

        Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSoundUri == null) {
            alarmSoundUri = notificationSoundUri;

            if (alarmSoundUri == null) {
                alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        if (alarmSoundUri != null) {
            ringtone = RingtoneManager.getRingtone(this, alarmSoundUri);
            ringtone.play();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAlarm(null);
                removeLock();
                notifMgr.cancel(id);
                builder.setContentTitle("Missed task");
                notifMgr.notify(id, builder.build());
            }
        }, ALARM_TIMER);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sendNotification();
    }

    private void acquireLock() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void removeLock() {
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAlarm(null);
        closeCursor();
    }

    private void bindViews() {
        reqContentTv = (TextView) findViewById(R.id.req_content);
        reqTimeTv = (TextView) findViewById(R.id.req_time);
        priorityCb = (CheckBox) findViewById(R.id.checklist_item_priority);
    }

    private void closeCursor() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
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
                            ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED,
                            ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME
                    },
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            content = cursor.getString(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
            checkedState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE)) != 0;
            priorityState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_PRIORITY)) != 0;
            notifyTime = cursor.getLong(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME));
        }
    }

    public void checkItem(View view) {
        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, true);
        values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, System.currentTimeMillis());
        Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), id);

        int count = getContentResolver().update(uri, values, null, null);

        if (count > 0) {
            Toast.makeText(this, "Item checked.", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

    private void unsetAlarm() {
        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME, -1);
        values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, System.currentTimeMillis());
        Uri uri = ContentUris.withAppendedId(ListProvider.CONTENT_URI.buildUpon().appendPath("items").build(), id);

        int count = getContentResolver().update(uri, values, null, null);
    }

    public void stopAlarm(View view) {
        unsetAlarm();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.INVISIBLE);

            if (notifMgr != null && notification != null) {
                notifMgr.cancel(id);
                Intent resultIntent = new Intent(this, ChecklistItemEditorActivity.class);
                resultIntent.putExtra("_id", id);
                startService(resultIntent);
            }
        }
    }

}
