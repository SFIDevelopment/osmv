package at.the.gogo.parkoid.sensors.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.activities.ParkuhrActivity;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.LocationPoint;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.webservices.YahooGeocoding;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationBroadcastService;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

public class GlobalBroadcastReceiver extends BroadcastReceiver {
    public final static int NO_VALID_VALUE = -1;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        // check for battery event

        final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,
                NO_VALID_VALUE);
        final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,
                NO_VALID_VALUE);
        if ((level != NO_VALID_VALUE) && (scale != NO_VALID_VALUE)) {
            final float batteryPct = level / (float) scale;

            Util.d("Battery State changed: " + (int) batteryPct + "%");
        }

        // check for docking state
        final int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                NO_VALID_VALUE);

        if (dockState != NO_VALID_VALUE) {
            final boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;

            final boolean isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
            final boolean isDesk = (dockState == Intent.EXTRA_DOCK_STATE_DESK)
                    || (dockState == Intent.EXTRA_DOCK_STATE_LE_DESK)
                    || (dockState == Intent.EXTRA_DOCK_STATE_HE_DESK);

            Util.d("Dock State changed: "
                    + (isDocked ? (isCar ? "Car dock" : (isDesk ? "Desk"
                            : "unknown")) : "undocked"));
        }

        // powerstate

        final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status != NO_VALID_VALUE) {
            final boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                    || (status == BatteryManager.BATTERY_STATUS_FULL);

            final int chargePlug = intent.getIntExtra(
                    BatteryManager.EXTRA_PLUGGED, -1);
            final boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            // final boolean acCharge = chargePlug ==
            // BatteryManager.BATTERY_PLUGGED_AC;

            Util.d("Powerconnection State changed: "
                    + (isCharging ? (usbCharge ? "USB charge" : "AC charge")
                            : "not isCharging"));
        }

        if (intent
                .getAction()
                .equalsIgnoreCase(
                        "com.littlefluffytoys.littlefluffylocationlibrary.LOCATION_CHANGED")) {

            final LocationInfo locationInfo = (LocationInfo) intent
                    .getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
            if (CoreInfoHolder.getInstance().getContext() == null) {
                CoreInfoHolder.getInstance().setContext(context);
            }

            CoreInfoHolder
                    .getInstance()
                    .getDbManager()
                    .getDatabase()
                    .insertLocation(locationInfo.lastLat,
                            locationInfo.lastLong, locationInfo.lastAccuracy,
                            locationInfo.lastLocationUpdateTimestamp,
                            locationInfo.provider);

            // start service if difference larger
            // do some complex stuff ...
            // filter coords ...
            if (Util.isInternetConnectionAvailable(context)) {

                // we can reverse geocode and show address
                LocationBroadcastService.forceDelayedServiceCall(context, 10,AdressDisplayService.class); 
            }
        }
    }

    public class AdressDisplayService extends Service {

        private final IBinder mBinder = new Binder() {
                                          @Override
                                          protected boolean onTransact(
                                                  int code, Parcel data,
                                                  Parcel reply, int flags)
                                                  throws RemoteException {
                                              return super.onTransact(code,
                                                      data, reply, flags);
                                          }
                                      };

        Runnable              mTask   = new Runnable() {
                                          public void run() {

                                              final LocationPoint point = CoreInfoHolder
                                                      .getInstance()
                                                      .getDbManager()
                                                      .getDatabase()
                                                      .retrieveLatestPoint();

                                              double lat = 0.0;
                                              double lon = 0.0;

                                              if (point != null) {

                                                  lat = point.getLatitude();
                                                  lon = point.getLongitude();

                                              } else if (Util.DEBUGMODE) {

                                                  lat = 48.208336;
                                                  lon = 16.372223;

                                              }

                                              if ((lat != 0.0) && (lon != 0.0)) {
                                                  final GeoCodeResult result = YahooGeocoding
                                                          .reverseGeoCode(lat,
                                                                  lon);

                                                  CoreInfoHolder.getInstance()
                                                          .setLastKnownAddress(
                                                                  result);

                                                  Notification notification = new Notification(
                                                          R.drawable.notify,
                                                          "Locaton updated "
                                                                  + point.getTime()
                                                                  / 1000
                                                                  + " seconds ago",
                                                          System.currentTimeMillis());

                                                  Intent contentIntent = new Intent(
                                                          CoreInfoHolder
                                                                  .getInstance()
                                                                  .getContext(),
                                                          ParkuhrActivity.class);
                                                  PendingIntent contentPendingIntent = PendingIntent
                                                          .getActivity(
                                                                  CoreInfoHolder
                                                                          .getInstance()
                                                                          .getContext(),
                                                                  0,
                                                                  contentIntent,
                                                                  PendingIntent.FLAG_UPDATE_CURRENT);

                                                  notification
                                                          .setLatestEventInfo(
                                                                  CoreInfoHolder
                                                                          .getInstance()
                                                                          .getContext(),
                                                                  "Location update broadcast received",
                                                                  "Timestamped "
                                                                          + LocationInfo
                                                                                  .formatTimeAndDay(
                                                                                          point.getTime() / 1000,
                                                                                          true),
                                                                  contentPendingIntent);

                                                  // Trigger the notification.
                                                  ((NotificationManager) CoreInfoHolder
                                                          .getInstance()
                                                          .getContext()
                                                          .getSystemService(
                                                                  Context.NOTIFICATION_SERVICE))
                                                          .notify(1234,
                                                                  notification);

                                              } else {
                                                  Util.e("Address lookup failed due to missing coords");
                                              }
                                              AdressDisplayService.this
                                                      .stopSelf();
                                          }
                                      };

        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            new Thread(null, mTask).start();

            // We want this service to continue running until it is explicitly
            // stopped, so return sticky.
            return START_STICKY;
        }

        @Override
        public void onDestroy() {
        }

    }

}
