package org.outlander.overlays;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.outlander.R;
import org.outlander.model.Track;
import org.outlander.utils.Ut;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Message;
import android.preference.PreferenceManager;

public class CurrentTrackOverlay extends OpenStreetMapViewOverlay {
    private final Paint                 mPaint;
    private OpenStreetMapViewProjection mBasePj;
    private int                         mLastZoom;
    private Path                        mPath;
    private Track                       mTrack;
    private final Point                 mBaseCoords;
    private final GeoPoint              mBaseLocation;
    // private PoiManager mPoiManager;
    private final TrackThread           mThread;
    private boolean                     mThreadRunned   = false;
    protected ExecutorService           mThreadExecutor = Executors
                                                                .newSingleThreadExecutor();
    private final OpenStreetMapView     mOsmv;
    // private Handler mMainMapActivityCallbackHandler;
    private final Context               mContext;

    // IRemoteService mService = null;
    private boolean                     mIsBound;

    public CurrentTrackOverlay(final Context context,
            final OpenStreetMapView osmv) {
        mTrack = new Track();
        mContext = context;
        // mPoiManager = poiManager;
        mBaseCoords = new Point();
        mBaseLocation = new GeoPoint(0, 0);
        mLastZoom = -1;
        mBasePj = null;

        mOsmv = osmv;
        mThread = new TrackThread();
        mThread.setName("Current Track thread");

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        mPaint.setColor(sharedPreferences.getInt("color_track_current", context
                .getResources().getColor(R.color.currenttrack)));

        // mContext.bindService(new Intent(IRemoteService.class.getName()),
        // mConnection, 0 /* Context.BIND_AUTO_CREATE */);
        mIsBound = true;
    }

    public void unbindService() {
        // mContext.unbindService(mConnection);
    }

    private class TrackThread extends Thread {

        @Override
        public void run() {
            Ut.d("run CurrentTrackThread");

            mPath = null;
            if (mTrack == null) {
                mTrack = new Track();
            } else {
                mTrack.getPoints().clear();
            }

            final File folder = Ut.getTschekkoMapsMainDir(mContext, "data");
            if (folder.canRead()) {
                SQLiteDatabase db = null;
                try {
                    db = new org.outlander.io.db.TrackWriterDatabaseHelper(
                            mContext, folder.getAbsolutePath()
                                    + "/writtentrack.db").getReadableDatabase();
                } catch (final Exception e) {
                    db = null;
                }

                if (db != null) {
                    final Cursor c = db.rawQuery(
                            "SELECT lat, lon FROM trackpoints ORDER BY id",
                            null);

                    if (c != null) {
                        if (c.moveToFirst()) {
                            do {
                                mTrack.AddTrackPoint();
                                mTrack.LastTrackPoint.setLatitude(c
                                        .getDouble(0));
                                mTrack.LastTrackPoint.setLongitude(c
                                        .getDouble(1));
                            } while (c.moveToNext());
                        }
                        c.close();
                    }
                    db.close();
                }
            }

            mBasePj = mOsmv.getProjection();
            mPath = mBasePj.toPixelsTrackPoints(mTrack.getPoints(),
                    mBaseCoords, mBaseLocation, null);

            Ut.d("Track mapped");

            Message.obtain(
                    mOsmv.getHandler(),
                    OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID)
                    .sendToTarget();

            mThreadRunned = false;
        }
    }

    // private final ServiceConnection mConnection = new ServiceConnection() {
    // public void onServiceConnected(
    // final ComponentName className,
    // final IBinder service) {
    // mService = IRemoteService.Stub
    // .asInterface(service);
    //
    // try {
    // mService.registerCallback(mCallback);
    // } catch (final RemoteException e) {
    // }
    // }
    //
    // public void onServiceDisconnected(
    // final ComponentName className) {
    // mService = null;
    // }
    // };

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {
        if (!mThreadRunned
                && ((mTrack == null) || (mLastZoom != osmv.getZoomLevel()))) {
            // mPath = null;
            mLastZoom = osmv.getZoomLevel();
            // mMainMapActivityCallbackHandler = osmv.getHandler();
            Ut.d("mThreadExecutor.execute " + mThread.isAlive());
            mThreadRunned = true;
            mThreadExecutor.execute(mThread);
            return;
        }

        if (mPath == null) {
            return;
        }

        Ut.d("Draw track");
        final OpenStreetMapViewProjection pj = osmv.getProjection();
        final Point screenCoords = new Point();

        pj.toPixels(mBaseLocation, screenCoords);

        // final long startMs = System.currentTimeMillis();

        if ((screenCoords.x != mBaseCoords.x)
                && (screenCoords.y != mBaseCoords.y)) {
            c.save();
            c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y
                    - mBaseCoords.y);
            c.drawPath(mPath, mPaint);
            c.restore();
        } else {
            c.drawPath(mPath, mPaint);
        }
    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
        // TODO Auto-generated method stub

    }

    public void onResume() {

    }

    public void onPause() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            // if (mService != null) {
            // try {
            // mService.unregisterCallback(mCallback);
            // } catch (final RemoteException e) {
            // // There is nothing special we need to do if the service
            // // has crashed.
            // }
            // }

            // Detach our existing connection.
            // mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }

    // private final ITrackWriterCallback mCallback = new
    // ITrackWriterCallback.Stub() {
    // public void newPointWrited(
    // final double lat,
    // final double lon) {
    // Ut.dd("newPointWritten "
    // + lat
    // + " "
    // + lon
    // + " mThreadRunned="
    // + mThreadRunned);
    //
    // if (mThreadRunned) {
    // return;
    // }
    //
    // Ut.dd("hello");
    //
    // if (mPath == null) {
    // mPath = new Path();
    // mBaseLocation = new GeoPoint(
    // (int) (lat * 1E6),
    // (int) (lon * 1E6));
    // mBasePj = mOsmv
    // .getProjection();
    // mBaseCoords = mBasePj
    // .toPixels2(mBaseLocation);
    // mPath.setLastPoint(
    // mBaseCoords.x,
    // mBaseCoords.y);
    // Ut.dd("setLastPoint "
    // + mBaseCoords.x
    // + " "
    // + mBaseCoords.y);
    // } else {
    // final GeoPoint geopoint = new GeoPoint(
    // (int) (lat * 1E6),
    // (int) (lon * 1E6));
    // final Point point = mBasePj
    // .toPixels2(geopoint);
    // mPath.lineTo(
    // point.x,
    // point.y);
    // Ut.dd("lineTo "
    // + point.x
    // + " "
    // + point.y);
    // }
    //
    // }
    // };
    //
}
