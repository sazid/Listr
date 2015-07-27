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

package com.mohammedsazid.android.listr.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class ListDbContract {

    public static class ChecklistItems implements BaseColumns {

        // Name of the table
        public static final String TABLE_NAME = "checklist_items";

        // Column names
        public static final String COLUMN_LABEL = "label";
        // 0 -> FALSE, 1 -> TRUE
        public static final String COLUMN_CHECKED_STATE = "checked_state";

        // SQL query for creating the table
        public static final String DATABASE_CREATE_SQL =
                "CREATE TABLE " + TABLE_NAME +
                    " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_LABEL + " TEXT NOT NULL, "
                    + COLUMN_CHECKED_STATE + " INTEGER NOT NULL" +
                    ")" +
                    ";";

        public static final void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE_SQL);
        }

        public static final void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            // For now, drop the table and create a new blank one
            database.execSQL("DROP TABLE " + TABLE_NAME + " IF EXISTS");
            onCreate(database);
        }

    }

}
