package org.outlander.sensors.receiver;

import java.io.IOException;
import java.util.List;

import org.outlander.utils.Ut;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

public class SmsLocatorService extends Service implements LocationListener {

    public static final String INTENT_DESTINATION_NUMBER = "intentdestinationnumber";

    private LocationManager    lm;

    private String             destination;

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);

        destination = intent.getStringExtra(INTENT_DESTINATION_NUMBER);

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (lm.getProvider(LocationManager.GPS_PROVIDER) != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

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
            Ut.e("Unable to geocode:" + e.getMessage());
        }

        return addressName;
    }

    private String getShortMapsUrl(final Location location) {

        final StringBuffer sb = new StringBuffer();
        sb.append("http://mapof.it/");
        sb.append(location.getLatitude());
        sb.append(",");
        sb.append(location.getLongitude());

        return sb.toString();
    }

    private void sendSms(final String message) {
        final SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(destination, null, message, null, null);

        Ut.i("Sending '" + message + "' to " + destination);
    }

    @Override
    public void onLocationChanged(final Location location) {

        final String address = getStreetName(location);

        final String url = getShortMapsUrl(location);

        final String message = "Your phone is located near:" + address + " "
                + url;

        sendSms(message);

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
