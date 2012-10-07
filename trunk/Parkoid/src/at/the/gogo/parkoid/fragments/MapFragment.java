package at.the.gogo.parkoid.fragments;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.map.C2G_CarOverlay;
import at.the.gogo.parkoid.map.ParkStripIconOverlay;
import at.the.gogo.parkoid.map.ParkingCarOverlay;
import at.the.gogo.parkoid.map.VKPZOverlay;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MapFragment extends LocationListenerFragment {

    private MapView           mapView;
    private MyLocationOverlay whereAmI;
    private MapController     mapController;

    private VKPZOverlay       parkingZonesOverlay;
    private C2G_CarOverlay    c2g_CarOverlay;
    private SensorManager     mSensorManager;
    SensorEventListener       orientationListener;
    private final boolean     mTiltEnabled = true;

    public static MapFragment newInstance() {
        final MapFragment fragment = new MapFragment();

        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        mSensorManager = (SensorManager) getActivity().getSystemService(
                Context.SENSOR_SERVICE);

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        setUpdateVPZ(false);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.map, container, false);

        initializeGUI(view);

        mapView = (MapView) view.findViewById(R.id.map);

        mapController = mapView.getController();
        // mapController.animateTo(point);
        mapController.setZoom(16);

        whereAmI = new MyLocationOverlay(getActivity(), mapView);
        whereAmI.setDrawAccuracyEnabled(true);

        parkingZonesOverlay = new VKPZOverlay(getActivity());

        CoreInfoHolder.getInstance().setParkingCarOverlay(
                ParkingCarOverlay.getNewOverlay(getActivity()));

        CoreInfoHolder.getInstance().setParkStripOverlay(
                ParkStripIconOverlay.getNewOverlay(getActivity()));

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        final boolean showZoneIcons = sharedPreferences.getBoolean(
                "pref_layer_zones", true);

        CoreInfoHolder.getInstance().getParkStripOverlay()
                .setEnabled(showZoneIcons);

        // c2g_CarOverlay = C2G_CarOverlay.overlayFactory(getActivity());
        // c2g_CarOverlay.setEnabled(true);

        // add overlays

        mapView.getOverlays().add(new ScaleBarOverlay(getActivity()));

        mapView.getOverlays().add(parkingZonesOverlay);

        mapView.getOverlays().add(
                CoreInfoHolder.getInstance().getParkStripOverlay());

        mapView.getOverlays().add(
                CoreInfoHolder.getInstance().getParkingCarOverlay());

        mapView.getOverlays().add(whereAmI);

        mapView.setMultiTouchControls(true);
        mapView.postInvalidate();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        toggleOrientationSensor(true);

        resume();
    }

    private void resume() {
        refreshOverlay();

        if (Util.DEBUGMODE) {
            final GeoPoint testCenter = new GeoPoint(48.208336, 16.372223, 0);
            mapController.setCenter(testCenter);
        } else {
            final GeoPoint lastKnownPoint = whereAmI.getMyLocation();
            if (lastKnownPoint != null) {
                mapController.setCenter(whereAmI.getMyLocation());
            } else {
                final Handler handler = new Handler();

                whereAmI.runOnFirstFix(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                CoreInfoHolder.getInstance()
                                        .setLastKnownLocation(
                                                whereAmI.getLastFix());
                                updateLocation();

                                mapController.setCenter(whereAmI
                                        .getMyLocation());

                                // mapView.setBuiltInZoomControls(true);
                            }
                        });
                    }
                });
            }
        }
        whereAmI.enableMyLocation();
        whereAmI.enableFollowLocation();
        whereAmI.enableCompass();

        mapView.setBuiltInZoomControls(true);

    }

    public void refreshOverlay() {

        // refresh from db
        if (CoreInfoHolder.getInstance().getParkStripOverlay() != null) {
            CoreInfoHolder.getInstance().getParkStripOverlay().refresh();
        }
        if (CoreInfoHolder.getInstance().getParkingCarOverlay() != null) {
            CoreInfoHolder.getInstance().getParkingCarOverlay().refresh();
        }
        // just for testing - should only be triggered if zones really NEEEED to
        // be refreshed
        if (parkingZonesOverlay != null) {
            parkingZonesOverlay.refresh();
        }

        if (c2g_CarOverlay != null) {
            c2g_CarOverlay.refresh();
        }
    }

    @Override
    public void onLowMemory() {

        CoreInfoHolder.getInstance().setVKPZCacheList(null);
        System.gc();
        Util.dd("Low Memory: Released VKPZ Cache");

        refreshOverlay();

        super.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        mapView.setBuiltInZoomControls(false);

        toggleOrientationSensor(false);

        whereAmI.disableMyLocation();
        whereAmI.disableFollowLocation();
        whereAmI.disableCompass();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        final boolean showActionBar = sharedPreferences.getBoolean(
                "pref_gui_showactionbar", true);

        inflater.inflate(showActionBar ? R.menu.map_option_menu
                : R.menu.map_option_menu_plain, menu);

        // !! adapt if menu changes !!
        menu.getItem(menu.size() - 1).setEnabled(parkingLotAvailable());
        menu.getItem(menu.size() - 2).setEnabled(parkingLotAvailable());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.mylocation: {
                focusOnCurrentLocation();
                result = true;
                break;
            }
            case R.id.navigateToCar: {
                navigateToCar();
                result = true;
                break;
            }
            default:
                break;
        }
        return result;
    }

    private void focusOnCurrentLocation() {

        final GeoPoint lastKnownPoint = whereAmI.getMyLocation();
        if (lastKnownPoint != null) {
            mapController.setCenter(whereAmI.getMyLocation());
        }
        whereAmI.enableFollowLocation();
    }

    @Override
    public void zoneLevelUpdate(final Boolean inZone) {
        try { // we will use this callback to update our zones
            super.zoneLevelUpdate(inZone);
            parkingZonesOverlay.refresh();
        } catch (final Exception x) {
            Util.e(x.getMessage());
        }
    }

    @Override
    public void pageGetsActivated() {
        super.pageGetsActivated();
        resume();
    }

    @Override
    public void pageGetsDeactivated() {
        super.pageGetsDeactivated();
        pause();
    }

    private void toggleOrientationSensor(final boolean switchOn) {
        if (switchOn) {
            mSensorManager.registerListener(getOrientationListener(),
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            mSensorManager.unregisterListener(getOrientationListener());
        }
    }

    public SensorEventListener getOrientationListener() {
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

                    // mCompassOverlay.setAzimuth(event.values[0]
                    // + (90 * iOrientation));

                    // if (mTiltEnabled) {
                    // mapView.setMapOrientation((int) (event.values[0])
                    // + (90 * iOrientation));
                    // }
                }

                @Override
                public void onAccuracyChanged(final Sensor sensor,
                        final int accuracy) {
                }
            };
        }
        return orientationListener;
    }
}
