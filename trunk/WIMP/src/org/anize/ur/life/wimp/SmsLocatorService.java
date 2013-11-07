package org.anize.ur.life.wimp;

import org.anize.ur.life.wimp.util.Util;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

public class SmsLocatorService extends Service implements LocationListener {

	public static final String INTENT_DESTINATION_NUMBER = "intentdestinationnumber";

	private LocationManager lm;

	private String destination;

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@TargetApi(9)
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		destination = intent.getStringExtra(INTENT_DESTINATION_NUMBER);

		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		Location location = null;

		final Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_MEDIUM); // should be fine ??
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);

		final String strLocationProvider = lm.getBestProvider(criteria, true);

		location = lm.getLastKnownLocation(strLocationProvider);
		if (Math.abs(location.getTime() - System.currentTimeMillis()) > (15 * 60 * 1000)) // older
		// than
		// 15min
		{
			location = null;
		}

		if (location == null) {
			// get asyncronous
			lm.requestSingleUpdate(criteria, this, null);

			// if (lm.getProvider(LocationManager.GPS_PROVIDER) != null) {
			// lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
			// this);
			//
			// } else if (lm.getProvider(LocationManager.NETWORK_PROVIDER) !=
			// null) {
			// lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
			// 0, this);
			// } else if (lm.getProvider(LocationManager.PASSIVE_PROVIDER) !=
			// null) {
			// lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0,
			// 0, this);
			// }
		} else {
			sendLocation(location);
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		lm.removeUpdates(this);
	}


	private void sendSms(final String message) {
		final SmsManager sm = SmsManager.getDefault();
		sm.sendTextMessage(destination, null, message, null, null);

		Util.i("Sending '" + message + "' to " + destination);
	}

	
	private void sendLocation(final Location location) {
		sendSms(Util.getSendLocationInfo(this,location.getLatitude(),location.getLongitude(),getResources().getString(R.string.sms_phonelocation)));

	}

	@Override
	public void onLocationChanged(final Location location) {

		sendLocation(location);
		stopSelf();
	}

	@Override
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onStatusChanged(final String arg0, final int arg1,
			final Bundle arg2) {
	}
}
