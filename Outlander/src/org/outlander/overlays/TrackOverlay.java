package org.outlander.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.outlander.R;
import org.outlander.model.Track;
import org.outlander.model.TrackPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Message;
import android.preference.PreferenceManager;

public class TrackOverlay extends OpenStreetMapViewOverlay implements
        RefreshableOverlay {
    private final Paint             mPaint;
    private int                     mLastZoom;
    private Path                    mPath;
    private Track                   mTrack;
    private final Point             mBaseCoords;
    private final GeoPoint          mBaseLocation;

    private final TrackThread       mThread;
    private boolean                 mThreadRunning  = false;
    private OpenStreetMapView       mOsmv;
    private boolean                 mStopDraw       = false;
    private final SharedPreferences sharedPreferences;

    protected ExecutorService       mThreadExecutor = Executors
                                                            .newSingleThreadExecutor();

    public final static int         TRACK_MAPPED    = 1234;
    public final static String      TRACK_REFRESH   = "TRACK_REFRESH";

    protected void messageReceived(Context context, Intent intent) {
        if (intent.getAction().equals(TRACK_REFRESH)) {
            refreshTrack();
        }
    }

    private void recalcPath() {
        if ((mTrack != null) && (mOsmv != null)) {
            final OpenStreetMapViewProjection pj = mOsmv.getProjection();
            mPath = pj.toPixelsTrackPoints(mTrack.getPoints(), mBaseCoords,
                    mBaseLocation, null);

            Ut.d("Track mapped");
        }
    }

    public TrackOverlay(final Context context) {
        mTrack = null;

        mBaseCoords = new Point();
        mBaseLocation = new GeoPoint(0, 0);
        mLastZoom = -1;
        mThread = new TrackThread();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        // active & inactive (at the moment)
        mPaint.setColor(sharedPreferences.getInt("color_track", context
                .getResources().getColor(R.color.track)));

        refreshTrack();
    }

    public void setStopDraw(final boolean stopdraw) {
        mStopDraw = stopdraw;
    }

    public void refreshTrack() {
        mTrack = null;
        mPath = null;
        mThreadRunning = true;
    }

    public Track getTrack() {
        return mTrack;
    }

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {
        if (mOsmv == null) {
            mOsmv = osmv;
        }
        if (mThreadRunning) {
            mThreadExecutor.execute(mThread);
            return;
        }
        if (mStopDraw) {
            return;
        }

        if (mTrack == null) {
            return;
        }

        if (((mLastZoom != osmv.getZoomLevel())) && (mPath != null)) {
            mLastZoom = osmv.getZoomLevel();
            recalcPath();
        }

        if (mPath == null) {
            return;
        }

        Ut.d("Draw track");
        final OpenStreetMapViewProjection pj = osmv.getProjection();
        final Point screenCoords = new Point();

        pj.toPixels(mBaseLocation, screenCoords);

        // final long startMs = System.currentTimeMillis();

        final boolean translateCoords = ((screenCoords.x != mBaseCoords.x) && (screenCoords.y != mBaseCoords.y));

        if (translateCoords) {
            c.save();
            c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y
                    - mBaseCoords.y);
        }
        // draw the trackpath
        c.drawPath(mPath, mPaint);

        if (translateCoords) {
            c.restore();
        }
    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
    }

    public void clearTrack() {
        mTrack = null;
    }

    private class TrackThread implements Runnable {

        public void quickShrinkTrack(final int deleteEachN) {
            if (mTrack != null) {

                final List<TrackPoint> shortList = new ArrayList<TrackPoint>();

                int i = 0;

                for (final TrackPoint point : mTrack.getPoints()) {
                    if (i == deleteEachN) {
                        i = 0;
                    } else {
                        shortList.add(point);
                    }
                    i++;
                }

                mTrack.setPoints(shortList);
            }
        }

        @Override
        public void run() {
            Ut.d("run TrackThread");

            mPath = null;

            if (mTrack == null) {

                mTrack = CoreInfoHandler
                        .getInstance()
                        .getDBManager(
                                CoreInfoHandler.getInstance().getMainActivity())
                        .getTrackChecked();

                if (mTrack == null) {
                    Ut.d("no Track loaded");
                    mThreadRunning = false;
                    mStopDraw = true;
                    return;
                }
                Ut.d("Track loaded");
            }

            // optimization for display

            if (mTrack.getPoints().size() > 100) {
                final int percentShow = sharedPreferences.getInt(
                        "pref_trackoptimization", 30);

                // TODO: fix porting bug......
                // int targetSize = (mTrack.getPoints().size() / 100)
                // * percentShow;
                //
                // GeoMathUtil.shrinkTrack(mTrack.getPoints(), targetSize);
                // }

                quickShrinkTrack(mTrack.getPoints().size() / percentShow);

            }

            recalcPath();

            Message.obtain(mOsmv.getCallbackHandler(), TrackOverlay.// OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID)
                    TRACK_MAPPED).sendToTarget();

            mThreadRunning = false;
        }
    }

    @Override
    public void refresh() {
        refreshTrack();
    }

}
