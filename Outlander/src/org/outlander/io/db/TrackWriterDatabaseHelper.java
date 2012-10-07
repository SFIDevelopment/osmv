package org.outlander.io.db;

import org.outlander.constants.DBConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class TrackWriterDatabaseHelper extends SQLiteOpeHelper {

    public TrackWriterDatabaseHelper(final Context context, final String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(DBConstants.SQL_CREATE_trackpoints);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
    }

}
