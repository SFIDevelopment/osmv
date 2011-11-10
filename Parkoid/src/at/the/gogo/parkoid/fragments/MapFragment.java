package at.the.gogo.parkoid.fragments;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.map.ParkingCarOverlay;
import at.the.gogo.parkoid.map.VKPZOverlay;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;

public class MapFragment extends LocationListenerFragment {

    private MapView           mapView;
    private MyLocationOverlay whereAmI;
    private MapController     mapController;
    private ParkingCarOverlay carParkingOverlay;
    private VKPZOverlay       parkingZonesOverlay;

    public static MapFragment newInstance() {
        final MapFragment fragment = new MapFragment();

        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

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
        carParkingOverlay = ParkingCarOverlay.overlayFactory(getActivity());

        mapView.getOverlays().add(parkingZonesOverlay);
        mapView.getOverlays().add(new ScaleBarOverlay(getActivity()));

        mapView.getOverlays().add(carParkingOverlay);
        mapView.getOverlays().add(whereAmI);

        mapView.setMultiTouchControls(true);

        mapView.postInvalidate();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    private void resume() {
        refreshOverlay();

        if (Util.DEBUGMODE) {
            final GeoPoint testCenter = new GeoPoint(48.208336, 16.372223, 0);
            mapController.setCenter(testCenter);
        }
        whereAmI.enableMyLocation();
        whereAmI.enableFollowLocation();
        whereAmI.enableCompass();
        
        mapView.setBuiltInZoomControls(true);
        
        final Handler handler = new Handler();

        whereAmI.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mapController.setCenter(whereAmI.getMyLocation());
                        mapView.setBuiltInZoomControls(true);
                    }
                });
            }
        });

    }

    public void refreshOverlay() {

        // refresh from db
        carParkingOverlay.refresh();
        // just for testing - should only be triggered if zones really NEEEED to
        // be refreshed
        parkingZonesOverlay.refresh();
    }

    @Override
    public void onLowMemory() {

        CoreInfoHolder.getInstance().setVKPZCacheList(null);
        System.gc();
        Util.dd("Low Memory: Released VKPZ Cache");

        super.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        mapView.setBuiltInZoomControls(false);

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
        whereAmI.enableFollowLocation();
    }

    @Override
    public void updateInfoList(final Boolean inZone) {
        // we will use this callback to update our zones
        parkingZonesOverlay.refresh();
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
}
