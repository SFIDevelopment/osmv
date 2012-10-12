package org.anize.ur.life.wimp.locationstuff;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.activities.MainActivity;
import org.anize.ur.life.wimp.models.GeoCodeResult;
import org.anize.ur.life.wimp.models.LocationPoint;
import org.anize.ur.life.wimp.util.Util;
import org.anize.ur.life.wimp.util.webservices.YahooGeocoding;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

@TargetApi(11)
public class AdressDisplayService extends Service {

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(final int code, final Parcel data,
				final Parcel reply, final int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

	Runnable mTask = new Runnable() {

		@Override
		public void run() {

			final LocationPoint point = GlobalBroadcastReceiver.getLastPoint(AdressDisplayService.this);
			// CoreInfoHolder.getInstance()
			// .getDbManager().getDatabase().retrieveLatestPoint();

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
				final GeoCodeResult result = YahooGeocoding.reverseGeoCode(lat,
						lon);

				final String[] address = YahooGeocoding.formatAddress2(result);

				// CoreInfoHolder.getInstance().setLastKnownAddress(result);

				final Intent contentIntent = new Intent(
						AdressDisplayService.this, MainActivity.class);

				contentIntent.putExtra("lat", lat);
				contentIntent.putExtra("lon", lon);
				contentIntent.putExtra("title", address[0]);
				contentIntent.putExtra("content", address[1]);
				
				final PendingIntent contentPendingIntent = PendingIntent
						.getActivity(AdressDisplayService.this, 0,
								contentIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);

				final Notification notification =

				new Notification.Builder(AdressDisplayService.this)
						.setSmallIcon(R.drawable.ic_stat_world)
						.setTicker("current Address changed...")
						.setWhen(point.getTime())
						.setContentTitle(address[0]).setContentText(address[1])
						.setLargeIcon(
								BitmapFactory.decodeResource(
										AdressDisplayService.this
												.getResources(),
										R.drawable.notifyer))
						.setContentInfo("info")
						.setContentIntent(contentPendingIntent)
						.setAutoCancel(true).getNotification();

				// .build();

				final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				nm.notify(1234, notification);

				//
				// Notification notification = new Notification(
				// R.drawable.notify, "Locaton updated "
				// + point.getTime() / 1000 + " seconds ago",
				// System.currentTimeMillis());
				//
				//
				// notification.setLatestEventInfo(
				// CoreInfoHolder.getInstance().getContext(),
				// "Location update broadcast received",
				// "Timestamped "
				// + LocationInfo.formatTimeAndDay(
				// point.getTime() / 1000, true),
				// contentPendingIntent);
				//
				// // Trigger the notification.
				// ((NotificationManager) CoreInfoHolder.getInstance()
				// .getContext()
				// .getSystemService(Context.NOTIFICATION_SERVICE))
				// .notify(1234, notification);

			} else {
				Util.e("Address lookup failed due to missing coords");
			}
			AdressDisplayService.this.stopSelf();
		}
	};

	@Override
	public IBinder onBind(final Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		Util.i("lookup address started");
		new Thread(null, mTask).start();

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
	}

}