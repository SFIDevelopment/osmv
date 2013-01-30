package org.anize.ur.life.wimp.locationstuff;

import java.util.List;
import java.util.Locale;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.activities.MainActivity;
import org.anize.ur.life.wimp.models.GeoCodeResult;
import org.anize.ur.life.wimp.models.LocationPoint;
import org.anize.ur.life.wimp.util.Util;
import org.anize.ur.life.wimp.util.webservices.YahooGeocoding;
import org.geonames.WebService;
import org.geonames.WikipediaArticle;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AdressDisplayService extends Service {

	public final static int STAT_ID = 1234;

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

			final LocationPoint point = GlobalBroadcastReceiver
					.getLastPoint(AdressDisplayService.this);
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

			// display notification info

			if ((lat != 0.0) && (lon != 0.0)) {

				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(AdressDisplayService.this);

				boolean showAddress = prefs.getBoolean("notify_address", false) && (Util.isInternetConnectionAvailable(AdressDisplayService.this));
				boolean showWiki = prefs.getBoolean("notify_wiki", false) && (Util.isInternetConnectionAvailable(AdressDisplayService.this));

				if (showAddress) {
					showAddressNotification(point);
				}
				if (showWiki) {
					showWikiNotification(point);
				}

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

	private void showAddressNotification(LocationPoint point) {
		final GeoCodeResult result = YahooGeocoding.reverseGeoCode(
				point.getLatitude(), point.getLongitude());

		final String[] address = YahooGeocoding.formatAddress2(result);

		// CoreInfoHolder.getInstance().setLastKnownAddress(result);

		final Intent contentIntent = new Intent(AdressDisplayService.this,
				MainActivity.class);

		contentIntent.putExtra("lat", point.getLatitude());
		contentIntent.putExtra("lon", point.getLongitude());
		contentIntent.putExtra("title", address[0]);
		contentIntent.putExtra("content", address[1]);

		final PendingIntent contentPendingIntent = PendingIntent.getActivity(
				AdressDisplayService.this, 0, contentIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		final Notification notification =

		new Notification.Builder(AdressDisplayService.this)
				.setSmallIcon(R.drawable.ic_stat_world)
				.setTicker(
						getResources()
								.getText(R.string.notification_ticker_adr))
				.setWhen(point.getTime())
				.setContentTitle(address[0])
				.setContentText(address[1])
				.setLargeIcon(
						BitmapFactory.decodeResource(
								AdressDisplayService.this.getResources(),
								R.drawable.ic_stat_world))
				.setContentInfo("info").setContentIntent(contentPendingIntent)
				.setAutoCancel(true).build();

		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		nm.notify(STAT_ID, notification);

	}

	private void showWikiNotification(LocationPoint point) {
		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		WebService.setUserName(WebService.USERNAME);
		try {
			List<WikipediaArticle> weblinks = WebService.findNearbyWikipedia(
					point.getLatitude(), point.getLongitude(), 1, Locale
							.getDefault().getLanguage(), 3);

			if (weblinks.size() < 1) {
				weblinks = WebService.findNearbyWikipedia(point.getLatitude(),
						point.getLongitude(), 1, "en", 3);
			}

			int i = 0;
			for (WikipediaArticle article : weblinks) {

				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://" + article.getWikipediaUrl()));
				browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				browserIntent.putExtra(Browser.EXTRA_APPLICATION_ID,
						getPackageName());

				Util.i("wikiURL: " + article.getWikipediaUrl());

				PendingIntent pendingBrowserIntent = PendingIntent.getActivity(
						AdressDisplayService.this, i, browserIntent,
						PendingIntent.FLAG_ONE_SHOT);

				final Notification wikinotification = new Notification.Builder(
						AdressDisplayService.this)
						.setSmallIcon(R.drawable.ic_stat_wiki)
						.setTicker(
								getResources().getText(
										R.string.notification_ticker_wiki))
						.setWhen(point.getTime())
						.setContentTitle(article.getTitle())
						.setContentText(article.getSummary())
						.setLargeIcon(
								BitmapFactory.decodeResource(
										AdressDisplayService.this
												.getResources(),
										R.drawable.ic_stat_wiki))
						.setContentInfo("wiki")
						.setContentIntent(pendingBrowserIntent)
						.setAutoCancel(true).build();
				i++;
				nm.notify(STAT_ID + i, wikinotification);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("WIMP", "Wikipedia failed:" + e.toString());
		}
	}

}