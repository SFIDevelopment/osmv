package org.outlander.io.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.outlander.constants.DBConstants;
import org.outlander.model.LocationPoint;
import org.outlander.utils.Ut;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class CachedLocationsDatabaseHelper extends SQLiteOpeHelper {

    private static CachedLocationsDatabaseHelper db = null;

    public static CachedLocationsDatabaseHelper getInstance(
            final Context context) {
        if (db == null) {
            final File folder = Ut.getTschekkoMapsMainDir(context, "data");
            if (folder.canRead()) {
                try {
                    db = new CachedLocationsDatabaseHelper(context,
                            folder.getAbsolutePath() + "/cachedlocations.db");
                } catch (final Exception e) {
                    Ut.e("CachedLocationsDatabaseHelper failed");
                    db = null;
                }
            }
        }
        return db;
    }

    public CachedLocationsDatabaseHelper(final Context context,
            final String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onOpen(final SQLiteDatabase db) {

        super.onOpen(db);
        db.execSQL("DROP TABLE IF EXISTS locationcache;");
        db.execSQL(DBConstants.SQL_CREATE_LOCATION_CACHE);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
    }

    public void insertLocationPoint(final double lat, final double lon,
            final double alt, final float acc, final long time,
            final String provider) {

        final ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.LC_COLUMN_LATITUDE, lat);
        contentValues.put(DBConstants.LC_COLUMN_LONGITUDE, lon);
        contentValues.put(DBConstants.LC_COLUMN_ALTITUDE, alt);
        contentValues.put(DBConstants.LC_COLUMN_ACCURACY, acc);
        contentValues.put(DBConstants.LC_COLUMN_TIME, time);
        contentValues.put(DBConstants.LC_COLUMN_PROVIDER, provider);

        db.getWritableDatabase().insert("locationcache", null, contentValues);

    }

    public Cursor retrieveLatestLocationPointCursor() {
        final String[] columns = { DBConstants.LC_COLUMN_LATITUDE,
                DBConstants.LC_COLUMN_LONGITUDE,
                DBConstants.LC_COLUMN_ALTITUDE, DBConstants.LC_COLUMN_ACCURACY,
                DBConstants.LC_COLUMN_TIME, DBConstants.LC_COLUMN_PROVIDER };

        final Cursor cursor = db.getWritableDatabase().query("locationcache",
                columns, null, null, null, null, "time DESC", "1");

        return cursor;
    }

    public Cursor retrieveLatestLocationPointsCursor() {
        Cursor cursor = null;

        cursor = db.getWritableDatabase().rawQuery(
                DBConstants.SQL_GET_LAST_POINTS_FROM_CACHE, null);

        return cursor;
    }

    public void insertLocationPoint(final Location location) {

        insertLocationPoint(location.getLatitude(), location.getLongitude(),
                location.getAltitude(), location.getAccuracy(),
                location.getTime(), location.getProvider());
    }

    public LocationPoint retrieveLatestLocationPoint() {

        final Cursor cursor = retrieveLatestLocationPointCursor();

        LocationPoint point;

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                point = setLPFromCursor(cursor);
            } else {
                point = null;
            }
        } catch (final Exception e) {

            point = null;
        } finally {
            cursor.close();
        }

        return point;
    }

    private LocationPoint setLPFromCursor(final Cursor cursor) {
        final LocationPoint point = new LocationPoint();

        // use index !
        point.setLatitude(cursor.getDouble(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_LATITUDE)));
        point.setLongitude(cursor.getDouble(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_LONGITUDE)));
        point.setAltitude(cursor.getDouble(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_ALTITUDE)));
        point.setAccuracy(cursor.getFloat(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_ACCURACY)));
        point.setTime(cursor.getLong(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_TIME)));
        point.setAccuracy(cursor.getFloat(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_ACCURACY)));
        point.setProvider(cursor.getString(cursor
                .getColumnIndex(DBConstants.LC_COLUMN_PROVIDER)));

        return point;
    }

    public List<LocationPoint> retrieveLatestLocationPoints() {
        final List<LocationPoint> locationPoints = new ArrayList<LocationPoint>();

        final Cursor cursor = retrieveLatestLocationPointsCursor();
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                do {
                    locationPoints.add(setLPFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return locationPoints;

    }

}
