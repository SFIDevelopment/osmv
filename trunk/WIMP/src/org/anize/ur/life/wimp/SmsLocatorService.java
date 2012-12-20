package org.anize.ur.life.wimp;

import java.io.IOException;
import java.util.List;

import org.anize.ur.life.wimp.util.Util;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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

	private String getStreetName(final Location location) {

		// Get the street address
		final Geocoder geocoder = new Geocoder(this);

		String addressName = null;
		try {
			final List<Address> addresses = geocoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
			if ((addresses != null) && (addresses.size() > 0)) {
				final Address address = addresses.get(0);
				final StringBuffer sb = new StringBuffer();
				sb.append((address.getAddressLine(0) != null) ? address
						.getAddressLine(0) : "");
				sb.append((address.getLocality() != null) ? " "
						+ address.getLocality() : "");
				sb.append((address.getCountryCode() != null) ? " "
						+ address.getCountryCode() : "");
				addressName = sb.toString();
			}

		} catch (final IOException e) {
			Util.e("Unable to geocode:" + e.getMessage());
		}

		return addressName;
	}

	private String getShortMapsUrl(final Location location) {

		final StringBuffer sb = new StringBuffer();
		sb.append("http://maps.google.com/maps?q=");
		sb.append(location.getLatitude());
		sb.append(",");
		sb.append(location.getLongitude());
		sb.append(getResources().getString(R.string.sms_phonehere));

		return sb.toString();
	}

	private void sendSms(final String message) {
		final SmsManager sm = SmsManager.getDefault();
		sm.sendTextMessage(destination, null, message, null, null);

		Util.i("Sending '" + message + "' to " + destination);
	}

	private void sendLocation(final Location location) {
		final String address = getStreetName(location);

		final String url = getShortMapsUrl(location);

		final String message = getResources().getString(R.string.sms_phonelocation)
				+ (address != null ? address : " ") + " " + url;

		sendSms(message);

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
