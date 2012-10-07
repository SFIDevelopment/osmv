package at.the.gogo.parkoid.util.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.LocationPoint;
import at.the.gogo.parkoid.util.Util;

public class Database {

    private static final int         DB_VERSION          = 2;

    protected final Context          mCtx;
    private SQLiteDatabase           mDatabase;
    protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat(
                                                                 "yyyy-MM-dd'T'HH:mm:ss.SSS");

    public Database(final Context ctx) {
        super();
        mCtx = ctx;
        mDatabase = getDatabase();
    }

    private boolean isDatabaseReady() {
        boolean ret = true;

        if (mDatabase == null) {
            mDatabase = getDatabase();
        }

        if (mDatabase == null) {
            ret = false;
        } else if (!mDatabase.isOpen()) {
            mDatabase = getDatabase();
        }

        if (ret == false) {
            try {
                Toast.makeText(mCtx,
                        mCtx.getText(R.string.message_db_notavailable),
                        Toast.LENGTH_LONG).show();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public void freeDatabases() {
        if (mDatabase != null) {
            if (mDatabase.isOpen()) {
                mDatabase.close();
                mDatabase = null;
            }
        }
    }

    protected SQLiteDatabase getDatabase() {
        final File folder = Util.getDBDir(mCtx, DBConstants.DATA);
        if (!folder.exists()) {
            return null;
        }

        final SQLiteDatabase db = new DatabaseHelper(mCtx,
                folder.getAbsolutePath() + DBConstants.DATA_FILENAME)
                .getWritableDatabase();

        return db;
    }

    protected class DatabaseHelper extends DBCoreHelper {
        public DatabaseHelper(final Context context, final String name) {
            super(context, name, null, Database.DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {

            // create tables

            db.execSQL(DBConstants.SQL_CREATE_Cars);
            db.execSQL(DBConstants.SQL_CREATE_locations);
            db.execSQL(DBConstants.SQL_CREATE_Sms);
            db.execSQL(DBConstants.SQL_CREATE_Smsr);

            // create indecies

            // add default data

        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                final int newVersion) {
            Util.dd("Upgrade data.db from ver." + oldVersion + " to ver."
                    + newVersion);

            if (oldVersion < 2) {

                db.execSQL(DBConstants.SQL_CREATE_Sms);
                db.execSQL(DBConstants.SQL_CREATE_Smsr);
                // db.execSQL(DBConstants.SQL_UPDATE_3_1);
            }

        }

        @Override
        public void onOpen(final SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS locationcache;");
            db.execSQL(DBConstants.SQL_CREATE_LOCATION_CACHE);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        if (mDatabase != null) {
            if (mDatabase.isOpen()) {
                mDatabase.close();
                mDatabase = null;
            }
        }
        super.finalize();
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void rollbackTransaction() {
        mDatabase.endTransaction();
    }

    public void commitTransaction() {
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    // CAR
    public Cursor getCar(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.SQL_GET_Car, args);
        }

        return null;
    }

    public void addCar(final String aName, final String aLicence) {
        if (isDatabaseReady()) {

            final ContentValues cv = new ContentValues();
            cv.put("name", aName);
            cv.put("licence", aLicence);
            mDatabase.insert(DBConstants.TABLE_CARS, null, cv);
        }
    }

    public void updateCar(final int id, final String aName,
            final String aLicence) {
        if (isDatabaseReady()) {
            final ContentValues cv = new ContentValues();
            cv.put("name", aName);
            cv.put("licence", aLicence);

            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.TABLE_CARS, cv,
                    DBConstants.SQL_UPDATE_Car, args);
        }
    }

    public void deleteCar(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            mDatabase.execSQL(DBConstants.SQL_DELETE_Car, args);
        }
    }

    public Cursor getCarListCursor() {
        if (isDatabaseReady()) {

            return mDatabase.rawQuery(DBConstants.SQL_GET_CarList, null);
        }
        return null;
    }

    public int getNrofCars() {
        int nr = 0;
        if (isDatabaseReady()) {

            final Cursor cursor = mDatabase.rawQuery(
                    DBConstants.SQL_GET_CarList, null);
            nr = cursor.getCount();
            cursor.close();
        }
        return nr;
    }

    public void deleteAllCars() {
        if (isDatabaseReady()) {
            mDatabase.execSQL(DBConstants.SQL_DELETE_Cars);
        }
    }

    // LOCATIONS
    public Cursor getCarLocation(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.SQL_GET_CarLocation, args);
        }

        return null;
    }

    public Cursor getLocation(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.SQL_GET_Location, args);
        }

        return null;
    }

    public void addLocation(final int carId, final double lat,
            final double lon, final Date date) {
        if (isDatabaseReady()) {

            final ContentValues cv = new ContentValues();
            cv.put("carid", carId);
            cv.put("lat", lat);
            cv.put("lon", lon);
            cv.put("date", date.getTime());
            mDatabase.insert(DBConstants.TABLE_LOCATIONS, null, cv);
        }
    }

    public void updateLocation(final int id, final int carId, final double lat,
            final double lon, final Date date) {
        if (isDatabaseReady()) {
            final ContentValues cv = new ContentValues();
            cv.put("carid", carId);
            cv.put("lat", lat);
            cv.put("lon", lon);
            cv.put("date", date.getTime());

            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.TABLE_LOCATIONS, cv,
                    DBConstants.SQL_UPDATE_Location, args);
        }
    }

    public void deleteLocation(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            mDatabase.execSQL(DBConstants.SQL_DELETE_Location, args);
        }
    }

    public void deleteCarLocation(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            mDatabase.execSQL(DBConstants.SQL_DELETE_CarLocation, args);
        }
    }

    public Cursor getLocationCarListCursor(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.SQL_GET_CarLocation, args);
        }
        return null;
    }

    public Cursor getLocationListCursor() {
        if (isDatabaseReady()) {

            return mDatabase.rawQuery(DBConstants.SQL_GET_CarLocations, null);
        }
        return null;
    }

    public void deleteAllLocations() {
        if (isDatabaseReady()) {
            mDatabase.execSQL(DBConstants.SQL_DELETE_Locations);
        }
    }

    // SMS send
    public Cursor getSMS(final int id, final String tableName) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase
                    .rawQuery(
                            tableName.equals(DBConstants.TABLE_SMS) ? DBConstants.SQL_GET_Sms
                                    : DBConstants.SQL_GET_Smsr, args);
        }

        return null;
    }

    public void addSMS(final String receiver, final String text,
            final Date date, final String tableName) {
        if (isDatabaseReady()) {

            final ContentValues cv = new ContentValues();
            // cv.put("smsid", smsId);
            cv.put("name", receiver);
            cv.put("text", text);
            cv.put("date", date.getTime());

            mDatabase.insert(tableName, null, cv);
        }
    }

    public void updateSMS(final int smsId, final String receiver,
            final String text, final Date date, final String tableName) {
        if (isDatabaseReady()) {
            final ContentValues cv = new ContentValues();
            // cv.put("smsid", smsId);
            cv.put("name", receiver);
            cv.put("text", text);
            cv.put("date", date.getTime());

            final String[] args = { Integer.toString(smsId) };
            mDatabase.update(tableName, cv, (tableName
                    .equals(DBConstants.TABLE_SMS) ? DBConstants.SQL_UPDATE_Sms
                    : DBConstants.SQL_UPDATE_Smsr), args);
        }
    }

    public void deleteSMS(final int id, final String tableName) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            mDatabase
                    .execSQL(
                            tableName.equals(DBConstants.TABLE_SMS) ? DBConstants.SQL_DELETE_Sms
                                    : DBConstants.SQL_DELETE_Smsr, args);
        }
    }

    public Cursor getSMSListCursor(final String tableName) {
        if (isDatabaseReady()) {

            return mDatabase
                    .rawQuery(
                            tableName.equals(DBConstants.TABLE_SMS) ? DBConstants.SQL_GET_SmsList
                                    : DBConstants.SQL_GET_SmsrList, null);
        }
        return null;
    }

    public void deleteAllSMS(final String tableName) {
        if (isDatabaseReady()) {
            mDatabase
                    .execSQL(tableName.equals(DBConstants.TABLE_SMS) ? DBConstants.SQL_DELETE_Smss
                            : DBConstants.SQL_DELETE_Smssr);
        }
    }

    public static final String COLUMN_LATITUDE  = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ACCURACY  = "accuracy";
    public static final String COLUMN_TIME      = "time";
    public static final String COLUMN_PROVIDER  = "provider";

    public void insertLocation(final Location location) {
        insertLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getTime(), location.getProvider());
    }

    public void insertLocation(double lat, double lon, float acc, long time,
            String provider) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_LATITUDE, lat);
            contentValues.put(COLUMN_LONGITUDE, lon);
            contentValues.put(COLUMN_ACCURACY, acc);
            contentValues.put(COLUMN_TIME, time);
            contentValues.put(COLUMN_PROVIDER, provider);

            mDatabase.insert("locationcache", null, contentValues);
        }
    }

    public LocationPoint retrieveLatestPoint() {
        final String[] columns = { COLUMN_LATITUDE, COLUMN_LONGITUDE,
                COLUMN_ACCURACY, COLUMN_TIME, COLUMN_PROVIDER };

        final Cursor cursor = mDatabase.query("locationcache", columns, null,
                null, null, null, "time DESC", "1");
        LocationPoint point;

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                point = new LocationPoint();
                point.setLatitude(cursor.getDouble(cursor
                        .getColumnIndex(COLUMN_LATITUDE)));
                point.setLongitude(cursor.getDouble(cursor
                        .getColumnIndex(COLUMN_LONGITUDE)));
                point.setAccuracy(cursor.getFloat(cursor
                        .getColumnIndex(COLUMN_ACCURACY)));
                point.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
                point.setProvider(cursor.getString(cursor
                        .getColumnIndex(COLUMN_PROVIDER)));
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
}
