package org.outlander.fragments;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.activities.TachoActivity;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.views.GaugeView;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class NavigationFragment extends Fragment implements PageChangeNotifyer {

    final static int MAX_FIELDS = 9;

    TextView[]       values;

    LocationListener locationListener;

    // int coordFormt;
    // int metric;
    // int speed;

    ToggleButton     recordBtn;
    ToggleButton     naviBtn;
    GaugeView        speedoMeter;

    
    // font
    /*
     * Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/digital_07.otf");
        TextView tv = (TextView) findViewById(R.id.digitalclock);
        tv.setTypeface(tf);

     */
    
    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    // <string name="field_title_1">Current Position</string>
    // <string name="field_title_2">Target Position</string>
    // <string name="field_title_3">Bearing</string>
    // <string name="field_title_4">Distance</string>
    // <string name="field_title_5">Speed</string>
    // <string name="field_title_6">Altitude</string>
    // <string name="field_title_7">Accuracy</string>
    // <string name="field_title_8">Satellites</string>
    // <string name="field_title_9">Address</string>

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.navigation, null);

        values = new TextView[NavigationFragment.MAX_FIELDS];

        inflateField(view, R.id.cell1, 0, R.string.field_title_1);
        inflateField(view, R.id.cell2, 1, R.string.field_title_2);
        inflateField(view, R.id.cell3, 2, R.string.field_title_3);
        inflateField(view, R.id.cell4, 3, R.string.field_title_4);
        inflateField(view, R.id.cell5, 4, R.string.field_title_5);
        inflateField(view, R.id.cell6, 5, R.string.field_title_6);
        inflateField(view, R.id.cell7, 6, R.string.field_title_7);
        inflateField(view, R.id.cell8, 7, R.string.field_title_8);
        inflateField(view, R.id.cell9, 8, R.string.field_title_9);

        recordBtn = (ToggleButton) view.findViewById(R.id.toggleRecord);
        naviBtn = (ToggleButton) view.findViewById(R.id.toggleTarget);

        speedoMeter = (GaugeView) view.findViewById(R.id.speedmeter1);

        speedoMeter.setClickable(true);
        speedoMeter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startBiggerTacho();

            }
        });

        naviBtn.setClickable(true);
        naviBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (CoreInfoHandler.getInstance().getCurrentTarget() != null) {
                    CoreInfoHandler.getInstance()
                            .setUseCurrentTarget(
                                    !CoreInfoHandler.getInstance()
                                            .isUseCurrentTarget());
                }
                if (!CoreInfoHandler.getInstance().isUseCurrentTarget()) {
                    cleanTarget();
                } else {
                    updateTargetLocationField(CoreInfoHandler.getInstance()
                            .getCurrentTarget());

                }
                updateButtons();

            }
        });

        return view;
    }

    private void inflateField(final View view, final int id, final int valueIx,
            final int captionId) {

        final LinearLayout ll = (LinearLayout) view.findViewById(id);

        // ll.findViewById(R.id.textView1);

        final TextView caption = (TextView) ll.findViewById(R.id.textView1);
        caption.setText(captionId);

        values[valueIx] = (TextView) ll.findViewById(R.id.textView2);

        ll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                switch (v.getId()) {

                    case R.id.cell1: // location
                    case R.id.cell2: {

                        CoreInfoHandler.getInstance().incCoordFormatId();

                        updateCurrentLocationField(CoreInfoHandler
                                .getInstance().getCurrentLocation());

                        final GeoPoint target = CoreInfoHandler.getInstance()
                                .getCurrentTarget();

                        if (target != null) {
                            updateTargetLocationField(target);
                        }
                        break;
                    }
                    case R.id.cell3: {
                        break;
                    }
                    case R.id.cell4: {
                        break;
                    }
                    case R.id.cell5: { // speed
                        CoreInfoHandler.getInstance().incSpeedFormatId();

                        if (CoreInfoHandler.getInstance().getCurrentLocation() != null) {
                            updateSpeedField(CoreInfoHandler.getInstance()
                                    .getCurrentLocation());
                        }

                        break;
                    }
                    case R.id.cell6: {
                        break;
                    }
                    case R.id.cell7: {
                        break;
                    }
                    case R.id.cell8: {
                        break;
                    }
                    case R.id.cell9: {
                        // get address
                        if (Ut.isInternetConnectionAvailable(CoreInfoHandler
                                .getInstance().getMainActivity())) {
                            final GetAddressTask getAddress = new GetAddressTask();
                            getAddress.execute(CoreInfoHandler.getInstance()
                                    .getCurrentLocation());
                        }
                        break;
                    }

                }
            }
        });

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

        updateCurrentLocationField(loc);

        final GeoPoint target = CoreInfoHandler.getInstance()
                .getCurrentTarget();

        if (target != null) {
            updateTargetLocationField(target);
            // bearing

            final Double targetHeading = GeoMathUtil.azimuthTo(
                    loc.getLatitude(), loc.getLongitude(),
                    target.getLatitude(), target.getLongitude());

            values[2].setText(targetHeading.intValue() + "°");

            updateDistanceField(loc, target);

        } else {
            cleanTarget();
        }
        // <string name="field_title_5">Speed</string>
        // <string name="field_title_6">Altitude</string>
        // <string name="field_title_7">Accuracy</string>
        // <string name="field_title_8">Satellites</string>
        // <string name="field_title_9">Address</string>

        updateSpeedField(loc);
        updateAlitudeField(loc);
        updateAccuracyField(loc);
    }

    private void cleanTarget() {
        for (int i = 0; i < 3; i++) {
            values[1].setText(R.string.unknown);
        }

    }

    private void updateTargetLocationField(final GeoPoint target) {
        // target
        values[1].setText(GeoMathUtil.formatCoordinate(target.getLatitude(),
                target.getLongitude(), CoreInfoHandler.getInstance()
                        .getCoordFormatId()));
    }

    private void updateCurrentLocationField(final Location loc) {
        values[0].setText(GeoMathUtil.formatLocation(loc, CoreInfoHandler
                .getInstance().getCoordFormatId()));
    }

    private void updateDistanceField(final Location loc, final GeoPoint target) {
        // distance
        // final boolean imperialUnits =
        // (CoreInfoHandler.getInstance().getUnitFormatId() == 1); //TODO:
        // CONST!!

        values[3]
                .setText(" → "
                        + GeoMathUtil.getHumanDistanceString(GeoMathUtil
                                .distanceTo(loc.getLatitude(),
                                        loc.getLongitude(),
                                        target.getLatitude(),
                                        target.getLongitude()), CoreInfoHandler
                                .getInstance().getDistanceUnitFormatId()));

    }

    private void updateAlitudeField(final Location loc) {
        values[5]
                .setText((loc.hasAltitude() ? " ↑ "
                        + GeoMathUtil.getHumanDistanceString(loc.getAltitude(),
                                CoreInfoHandler.getInstance()
                                        .getDistanceUnitFormatId()) : "?"));

    }

    private void updateAccuracyField(final Location loc) {
        values[6].setText((loc.hasAccuracy() ? GeoMathUtil
                .getHumanDistanceString(loc.getAccuracy(), CoreInfoHandler
                        .getInstance().getDistanceUnitFormatId()) : "?"));
    }

    private void updateSpeedField(final Location loc) {
        values[4]
                .setText((loc.hasSpeed() ? GeoMathUtil.twoDecimalFormat
                        .format(GeoMathUtil.convertSpeed(loc.getSpeed(),
                                CoreInfoHandler.getInstance()
                                        .getSpeedFormatId())) : "?")
                        + " "
                        + CoreInfoHandler.getInstance().getMainActivity()
                                .getResources()
                                .getStringArray(R.array.speed_unit_title)[CoreInfoHandler
                                .getInstance().getSpeedFormatId()]);

        if (speedoMeter != null) {
            speedoMeter.setValue((int) GeoMathUtil.convertSpeed(loc.getSpeed(),
                    1));
        }
    }

    private void registerLocationListener() {
        CoreInfoHandler.getInstance().registerLocationListener(
                getLocationListener());
    }

    private void deregisterLocationListener() {
        CoreInfoHandler.getInstance().deregisterLocationListener(
                getLocationListener());
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    private void pause() {
        // unregister listeners
        deregisterLocationListener();
    }

    private void resume() {

        if (CoreInfoHandler.getInstance().getCurrentLocation() != null) {
            onLocationChange(CoreInfoHandler.getInstance().getCurrentLocation());
        } else {
            for (final TextView value : values) {
                value.setText("unknown");
            }
        }
        updateButtons();
        // (re)activate listener
        registerLocationListener();

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
        resume();
    }

    private void updateButtons() {
        naviBtn.setChecked((CoreInfoHandler.getInstance().getCurrentTarget() != null)
                && (!CoreInfoHandler.getInstance().isUseCurrentTarget()));
    }

    public class GetAddressTask extends AsyncTask<Location, Void, Address> {

        @Override
        protected Address doInBackground(final Location... params) {
            Address result = null;

            if (params[0] != null) {
                if (Ut.isInternetConnectionAvailable(CoreInfoHandler
                        .getInstance().getMainActivity())) {
                    result = Ut.getRawAddressFromYahoo(CoreInfoHandler
                            .getInstance().getMainActivity(), params[0]
                            .getLatitude(), params[0].getLongitude());

                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Address address) {
            values[8].setText(MapFragment.formatAddress(address));
        }
    }

    private void startBiggerTacho() {
        final Intent intent = new Intent(getActivity(), TachoActivity.class);
        startActivity(intent);

    }
}
