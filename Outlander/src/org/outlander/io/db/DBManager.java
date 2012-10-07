package org.outlander.io.db;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.activities.PoiActivity;
import org.outlander.constants.DBConstants;
import org.outlander.model.PoiCategory;
import org.outlander.model.PoiPoint;
import org.outlander.model.ProximityTarget;
import org.outlander.model.Route;
import org.outlander.model.RouteCategory;
import org.outlander.model.Track;
import org.outlander.model.TrackPoint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class DBManager {
    protected final Context   mCtx;
    private final GeoDatabase mGeoDatabase;

    public DBManager(final Context ctx) {
        super();
        mCtx = ctx;
        mGeoDatabase = new GeoDatabase(ctx);
    }

    public GeoDatabase getGeoDatabase() {
        return mGeoDatabase;
    }

    public void freeDatabases() {
        mGeoDatabase.freeDatabases();
    }

    public void addPoi(final String title, final String descr,
            final GeoPoint point) {
        mGeoDatabase.addPoi(title, descr, point.getLatitude(),
                point.getLongitude(), DBConstants.ZERO, DBConstants.ZERO,
                DBConstants.ZERO, DBConstants.ZERO, R.drawable.poi);
    }

    public void updatePoi(final PoiPoint point) {
        if (point.getId() < 0) {
            mGeoDatabase.addPoi(point.getTitle(), point.getDescr(), point
                    .getGeoPoint().getLatitude(), point.getGeoPoint()
                    .getLongitude(), point.getAlt(), point.getCategoryId(),
                    point.getPointSourceId(),
                    point.isHidden() == true ? DBConstants.ONE
                            : DBConstants.ZERO, point.getIconId());
        } else {
            mGeoDatabase.updatePoi(point.getId(), point.getTitle(), point
                    .getDescr(), point.getGeoPoint().getLatitude(), point
                    .getGeoPoint().getLongitude(), point.getAlt(), point
                    .getCategoryId(), point.getPointSourceId(), point
                    .isHidden() == true ? DBConstants.ONE : DBConstants.ZERO,
                    point.getIconId());
        }
    }

    private List<PoiPoint> doCreatePoiListFromCursor(final Cursor c) {
        final ArrayList<PoiPoint> items = new ArrayList<PoiPoint>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    // 0 1 2 3 4 5 6 7 8
                    // "SELECT lat, lon,alt, name, descr, pointid _id,iconid, categoryid,hidden FROM points ORDER BY categoryid,name";
                    // final int id, final String title, final String descr,
                    // final GeoPoint geoPoint, final int iconid, final int
                    // categoryid,
                    // final double alt, final int sourceid, final int hidden

                    items.add(new PoiPoint(c.getInt(5), c.getString(3), c
                            .getString(4), new GeoPoint((int) (1E6 * c
                            .getDouble(0)), (int) (1E6 * c.getDouble(1))), c
                            .getInt(6), c.getInt(7), c.getInt(2), -1, c
                            .getInt(8)));
                } while (c.moveToNext());
            }
            c.close();
        }

        return items;
    }

    public List<PoiPoint> getPoiList() {
        return doCreatePoiListFromCursor(mGeoDatabase.getPoiListCursor());
    }

    public List<PoiPoint> getPoiListUser() {
        return doCreatePoiListFromCursor(mGeoDatabase.getPoiListUserCursor());
    }

    public List<PoiPoint> getPoiListForCategory(final int categoryid) {
        return doCreatePoiListFromCursor(mGeoDatabase
                .getPoiListForCategory(categoryid));
    }

    public List<PoiPoint> getPoiListForCategoryWiki() {
        return getPoiListForCategory(DBConstants.POI_CATEGORY_WIKI);
    }

    public List<PoiPoint> getPoiListForCategoryTopo() {
        return getPoiListForCategory(DBConstants.POI_CATEGORY_TOPO);
    }

    public List<PoiPoint> getPoiListForCategoryTargets() {
        return getPoiListForCategory(DBConstants.POI_CATEGORY_TARGET);
    }

    public List<PoiPoint> getPoiListNotHidden(final int zoom,
            final GeoPoint center, final double deltaX, final double deltaY) {
        return doCreatePoiListFromCursor(mGeoDatabase
                .getPoiListNotHiddenCursor(zoom,
                        center.getLongitude() - deltaX, center.getLongitude()
                                + deltaX, center.getLatitude() + deltaY,
                        center.getLatitude() - deltaY));
    }

    public void addPoiStartActivity(final Context ctx,
            final GeoPoint touchDownPoint) {
        ctx.startActivity((new Intent(ctx, PoiActivity.class)).putExtra(
                DBConstants.LAT, touchDownPoint.getLatitude()).putExtra(
                DBConstants.LON, touchDownPoint.getLongitude()));
    }

    public PoiPoint getPoiPoint(final int id) {
        PoiPoint point = null;
        final Cursor c = mGeoDatabase.getPoi(id);
        if (c != null) {
            if (c.moveToFirst()) {
                point = new PoiPoint(c.getInt(4), c.getString(2),
                        c.getString(3), new GeoPoint(
                                (int) (1E6 * c.getDouble(0)),
                                (int) (1E6 * c.getDouble(1))), c.getInt(9),
                        c.getInt(7), c.getInt(5), c.getInt(8), c.getInt(6));
            }
            c.close();
        }

        return point;
    }

    public PoiPoint getPoiPointByName(final String name) {
        PoiPoint point = null;
        final Cursor c = mGeoDatabase.getPoiByName(name);
        if (c != null) {
            if (c.moveToFirst()) {
                point = new PoiPoint(c.getInt(4), c.getString(2),
                        c.getString(3), new GeoPoint(
                                (int) (1E6 * c.getDouble(0)),
                                (int) (1E6 * c.getDouble(1))), c.getInt(9),
                        c.getInt(7), c.getInt(5), c.getInt(8), c.getInt(6));
            }
            c.close();
        }

        return point;
    }

    public void deletePoi(final int id) {
        mGeoDatabase.deletePoi(id);
    }

    public void deletePoisOfCategoryTopo() {
        mGeoDatabase.deletePoisOfCategory(DBConstants.POI_CATEGORY_TOPO);
    }

    public void deletePoisOfCategoryWiki() {
        mGeoDatabase.deletePoisOfCategory(DBConstants.POI_CATEGORY_WIKI);
    }

    public void deletePoisOfCategoryTarget() {
        mGeoDatabase.deletePoisOfCategory(DBConstants.POI_CATEGORY_TARGET);
    }

    public void deletePoiCategory(final int id) {
        mGeoDatabase.deletePoiCategory(id);
    }

    public PoiCategory getPoiCategory(final int id) {
        PoiCategory category = null;
        final Cursor c = mGeoDatabase.getPoiCategory(id);
        if (c != null) {
            if (c.moveToFirst()) {
                category = new PoiCategory(id, c.getString(0),
                        c.getInt(2) == DBConstants.ONE ? true : false,
                        c.getInt(3), c.getInt(4), c.getString(5));
            }
            c.close();
        }

        return category;
    }

    public List<PoiCategory> getPoiCategoryListFromCursor(final Cursor c) {
        final List<PoiCategory> catList = new ArrayList<PoiCategory>();
        // SELECT name, categoryid _id,iconid,minzoom descr FROM category ORDER
        // BY name"
        // public PoiCategory(final int id, final String title, final boolean
        // hidden,
        // final int iconid, final int minzoom, final String descr) {

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    final PoiCategory category = new PoiCategory(c.getInt(1),
                            c.getString(0), false, c.getInt(2), c.getInt(3),
                            c.getString(4));
                    catList.add(category);
                } while (c.moveToNext());
            }
            c.close();
        }
        return catList;

    }

    public List<PoiCategory> getPoiCategoryList() {
        return getPoiCategoryListFromCursor(mGeoDatabase
                .getPoiCategoryListCursor());
    }

    public List<PoiCategory> getPoiUserCategoryList() {
        return getPoiCategoryListFromCursor(mGeoDatabase
                .getPoiUserCategoryListCursor());
    }

    public void updatePoiCategory(final PoiCategory poiCategory) {
        if (poiCategory.getId() < DBConstants.ZERO) {
            mGeoDatabase.addPoiCategory(poiCategory.Title,
                    poiCategory.Hidden == true ? DBConstants.ONE
                            : DBConstants.ZERO, poiCategory.IconId,
                    poiCategory.Descr);
        } else {
            mGeoDatabase.updatePoiCategory(poiCategory.getId(),
                    poiCategory.Title,
                    poiCategory.Hidden == true ? DBConstants.ONE
                            : DBConstants.ZERO, poiCategory.IconId,
                    poiCategory.MinZoom, poiCategory.Descr);
        }
    }

    public void deleteAllPois() {
        mGeoDatabase.deleteAllPois();
    }

    public void beginTransaction() {
        mGeoDatabase.beginTransaction();
    }

    public void rollbackTransaction() {
        mGeoDatabase.rollbackTransaction();
    }

    public void commitTransaction() {
        mGeoDatabase.commitTransaction();
    }

    /**
     * 
     * @param track
     * @param dotransient
     *            with or without points
     */
    public void updateTrack(final Track track, final boolean dotransient) {
        int trackId;

        if (track.getId() < 0) {
            trackId = (int) mGeoDatabase.addTrack(track.Name, track.Descr,
                    track.Show ? DBConstants.ONE : DBConstants.ZERO,
                    track.AvgSpeed, track.Distance, track.Time);
        } else {
            trackId = track.getId();
            // delete Trackpoints and add new
            if (dotransient) {
                mGeoDatabase.deleteAllTrackPoints(trackId);
            }
            mGeoDatabase.updateTrack(trackId, track.Name, track.Descr,
                    track.Show ? DBConstants.ONE : DBConstants.ZERO,
                    track.AvgSpeed, track.Distance, track.Time);
        }
        if (dotransient) {
            for (final TrackPoint trackpoint : track.getPoints()) {
                mGeoDatabase.addTrackPoint(trackId, trackpoint.getLatitude(),
                        trackpoint.getLongitude(), trackpoint.alt,
                        trackpoint.speed, trackpoint.date);
            }
        }
    }

    public boolean haveTrackChecked() {
        boolean ret = false;
        final Cursor c = mGeoDatabase.getTrackChecked();
        if (c != null) {
            if (c.moveToFirst()) {
                ret = true;
            }
            c.close();
        }

        return ret;
    }

    public Track getTrackChecked() {
        Track track = null;
        final Cursor c = mGeoDatabase.getTrackChecked();
        if (c != null) {
            if (c.moveToFirst()) {
                track = new Track(c.getInt(3), c.getString(0), c.getString(1),
                        c.getInt(2) == DBConstants.ONE ? true : false);
            } else {
                c.close();
                return null;
            }
            c.close();

            if (track != null) {
                addTrackPointsToTrack(track);
            }
        }
        return track;
    }

    public List<Track> getTracksChecked() {
        List<Track> trackList = null;

        final Cursor c = mGeoDatabase.getTrackChecked();
        if (c != null) {
            if (c.moveToFirst()) {
                trackList = new ArrayList<Track>();
                do {
                    final Track track = new Track(c.getInt(3), c.getString(0),
                            c.getString(1),
                            c.getInt(2) == DBConstants.ONE ? true : false);
                    if (track != null) {
                        addTrackPointsToTrack(track);
                    }
                    trackList.add(track);
                } while (c.moveToNext());
            }
            c.close();
        }
        return trackList;
    }

    private void addTrackPointsToTrack(final Track track) {
        final Cursor c = mGeoDatabase.getTrackPoints(track.getId());
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    track.AddTrackPoint(); // track.trackpoints.size()
                    track.LastTrackPoint.setLatitude(c.getDouble(0));
                    track.LastTrackPoint.setLongitude(c.getDouble(1));
                    track.LastTrackPoint.alt = c.getDouble(2);
                    track.LastTrackPoint.speed = c.getDouble(3);
                    track.LastTrackPoint.date.setTime(c.getLong(4) * 1000); // System.currentTimeMillis()
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    public Track getTrack(final int id) {
        Track track = null;
        Cursor c = mGeoDatabase.getTrack(id);
        if (c != null) {
            if (c.moveToFirst()) {
                track = new Track(id, c.getString(0), c.getString(1),
                        c.getInt(2) == DBConstants.ONE ? true : false);
            }
            c.close();
            c = null;

            if (track != null) {
                addTrackPointsToTrack(track);
            }
        }
        return track;
    }

    public Track getTrackByName(final String name) {
        Track track = null;
        Cursor c = mGeoDatabase.getTrackByName(name);
        if (c != null) {
            if (c.moveToFirst()) {
                track = new Track(c.getInt(3), c.getString(0), c.getString(1),
                        c.getInt(2) == DBConstants.ONE ? true : false);
            }
            c.close();
            c = null;

            if (track != null) {
                addTrackPointsToTrack(track);
            }
        }
        return track;
    }

    public void setTrackChecked(final int id, final boolean visible) {
        mGeoDatabase.setTrackChecked(id, visible);
    }

    public void setTracksChecked(final boolean visible) {
        mGeoDatabase.setTracksChecked(visible);
    }

    public void deleteTrack(final int id) {
        mGeoDatabase.deleteTrack(id);
    }

    public void deleteAllTracks() {
        mGeoDatabase.deleteAllTracks();
    }

    public RouteCategory getTrackCategory(final int id) {
        RouteCategory category = null;
        final Cursor c = mGeoDatabase.getTrackCategory(id);
        if (c != null) {
            if (c.moveToFirst()) {
                category = new RouteCategory(id, c.getString(0),
                        c.getString(1), c.getInt(2) == DBConstants.ONE ? true
                                : false, c.getInt(3), c.getInt(4));
            }
            c.close();
        }
        return category;
    }

    public List<RouteCategory> getRouteCategories() {
        final List<RouteCategory> list = new ArrayList<RouteCategory>();

        final Cursor c = mGeoDatabase.getRouteCategoryListCursor();

        if (c != null) {
            if (c.moveToFirst()) {

                do {
                    // 0 1 2
                    // "SELECT name,descr, categoryid _id FROM routecategory ORDER BY name";
                    final RouteCategory routeCategory = new RouteCategory(
                            c.getInt(2), c.getString(0), c.getString(1), false,
                            -1, -1);

                    list.add(routeCategory);

                } while (c.moveToNext());
            }
            c.close();
        }

        return list;
    }

    /**
     * save or update route and points
     * 
     * @param route
     * @param dotransient
     */
    public void updateRoute(final Route route, final boolean dotransient) {

        long routeId;

        if (route.getId() < 0) {
            routeId = mGeoDatabase.addRoute(route.getName(), route.getDescr(),
                    route.isShow() ? DBConstants.ONE : DBConstants.ZERO,
                    route.getCategory());
        } else {
            routeId = route.getId();

            // remove old points and add new
            if (dotransient) {
                mGeoDatabase.deleteAllRoutePoi((int) routeId);
            }
            mGeoDatabase.updateRoute((int) routeId, route.getName(),
                    route.getDescr(), route.isShow() ? 1 : 0,
                    route.getCategory());
        }
        if (dotransient) {
            for (final PoiPoint point : route.getPoints()) {
                mGeoDatabase.addRoutePoi(point.getTitle(), point.getDescr(),
                        point.getGeoPoint().getLatitude(), point.getGeoPoint()
                                .getLongitude(), point.getAlt(), 0,
                        (int) routeId, DBConstants.ONE, 0);
            }
        }
    }

    public Route getRoute(final int id) {
        Route route = null;
        Cursor c = mGeoDatabase.getRoute(id);
        if (c != null) {
            if (c.moveToFirst()) { // name, descr, show,categoryid ,routeid

                route = new Route(id, c.getString(0), c.getString(1),
                        c.getInt(2) == DBConstants.ONE ? true : false,
                        c.getInt(3));
                //
                //
                // route = new Route(id, c.getString(0), c.getString(1),
                // c.getInt(2) == ONE ? true : false, c.getInt(4));
            }
            c.close();
            c = null;
            if (route != null) {
                addPointsToRoute(route);
            }
        }
        return route;
    }

    public Route getRouteByName(final String name) {
        Route route = null;
        Cursor c = mGeoDatabase.getRouteByName(name);
        if (c != null) {
            if (c.moveToFirst()) { // name, descr, show,categoryid ,routeid

                route = new Route(c.getInt(4), c.getString(0), c.getString(1),
                        c.getInt(2) == DBConstants.ONE ? true : false,
                        c.getInt(3));
            }
            c.close();
            c = null;
            if (route != null) {
                addPointsToRoute(route);
            }
        }
        return route;
    }

    private void addPointsToRoute(final Route route) {
        final Cursor c = mGeoDatabase.getRoutePoints(route.getId());
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    // lat, lon, name, descr, pointid, alt, hidden, categoryid,
                    // pointsourceid, iconid
                    // 0 1 2 3 4 5 6 7 8 9
                    // String mTitle, String mDescr, GeoPoint mGeoPoint, int
                    // iconid, int categoryid, double alt, int hidden)
                    route.addRoutePoint(
                            c.getString(2),
                            c.getString(3),
                            new GeoPoint((int) (c.getDouble(0) * 1E6), (int) (c
                                    .getDouble(1) * 1E6)), c.getInt(9), 0, c
                                    .getDouble(5), 0);
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    public Route getRouteChecked() {
        Route route = null;
        final Cursor c = mGeoDatabase.getRouteChecked();
        if (c != null) {
            if (c.moveToFirst()) {
                route = new Route(c.getInt(2), c.getString(0), c.getString(1),
                        c.getInt(4) == DBConstants.ONE ? true : false,
                        c.getInt(3));
                //
                //
                // route = new Route(c.getInt(3), c.getString(0),
                // c.getString(1),
                // c.getInt(2) == ONE ? true : false, c.getInt(4));
            } else {
                c.close();
                return null;
            }
            c.close();
            if (route != null) {
                addPointsToRoute(route);
            }

        }
        return route;
    }

    /**
     * 
     * @return at least empty list to keep overlay satisfied
     * 
     */
    public List<Route> getRoutesChecked() {
        final List<Route> routeList = new ArrayList<Route>();

        final Cursor c = mGeoDatabase.getRoutesChecked();
        if (c != null) {
            if (c.moveToFirst()) {

                do {
                    final Route route = new Route(c.getInt(2), c.getString(0),
                            c.getString(1),
                            c.getInt(4) == DBConstants.ONE ? true : false,
                            c.getInt(3));
                    if (route.isShow()) {
                        addPointsToRoute(route);
                        routeList.add(route);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        return routeList;
    }

    public List<Route> getAllRoutes() {
        final List<Route> routeList = new ArrayList<Route>();

        final Cursor c = mGeoDatabase.getRouteListCursor();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    final Route route = new Route(c.getInt(2), c.getString(0),
                            c.getString(1),
                            c.getInt(4) == DBConstants.ONE ? true : false,
                            c.getInt(3));

                    addPointsToRoute(route);
                    routeList.add(route);
                } while (c.moveToNext());
            }
            c.close();
        }
        return routeList;
    }

    public void setRouteChecked(final int id, final boolean visible) {
        mGeoDatabase.setRouteChecked(id, visible);
    }

    public void setPoiChecked(final int id, final boolean visible) {
        mGeoDatabase.setPoiChecked(id, visible);
    }

    public void setPoisChecked(final boolean visible) {
        mGeoDatabase.setPoisChecked(visible);
    }

    public void setRoutesChecked(final boolean visible) {
        mGeoDatabase.setRoutesChecked(visible);
    }

    public void deleteRoute(final int id) {
        mGeoDatabase.deleteRoute(id);
    }

    public void deleteAllRoutes() {
        mGeoDatabase.deleteAllRoutes();
    }

    public void deleteRouteCategory(final int id) {
        mGeoDatabase.deleteRouteCategory(id);
    }

    public RouteCategory getRouteCategory(final int id) {
        RouteCategory category = null;
        final Cursor c = mGeoDatabase.getRouteCategory(id);
        if (c != null) {
            if (c.moveToFirst()) {

                category = new RouteCategory(id, c.getString(0),
                        c.getString(1), c.getInt(2) == DBConstants.ONE ? true
                                : false, c.getInt(3), c.getInt(4));
            }
            c.close();
        }
        return category;
    }

    public void updateRouteCategory(final RouteCategory routeCategory) {
        if (routeCategory.getId() < DBConstants.ZERO) {
            mGeoDatabase.addRouteCategory(routeCategory.Title,
                    routeCategory.Description,
                    routeCategory.Hidden == true ? DBConstants.ONE
                            : DBConstants.ZERO, routeCategory.IconId,
                    routeCategory.MinZoom);
        } else {
            mGeoDatabase.updateRouteCategory(routeCategory.getId(),
                    routeCategory.Title, routeCategory.Description,
                    routeCategory.Hidden == true ? DBConstants.ONE
                            : DBConstants.ZERO, routeCategory.IconId,
                    routeCategory.MinZoom);
        }
    }

    // proximity Target

    public void addProximityTarget(final String title, final String descr,
            final GeoPoint point) {
        mGeoDatabase.addPoi(title, descr, point.getLatitude(),
                point.getLongitude(), DBConstants.ZERO, DBConstants.ZERO,
                DBConstants.ZERO, DBConstants.ZERO, R.drawable.poi);
    }

    public void updateProximityTarget(final ProximityTarget point) {
        if (point.getId() < 0) {
            mGeoDatabase.addProximityTarget(point.Title, point.Descr,
                    point.GeoPoint.getLatitude(),
                    point.GeoPoint.getLongitude(), point.RouteId,
                    point.RouteOrder, point.Distance,
                    point.enterOrLeave == true ? DBConstants.ONE
                            : DBConstants.ZERO);
        } else {
            mGeoDatabase.updateProximityTarget(point.getId(), point.Title,
                    point.Descr, point.GeoPoint.getLatitude(), point.GeoPoint
                            .getLongitude(), point.RouteId, point.RouteOrder,
                    point.Distance,
                    point.enterOrLeave == true ? DBConstants.ONE
                            : DBConstants.ZERO);
        }
    }

    private List<ProximityTarget> doCreateProximityTargetListFromCursor(
            final Cursor c) {
        final ArrayList<ProximityTarget> items = new ArrayList<ProximityTarget>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    items.add(new ProximityTarget(c.getInt(0), c.getString(1),
                            c.getString(2), new GeoPoint((int) (1E6 * c
                                    .getDouble(3)),
                                    (int) (1E6 * c.getDouble(4))), c.getInt(5),
                            c.getInt(6), c.getInt(7), c.getInt(8)));
                } while (c.moveToNext());
            }
            c.close();
        }

        return items;
    }

    public List<ProximityTarget> getProximityTargetList() {
        return doCreateProximityTargetListFromCursor(mGeoDatabase
                .getProximityTargetListCursor());
    }

    public void deleteProximityTarget(final int id) {
        mGeoDatabase.deleteProximityTarget(id);
    }

}
