package org.outlander.instruments.compass;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.fragments.PageChangeNotifyer;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.views.LargeCompassView;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

public class CompassFragment extends Fragment implements PageChangeNotifyer {

    private Double           northHeading    = new Double(0);
    private TextView         navType         = null;
    private TextView         navAccuracy     = null;
    private TextView         navSatellites   = null;
    private TextView         navLocation     = null;
    private TextView         distanceView    = null;
    private TextView         headingView     = null;
    private TextView         destinationView = null;
    private LargeCompassView compassView     = null;
    Double                   targetHeading;
    private View             navigationView  = null;

    SensorEventListener      orientationListener;
    LocationListener         locationListener;

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    public void initialize(final Context context, final ViewGroup container,
            final LayoutInflater inflater) {

        // navigationView = (container == null) ? inflater.inflate(
        // R.layout.compass, null) : inflater.inflate(R.layout.compass,
        // container, false);

        navigationView = inflater.inflate(R.layout.compass, null);

        navType = (TextView) navigationView.findViewById(R.id.nav_type);
        navAccuracy = (TextView) navigationView.findViewById(R.id.nav_accuracy);
        navSatellites = (TextView) navigationView
                .findViewById(R.id.nav_satellites);
        navLocation = (TextView) navigationView.findViewById(R.id.nav_location);

        compassView = (LargeCompassView) navigationView
                .findViewById(R.id.compass);
        destinationView = (TextView) navigationView
                .findViewById(R.id.destination);

        distanceView = (TextView) navigationView.findViewById(R.id.distance);
        headingView = (TextView) navigationView.findViewById(R.id.heading);

        // final SharedPreferences sharedPreferences = PreferenceManager
        // .getDefaultSharedPreferences(context);
        //
        // coordFormt = Integer.parseInt(sharedPreferences.getString(
        // "pref_coords", "1"));
        //
        // metric = Integer.parseInt(sharedPreferences
        // .getString("pref_units", "0"));

    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        // if (navigationView == null) {
        initialize(inflater.getContext(), container, inflater);
        // }

        return navigationView;
    }

    @Override
    public View getView() {
        return navigationView;
    }

    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    private void pause() {

        // unregister listeners
        deregisterLocationListener();
        deregisterOrientationListener();
    }

    private void resume() {

        if (CoreInfoHandler.getInstance().getCurrentLocation() != null) {
            onLocationChange(CoreInfoHandler.getInstance().getCurrentLocation());
        }

        // (re)activate listener
        registerLocationListener();
        registerOrientationListener();
    }

    public void onStatusChanged(final String provider, final int status,
            final Bundle extras) {
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

    public void onLocationChange(final Location loc) {

        navLocation.setText(GeoMathUtil.formatLocation(loc, CoreInfoHandler
                .getInstance().getCoordFormatId()));

        final GeoPoint target = CoreInfoHandler.getInstance()
                .getCurrentTarget();

        if (target != null) {
            destinationView.setText(GeoMathUtil.formatCoordinate(
                    target.getLatitude(), target.getLongitude(),
                    CoreInfoHandler.getInstance().getCoordFormatId()));

            targetHeading = GeoMathUtil.azimuthTo(loc.getLatitude(),
                    loc.getLongitude(), target.getLatitude(),
                    target.getLongitude());
            headingView.setText(targetHeading.intValue() + "°");

            distanceView.setText(GeoMathUtil.getHumanDistanceString(GeoMathUtil
                    .distanceTo(loc.getLatitude(), loc.getLongitude(),
                            target.getLatitude(), target.getLongitude()),
                    CoreInfoHandler.getInstance().getDistanceUnitFormatId()));

            if (loc.hasAccuracy()) {
                navAccuracy
                        .setText((loc.hasAccuracy() ? GeoMathUtil.twoDecimalFormat
                                .format(loc.getAccuracy()) : "?")
                                + " m");
            }
        }
        // compassView.set
    }

    public void onOrientationSensorChanged(final SensorEvent event,
            final int orientation) {
        // compassView.setAzimuth((double) event.values[0] + (90 *
        // orientation));
        compassView.setBearing((double) event.values[0] + (90 * orientation));
        northHeading = (double) event.values[0] + (90 * orientation);
        compassView.updateNorth(northHeading, targetHeading);
        compassView.invalidate();
    }

    private LocationListener getLocationListener() {
        if (locationListener == null) {
            locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(final String provider,
                        final int status, final Bundle extras) {
                    onStatusChanged(provider, status, extras);
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

    private SensorEventListener getOrientationListener() {
        if (orientationListener == null) {
            orientationListener = new SensorEventListener() {

                private int iOrientation = -1;

                @Override
                public void onSensorChanged(final SensorEvent event) {
                    if (iOrientation < 0) {
                        iOrientation = ((WindowManager) getActivity()
                                .getSystemService(Context.WINDOW_SERVICE))
                                .getDefaultDisplay().getRotation();
                    }

                    onOrientationSensorChanged(event, iOrientation);
                }

                @Override
                public void onAccuracyChanged(final Sensor sensor,
                        final int accuracy) {
                    // TODO Auto-generated method stub

                }
            };
        }
        return orientationListener;
    }

    private void registerLocationListener() {
        CoreInfoHandler.getInstance().registerLocationListener(
                getLocationListener());
    }

    private void deregisterLocationListener() {
        CoreInfoHandler.getInstance().deregisterLocationListener(
                getLocationListener());
    }

    private void registerOrientationListener() {
        CoreInfoHandler.getInstance().registerOrientationListener(
                getOrientationListener());
    }

    private void deregisterOrientationListener() {
        CoreInfoHandler.getInstance().deregisterOrientationListener(
                getOrientationListener());
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
        // TODO Auto-generated method stub

    }
}
