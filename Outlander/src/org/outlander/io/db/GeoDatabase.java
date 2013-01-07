package org.outlander.io.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.outlander.R;
import org.outlander.constants.DBConstants;
import org.outlander.utils.Ut;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.widget.Toast;

public class GeoDatabase {

    private static final int         GEODB_VERSION       = 17;

    protected final Context          mCtx;
    private SQLiteDatabase           mDatabase;
    protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public GeoDatabase(final Context ctx) {
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
        }
        else if (!mDatabase.isOpen()) {
            mDatabase = getDatabase();
        }

        if (ret == false) {
            try {
                Toast.makeText(mCtx, mCtx.getText(R.string.message_geodata_notavailable), Toast.LENGTH_LONG).show();
            }
            catch (final Exception e) {
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
        final File folder = Ut.getTschekkoMapsMainDir(mCtx, DBConstants.DATA);
        if (!folder.exists()) {
            return null;
        }

        final SQLiteDatabase db = new GeoDatabaseHelper(mCtx, folder.getAbsolutePath() + DBConstants.GEODATA_FILENAME).getWritableDatabase();

        return db;
    }

    protected class GeoDatabaseHelper extends SQLiteOpeHelper {

        public GeoDatabaseHelper(final Context context, final String name) {
            super(context, name, null, GeoDatabase.GEODB_VERSION);

        }

        @Override
        public void onOpen(final SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS locationcache;");
            // db.execSQL(DBConstants.SQL_CREATE_LOCATION_CACHE);
            
            if (Debug.isDebuggerConnected())
            {
                db.execSQL(DBConstants.SQL_UPDATE_POINT_ICON_ID);
                db.execSQL(DBConstants.SQL_UPDATE_CAT_POI_ICON_ID);
                db.execSQL(DBConstants.SQL_UPDATE_CAT_WIKI_ICON_ID);
                db.execSQL(DBConstants.SQL_UPDATE_CAT_TOPO_ICON_ID);

            }
            
            
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {

            // create tables

            db.execSQL(DBConstants.SQL_CREATE_points);
            db.execSQL(DBConstants.SQL_CREATE_pointsource);
            db.execSQL(DBConstants.SQL_CREATE_category);
            db.execSQL(DBConstants.SQL_CREATE_tracks);
            db.execSQL(DBConstants.SQL_CREATE_trackpoints);
            db.execSQL(DBConstants.SQL_CREATE_routepoints);
            db.execSQL(DBConstants.SQL_CREATE_routes);
            db.execSQL(DBConstants.SQL_CREATE_routecategory);
            db.execSQL(DBConstants.SQL_CREATE_trackcategory);
            db.execSQL(DBConstants.SQL_CREATE_proximitytarget);

            // create indecies

            db.execSQL(DBConstants.SQL_CREATE_INDEX_POI_NAME);
            db.execSQL(DBConstants.SQL_CREATE_INDEX_ROUTE_NAME);
            db.execSQL(DBConstants.SQL_CREATE_INDEX_TRACK_NAME);

            db.execSQL(DBConstants.SQL_CREATE_INDEX_TRACKPOINT_TID);

            // add default data

            db.execSQL(DBConstants.SQL_ADD_category1);
            db.execSQL(DBConstants.SQL_ADD_category2);
            db.execSQL(DBConstants.SQL_ADD_category3);
            db.execSQL(DBConstants.SQL_ADD_category4);

            db.execSQL(DBConstants.SQL_ADD_routecategory1);
            db.execSQL(DBConstants.SQL_ADD_routecategory2);
            db.execSQL(DBConstants.SQL_ADD_routecategory3);

            db.execSQL(DBConstants.SQL_ADD_trackcategory1);
            db.execSQL(DBConstants.SQL_ADD_trackcategory2);

        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            Ut.dd("Upgrade data.db from ver." + oldVersion + " to ver." + newVersion);

            if (oldVersion < 9) {

                db.execSQL(DBConstants.SQL_UPDATE_3_1);
                db.execSQL(DBConstants.SQL_UPDATE_3_2);
            }

            if (oldVersion < 10) {
                db.execSQL(DBConstants.SQL_CREATE_proximitytarget);
            }
            if (oldVersion < 11) {
                db.execSQL(DBConstants.SQL_CREATE_INDEX_POI_NAME);
                db.execSQL(DBConstants.SQL_CREATE_INDEX_ROUTE_NAME);
                db.execSQL(DBConstants.SQL_CREATE_INDEX_TRACK_NAME);
                db.execSQL(DBConstants.SQL_CREATE_INDEX_TRACKPOINT_TID);
            }
            if (oldVersion < 13) {
                db.execSQL(DBConstants.SQL_UPDATE_3_3_1);
                db.execSQL(DBConstants.SQL_UPDATE_3_3_2);
                db.execSQL(DBConstants.SQL_UPDATE_3_3_3);
            }
            if (oldVersion < 14) {
                db.execSQL(DBConstants.SQL_UPDATE_4_1);
            }
            if (oldVersion < 15) {
                db.execSQL(DBConstants.SQL_UPDATE_4_3);
            }

            if (oldVersion < 17) {
                try {

                    db.execSQL(DBConstants.SQL_UPDATE_4_2);
                    db.execSQL("delete from category;");
                    // add all new
                    db.execSQL(DBConstants.SQL_ADD_category1);
                    db.execSQL(DBConstants.SQL_ADD_category2);
                    db.execSQL(DBConstants.SQL_ADD_category3);
                    db.execSQL(DBConstants.SQL_ADD_category4);

                }
                catch (final Exception x) {
                    Ut.e("DB Upgrade failed:" + x.getMessage());
                }
            }
            
            
        }
    }

    public void addPoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId,
            final int aPointSourceId, final int hidden, final int iconid) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, aName);
            contentValues.put(DBConstants.DESCR, aDescr);
            contentValues.put(DBConstants.LAT, aLat);
            contentValues.put(DBConstants.LON, aLon);
            contentValues.put(DBConstants.ALT, aAlt);
            contentValues.put(DBConstants.CATEGORYID, aCategoryId);
            contentValues.put(DBConstants.POINTSOURCEID, aPointSourceId);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            mDatabase.insert(DBConstants.POINTS, null, contentValues);
        }
    }

    public void updatePoi(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt,
            final int aCategoryId, final int aPointSourceId, final int hidden, final int iconid) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, aName);
            contentValues.put(DBConstants.DESCR, aDescr);
            contentValues.put(DBConstants.LAT, aLat);
            contentValues.put(DBConstants.LON, aLon);
            contentValues.put(DBConstants.ALT, aAlt);
            contentValues.put(DBConstants.CATEGORYID, aCategoryId);
            contentValues.put(DBConstants.POINTSOURCEID, aPointSourceId);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.POINTS, contentValues, DBConstants.UPDATE_POINTS, args);
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

    public Cursor getPoiListCursor() { // get ALL !!!!!
        if (isDatabaseReady()) {
            return mDatabase.rawQuery(DBConstants.STAT_GET_POI_LIST, null);
        }

        return null;
    }

    public Cursor getPoiListUserCursor() { // get only uservisible
        if (isDatabaseReady()) {
            return mDatabase.rawQuery(DBConstants.STAT_GET_POI_LIST_USER_ONLY, null);
        }

        return null;
    }

    public Cursor getPoiListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(zoom + 1), Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top) };
            return mDatabase.rawQuery(DBConstants.STAT_PoiListNotHidden, args);
        }

        return null;
    }

    public Cursor getPoiCategoryListCursor() {
        if (isDatabaseReady()) {
            return mDatabase.rawQuery(DBConstants.STAT_getPoiCategoryList, null);
        }

        return null;
    }

    public Cursor getPoiUserCategoryListCursor() {
        if (isDatabaseReady()) {
            return mDatabase.rawQuery(DBConstants.STAT_getPoiUserCategoryList, null);
        }

        return null;
    }

    public Cursor getPoi(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.STAT_getPoi, args);
        }

        return null;
    }

    public Cursor getPoiByName(final String name) {
        if (isDatabaseReady()) {
            final String[] args = { name };
            return mDatabase.rawQuery(DBConstants.STAT_getPoiByName, args);
        }

        return null;
    }

    public Cursor getPoiListForCategory(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.STAT_getPoiListForCategory, args);
        }

        return null;
    }

    public int getNrofPoiForCategory(final int categoryId) {
        int nr = 0;
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(categoryId) };
            final Cursor cursor = mDatabase.rawQuery(DBConstants.STAT_getPoiListForCategory, args);
            nr = cursor.getCount();
            cursor.close();
        }
        return nr;
    }

    public void deletePoi(final int id) {
        if (isDatabaseReady()) {
            final Double[] args = { Double.valueOf(id) };
            mDatabase.execSQL(DBConstants.STAT_deletePoi, args);
        }
    }

    public void deletePoisOfCategory(final int categoryId) {
        if (isDatabaseReady()) {
            final Double[] args = { Double.valueOf(categoryId) };
            mDatabase.execSQL(DBConstants.STAT_deletePoisOfCategory, args);
        }
    }

    public void deletePoiCategory(final int id) {
        if (isDatabaseReady() && (id != DBConstants.ZERO)) { // predef category
                                                             // My POI never
            // delete
            final String[] args = { Integer.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deletePoiCategory, args);
        }
    }

    public Cursor getPoiCategory(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.STAT_getPoiCategory, args);
        }

        return null;
    }

    public void addPoiCategory(final String title, final int hidden, final int iconid, final String descr) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, title);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            contentValues.put("descr", descr);
            mDatabase.insert(DBConstants.CATEGORY, null, contentValues);
        }
    }

    public void updatePoiCategory(final int id, final String title, final int hidden, final int iconid, final int minzoom, final String descr) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, title);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            contentValues.put(DBConstants.MINZOOM, minzoom);
            contentValues.put("descr", descr);
            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.CATEGORY, contentValues, DBConstants.UPDATE_CATEGORY, args);
        }
    }

    public void deleteAllPois() {
        if (isDatabaseReady()) {
            mDatabase.execSQL(DBConstants.STAT_DeleteAllPoi);
        }
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

    public Cursor getTrackListCursor() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackList, null);
        }
        return cursor;
    }

    public long addTrack(final String name, final String descr, final int show, final double avgSpeed, final double distance, final long time) {
        long newId = -1;

        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, name);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.SHOW, show);
            contentValues.put("avgspeed", avgSpeed);
            contentValues.put("distance", distance);
            contentValues.put("time", time);
            newId = mDatabase.insert(DBConstants.TRACKS, null, contentValues);
        }
        return newId;
    }

    public void updateTrack(final int id, final String name, final String descr, final int show, final double avgSpeed, final double distance, final long time) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, name);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.SHOW, show);
            contentValues.put("avgspeed", avgSpeed);
            contentValues.put("distance", distance);
            contentValues.put("time", time);

            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.TRACKS, contentValues, DBConstants.UPDATE_TRACKS, args);
        }
    }

    public void addTrackPoint(final long trackid, final double lat, final double lon, final double alt, final double speed, final Date date) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.TRACKID, trackid);
            contentValues.put(DBConstants.LAT, lat);
            contentValues.put(DBConstants.LON, lon);
            contentValues.put(DBConstants.ALT, alt);
            contentValues.put(DBConstants.SPEED, speed);
            contentValues.put("date", date.getTime());
            mDatabase.insert(DBConstants.TRACKPOINTS, null, contentValues);
        }
    }

    public Cursor getTrackChecked() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackChecked, null);
        }
        return cursor;
    }

    public Cursor getTracksChecked() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTracksChecked, null);
        }
        return cursor;
    }

    public Cursor getTrack(final long id) {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            final String[] args = { Long.toString(id) };
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrack, args);
        }
        return cursor;
    }

    public Cursor getTrackByName(final String name) {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            final String[] args = { name };
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackByName, args);
        }
        return cursor;
    }

    public Cursor getTrackPoints(final long id) {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            final String[] args = { Long.toString(id) };
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackPoints, args);
        }
        return cursor;
    }

    public void setTrackChecked(final int id, final boolean visible) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(visible ? 1 : 0), Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_setTrackChecked, args);
            // just one remains checked!!
            final String[] args2 = { Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_setTrackChecked_2, args2);

        }
    }

    public void setTracksChecked(final boolean visible) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(visible ? 1 : 0) };
            mDatabase.execSQL(DBConstants.STAT_setTracksChecked, args);
        }
    }

    public void deleteTrack(final int id) {
        if (isDatabaseReady()) {
            beginTransaction();
            final String[] args = { Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteTrack_1, args); // delete
                                                                     // all
                                                                     // points
            mDatabase.execSQL(DBConstants.STAT_deleteTrack_2, args); // delete
                                                                     // track
            commitTransaction();
        }
    }

    public void deleteAllTrackPoints(final int id) {
        if (isDatabaseReady()) {
            beginTransaction();
            final String[] args = { Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteTrack_1, args); // delete
                                                                     // all
                                                                     // points
            commitTransaction();
        }
    }

    public void deleteAllTracks() {
        if (isDatabaseReady()) {
            beginTransaction();
            mDatabase.execSQL(DBConstants.STAT_deleteAllTracks1);
            mDatabase.execSQL(DBConstants.STAT_deleteAllTracks2);
            commitTransaction();
        }
    }

    public int saveTrackFromWriter(final SQLiteDatabase db) {
        int res = 0;
        if (isDatabaseReady()) {
            final Cursor c = db.rawQuery(DBConstants.STAT_saveTrackFromWriter, null);
            if (c != null) {
                if (c.getCount() > 1) {
                    beginTransaction();

                    res = c.getCount();
                    long newId = -1;

                    final ContentValues contentValues = new ContentValues();
                    contentValues.put(DBConstants.NAME, DBConstants.TRACK);
                    contentValues.put(DBConstants.SHOW, 0);
                    contentValues.put(DBConstants.CATEGORYID, DBConstants.TRACK_CATEGORY_DEFAULT_RECORDED);
                    newId = mDatabase.insert(DBConstants.TRACKS, null, contentValues);

                    contentValues.put(DBConstants.NAME, DBConstants.TRACK + DBConstants.ONE_SPACE + newId);
                    final String[] args = { Long.toString(newId) };
                    mDatabase.update(DBConstants.TRACKS, contentValues, DBConstants.UPDATE_TRACKS, args);

                    if (c.moveToFirst()) {
                        do {
                            contentValues.clear();
                            contentValues.put(DBConstants.TRACKID, newId);
                            contentValues.put(DBConstants.LAT, c.getDouble(0));
                            contentValues.put(DBConstants.LON, c.getDouble(1));
                            contentValues.put(DBConstants.ALT, c.getDouble(2));
                            contentValues.put(DBConstants.SPEED, c.getDouble(3));
                            contentValues.put(DBConstants.DATE, c.getInt(4));
                            mDatabase.insert(DBConstants.TRACKPOINTS, null, contentValues);
                        } while (c.moveToNext());
                    }

                    commitTransaction();
                }
                c.close();

                db.execSQL(DBConstants.STAT_CLEAR_TRACKPOINTS);
            }
        }
        return res;
    }

    public Cursor getTrackCategoryListCursor() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackCategoryList, null);
        }
        return cursor;
    }

    public int getNrofTracksForCategory(final int categoryId) {
        int nr = 0;
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(categoryId) };
            final Cursor cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackListForCategory, args);
            nr = cursor.getCount();
            cursor.close();
        }
        return nr;
    }

    public Cursor getTrackCategory(final int id) {
        Cursor cursor = null;
        if (isDatabaseReady()) {

            final String[] args = { Integer.toString(id) };
            cursor = mDatabase.rawQuery(DBConstants.STAT_getTrackCategory, args);
        }

        return cursor;
    }

    public void deleteTrackCategory(final int id) {
        if (isDatabaseReady() && (id != DBConstants.ZERO)) { // predef category
                                                             // My POI never
            // delete
            final String[] args = { Integer.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteTrackCategory, args);
        }
    }

    public void addTrackCategory(final String title, final String descr, final int hidden, final int iconid, final int minzoom) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, title);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            contentValues.put(DBConstants.MINZOOM, minzoom);
            mDatabase.insert(DBConstants.TRACKCATEGORY, null, contentValues);
        }
    }

    public void updateTrackCategory(final int id, final String title, final String descr, final int hidden, final int iconid, final int minzoom) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, title);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            contentValues.put(DBConstants.MINZOOM, minzoom);
            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.TRACKCATEGORY, contentValues, DBConstants.UPDATE_CATEGORY, args);
        }
    }

    // routes / routecategories / routepois

    public Cursor getRouteCategory(final int id) {
        if (isDatabaseReady()) {

            final String[] args = { Integer.toString(id) };
            return mDatabase.rawQuery(DBConstants.STAT_getRouteCategory, args);
        }

        return null;
    }

    public void deleteRouteCategory(final int id) {
        if (isDatabaseReady() && (id != DBConstants.ZERO)) { // predef category
                                                             // My POI never
            // delete
            final String[] args = { Integer.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteRouteCategory, args);
        }
    }

    public void addRouteCategory(final String title, final String descr, final int hidden, final int iconid, final int minzoom) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, title);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            contentValues.put(DBConstants.MINZOOM, minzoom);
            mDatabase.insert(DBConstants.ROUTECATEGORY, null, contentValues);
        }
    }

    public void updateRouteCategory(final int id, final String title, final String descr, final int hidden, final int iconid, final int minzoom) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, title);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            contentValues.put(DBConstants.MINZOOM, minzoom);
            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.ROUTECATEGORY, contentValues, DBConstants.UPDATE_CATEGORY, args);
        }
    }

    public Cursor getRoute(final Integer id) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(id) };

            return mDatabase.rawQuery(DBConstants.STAT_getRoute, args);
        }

        return null;
    }

    public Cursor getRouteByName(final String name) {
        if (isDatabaseReady()) {
            final String[] args = { name };

            return mDatabase.rawQuery(DBConstants.STAT_getRouteByName, args);
        }

        return null;
    }

    public long addRoute(final String name, final String descr, final int show, final int routecategoryId) {
        long newId = -1;

        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, name);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.SHOW, show);
            contentValues.put(DBConstants.CATEGORYID, routecategoryId);
            newId = mDatabase.insert(DBConstants.ROUTE, null, contentValues);
        }
        return newId;
    }

    public void updateRoute(final int id, final String name, final String descr, final int show, final int routecategoryId) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, name);
            contentValues.put(DBConstants.DESCR, descr);
            contentValues.put(DBConstants.SHOW, show);
            contentValues.put(DBConstants.CATEGORYID, routecategoryId);
            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.ROUTE, contentValues, DBConstants.UPDATE_ROUTES, args);
        }
    }

    public Cursor getRoutePoints(final long id) {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            final String[] args = { Long.toString(id) };
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRoutePoints, args);
        }
        return cursor;
    }

    public void addRoutePoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId,
            final int aPointSourceId, final int hidden, final int iconid) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, aName);
            contentValues.put(DBConstants.DESCR, aDescr);
            contentValues.put(DBConstants.LAT, aLat);
            contentValues.put(DBConstants.LON, aLon);
            contentValues.put(DBConstants.ALT, aAlt);
            contentValues.put(DBConstants.CATEGORYID, aCategoryId);
            contentValues.put(DBConstants.POINTSOURCEID, aPointSourceId);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            mDatabase.insert(DBConstants.ROUTEPOINTS, null, contentValues);
        }
    }

    public void updateRoutePoi(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt,
            final int aCategoryId, final int aPointSourceId, final int hidden, final int iconid) {
        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, aName);
            contentValues.put(DBConstants.DESCR, aDescr);
            contentValues.put(DBConstants.LAT, aLat);
            contentValues.put(DBConstants.LON, aLon);
            contentValues.put(DBConstants.ALT, aAlt);
            contentValues.put(DBConstants.CATEGORYID, aCategoryId);
            contentValues.put(DBConstants.POINTSOURCEID, aPointSourceId);
            contentValues.put(DBConstants.HIDDEN, hidden);
            contentValues.put(DBConstants.ICONID, iconid);
            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.ROUTEPOINTS, contentValues, DBConstants.UPDATE_POINTS, args);
        }
    }

    public void deleteAllRoutePoi(final int id) {
        if (isDatabaseReady()) {
            final String[] args = { Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteRoutePoi, args);
        }
    }

    public Cursor getRoutesForCategory(final long id) {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            final String[] args = { Long.toString(id) };
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRoutesForCategory, args);
        }
        return cursor;
    }

    public void deleteRoute(final int id) {
        if (isDatabaseReady()) {
            beginTransaction();
            final String[] args = { Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteRoute_1, args);
            mDatabase.execSQL(DBConstants.STAT_deleteRoute_2, args);
            commitTransaction();
        }
    }

    public void deleteAllRoutes() {
        if (isDatabaseReady()) {
            beginTransaction();
            mDatabase.execSQL(DBConstants.STAT_deleteAllRoutes1);
            mDatabase.execSQL(DBConstants.STAT_deleteAllRoutes2);
            commitTransaction();
        }
    }

    public void setRouteChecked(final int id, final boolean visible) {
        if (isDatabaseReady()) {

            final String[] args = { Integer.toString(visible ? 1 : 0), Long.toString(id), };
            mDatabase.execSQL(DBConstants.STAT_setRouteChecked, args);
        }
    }

    public void setRoutesChecked(final boolean visible) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(visible ? 1 : 0) };
            mDatabase.execSQL(DBConstants.STAT_setRoutesChecked, args);
        }
    }

    public Cursor getRouteListCursor() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRouteList, null);
        }
        return cursor;
    }

    public Cursor getRoutesChecked() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRoutesChecked, null);
        }
        return cursor;
    }

    public void setPoiChecked(final int id, final boolean visible) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(!visible ? 1 : 0), Long.toString(id) };
            mDatabase.execSQL(DBConstants.STAT_setPoiChecked, args);
        }
    }

    public void setPoisChecked(final boolean visible) {
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(!visible ? 1 : 0) };
            mDatabase.execSQL(DBConstants.STAT_setPoisChecked, args);
        }
    }

    public Cursor getRouteCategoryListCursor() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRouteCategoryList, null);
        }
        return cursor;
    }

    public Cursor getRouteUserCategoryListCursor() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRouteUserCategoryList, null);
        }
        return cursor;
    }

    public Cursor getRouteChecked() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getRouteChecked, null);
        }
        return cursor;
    }

    public int getNrofRoutesForCategory(final int categoryId) {
        int nr = 0;
        if (isDatabaseReady()) {
            final String[] args = { Integer.toString(categoryId) };
            final Cursor cursor = mDatabase.rawQuery(DBConstants.STAT_getRoutesForCategory, args);
            nr = cursor.getCount();
            cursor.close();
        }
        return nr;
    }

    // proximity

    public void addProximityTarget(final String aName, final String aDescr, final double aLat, final double aLon, final int aRouteid, final int aRouteorder,
            final int aDistance, final int enterOrLeave) {

        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, aName);
            contentValues.put(DBConstants.DESCR, aDescr);
            contentValues.put(DBConstants.LAT, aLat);
            contentValues.put(DBConstants.LON, aLon);
            contentValues.put("routeid", aRouteid);
            contentValues.put("routeorder", aRouteorder);
            contentValues.put("distance", aDistance);
            contentValues.put("enter", enterOrLeave);

            mDatabase.insert(DBConstants.PROXIMITYTARGETS, null, contentValues);
        }
    }

    public void updateProximityTarget(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final int aRouteid,
            final int aRouteorder, final int aDistance, final int enterOrLeave) {

        if (isDatabaseReady()) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.NAME, aName);
            contentValues.put(DBConstants.DESCR, aDescr);
            contentValues.put(DBConstants.LAT, aLat);
            contentValues.put(DBConstants.LON, aLon);
            contentValues.put("routeid", aRouteid);
            contentValues.put("routeorder", aRouteorder);
            contentValues.put("distance", aDistance);
            contentValues.put("enter", enterOrLeave);

            final String[] args = { Integer.toString(id) };
            mDatabase.update(DBConstants.POINTS, contentValues, DBConstants.UPDATE_POINTS, args);
        }
    }

    public void deleteProximityTarget(final int id) {
        if (isDatabaseReady()) {
            final Double[] args = { Double.valueOf(id) };
            mDatabase.execSQL(DBConstants.STAT_deleteProximityTarget, args);
        }
    }

    public void deleteAllProximityTarget() {
        if (isDatabaseReady()) {
            mDatabase.execSQL(DBConstants.STAT_DeleteAllProximityTarget);
        }
    }

    public Cursor getProximityTargetListCursor() {
        Cursor cursor = null;
        if (isDatabaseReady()) {
            cursor = mDatabase.rawQuery(DBConstants.STAT_getProximityTargetList, null);
        }
        return cursor;
    }

}
