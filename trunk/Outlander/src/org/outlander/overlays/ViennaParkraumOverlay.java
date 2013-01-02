package org.outlander.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.outlander.R;
import org.outlander.io.JSON.ParseWKPZ;
import org.outlander.io.db.DBManager;
import org.outlander.model.ViennaKurzParkZone;
import org.outlander.utils.Ut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;

public class ViennaParkraumOverlay extends OpenStreetMapViewOverlay {

    private final Paint              mAreaBorder, mAreaFill;
    private int                      mLastZoom;
    private List<Path>               mPaths          = null;

    private final Point              mBaseCoords;
    private final GeoPoint           mBaseLocation;

    private final JSonParseThread    mThread;
    private boolean                  mThreadRunning  = false;
    private OpenStreetMapView        mOsmv;
    private Handler                  mMainMapActivityCallbackHandler;
    private boolean                  mStopDraw       = true;
    private List<ViennaKurzParkZone> kurzparkzonen   = null;
    private List<Point>              screenpoints;
    private boolean                  initialized     = false;
    protected ExecutorService        mThreadExecutor = Executors.newSingleThreadExecutor();

    private class JSonParseThread implements Runnable {

        @Override
        public void run() {
            mThreadRunning = true;
            Ut.d("try parsing Vienna Parkraum JSON");

            final String filename = "/mnt/sdcard/TschekkoMaps/import/wien-kpz.json";

            kurzparkzonen = ParseWKPZ.parseJSONData(filename);

            Ut.d("Kurzparkzonen mapped");

            Message.obtain(mMainMapActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID).sendToTarget();

            if (kurzparkzonen != null) {
                mStopDraw = false;
            }
            initialized = true;
            mThreadRunning = false;
        }
    }

    private void calcScreenPaths() {
        final OpenStreetMapViewProjection pj = mOsmv.getProjection();

        mPaths = new ArrayList<Path>(kurzparkzonen.size());
        screenpoints = new ArrayList<Point>();

        for (final ViennaKurzParkZone vkpz : kurzparkzonen) {
            mPaths.add(pj.toPixelsTrackPoints(vkpz.getPolygon(), mBaseCoords, mBaseLocation, screenpoints));
        }
    }

    public ViennaParkraumOverlay(final Context context, final DBManager poiManager) {

        mBaseCoords = new Point();
        mBaseLocation = new GeoPoint(0, 0);
        mLastZoom = -1;
        mThread = new JSonParseThread();

        mAreaFill = new Paint();
        mAreaFill.setAntiAlias(true);
        mAreaFill.setStrokeWidth(2);
        mAreaFill.setStyle(Paint.Style.FILL);
        mAreaFill.setColor( context.getResources().getColor( R.color.areafill));
        // mAreaFill.setShadowLayer(5.5f, 6.0f, 6.0f, Color.BLACK);

        mAreaBorder = new Paint(mAreaFill);
        mAreaBorder.setStyle(Paint.Style.STROKE);
        mAreaBorder.setColor(context.getResources().getColor( R.color.areaborder));

    }

    public void setStopDraw(final boolean stopdraw) {
        mStopDraw = stopdraw;
    }

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {

        if (!initialized) {
            if (!mThreadRunning) {
                mThreadExecutor.execute(mThread);

                mMainMapActivityCallbackHandler = osmv.getHandler();
                mOsmv = osmv;
            }
        }

        if ((initialized) && (kurzparkzonen != null) && (mPaths == null)) {
            calcScreenPaths();
        }

        if ((mStopDraw) || (mPaths == null) || (mPaths.size() < 1)) {
            return;
        }

        if (mLastZoom != osmv.getZoomLevel()) {
            mPaths = null;
            if (kurzparkzonen != null) {
                calcScreenPaths();
            }
        }
        mLastZoom = osmv.getZoomLevel();

        Ut.d("Draw VKPZ");
        final OpenStreetMapViewProjection pj = osmv.getProjection();
        final Point screenCoords = new Point();

        pj.toPixels(mBaseLocation, screenCoords);

        final boolean translateCoords = ((screenCoords.x != mBaseCoords.x) && (screenCoords.y != mBaseCoords.y));

        if (translateCoords) {
            c.save();
            c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
        }

        for (final Path path : mPaths) {
            c.drawPath(path, mAreaFill);
            c.drawPath(path, mAreaBorder);
        }

        // for (Point point : screenpoints) {
        // c.drawCircle(point.x, point.y, 30, mAreaBorder);
        // }

        if (translateCoords) {
            c.restore();
        }
    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
    }

}
