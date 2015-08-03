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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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


public class NotifyActivity extends AppCompatActivity {

    int id;
    String content;
    boolean checkedState;
    boolean priorityState;
    Cursor cursor = null;
    TextView tv;
    CheckBox priorityCb;
    Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        id = getIntent().getIntExtra("_id", -1);

        loadContent();
        bindViews();

        tv.setText(content);
        priorityCb.setChecked(priorityState);

        sendNotification();

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Screen On
        acquireLock();
    }

    private void sendNotification() {
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_done)
                        .setContentTitle("Listr")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{400, 100, 400, 100, 400})
                        .setAutoCancel(true)
                        .setContentText(content);


        Intent resultIntent = new Intent(this, ChecklistItemEditorActivity.class);
        resultIntent.putExtra("_id", id);
        PendingIntent intentForService = PendingIntent.getService(this, id, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(intentForService);

        NotificationManager notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifMgr.notify(id, builder.build());

        Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSoundUri == null) {
            alarmSoundUri = notificationSoundUri;
        }

        ringtone = RingtoneManager.getRingtone(this, alarmSoundUri);
        ringtone.play();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAlarm(null);
                removeLock();
            }
        }, 1000 * 60 * 5);
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
        stopAlarm(null);
        closeCursor();
        super.onStop();
    }

    private void bindViews() {
        tv = (TextView) findViewById(R.id.req_content);
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
        }
    }

}
