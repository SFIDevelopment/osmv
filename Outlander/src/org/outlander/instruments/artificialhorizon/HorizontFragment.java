package org.outlander.instruments.artificialhorizon;

import org.outlander.R;
import org.outlander.fragments.PageChangeNotifyer;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.views.ArtificialHorizontView;
import org.outlander.views.LargeCompassView;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HorizontFragment extends Fragment implements PageChangeNotifyer {

    // private final Resources res = null;

    private float[]                aValues         = new float[3];
    private float[]                mValues         = new float[3];
    private ArtificialHorizontView artificialhorizontView;
    private View                   navigationView  = null;
    private Double                 dstLatitude     = null;
    private Double                 dstLongitude    = null;
    private Double                 targetHeading   = new Double(0);
    private Double                 northHeading    = new Double(0);
    private final String           title           = null;
    private final String           name            = null;
    private TextView               navType         = null;
    private TextView               navAccuracy     = null;
    private TextView               navSatellites   = null;
    private TextView               navLocation     = null;
    private TextView               distanceView    = null;
    private TextView               headingView     = null;
    private TextView               destinationView = null;
    private final LargeCompassView compassView     = null;

    SensorEventListener            orientationListener;
    SensorEventListener            accellerationListener;
    LocationListener               locationListener;

    // SensorManager sensorManager;
    // private SharedPreferences sharedPreferences;

    public static HorizontFragment newInstance() {
        return new HorizontFragment();
    }

    public void initialize(final Context context, final ViewGroup container, final LayoutInflater inflater) {

        // navigationView = (container == null) ? inflater.inflate(
        // R.layout.horizont, null) : inflater.inflate(R.layout.horizont,
        // container, false);

        navigationView = inflater.inflate(R.layout.horizont, null);

        artificialhorizontView = (ArtificialHorizontView) navigationView.findViewById(R.id.artificialhorizonView);

        navType = (TextView) navigationView.findViewById(R.id.nav_type);
        navAccuracy = (TextView) navigationView.findViewById(R.id.nav_accuracy);
        navSatellites = (TextView) navigationView.findViewById(R.id.nav_satellites);
        navLocation = (TextView) navigationView.findViewById(R.id.nav_location);

        // compassView = (CompassView)
        // navigationView.findViewById(R.id.compass);
        destinationView = (TextView) navigationView.findViewById(R.id.destination);

        distanceView = (TextView) navigationView.findViewById(R.id.distance);
        headingView = (TextView) navigationView.findViewById(R.id.heading);

        // sensorManager = (SensorManager)
        // getSystemService(Context.SENSOR_SERVICE);
        updateOrientation(new float[] { 0, 0, 0 });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        if (navigationView == null) {
            initialize(inflater.getContext(), container, inflater);
        }

        return navigationView;
    }

    @Override
    public View getView() {
        return navigationView;
    }

    private void updateOrientation(final float[] values) {
        if (artificialhorizontView != null) {
            artificialhorizontView.setBearing(values[0]);
            artificialhorizontView.setPitch(values[1]);
            artificialhorizontView.setRoll(-values[2]);
            artificialhorizontView.invalidate();
        }
    }

    public void updateWithCalculation() {
        updateOrientation(calculateOrientation());
    }

    private float[] calculateOrientation() {
        final float[] values = new float[3];
        final float[] R = new float[9];
        final float[] outR = new float[9];

        SensorManager.getRotationMatrix(R, null, aValues, mValues);
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);

        SensorManager.getOrientation(outR, values);

        // Convert from Radians to Degrees.
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        return values;
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    private void resume() {
        registerAccellerationListener();
        registerOrientationListener();
        registerLocationListener();
    }

    private void pause() {
        deregisterAccellerationListener();
        deregisterOrientationListener();
        deregisterLocationListener();
    }

    public void setDestination(final double latitude, final double longitude) {
        dstLatitude = latitude;
        dstLongitude = longitude;

        // update text done in event....
    }

    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        final int cnt = extras.getInt("satellites", Integer.MIN_VALUE);

        String statusTxt;
        switch (status) {
            case LocationProvider.AVAILABLE:
                statusTxt = "available";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                statusTxt = "oos";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                statusTxt = "temp unavailable";
                break;
            default:
                statusTxt = "unknown";
        }
        navType.setText(provider + " (" + statusTxt + ")");

        navSatellites.setText("#" + cnt);

    }

    public void onLocationChanged(final Location loc) {

        // final int coordFormt = Integer.parseInt(sharedPreferences.getString(
        // "pref_coords", "1"));
        //
        // final int metric = Integer.parseInt(sharedPreferences.getString(
        // "pref_units", "0"));
        //
        // final boolean imperialUnits = (metric == 1);

        navLocation.setText(GeoMathUtil.formatLocation(loc, CoreInfoHandler.getInstance().getCoordFormatId()));

        if (dstLatitude != null) {
            destinationView.setText(GeoMathUtil.formatCoordinate(dstLatitude, dstLongitude, CoreInfoHandler.getInstance().getCoordFormatId()));

            targetHeading = GeoMathUtil.azimuthTo(loc.getLatitude(), loc.getLongitude(), dstLatitude, dstLongitude);
            headingView.setText(targetHeading.intValue() + "°");

            distanceView.setText(GeoMathUtil.getHumanDistanceString(GeoMathUtil.distanceTo(loc.getLatitude(), loc.getLongitude(), dstLatitude, dstLongitude),
                    CoreInfoHandler.getInstance().getDistanceUnitFormatId()));
        }
        // compassView.set
    }

    public void onOrientationSensorChanged(final SensorEvent event, final int orientation) {
        northHeading = (double) event.values[0] + (90 * orientation);
        compassView.updateNorth(northHeading, targetHeading);
        compassView.invalidate();
    }

    public void updateAccelerationValues(final float[] values) {
        aValues = values;
    }

    public void updateMagneticValues(final float[] values) {
        mValues = values;
    }

    private void registerLocationListener() {
        CoreInfoHandler.getInstance().registerLocationListener(getLocationListener());
    }

    private void deregisterLocationListener() {
        CoreInfoHandler.getInstance().deregisterLocationListener(getLocationListener());
    }

    private void registerOrientationListener() {
        CoreInfoHandler.getInstance().registerOrientationListener(getOrientationListener());
    }

    private void registerAccellerationListener() {
        CoreInfoHandler.getInstance().registerAccellerationListener(getAccelerationListener());
    }

    private void deregisterOrientationListener() {
        CoreInfoHandler.getInstance().deregisterOrientationListener(getOrientationListener());
    }

    private void deregisterAccellerationListener() {
        CoreInfoHandler.getInstance().deregisterAccellerationListener(getAccelerationListener());
    }

    private SensorEventListener getAccelerationListener() {
        if (accellerationListener == null) {
            accellerationListener = new SensorEventListener() {

                @Override
                public void onSensorChanged(final SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        updateAccelerationValues(event.values);
                    }
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        updateMagneticValues(event.values);
                    }

                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
                    // TODO Auto-generated method stub

                }
            };
        }
        return accellerationListener;
    }

    private SensorEventListener getOrientationListener() {
        if (orientationListener == null) {
            orientationListener = new SensorEventListener() {

                private final int iOrientation = -1;

                @Override
                public void onSensorChanged(final SensorEvent event) {
                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
                }
            };
        }
        return orientationListener;
    }

    private LocationListener getLocationListener() {
        if (locationListener == null) {
            locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(final String provider, final int status, final Bundle extras) {
                    HorizontFragment.this.onStatusChanged(provider, status, extras);

                }

                @Override
                public void onProviderEnabled(final String provider) {

                }

                @Override
                public void onProviderDisabled(final String provider) {

                }

                @Override
                public void onLocationChanged(final Location location) {

                    HorizontFragment.this.onLocationChanged(location);
                }
            };
        }
        return locationListener;
    }

    @Override
    public void pageGetsActivated() {
        resume();

    }

    @Override
    public void pageGetsDeactivated() {
        pause();

    }

    @Override
    public void refresh() {

    }

}
