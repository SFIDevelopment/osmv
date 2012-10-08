package org.outlander.io.db;

import java.io.File;

import org.outlander.utils.Ut;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class SQLiteMapDatabase {

    private static final String SQL_CREATE_tiles = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s));";
    private static final String SQL_CREATE_info  = "CREATE TABLE IF NOT EXISTS info (maxzoom Int, minzoom Int);";
    private SQLiteDatabase      mDatabase;

    public void setFile(final String aFileName) throws SQLiteException {
        if (mDatabase != null) {
            mDatabase.close();
        }

        mDatabase = new CacheDatabaseHelper(null, aFileName).getWritableDatabase();
        Ut.d("CacheDatabase: Open SQLITEDB Database: " + aFileName);

    }

    public void setFile(final File aFile) throws SQLiteException {
        setFile(aFile.getAbsolutePath());
    }

    protected class CacheDatabaseHelper extends SQLiteOpeHelper {

        public CacheDatabaseHelper(final Context context, final String name) {
            super(context, name, null, 3);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(SQLiteMapDatabase.SQL_CREATE_tiles);
            db.execSQL(SQLiteMapDatabase.SQL_CREATE_info);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        }

    }

    public void updateMinMaxZoom() throws SQLiteException {
        if (mDatabase != null) {
            Ut.dd("Update min max");
            mDatabase.execSQL("DROP TABLE IF EXISTS info");
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS info AS SELECT MIN(z) AS minzoom, MAX(z) AS maxzoom FROM tiles");
        }
    }

    public/* synchronized */void putTile(final int aX, final int aY, final int aZ, final byte[] aData) {
        if (mDatabase != null) {
            final ContentValues cv = new ContentValues();
            cv.put("x", aX);
            cv.put("y", aY);
            cv.put("z", 17 - aZ);
            cv.put("s", 0);
            cv.put("image", aData);
            mDatabase.insert("tiles", null, cv);
        }
    }

    public/* synchronized */byte[] getTile(final int aX, final int aY, final int aZ) {
        byte[] ret = null;

        if (mDatabase != null) {
            final Cursor c = mDatabase.rawQuery("SELECT image FROM tiles WHERE s = 0 AND x = " + aX + " AND y = " + aY + " AND z = " + (17 - aZ), null);
            if (c != null) {
                if (c.moveToFirst()) {
                    ret = c.getBlob(c.getColumnIndexOrThrow("image"));
                }
                c.close();
            }
        }

        return ret;
    }

    public int getMaxZoom() {
        int ret = 99;
        if (mDatabase != null) {
            final Cursor c = mDatabase.rawQuery("SELECT 17-minzoom AS ret FROM info", null);
            if (c != null) {
                if (c.moveToFirst()) {
                    ret = c.getInt(c.getColumnIndexOrThrow("ret"));
                }
                c.close();
            }
        }
        ;
        return ret;
    }

    public int getMinZoom() {
        int ret = 0;
        if (mDatabase != null) {
            final Cursor c = mDatabase.rawQuery("SELECT 17-maxzoom AS ret FROM info", null);
            if (c != null) {
                if (c.moveToFirst()) {
                    ret = c.getInt(c.getColumnIndexOrThrow("ret"));
                }
                c.close();
            }
        }
        return ret;
    }

    @Override
    protected void finalize() throws Throwable {
        Ut.dd("finalize: Close SQLITEDB Database database");
        if (mDatabase != null) {
            mDatabase.close();
        }
        super.finalize();
    }

    public void freeDatabases() {
        if (mDatabase != null) {
            if (mDatabase.isOpen()) {
                mDatabase.close();
                mDatabase = null;
                Ut.dd("Close SQLITEDB Database");
            }
        }
    }
}
