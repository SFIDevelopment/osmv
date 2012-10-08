package org.outlander.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.outlander.R;
import org.outlander.constants.DBConstants;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Picture;
import android.graphics.Point;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.SparseArray;

public class RouteOverlay extends BasePointOverlay implements RefreshableOverlay {

    protected final Picture                mRoutePoint          = new Picture();
    int                                    mScale               = 1;
    int                                    mPointRadius         = 6;

    public final static int[]              markerArray          = { R.drawable.nm_01, R.drawable.nm_02, R.drawable.nm_03, R.drawable.nm_04, R.drawable.nm_05,
            R.drawable.nm_06, R.drawable.nm_07, R.drawable.nm_08, R.drawable.nm_09, R.drawable.nm_10, R.drawable.nm_11, R.drawable.nm_12, R.drawable.nm_13,
            R.drawable.nm_14, R.drawable.nm_15, R.drawable.nm_16, R.drawable.nm_17, R.drawable.nm_18, R.drawable.nm_19, R.drawable.nm_21 };

    public final static int                COMMON_ROUTE_ICON_ID = R.drawable.route_waypoint_small;

    public final static int                ROUTE_MAPPED         = 1235;
    Semaphore                              pathSemaphore        = new Semaphore(1);
    private final Paint                    mPaint;
    private List<Path>                     mPathList;
    private List<Route>                    routeList;

    private final Point                    mBaseCoords;
    private final GeoPoint                 mBaseLocation;
    private final RouteThread              mThread;
    private boolean                        mThreadRunned        = false;
    private OpenStreetMapView              mOsmv;
    private boolean                        mStopDraw            = false;
    protected SparseArray<List<PointInfo>> mItemLists;
    protected ExecutorService              mThreadExecutor      = Executors.newSingleThreadExecutor();

    private int                            oldSelectedRouteId   = -1;
    private int                            mRouteIx             = -1;                                                                      // selected
                                                                                                                                            // route
                                                                                                                                            // tapped-on
                                                                                                                                            // or
                                                                                                                                            // selected
    private int                            turnRouteIndex       = -1;
    private PathEffect                     activeRoutePathEffect;

    private final int                      colorRoute;
    private final int                      colorRouteActive;
    private final int                      colorRouteTurn;

    public final static String             ROUTE_CMD            = "ROUTE_CMD";
    public final static String             ROUTE_REFRESH        = "ROUTE_REFRESH";
    public final static String             ROUTE_DEL            = "ROUTE_DEL";
    public final static String             ROUTE_FOCUS          = "ROUTE_FOCUS";
    public final static String             ROUTE_ID             = "ID";

    public RouteOverlay(final Context context, final OnItemTapListener<PoiPoint> onItemTapListener) {

        super(context, onItemTapListener);

        // this.sharedPreferences = sharedPreferences;

        mBaseCoords = new Point();
        mBaseLocation = new GeoPoint(0, 0);

        mLastZoom = -1;
        mThread = new RouteThread();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);

        mPaint.setStyle(Paint.Style.STROKE);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        colorRoute = sharedPreferences.getInt("color_route", context.getResources().getColor(R.color.route));

        colorRouteActive = sharedPreferences.getInt("color_route_current", context.getResources().getColor(R.color.currentroute));

        colorRouteTurn = sharedPreferences.getInt("pref_color_turnroute", context.getResources().getColor(R.color.currentroute));

        createRoutePointPicture();
        registerMessageReceiver(context, ROUTE_CMD);
    }

    public static Path makePathDash(final int size) {
        final Path p = new Path();
        p.moveTo(size, 0);
        p.lineTo(0, -size);
        p.lineTo(size * 2, -size);
        p.lineTo(3 * size, 0);
        p.lineTo(size * 2, size);
        p.lineTo(0, size);
        return p;
    }

    private int getRouteColor(final boolean active) {
        return (active ? colorRouteActive : colorRoute);
    }

    private int getTurnRouteColor() {
        return colorRouteTurn;
    }

    private PathEffect getRoutePathEffect(final boolean active) {
        PathEffect effect = null;
        if (active) {
            if (activeRoutePathEffect == null) {
                activeRoutePathEffect = new PathDashPathEffect(makePathDash(2), 8, 0, PathDashPathEffect.Style.ROTATE);
            }
            effect = activeRoutePathEffect;
        }
        return effect;
    }

    public Route getSelectedRoute() {
        return mRouteIx > -1 ? routeList.get(mRouteIx) : null;
    }

    public void setSelectRoute(final int ix) {
        mRouteIx = ix;
    }

    public void setSelectRouteById(final int id) {
        int ix = 0;
        mRouteIx = -1;
        for (final Route route : routeList) {
            if (route.getId() == id) {
                mRouteIx = ix;
                CoreInfoHandler.getInstance().setCurrentRouteId(id);
                break;
            }
            ix++;
        }
    }

    public void setStopDraw(final boolean stopdraw) {
        mStopDraw = stopdraw;
    }

    public void refreshRoute() {

        oldSelectedRouteId = getSelectedRoute().getId();

        setStopDraw(false);
        mThreadRunned = false;
        routeList = null;
        mPathList = null;
        // delete points
        clear();
    }

    @Override
    protected void zoomLevelChanged() {
        recalcPath();
    }

    @Override
    public void onDraw(final Canvas c, final OpenStreetMapView osmv) {
        if (mStopDraw) {
            return;
        }

        if (mOsmv == null) {
            mOsmv = osmv;
        }
        if (!mThreadRunned && (routeList == null)) {
            mPathList = null;

            mThreadRunned = true;
            mThreadExecutor.execute(mThread);
            return;
        }

        if (mPathList == null) {
            return;
        }

        Ut.d("Draw route");
        final OpenStreetMapViewProjection pj = osmv.getProjection();
        final Point screenCoords = new Point();

        pj.toPixels(mBaseLocation, screenCoords);

        final boolean translateCoords = ((screenCoords.x != mBaseCoords.x) && (screenCoords.y != mBaseCoords.y));

        if (translateCoords) {
            c.save();
            c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
        }

        // draw the individual routes

        try {
            pathSemaphore.acquire();
            // draw line
            int ix = 0;
            for (final Path path : mPathList) {
                if (path != null) {

                    int pathColor;

                    if (turnRouteIndex == ix) {
                        pathColor = getTurnRouteColor();
                    }
                    else {
                        pathColor = getRouteColor(mRouteIx == ix);
                    }

                    mPaint.setColor(pathColor);

                    mPaint.setPathEffect(getRoutePathEffect((mRouteIx == ix) || (mRouteIx == turnRouteIndex))); // draw
                    // selected
                    // route
                    // special!
                    c.drawPath(path, mPaint);
                }
                ix++;
            }
            pathSemaphore.release();
        }
        catch (final InterruptedException e) {
            Ut.e("Routepathsema interrupted");
            pathSemaphore.release();
        }

        if (translateCoords) {
            c.restore();
        }

        // draw icons
        super.onDraw(c, osmv);

    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {

    }

    @Override
    protected void internalPointTapped(final PointInfo pointInfo) {

        // rebuild pointlist
        buildPointList();

    }

    @Override
    protected void internalPointLongPressed(final PointInfo pointInfo) {

        // rebuild pointlist
        buildPointList();
    }

    private void buildPointList() {
        // mItemLists = new HashMap<Integer, List<PointInfo>>();

        mItemLists = new SparseArray<List<PointInfo>>();

        final List<PointInfo> points = new ArrayList<PointInfo>();

        int routeCntr = 0;
        for (final Route route : routeList) {

            final List<PointInfo> itemList = new ArrayList<PointInfo>(route.getGeoPoints().size());

            mItemLists.put(route.getId(), itemList);

            int iconIx = 0;
            for (final PoiPoint point : route.getPoints()) {
                if (point.getTitle().lastIndexOf(" | ") == -1) {
                    point.setTitle(route.getName() + " | " + point.getTitle());
                }
                final PointInfo pInfo = new PointInfo();
                pInfo.poiPoint = point;

                // different icons have different hotspots... by now
                pInfo.hotspotType = (((mRouteIx > -1) && (routeCntr == mRouteIx)) || (routeCntr == turnRouteIndex)) ? BasePointOverlay.HOTSPOT_TYPE_BOTTOMCENTER
                        : BasePointOverlay.HOTSPOT_TYPE_CENTER;

                // turnroutes have their icons !!
                if (routeCntr != turnRouteIndex) {
                    pInfo.poiPoint.setIconId(((mRouteIx > -1) && (routeCntr == mRouteIx)) ? RouteOverlay.markerArray[iconIx]
                            : RouteOverlay.COMMON_ROUTE_ICON_ID);
                    pInfo.shadowIconId = R.drawable.poi_shadow;
                }
                else {
                    pInfo.shadowIconId = -1;
                }

                // for common routes use circle picture as icon...
                // NEW
                pInfo.picture = ((mRouteIx > -1) && (routeCntr == mRouteIx)) ? null : mRoutePoint;

                pInfo.iconId = pInfo.poiPoint.getIconId();

                itemList.add(pInfo);
                points.add(pInfo);
                iconIx++;
            }
            routeCntr++;
        }

        setItemList(points);

    }

    public void clearRoutes() {
        clear();
        routeList = null;
    }

    public void removeRoute(final int routeId) {
        int ix = -1;
        int i = -1;
        for (final Route route : routeList) {
            if (route.getId() == routeId) {
                ix = i;
                break;
            }
            i++;
        }
        if (ix > -1) {
            mPathList.remove(ix);
            mItemLists.remove(routeId);
        }
        // remove Points from baselayer
        removePoiBySourceId(routeId);

    }

    @Override
    public int getMarkerAtPoint(final int eventX, final int eventY, final OpenStreetMapView mapView) {
        int index = -1;
        int i = 0;

        if (routeList != null) {
            int routeix = 0;
            for (final Route route : routeList) {
                final List<PointInfo> itemList = mItemLists.get(route.getId());
                if (itemList != null) {
                    for (final PointInfo pointInfo : itemList) {
                        if (pointInfo.curMarkerBounds != null) {
                            if (pointInfo.curMarkerBounds.contains(eventX, eventY)) {

                                mRouteIx = routeix;
                                CoreInfoHandler.getInstance().setCurrentRouteId(route.getId());
                                index = i;
                                break;
                            }
                        }
                        i++;
                    }
                }
                routeix++;
            }
        }
        return index;
    }

    private void recalcPath() {
        if (!mStopDraw) {
            try {
                int i = 0;
                final OpenStreetMapViewProjection pj = mOsmv.getProjection();
                final ArrayList<Path> newPathList = new ArrayList<Path>();
                for (final Route route : routeList) {
                    // drawable route
                    final Path path = pj.toPixelsTrackPoints(route.getGeoPoints(), mBaseCoords, mBaseLocation, null);
                    newPathList.add(path);

                    // remember index of turnroute !! //TODO: just one is
                    // supported by now
                    if (route.getCategory() == DBConstants.ROUTE_CATEGORY_DEFAULT_NAVIROUTE) {
                        turnRouteIndex = i;
                    }
                    i++;
                }

                pathSemaphore.acquire();

                mPathList = newPathList;

                pathSemaphore.release();
            }
            catch (final InterruptedException e) {
                return;
            }
        }
    }

    private class RouteThread implements Runnable {

        @Override
        public void run() {
            Ut.d("run RouteThread");

            routeList = null;

            if (routeList == null) {
                routeList = CoreInfoHandler.getInstance().getDBManager(CoreInfoHandler.getInstance().getMainActivity()).getRoutesChecked();

                if (oldSelectedRouteId > -1) { // try to keep selection index
                    setSelectRouteById(oldSelectedRouteId);
                    oldSelectedRouteId = -1;
                }

                if (routeList == null) {
                    mThreadRunned = false;
                    mStopDraw = true;
                    return;
                }
                Ut.d("Routes loaded");
            }

            recalcPath();

            buildPointList();

            Message.obtain(mOsmv.getCallbackHandler(), RouteOverlay.ROUTE_MAPPED).sendToTarget();

            mThreadRunned = false;
        }
    }

    /**
     * the circle used for points
     */
    private void createRoutePointPicture() {

        final Paint innerPaint = new Paint();
        innerPaint.setColor(mPaint.getColor());
        innerPaint.setAntiAlias(true);
        innerPaint.setStyle(Style.FILL);
        innerPaint.setAlpha(200);

        final Paint outerPaint = new Paint();
        outerPaint.setColor(mPaint.getColor());
        outerPaint.setAntiAlias(true);
        outerPaint.setStyle(Style.STROKE);
        outerPaint.setStrokeWidth(2.0f);
        outerPaint.setAlpha(200);

        final int picBorderWidthAndHeight = ((mPointRadius + 5) * 2);
        final int center = picBorderWidthAndHeight / 2;

        final Canvas canvas = mRoutePoint.beginRecording(picBorderWidthAndHeight, picBorderWidthAndHeight);

        canvas.drawCircle(center, center, mPointRadius * mScale, innerPaint);
        canvas.drawCircle(center, center, mPointRadius * mScale, outerPaint);

        innerPaint.setColor(Color.RED);
        outerPaint.setColor(Color.RED);

        canvas.drawCircle(center, center, (mPointRadius / 2) * mScale, innerPaint);
        canvas.drawCircle(center, center, (mPointRadius / 2) * mScale, outerPaint);

        mRoutePoint.endRecording();
    }

    @Override
    public void requestPointsForArea(final double deltaX, final double deltaY) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void centerChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh() {
        refreshRoute();
    }

    protected void messageReceived(Context context, Intent intent) {

        // String action = intent.getAction();
        String cmd = intent.getStringExtra(ROUTE_CMD);

        if (cmd.equals(ROUTE_REFRESH)) {
            refreshRoute();
        }
        else if (cmd.equals(ROUTE_DEL)) {
            int routeId = intent.getIntExtra(ROUTE_ID, -1);
            if (routeId > -1)
                removeRoute(routeId);
        }
        else if (cmd.equals(ROUTE_FOCUS)) {
            int routeId = intent.getIntExtra(ROUTE_ID, -1);
            if (routeId > -1)
                setSelectRouteById(routeId);
        }
    }

}
