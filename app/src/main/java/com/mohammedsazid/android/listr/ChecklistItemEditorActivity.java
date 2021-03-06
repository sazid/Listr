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
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChecklistItemEditorActivity extends AppCompatActivity {

    private static int timeHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private static int timeMinute = Calendar.getInstance().get(Calendar.MINUTE);
    Toolbar toolbar;
    Cursor cursor = null;
    EditText checklistItemContentEt;
    String content;
    String previousContent;
    int id;
    boolean checkedState;
    boolean priorityState;
    boolean alarmState;
    boolean deletePressed = false;
    long notifyTime = -1;
    Menu menu;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_item_editor);

        id = getIntent().getIntExtra("_id", -1);

        bindViews();
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
        }
        loadContent();

        Intent intent = new Intent(this, NotifyService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("_id", id);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getService(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmState) {
            getSupportActionBar().setTitle(getAlarmText(notifyTime));
        } else if (id <= -1) {
            getSupportActionBar().setTitle("");
        }
    }

    private void bindViews() {
        checklistItemContentEt = (EditText) findViewById(R.id.checklist_item_content);
        toolbar = (Toolbar) findViewById(R.id.top_toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_checklist_item_editor, menu);

        setOptionVisibility(menu, R.id.action_notify_off, false);
        setOptionVisibility(menu, R.id.action_notify, false);

        if (alarmState) {
            setOptionVisibility(menu, R.id.action_notify, false);
            setOptionVisibility(menu, R.id.action_notify_off, true);
        } else {
            setOptionVisibility(menu, R.id.action_notify, true);
            setOptionVisibility(menu, R.id.action_notify_off, false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int menuId = item.getItemId();

        switch (menuId) {
            case R.id.action_delete:
                new MaterialDialog.Builder(ChecklistItemEditorActivity.this)
                        .title("Delete")
                        .content("Do you really want to delete the item?")
                        .positiveText("Ok")
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                deletePressed = true;
                                deleteItem(id);
                                content = "";
                                Intent intent = new Intent(ChecklistItemEditorActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();

                return true;
            case R.id.action_notify:
                TimePickerFragment fragment = new TimePickerFragment(new AlarmHandler());
                getSupportFragmentManager().beginTransaction()
                        .add(fragment, "time_picker")
                        .commit();

                return true;
            case R.id.action_notify_off:
                cancelAlarm();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setOptionVisibility(Menu menu, int id, boolean visible) {
        MenuItem item = menu.findItem(id);
        item.setVisible(visible);
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

            content = previousContent = cursor.getString(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
            checkedState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE)) != 0;
            priorityState = cursor.getInt(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_PRIORITY)) != 0;
            alarmState = cursor.getLong(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME)) > -1;
            notifyTime = cursor.getLong(
                    cursor.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME));

            checklistItemContentEt.setText(content);
        }
    }

    @Override
    public void onPause() {
        if (!deletePressed) {
            saveContentToDisk();
        }
        super.onPause();
    }

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

    private void saveContentToDisk() {
        if (id > -1) {
            content = checklistItemContentEt.getText().toString();
            updateItem(id);
        } else {
            String content = checklistItemContentEt.getText().toString();

            if (!TextUtils.isEmpty(content)) {
                long currentTime = System.currentTimeMillis();

                Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
                Uri uri = builder.build();

                ContentValues values = new ContentValues();
                values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
                values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, 0);
                values.put(ListDbContract.ChecklistItems.COLUMN_PRIORITY, 0);
                values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, currentTime);
                values.put(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME, notifyTime);

                Uri insertUri = this.getContentResolver().insert(uri, values);
                id = Integer.parseInt(insertUri.getLastPathSegment());

                Intent intent = new Intent(this, NotifyService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("_id", id);

                alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                pendingIntent = PendingIntent.getService(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                setAlarm();

                Toast.makeText(this, "New item inserted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Empty item discarded.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateItem(long id) {
        if (TextUtils.isEmpty(content)) {
            deleteItem(id);
        }

        long currentTime = System.currentTimeMillis();

        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        ContentValues values = new ContentValues();

        if (!content.equals(previousContent)) {
            values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, content);
            values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, checkedState);
            values.put(ListDbContract.ChecklistItems.COLUMN_PRIORITY, checkedState);
            values.put(ListDbContract.ChecklistItems.COLUMN_LAST_MODIFIED, currentTime);
        }

        values.put(ListDbContract.ChecklistItems.COLUMN_NOTIFY_TIME, notifyTime);

        int count = this.getContentResolver().update(
                uri,
                values,
                null,
                null
        );

        if (!content.equals(previousContent) && count > 0) {
            Toast.makeText(this, "Item updated.", Toast.LENGTH_SHORT).show();
        }

        setAlarm();
    }

    private void deleteItem(long id) {

        if (id > -1) {
            Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
            Uri uri = ContentUris.withAppendedId(builder.build(), id);

            int count = this.getContentResolver().delete(
                    uri,
                    null,
                    null
            );

            alarmManager.cancel(pendingIntent);

            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "Empty item discarded.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setAlarm() {
        if (notifyTime > -1 && notifyTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
            }

            setOptionVisibility(menu, R.id.action_notify_off, true);
            setOptionVisibility(menu, R.id.action_notify, false);

            getSupportActionBar().setTitle(getAlarmText(notifyTime));
        }
    }

    private void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            notifyTime = -1;
            setOptionVisibility(menu, R.id.action_notify_off, false);
            setOptionVisibility(menu, R.id.action_notify, true);

            getSupportActionBar().setTitle("");
        }
    }

    class AlarmHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            timeHour = bundle.getInt("time_hour");
            timeMinute = bundle.getInt("time_minute");
//            setAlarm();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timeHour);
            calendar.set(Calendar.MINUTE, timeMinute);

            notifyTime = calendar.getTimeInMillis();

            if (notifyTime <= System.currentTimeMillis()) {
                // add 1 day to the user selected time
                calendar.add(Calendar.DATE, 1);

                Toast.makeText(
                        ChecklistItemEditorActivity.this,
                        "Alarm set for tomorrow!",
                        Toast.LENGTH_SHORT).show();

                notifyTime = calendar.getTimeInMillis();
            }

            getSupportActionBar().setTitle(getAlarmText(notifyTime));

            setOptionVisibility(menu, R.id.action_notify_off, true);
            setOptionVisibility(menu, R.id.action_notify, false);
        }
    }

    private String getAlarmText(long notifyTime) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.add(Calendar.DATE, 1);

        String alarmText = (new SimpleDateFormat("h:mm a").format(notifyTime));

        if (notifyTime > c.getTimeInMillis()) {
            alarmText = "Tomorrow " + alarmText;
        }

        return alarmText;
    }

}
