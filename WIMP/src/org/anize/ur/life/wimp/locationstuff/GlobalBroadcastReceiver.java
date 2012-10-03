package org.anize.ur.life.wimp.locationstuff;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.models.LocationPoint;
import org.anize.ur.life.wimp.util.Util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationBroadcastService;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

@TargetApi(11)
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

			Util.i("Battery State changed: " + (int) batteryPct + "%");
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

			final String infoTitle = "Dock State changed: ";
			final String infoContext = (isDocked ? (isCar ? "Car dock"
					: (isDesk ? "Desk" : "unknown")) : "undocked");

			Util.d(infoTitle + infoContext);

			final Notification notification =

			new Notification.Builder(context)
					.setSmallIcon(R.drawable.app_notes)
					.setTicker("Dockstate Changed")
					.setWhen(System.currentTimeMillis())
					.setContentTitle(infoTitle).setContentText(infoContext)
					.setContentInfo("info").setAutoCancel(true)
					.getNotification();

			// .build();

			final NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			nm.notify(1202, notification);

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

			final String infoTitle = "Powerconnection State changed: ";
			final String infoContent = (isCharging ? (usbCharge ? "USB charge"
					: "AC charge") : "not isCharging");

			Util.i(infoTitle + infoContent);

			final Notification notification =

			new Notification.Builder(context)
					.setSmallIcon(R.drawable.app_notes)
					.setTicker("Powerconnectionstate Changed")
					.setWhen(System.currentTimeMillis())
					.setContentTitle(infoTitle).setContentText(infoContent)
					.setContentInfo("info").setAutoCancel(true)
					.getNotification();

			// .build();

			final NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			nm.notify(1203, notification);

		}

		if (intent
				.getAction()
				.equalsIgnoreCase(
						"com.littlefluffytoys.littlefluffylocationlibrary.LOCATION_CHANGED")) {

			final LocationInfo locationInfo = (LocationInfo) intent
					.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);

			final LocationPoint lp = getLastPoint(context);

			// show address if 30 min
			if ((lp.getTime() + (1000 * 60 * 30)) < locationInfo.lastLocationUpdateTimestamp) {
				//
				//
				// CoreInfoHolder
				// .getInstance()
				// .getDbManager()
				// .getDatabase()
				// .insertLocation(locationInfo.lastLat,
				// locationInfo.lastLong, locationInfo.lastAccuracy,
				// locationInfo.lastLocationUpdateTimestamp,
				// locationInfo.provider);

				// start service if difference larger
				// do some complex stuff ...
				// filter coords ...
				if (Util.isInternetConnectionAvailable(context)) {

					// we can reverse geocode and show address
					Util.i("lookup address delayed");
					LocationBroadcastService.forceDelayedServiceCall(context,
							10, AdressDisplayService.class);
				}

				saveLastPoint(context, locationInfo);

			} else {
				Util.i("LocationInfo ignored");
			}
		}
	}

	

	protected static final String KEY_LOCATION_UPDATE_TIME = "KEY_LOCATION_UPDATE_TIME";
	protected static final String KEY_LOCATION_UPDATE_LAT = "KEY_LOCATION_UPDATE_LAT";
	protected static final String KEY_LOCATION_UPDATE_LNG = "KEY_LOCATION_UPDATE_LNG";
	protected static final String KEY_LOCATION_UPDATE_PROVIDER = "KEY_LOCATION_UPDATE_PROVIDER";

	protected static final String KEY_LOCATION_UPDATE_ACCURACY = "KEY_LOCATION_UPDATE_ACCURACY";
	protected static final String KEY_LOCATION_BROADCAST_TIME = "KEY_LOCATION_SUBMIT_TIME";

	public static LocationPoint getLastPoint(final Context context) {

		final LocationPoint lp = new LocationPoint();

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		final float lastLat = prefs.getFloat(KEY_LOCATION_UPDATE_LAT,
				Long.MIN_VALUE);

		final float lastLong = prefs.getFloat(KEY_LOCATION_UPDATE_LNG,
				Long.MIN_VALUE);

		final int lastAccuracy = prefs.getInt(KEY_LOCATION_UPDATE_ACCURACY,
				Integer.MAX_VALUE);

		final long lastTime = prefs.getLong(KEY_LOCATION_UPDATE_TIME, 0);

		final String lastProvider = prefs.getString(
				KEY_LOCATION_UPDATE_PROVIDER, "Network");

		lp.setLatitude(lastLat);
		lp.setLongitude(lastLong);
		lp.setAccuracy(lastAccuracy);
		lp.setTime(lastTime);
		lp.setProvider(lastProvider);

		return lp;
	}

	public static void saveLastPoint(final Context context, final LocationInfo li) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final Editor prefsEditor = prefs.edit();

		prefsEditor.putFloat(KEY_LOCATION_UPDATE_LAT, li.lastLat);
		prefsEditor.putFloat(KEY_LOCATION_UPDATE_LNG, li.lastLong);
		prefsEditor.putInt(KEY_LOCATION_UPDATE_ACCURACY, li.lastAccuracy);
		prefsEditor.putLong(KEY_LOCATION_UPDATE_TIME,
				li.lastLocationUpdateTimestamp);
		prefsEditor.putString(KEY_LOCATION_UPDATE_PROVIDER, li.provider);

		prefsEditor.commit();
	}

}
