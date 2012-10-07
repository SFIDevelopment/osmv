package org.outlander.activities;

import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.outlander.R;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.views.GaugeView;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class TachoActivity extends SherlockActivity {

    GaugeView        gauge;
    LocationListener locationListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.tacho);

        gauge = (GaugeView) findViewById(R.id.speedmeter1);
        gauge.setValue(0);

    }

    @Override
    protected void onPause() {

        super.onPause();
        deregisterLocationListener();
    }

    @Override
    protected void onResume() {

        super.onResume();
        registerLocationListener();
        setupActionBar();
    }

    private void setupActionBar() {

        if (Ut.isMultiPane(this)) {
            getSupportActionBar().setNavigationMode(
                    ActionBar.NAVIGATION_MODE_STANDARD);

            getSupportActionBar().setDisplayUseLogoEnabled(false);

        } else {
            getSupportActionBar().hide();
        }

    }

    LocationManager mLocationManager;

    protected LocationManager getLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }

    private void registerLocationListener() {

        getLocationManager().requestLocationUpdates(OpenStreetMapConstants.GPS,
                2000, 30, getLocationListener());

        // CoreInfoHandler.getInstance().registerLocationListener(
        // getLocationListener());
    }

    private void deregisterLocationListener() {

        getLocationManager().removeUpdates(getLocationListener());

        // CoreInfoHandler.getInstance().deregisterLocationListener(
        // getLocationListener());
    }

    private LocationListener getLocationListener() {
        if (locationListener == null) {
            locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(final String provider,
                        final int status, final Bundle extras) {
                    // onStatusChange(provider, status, extras);
                }

                @Override
                public void onProviderEnabled(final String provider) {

                }

                @Override
                public void onProviderDisabled(final String provider) {

                }

                @Override
                public void onLocationChanged(final Location location) {
                    onLocationChange(location);
                }
            };
        }
        return locationListener;
    }

    public void onLocationChange(final Location loc) {

        if (loc.hasSpeed()) {
            gauge.setValue((int) GeoMathUtil.convertSpeed(loc.getSpeed(), 1));
        }

        // updateSpeedField(loc);
        // updateAlitudeField(loc);
        // updateAccuracyField(loc);
    }
}
