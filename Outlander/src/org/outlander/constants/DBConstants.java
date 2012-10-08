package org.outlander.constants;

import org.outlander.R;

public class DBConstants {

    public static final int    EMPTY_ID                         = -777;
    public static final int    ZERO                             = 0;
    public static final int    ONE                              = 1;
    public static final String EMPTY                            = "";
    public static final String ONE_SPACE                        = " ";

    public static final String LON                              = "lon";
    public static final String LAT                              = "lat";
    public static final String NAME                             = "name";
    public static final String DESCR                            = "descr";
    public static final String ALT                              = "alt";
    public static final String CATEGORYID                       = "categoryid";
    public static final String POINTSOURCEID                    = "pointsourceid";
    public static final String HIDDEN                           = "hidden";
    public static final String ICONID                           = "iconid";
    public static final String MINZOOM                          = "minzoom";
    public static final String SHOW                             = "show";
    public static final String TRACKID                          = "trackid";
    public static final String SPEED                            = "speed";
    public static final String DATE                             = "date";

    public static final String POINTS                           = "points";
    public static final String CATEGORY                         = "category";
    public static final String TRACKS                           = "tracks";
    public static final String TRACKPOINTS                      = "trackpoints";
    public static final String DATA                             = "data";
    public static final String GEODATA_FILENAME                 = "/geodata.db";
    public static final String TRACK                            = "Track";
    public static final String TRACKCATEGORY                    = "trackcategory";
    public static final String ROUTE                            = "routes";
    public static final String ROUTECATEGORY                    = "routecategory";

    public static final String ROUTEPOINTS                      = "routepoints";
    public static final String PROXIMITYTARGETS                 = "proximitytarget";

    public static final String UPDATE_POINTS                    = "pointid = @1";
    public static final String UPDATE_CATEGORY                  = "categoryid = @1";
    public static final String UPDATE_TRACKS                    = "trackid = @1";
    public static final String UPDATE_ROUTES                    = "routeid = @1";

    public static final String LC_COLUMN_LATITUDE               = "latitude";
    public static final String LC_COLUMN_LONGITUDE              = "longitude";
    public static final String LC_COLUMN_ALTITUDE               = "altitude";
    public static final String LC_COLUMN_ACCURACY               = "accuracy";
    public static final String LC_COLUMN_TIME                   = "time";
    public static final String LC_COLUMN_PROVIDER               = "provider";

    // 5
    public static final int    POI_CATEGORY_WIKI                = 0;

    public static final int    POI_CATEGORY_TOPO                = 1;
    public static final int    POI_CATEGORY_TARGET              = 2;
    public static final int    POI_CATEGORY_DEFAULT             = 3;

    public static final int    ROUTE_CATEGORY_DEFAULT_ROUTES    = 1;
    public static final int    ROUTE_CATEGORY_DEFAULT_HIKES     = 2;
    public static final int    ROUTE_CATEGORY_DEFAULT_NAVIROUTE = 0;

    public static final int    TRACK_CATEGORY_DEFAULT_RECORDED  = 0;
    public static final int    TRACK_CATEGORY_DEFAULT_IMPORTED  = 1;

    public static final String STAT_GET_POI_LIST                = "SELECT lat, lon,alt, name, descr, pointid _id,iconid, categoryid,hidden FROM points ORDER BY categoryid,name";
    public static final String STAT_GET_POI_LIST_USER_ONLY      = "SELECT lat, lon,alt, name, descr, pointid _id,iconid, categoryid,hidden FROM points where categoryid >="
                                                                        + POI_CATEGORY_DEFAULT + " ORDER BY categoryid,name";

    public static final String STAT_PoiListNotHidden            = "SELECT poi.lat, poi.lon, poi.name, poi.descr, poi.pointid, poi.pointid _id, poi.pointid ID, poi.categoryid, cat.iconid FROM points poi LEFT JOIN category cat ON cat.categoryid = poi.categoryid WHERE poi.hidden = 0 AND cat.hidden = 0 "
                                                                        + "AND cat.minzoom <= @1"
                                                                        + " AND poi.lon BETWEEN @2 AND @3"
                                                                        + " AND poi.lat BETWEEN @4 AND @5" + " ORDER BY lat, lon";

    public static final String STAT_getPoiCategoryList          = "SELECT name, categoryid _id,iconid,minzoom, descr FROM category ORDER BY name";
    public static final String STAT_getPoiUserCategoryList      = "SELECT name, categoryid _id,iconid,minzoom, descr FROM category where categoryid >="
                                                                        + POI_CATEGORY_DEFAULT + " ORDER BY name ";
    public static final String STAT_getPoi                      = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE pointid = @1";
    public static final String STAT_getPoiByName                = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE name = @1";
    public static final String STAT_deletePoi                   = "DELETE FROM points WHERE pointid = @1";
    public static final String STAT_deletePoisOfCategory        = "DELETE FROM points WHERE categoryid = @1";
    public static final String STAT_setPoiChecked               = "UPDATE points SET hidden = @1 WHERE pointid = @2";
    public static final String STAT_setPoisChecked              = "UPDATE points SET hidden = @1";
    public static final String STAT_deletePoiCategory           = "DELETE FROM category WHERE categoryid = @1";
    public static final String STAT_getPoiCategory              = "SELECT name, categoryid _id, hidden, iconid, minzoom, descr FROM category WHERE categoryid = @1";
    public static final String STAT_getPoiListForCategory       = "SELECT lat, lon, name, descr, pointid _id, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE categoryid = @1 ORDER BY name";

    // public static final String STAT_getPoiCountForCategory =
    // "SELECT count(categoryid) FROM points WHERE categoryid = @1";
    public static final String STAT_DeleteAllPoi                = "DELETE FROM points";
    // public static final String STAT_getTrackList =
    // "SELECT name, descr, trackid _id,categoryid, avgspeed, distance, time , CASE WHEN show=1 THEN "
    // + R.drawable.btn_check_buttonless_on
    // + " ELSE "
    // + R.drawable.btn_check_buttonless_off
    // + " END as image FROM tracks ORDER BY categoryid,trackid DESC;"; // ,
    public static final String STAT_getTrackList                = "SELECT name, descr, trackid _id,categoryid, avgspeed, distance, time, show FROM tracks ORDER BY categoryid,trackid DESC;";
    public static final String STAT_getTrackChecked             = "SELECT name, descr, show, trackid ,avgspeed, distance, time FROM tracks WHERE show = 1 LIMIT 1";
    public static final String STAT_getTracksChecked            = "SELECT name, descr, show, trackid ,avgspeed, distance, time FROM tracks WHERE show = 1";
    public static final String STAT_getTrack                    = "SELECT name, descr, show, trackid, avgspeed, distance, time,categoryid FROM tracks WHERE trackid = @1";
    public static final String STAT_getTrackByName              = "SELECT name, descr, show, trackid, avgspeed, distance, time,categoryid FROM tracks WHERE name = @1";
    public static final String STAT_getTrackPoints              = "SELECT lat, lon, alt, speed, date FROM trackpoints WHERE trackid = @1 ORDER BY id";
    public static final String STAT_setTrackChecked             = "UPDATE tracks SET show = @1 WHERE trackid = @2";
    public static final String STAT_setTrackChecked_2           = "UPDATE tracks SET show = 0 WHERE trackid <> @1";
    public static final String STAT_setTracksChecked            = "UPDATE tracks SET show = @1";
    public static final String STAT_deleteTrack_1               = "DELETE FROM trackpoints WHERE trackid = @1";
    public static final String STAT_deleteTrack_2               = "DELETE FROM tracks WHERE trackid = @1";
    public static final String STAT_deleteAllTracks1            = "DELETE FROM trackpoints";
    public static final String STAT_deleteAllTracks2            = "DELETE FROM tracks";
    public static final String STAT_saveTrackFromWriter         = "SELECT lat, lon, alt, speed, date FROM trackpoints ORDER BY id;";
    public static final String STAT_CLEAR_TRACKPOINTS           = "DELETE FROM 'trackpoints';";
    public static final String STAT_getTrackCategoryList        = "SELECT name, descr, categoryid _id FROM trackcategory ORDER BY name";
    public static final String STAT_getTrackCategory            = "SELECT name, descr, categoryid _id, hidden, minzoom FROM trackcategory WHERE categoryid = @1";
    public static final String STAT_deleteTrackCategory         = "DELETE FROM trackcategory WHERE categoryid = @1";
    public static final String STAT_getTrackListForCategory     = "SELECT name, descr, show, trackid ,categoryid, avgspeed, distance, time FROM tracks WHERE categoryid = @1 ORDER BY name";

    public static final String STAT_getRoute                    = "SELECT name, descr, show,categoryid,routeid FROM routes WHERE routeid = @1";
    public static final String STAT_getRouteByName              = "SELECT name, descr, show,categoryid,routeid FROM routes WHERE name = @1";
    public static final String STAT_setRouteChecked             = "UPDATE routes SET show = @1 WHERE routeid = @2";
    public static final String STAT_setRoutesChecked            = "UPDATE routes SET show = @1";
    public static final String STAT_deleteRoute_1               = "DELETE FROM routepoints WHERE pointsourceid = @1";
    public static final String STAT_deleteRoute_2               = "DELETE FROM routes WHERE routeid = @1";
    public static final String STAT_deleteAllRoutes1            = "DELETE FROM routepoints";
    public static final String STAT_deleteAllRoutes2            = "DELETE FROM routes";

    public static final String STAT_getRoutePoi                 = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM routepoints WHERE pointid = @1";
    public static final String STAT_getRoutePoints              = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM routepoints WHERE pointsourceid = @1 ORDER BY pointid";
    public static final String STAT_deleteRoutePoi              = "DELETE FROM routepoints WHERE pointid = @1";
    public static final String STAT_getRouteCategoryList        = "SELECT name,descr, categoryid _id FROM routecategory ORDER BY name";
    public static final String STAT_getRouteUserCategoryList    = "SELECT name,descr, categoryid _id FROM routecategory where categoryid > "
                                                                        + ROUTE_CATEGORY_DEFAULT_NAVIROUTE + " ORDER BY name";
    public static final String STAT_deleteRouteCategory         = "DELETE FROM routecategory WHERE categoryid = @1";
    public static final String STAT_getRouteCategory            = "SELECT name, descr, categoryid _id, hidden, minzoom FROM routecategory WHERE categoryid = @1";
    // public static final String STAT_getRoutesForCategory =
    // "SELECT name, descr, show, routeid _id,categoryid, CASE WHEN show=1 THEN "
    // + R.drawable.btn_check_buttonless_on
    // + " ELSE "
    // + R.drawable.btn_check_buttonless_off
    // + " END as image FROM routes WHERE categoryid = @1 ORDER BY name";
    public static final String STAT_getRoutesForCategory        = "SELECT name, descr, show, routeid _id,categoryid, show FROM routes WHERE categoryid = @1 ORDER BY name";
    public static final String STAT_getRoutesCountForCategory   = "SELECT count(categoryid) FROM routes WHERE categoryid = @1";
    // public static final String STAT_getRouteList =
    // "SELECT name, descr, routeid _id,categoryid, CASE WHEN show=1 THEN "
    // + R.drawable.btn_check_buttonless_on
    // + " ELSE "
    // + R.drawable.btn_check_buttonless_off
    // + " END as image FROM routes  ORDER BY categoryid,name";
    public static final String STAT_getRouteList                = "SELECT name, descr, routeid _id,categoryid, show FROM routes  ORDER BY categoryid,name";

    // public static final String STAT_getRoutesChecked =
    // "SELECT name, descr, routeid _id,categoryid, CASE WHEN show=1 THEN "
    // + R.drawable.btn_check_buttonless_on
    // + " ELSE "
    // + R.drawable.btn_check_buttonless_off
    // + " END as image FROM routes  WHERE show = 1 ORDER BY categoryid,name";
    public static final String STAT_getRoutesChecked            = "SELECT name, descr, routeid _id,categoryid, show FROM routes  WHERE show = 1 ORDER BY categoryid,name";

    public static final String STAT_getProximityTargetList      = "SELECT pointid _id, name ,descr ,lat, lon ,routeid, routeorder, distance, enter FROM proximitytarget";
    public static final String STAT_getProximityTarget          = "SELECT pointid _id, name ,descr ,lat, lon ,routeid, routeorder, distance, enter FROM proximitytarget WHERE _id = @1";
    public static final String STAT_deleteProximityTarget       = "DELETE FROM proximitytarget WHERE pointid = @1";
    public static final String STAT_DeleteAllProximityTarget    = "DELETE FROM proximitytarget";

    // + " END as image FROM routes GROUP BY categoryid ORDER BY name";
    public static final String STAT_getRouteChecked             = "SELECT name, descr, show, routeid,categoryid FROM routes WHERE show = 1 LIMIT 1";
    public static final String SQL_CREATE_points                = "CREATE TABLE 'points' (pointid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,descr VARCHAR,lat FLOAT DEFAULT '0',lon FLOAT DEFAULT '0',alt FLOAT DEFAULT '0',hidden INTEGER DEFAULT '0',categoryid INTEGER,pointsourceid INTEGER,iconid INTEGER DEFAULT NULL);";
    public static final String SQL_CREATE_category              = "CREATE TABLE 'category' (categoryid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, hidden INTEGER DEFAULT '0', iconid INTEGER DEFAULT NULL, minzoom INTEGER DEFAULT '14');";
    public static final String SQL_CREATE_pointsource           = "CREATE TABLE IF NOT EXISTS 'pointsource' (pointsourceid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR);";
    public static final String SQL_CREATE_tracks                = "CREATE TABLE IF NOT EXISTS 'tracks' (trackid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, date DATETIME, show INTEGER,categoryid INTEGER default '0' ,avgspeed FLOAT default '0', distance FLOAT DEFAULT '0',time LONG DEFAULT '0' );";
    public static final String SQL_CREATE_trackpoints           = "CREATE TABLE IF NOT EXISTS 'trackpoints' (trackid INTEGER NOT NULL, id INTEGER NOT NULL PRIMARY KEY UNIQUE, lat FLOAT, lon FLOAT, alt FLOAT, speed FLOAT, date DATETIME);";
    public static final String SQL_CREATE_routepoints           = "CREATE TABLE 'routepoints' (pointid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,descr VARCHAR,lat FLOAT DEFAULT '0',lon FLOAT DEFAULT '0',alt FLOAT DEFAULT '0',hidden INTEGER DEFAULT '0',categoryid INTEGER,pointsourceid INTEGER,iconid INTEGER DEFAULT NULL);";
    public static final String SQL_CREATE_routes                = "CREATE TABLE IF NOT EXISTS 'routes' (routeid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, date DATETIME, show INTEGER,categoryid INTEGER);";

    public static final String SQL_CREATE_routecategory         = "CREATE TABLE IF NOT EXISTS 'routecategory' (categoryid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR,descr VARCHAR, hidden INTEGER DEFAULT '0', minzoom INTEGER DEFAULT '14');";
    public static final String SQL_CREATE_trackcategory         = "CREATE TABLE IF NOT EXISTS 'trackcategory' (categoryid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR,descr VARCHAR, hidden INTEGER DEFAULT '0', minzoom INTEGER DEFAULT '14');";
    public static final String SQL_CREATE_proximitytarget       = "CREATE TABLE IF NOT EXISTS 'proximitytarget' (pointid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,descr VARCHAR,lat FLOAT DEFAULT '0',lon FLOAT DEFAULT '0',routeid INTEGER DEFAULT '-1',routeorder INTEGER DEFAULT '-1',distance INTEGER DEFAULT '10',enter INTEGER DEFAULT '1');";

    public static final String SQL_CREATE_INDEX_POI_NAME        = "CREATE INDEX poi_name_idx ON points(name);";
    public static final String SQL_CREATE_INDEX_ROUTE_NAME      = "CREATE INDEX route_name_idx ON routes(name);";
    public static final String SQL_CREATE_INDEX_TRACK_NAME      = "CREATE INDEX track_name_idx ON tracks(name);";

    public static final String SQL_CREATE_LOCATION_CACHE        = "CREATE TABLE IF NOT EXISTS 'locationcache' (_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude REAL, longitude REAL, altitude REAL, accuracy REAL, time INTEGER, provider TEXT);";

    public static final String SQL_CREATE_INDEX_TRACKPOINT_TID  = "CREATE INDEX trackpoints_tid_idx ON trackpoints(trackid);";

    public static final String SQL_ADD_category1                = "INSERT INTO 'category' (categoryid, name, hidden, iconid,minzoom) VALUES ("
                                                                        + POI_CATEGORY_DEFAULT + ", 'My POIs', 0, " + R.drawable.poiblue + ",10);";

    public static final String SQL_ADD_category2                = "INSERT INTO 'category' (categoryid, name, hidden, iconid,minzoom) VALUES ("
                                                                        + POI_CATEGORY_WIKI + ", 'Wiki', 0, " + R.drawable.poi_attraction + ",10);";

    public static final String SQL_ADD_category3                = "INSERT INTO 'category' (categoryid, name, hidden, iconid,minzoom) VALUES ("
                                                                        + POI_CATEGORY_TOPO + ", 'Topo', 0, " + R.drawable.poiyellow + ",10);";

    public static final String SQL_ADD_category4                = "INSERT INTO 'category' (categoryid, name, hidden, iconid,minzoom) VALUES ("
                                                                        + POI_CATEGORY_TARGET + ", 'Targets', 0, " + R.drawable.poi_finish + ",10);";

    public static final String SQL_ADD_routecategory1           = "INSERT INTO 'routecategory' (categoryid, name, hidden) VALUES ("
                                                                        + ROUTE_CATEGORY_DEFAULT_ROUTES + ", 'My Routes', 0 );";
    public static final String SQL_ADD_routecategory2           = "INSERT INTO 'routecategory' (categoryid, name, hidden) VALUES ("
                                                                        + ROUTE_CATEGORY_DEFAULT_HIKES + ", 'My Hikes', 0 );";
    public static final String SQL_ADD_routecategory3           = "INSERT INTO 'routecategory' (categoryid, name, hidden) VALUES ("
                                                                        + ROUTE_CATEGORY_DEFAULT_NAVIROUTE + ", 'My NaviRoute', 0 );";

    public static final String SQL_ADD_trackcategory1           = "INSERT INTO 'trackcategory' (categoryid, name, hidden) VALUES (0, 'recorded Tracks', 0 );";
    public static final String SQL_ADD_trackcategory2           = "INSERT INTO 'trackcategory' (categoryid, name, hidden) VALUES (1, 'imported Tracks', 0 );";

    public static final String SQL_UPDATE_3_1                   = "ALTER TABLE 'routecategory' add descr VARCHAR;";
    public static final String SQL_UPDATE_3_2                   = "ALTER TABLE 'tracks' add categoryid INTEGER DEFAULT 0;";
    public static final String SQL_UPDATE_3_3_1                 = "ALTER TABLE 'tracks' add avgspeed FLOAT DEFAULT 0;";
    public static final String SQL_UPDATE_3_3_2                 = "ALTER TABLE 'tracks' add distance FLOAT DEFAULT 0;";
    public static final String SQL_UPDATE_3_3_3                 = "ALTER TABLE 'tracks' add time LONG DEFAULT 0;";

    public static final String SQL_UPDATE_4_1                   = "ALTER TABLE 'category' add descr VARCHAR;";

    public static final String SQL_UPDATE_4_2                   = "UPDATE points SET categoryid=" + POI_CATEGORY_DEFAULT + " where categoryid = 0;";

    // for backpoints
    public static final String SQL_GET_LAST_POINTS_FROM_CACHE   = "SELECT * FROM locationcache ORDER BY _id DESC LIMIT 10";

}
