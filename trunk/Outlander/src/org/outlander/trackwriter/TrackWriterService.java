package org.outlander.trackwriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.geonames.WebService;
import org.outlander.R;
import org.outlander.activities.TrackListActivity;
import org.outlander.io.db.DBManager;
import org.outlander.io.db.TrackWriterDatabaseHelper;
import org.outlander.utils.Ut;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class TrackWriterService extends Service implements OpenStreetMapConstants {

    private SQLiteDatabase                   db;
    private NotificationManager              mNotificationManager;

    protected LocationManager                mLocationManager;
    protected TrackRecordingLocationListener mLocationListener;
    private final boolean                    sendToLiveLogger      = false;
    private String                           trackingServerUrl;

    private final ExecutorService            mThreadPool           = Executors.newFixedThreadPool(5);

    private boolean                          isRecording           = false;

    /** The timer posts a runnable to the main thread via this handler. */
    private final Handler                    handler               = new Handler();

    private ServiceBinder                    binder                = new ServiceBinder(this);
    /**
     * Task invoked by a timer periodically to make sure the location listener
     * is still registered.
     */
    private final TimerTask                  checkLocationListener = new TimerTask() {

                                                                       @Override
                                                                       public void run() {
                                                                           // It's
                                                                           // always
                                                                           // safe
                                                                           // to
                                                                           // assume
                                                                           // that
                                                                           // if
                                                                           // isRecording()
                                                                           // is
                                                                           // true,
                                                                           // it
                                                                           // implies
                                                                           // that
                                                                           // onCreate()
                                                                           // has
                                                                           // finished.
                                                                           if (isRecording()) {
                                                                               handler.post(new Runnable() {

                                                                                   @Override
                                                                                   public void run() {
                                                                                       Ut.d("Re-registering location listener with TrackRecordingService.");
                                                                                       unregisterLocationListener();
                                                                                       registerLocationListener();
                                                                                   }
                                                                               });
                                                                           }
                                                                       }
                                                                   };

    @Override
    public void onCreate() {
        super.onCreate();

        registerLocationListener();

        showNotification();
    }

    public void startRecording() {
        isRecording = true;

        final File folder = Ut.getTschekkoMapsMainDir(this, "data");
        if (folder.canRead()) {
            try {
                db = new TrackWriterDatabaseHelper(this, folder.getAbsolutePath() + "/writtentrack.db").getWritableDatabase();
            }
            catch (final Exception e) {
                db = null;
            }
        }

        if (db == null) {
            Toast.makeText(this, getString(R.string.message_cantstarttrackwriter) + " " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
            this.stopSelf();
            return;
        }

        trackingServerUrl = getString(R.string.livetrackingurl);
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the
        // expanded notification
        final CharSequence text = getText(R.string.remote_service_started);

        // Set the icon, scrolling text and timestamp
        final Notification notification = new Notification(R.drawable.ic_stat_track_writer_service, text, System.currentTimeMillis());
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;

        // The PendingIntent to launch our activity if the user selects this
        // notification
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TrackListActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.remote_service_started), text, contentIntent);

        // Send the notification.
        // We use a string id because it is a unique number. We use it later to
        // cancel.
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(R.string.remote_service_started, notification);
    }

    @Override
    public void onDestroy() {

        isRecording = false;

        binder.detachFromService();
        binder = null;

        // Cancel the persistent notification.
        if (mNotificationManager != null) {
            mNotificationManager.cancel(R.string.remote_service_started);
        }

        unregisterLocationListener();

        if (db != null) {
            db.close();
        }

        doSaveTrack();

        // Tell the user we stopped.
        // Toast.makeText(this, R.string.remote_service_stopped,
        // Toast.LENGTH_SHORT).show();

        // Unregister all callbacks.
        // if (mCallbacks != null) {
        // mCallbacks.kill();
        // }

        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        // mHandler.removeMessages(REPORT_MSG);
    }

    public void registerLocationListener() {
        try {

            mLocationListener = new TrackRecordingLocationListener();
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

            final int minTime = Integer.parseInt(pref.getString("pref_trackwriter_mintime", "10000"));
            final int minDistance = Integer.parseInt(pref.getString("pref_trackwriter_mindistance", "15"));

            mLocationListener.Init(minTime, minDistance);
            getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);

        }
        catch (final RuntimeException e) {
            Ut.e("Could not register location listener: " + e.getMessage());
        }
    }

    public void unregisterLocationListener() {

        if (mLocationListener != null) {
            getLocationManager().removeUpdates(mLocationListener);
            Ut.d("Location listener now unregistered w/ TrackRecordingService.");
        }
    }

    private void doSaveTrack() {
        // showDialog(R.id.dialog_wait);

        mThreadPool.execute(new Runnable() {

            DBManager mPoiManager = new DBManager(TrackWriterService.this);

            @Override
            public void run() {
                SQLiteDatabase db = null;
                final File folder = Ut.getTschekkoMapsMainDir(TrackWriterService.this, "data");
                if (folder.canRead()) {
                    try {
                        db = new TrackWriterDatabaseHelper(TrackWriterService.this, folder.getAbsolutePath() + "/writtentrack.db").getWritableDatabase();
                    }
                    catch (final Exception e) {
                        db = null;
                    }
                }

                if (db != null) {
                    try {
                        mPoiManager.getGeoDatabase().saveTrackFromWriter(db);
                    }
                    catch (final Exception e) {
                    }
                    db.close();
                }

                // dlgWait.dismiss();
                // mHandler.sendMessage(mHandler.obtainMessage(R.id.tracks,
                // res));
                // Message.obtain(mHandler, R.id.tracks, res, 0).sendToTarget();
            }
        });
    }

    private LocationManager getLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        Ut.d("TrackRecordingService.onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Ut.d("TrackRecordingService.onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public boolean stopService(final Intent name) {
        Ut.d("TrackRecordingService.stopService");
        unregisterLocationListener();
        return super.stopService(name);
    }

    @Override
    public void onStart(final Intent intent, final int startId) {

    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        return START_STICKY;
    }

    public void addPointToDB(final double latitude, final double longitude, final double altitude, final float speed, final long currentTimeMillis) {

        final ContentValues cv = new ContentValues();

        cv.put("trackid", 0);
        cv.put("lat", latitude);
        cv.put("lon", longitude);
        cv.put("alt", altitude);
        cv.put("speed", speed);
        cv.put("date", currentTimeMillis / 1000);

        db.insert("trackpoints", null, cv);
    }

    public void sendPoint(final double latitude, final double longitude, final double altitude, final float speed, final long currentTimeMillis) {

        if (trackingServerUrl != null) {
            mThreadPool.execute(new Runnable() {

                @Override
                public void run() {

                    // test.livetrack24.com/track.php?leolive=1&amp;client=%s&amp;v=1&amp;user=%2&amp;pass=%3&amp;phone=%4&amp;gps=Internal
                    // GPS&amp;vtype=%s&amp;vname=%s&amp;lat=%.2f&amp;lon=%.2f&amp;alt=%d&amp;sog=%d&amp;cog=%d&amp;tm=%d

                    final String urlStr = String.format(trackingServerUrl, "testdevice", "testuser", "testpwd", "phone", "type", "name", latitude, longitude,
                            speed, 0, currentTimeMillis);

                    try {
                        final URL url = new URL(urlStr);

                        final URLConnection conn = url.openConnection();
                        conn.setConnectTimeout(WebService.connectTimeOut);
                        conn.setReadTimeout(WebService.readTimeOut);
                        // conn.setRequestProperty("User-Agent",
                        // WebService.USER_AGENT);

                        String line;
                        final StringBuilder sb = new StringBuilder();
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();
                        Ut.d("leolive: " + sb.toString());

                    }
                    catch (final Exception x) {
                        Ut.d("leolive: " + x.toString());
                    }

                }

            });
        }
    }

    private static class ServiceBinder extends ITrackWriterService.Stub {

        private TrackWriterService service;

        public ServiceBinder(final TrackWriterService service) {
            this.service = service;
        }

        /**
         * Clears the reference to the outer class to minimize the leak.
         */
        public void detachFromService() {
            this.service = null;
            attachInterface(null, null);
        }

        @Override
        public boolean isRecording() {
            checkService();
            return service.isRecording();
        }

        private void checkService() {
            if (service == null) {
                throw new IllegalStateException("The service has been already detached!");
            }
        }

        @Override
        public void newPointRecorded(final double lat, final double lon) throws RemoteException {
            // TODO Auto-generated method stub

        }

        @Override
        public void startNewTrack() throws RemoteException {
            service.startRecording();
        }

        @Override
        public void finishTrack() throws RemoteException {
            if (service.isRecording()) {
                service.doSaveTrack();
            }
        }
    }

    private class TrackRecordingLocationListener implements LocationListener {

        private Location   mLastWritedLocation      = null;
        private Location   mLastLocation            = null;
        private long       mMinTime                 = 2000;
        private final long mMaxTime                 = 2000;
        private int        mMinDistance             = 10;
        private double     mDistanceFromLastWriting = 0;
        private long       mTimeFromLastWriting     = 0;

        @Override
        public void onLocationChanged(final Location loc) {

            // if (!isRecording()) {
            // return;
            // }

            if (loc != null) {
                boolean needWrite = false;
                if (mLastLocation != null) {
                    mDistanceFromLastWriting = +loc.distanceTo(mLastLocation);
                }
                if (mLastWritedLocation != null) {
                    mTimeFromLastWriting = loc.getTime() - mLastWritedLocation.getTime();
                }

                if ((mLastWritedLocation == null) || (mLastLocation == null)) {
                    needWrite = true;
                }
                else if ((mTimeFromLastWriting > mMaxTime) || ((mDistanceFromLastWriting > mMinDistance) && (loc.getAccuracy() > mLastLocation.getAccuracy()))) {
                    needWrite = true;
                }
                if (needWrite) {
                    // Ut.dd("addPoint mDistanceFromLastWriting="+mDistanceFromLastWriting+" mTimeFromLastWriting="+(mTimeFromLastWriting/1000));
                    mLastWritedLocation = loc;
                    mLastLocation = loc;
                    mDistanceFromLastWriting = 0;

                    final long time = System.currentTimeMillis();

                    if (isRecording()) {
                        addPointToDB(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getSpeed(), time);

                        if (sendToLiveLogger) {
                            sendPoint(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getSpeed(), time);
                        }
                    }

                    // send global locationupdate
                    Intent intent = new Intent(LOC_UPDATE_EVENT);
                    intent.putExtra(LOC_UPDATE_LOC, loc);

                    LocalBroadcastManager.getInstance(TrackWriterService.this).sendBroadcast(intent);

                }
                else {
                    // Ut.dd("NOT addPoint mDistanceFromLastWriting="+mDistanceFromLastWriting+" mTimeFromLastWriting="+(mTimeFromLastWriting/1000));
                    mLastLocation = loc;
                }
            }
        }

        public void Init(final int mintime, final int mindistance) {
            mMinTime = mintime;
            mMinDistance = mindistance;
            Ut.dd("mintime=" + mintime + " mindistance=" + mindistance);
        }

        @Override
        public void onStatusChanged(final String a, final int status, final Bundle b) {
        }

        @Override
        public void onProviderEnabled(final String a) {
        }

        @Override
        public void onProviderDisabled(final String a) {
        }
    }

}
