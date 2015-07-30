package com.mohammedsazid.android.listr;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mohammedsazid.android.listr.data.ListDbContract;
import com.mohammedsazid.android.listr.data.ListProvider;


public class MainActivity extends AppCompatActivity {

    Toolbar topToolbar;
    Toolbar bottomToolbar;

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ChecklistItemsFragment())
                .commit();

        bindViews();

        setSupportActionBar(topToolbar);

//        createEntry("Simple checklist item", "1");
//        deleteItem(11);
//        updateItem(3);
//        showResults();
    }

    public void bottomToolbarOnClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ChecklistItemEditor())
                .addToBackStack("editor")
                .commit();
    }

    private void bindViews() {
        topToolbar = (Toolbar) findViewById(R.id.top_toolbar);
    }

    /*
    private void updateItem(long id) {
        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, "Most simple item");
        values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, "1");

        int count = getContentResolver().update(
                uri,
                values,
                null,
                null
        );

        Toast.makeText(this, "Updated " + count + " item(s).", Toast.LENGTH_SHORT).show();
    }

    private void deleteItem(long id) {
        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = ContentUris.withAppendedId(builder.build(), id);

        int count = getContentResolver().delete(
                uri,
                null,
                null
        );

        Toast.makeText(this, "Deleted " + count + " items.", Toast.LENGTH_SHORT).show();
    }

    private void createEntry(String label, String checkedState) {
        ContentValues values = new ContentValues();
        values.put(ListDbContract.ChecklistItems.COLUMN_LABEL, label);
        values.put(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE, checkedState);

        Uri insertUri = getContentResolver().insert(
                ListProvider.CONTENT_URI,
                values
        );

        Toast.makeText(this, insertUri.toString(), Toast.LENGTH_SHORT).show();

    }

    private void showResults() {
        TextView textView = (TextView) findViewById(R.id.textView);

        Uri.Builder builder = ListProvider.CONTENT_URI.buildUpon().appendPath("items");
        Uri uri = builder.build();

        Cursor c = getContentResolver().query(
                uri,
                new String[] {
                        ListDbContract.ChecklistItems.COLUMN_LABEL,
                        ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE
                },
                null,
                null,
                null
        );

        c.moveToFirst();

        String buildUpString = "";
        while (!c.isAfterLast()) {
            String label = c.getString(c.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_LABEL));
            String checkedString = c.getString(c.getColumnIndex(ListDbContract.ChecklistItems.COLUMN_CHECKED_STATE));
            if (checkedString.equals("0")) {
                checkedString = "Unchecked";
            } else if (checkedString.equals("1")) {
                checkedString = "Checked";
            }

            buildUpString += label + ": " + checkedString + "\n";

            c.moveToNext();
        }


        textView.setText(buildUpString);
    }

    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
