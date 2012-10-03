package org.anize.ur.life.wimp.util.db;

import java.io.File;
import java.text.SimpleDateFormat;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.models.LocationPoint;
import org.anize.ur.life.wimp.util.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.widget.Toast;

public class Database {

	private static final int DB_VERSION = 1;

	protected final Context mCtx;
	private SQLiteDatabase mDatabase;
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

			db.execSQL(DBConstants.SQL_CREATE_LOCATION_CACHE);

			// create indecies

			// add default data

		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			Util.dd("Upgrade data.db from ver." + oldVersion + " to ver."
					+ newVersion);

			if (oldVersion < 2) {
				// db.execSQL(DBConstants.SQL_UPDATE_3_1);
			}

		}

		@Override
		public void onOpen(final SQLiteDatabase db) {
			// db.execSQL("DROP TABLE IF EXISTS locationcache;");
			// db.execSQL(DBConstants.SQL_CREATE_LOCATION_CACHE);
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

	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_ACCURACY = "accuracy";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_PROVIDER = "provider";

	public void insertLocation(final Location location) {
		insertLocation(location.getLatitude(), location.getLongitude(),
				location.getAccuracy(), location.getTime(),
				location.getProvider());
	}

	public void insertLocation(final double lat, final double lon,
			final float acc, final long time, final String provider) {
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
