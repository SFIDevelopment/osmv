package org.outlander.fragments;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.controller.OpenStreetMapViewController;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.geonames.Toponym;
import org.outlander.R;
import org.outlander.activities.MainActivity;
import org.outlander.activities.PagerActivity;
import org.outlander.constants.DBConstants;
import org.outlander.io.XML.PredefMapsParser;
import org.outlander.model.LocationPoint;
import org.outlander.model.MapMenuItemInfo;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;
import org.outlander.model.Track;
import org.outlander.overlays.BasePointOverlay;
import org.outlander.overlays.CurrentTrackOverlay;
import org.outlander.overlays.DistanceCircleOverlay;
import org.outlander.overlays.ExternalPointOverlay;
import org.outlander.overlays.GeoGridOverlay;
import org.outlander.overlays.LastLocationCircleOverlay;
import org.outlander.overlays.MyLocationOverlay;
import org.outlander.overlays.PoiOverlay;
import org.outlander.overlays.RouteOverlay;
import org.outlander.overlays.TrackOverlay;
import org.outlander.utils.AddressTask;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.FilteringLocationHelper;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.utils.img.ScaleBarDrawable;
import org.outlander.views.BaseCompassView;
import org.outlander.views.SmallCompass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MapFragment extends SherlockFragment implements PageChangeNotifyer {

    public static final int           MAP_CMD_NO            = -1;
    public static final int           MAP_CMD_CURR_LOC      = 0;
    public static final int           MAP_CMD_SHOW_POI      = 1;
    public static final int           MAP_CMD_SHOW_ROUTE    = 2;
    public static final int           MAP_CMD_SHOW_TRACK    = 3;
    public static final int           MAP_CMD_SHOW_SEARCH   = 4;
    public static final int           MAP_CMD_SHOW_EXTERNAL = 5;

    private OpenStreetMapView         mOsmStandardView;
    private boolean                   mDistanceCircles;
    private boolean                   mCompassEnabled;
    private boolean                   mTiltEnabled;
    private boolean                   mShowAddressOrCoords  = false;
    private TextView                  bearingTextView;
    private TextView                  currentPositionTextView;
    private TextView                  screenCenterTextView;
    private TextView                  targetTextView;
    private TextView                  gpsInfoTextView;
    private TextView                  mapInfoTextView;
    LinearLayout                      infoLayer;
    private SharedPreferences         sharedPreferences;
    private ToggleButton              buttonAutoFollow;
    private ToggleButton              buttonCompass;
    // private Button buttonSelectMap;
    private ToggleButton              buttonTilt;
    private ToggleButton              buttonMeasure;
    private Button                    buttonData;
    private Button                    zoomIn;
    private Button                    zoomOut;
    private boolean                   mDrivingDirectionUp;
    private boolean                   mNorthDirectionUp;
    private float                     mLastSpeed, mLastBearing;

    private MyLocationOverlay         mMyLocationOverlay;
    private PoiOverlay                mPoiOverlay;
    private CurrentTrackOverlay       mCurrentTrackOverlay;
    private TrackOverlay              mTrackOverlay;
    private RouteOverlay              mRouteOverlay;
    private DistanceCircleOverlay     mDistanceCircleOverlay;
    private LastLocationCircleOverlay mLastLocOverlay;
    // private SearchResultOverlay mSearchResultOverlay;
    private GeoGridOverlay            mGeoGridOverlay;
    // private ExternalPointOverlay mExternalPointOverlay;
    // private ViennaParkraumOverlay mViennaShortParkingAreaOverlay;

    private QuickAction               quickActionStd;
    private QuickAction               quickActionAddress;
    private QuickAction               quickActionSelected;

    private int                       mPoiIndex             = -1;
    private int                       mRouteIndex           = -1;

    SensorEventListener               orientationListener;
    SensorEventListener               accellerationListener;
    LocationListener                  locationListener;

    BaseCompassView                   compass;
    boolean                           zoomOnSpeed           = true;

    ActionMode                        mMode;

    public static MapFragment newInstance() {

        CoreInfoHandler.getInstance().setMapFragment(new MapFragment());

        return CoreInfoHandler.getInstance().getMapFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        CoreInfoHandler.getInstance().setAutoFollow(sharedPreferences.getBoolean("AutoFollow", true));

        zoomOnSpeed = sharedPreferences.getBoolean("pref_ZoomOnSpeed", true);

        // // coordformat
        // CoreInfoHandler.getInstance().setCoordFormatId(
        // Integer.parseInt(sharedPreferences
        // .getString("pref_coords", "1")));

        final View view = inflater.inflate(R.layout.map, null);

        final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.screen);

        if (mOsmStandardView != null) {
            mOsmStandardView.freeDatabases();
        }
        mOsmStandardView = new OpenStreetMapView(getActivity(), new OpenStreetMapRendererInfo(getResources(), ""));

        mOsmStandardView.setCallbackHandler(new MyCallbackHandler());

        // transparent layer for additional controls
        rl.addView(mOsmStandardView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setHasOptionsMenu(true);

        registerForContextMenu(mOsmStandardView); // for events only
        setupQuickActions();

        {

            compass = new SmallCompass(getActivity());
            compass.setId(12345);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.setMargins(5, 5, 0, 0);
            rl.addView(compass, layoutParams);

            final View zoomView = inflater.inflate(R.layout.zoombtns, null);

            final Button zoomIn = (Button) zoomView.findViewById(R.id.button_zoom_up);

            final Button zoomOut = (Button) zoomView.findViewById(R.id.button_zoom_down);

            final View toolbarView = inflater.inflate(R.layout.toolbar, null);
            final View infobarView = inflater.inflate(R.layout.infobar, null);

            buttonData = (Button) toolbarView.findViewById(R.id.button_data);

            if (Ut.isMultiPane(getActivity())) {
                buttonData.setVisibility(View.GONE);
            }
            else {
                buttonData.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        showData(-1);
                    }
                });
            }

            buttonCompass = (ToggleButton) toolbarView.findViewById(R.id.button_compass);

            buttonAutoFollow = (ToggleButton) toolbarView.findViewById(R.id.button_af);

            buttonTilt = (ToggleButton) toolbarView.findViewById(R.id.button_tilt);

            buttonMeasure = (ToggleButton) toolbarView.findViewById(R.id.button_measure);

            infoLayer = (LinearLayout) infobarView.findViewById(R.id.InfoLayer);
            infoLayer.setClickable(true);

            final ImageView buttonAddress = (ImageView) infobarView.findViewById(R.id.ImageView02);

            buttonAddress.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    mShowAddressOrCoords = !mShowAddressOrCoords;

                    if (infoLayer.getVisibility() == View.VISIBLE) {

                        if ((mShowAddressOrCoords) && (Ut.isInternetConnectionAvailable(CoreInfoHandler.getInstance().getMainActivity()))
                                && (CoreInfoHandler.getInstance().getCurrentLocation() != null)) {

                            // fade out
                            infoLayer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right));
                            infoLayer.setVisibility(View.INVISIBLE);

                            // get address
                            final GetAddressTask getAddress = new GetAddressTask();
                            getAddress.execute(new LocationPoint(CoreInfoHandler.getInstance().getCurrentLocation()));
                            // fade in async !
                        }

                    }
                    else {

                        infoLayer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));
                        infoLayer.setVisibility(View.VISIBLE);
                    }
                }

            });

            layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.setMargins(0, 5, 5, 0);
            rl.addView(infobarView, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            layoutParams.setMargins(0, 0, 15, 65);
            rl.addView(zoomView, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            // layoutParams.addRule(RelativeLayout.ABOVE, zoomIn.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.setMargins(0, 0, 15, 220);

            rl.addView(toolbarView, layoutParams);

            // buttonSelectMap.setOnClickListener(new OnClickListener() {
            // @Override
            // public void onClick(final View v) {
            // // mOsmv.zoomOut();
            // }
            // });
            // buttonSelectMap.setOnLongClickListener(new OnLongClickListener()
            // {
            //
            // @Override
            // public boolean onLongClick(final View v) {
            // return true;
            // }
            // });

            // registerForContextMenu(buttonSelectMap);

            zoomOut.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    if (mOsmStandardView.getZoomLevel() > mOsmStandardView.getRenderer().ZOOM_MINLEVEL) {
                        mOsmStandardView.zoomOut();

                        checkZoomButtons();

                        setTitle();
                    }
                }
            });
            zoomOut.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                    final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    final int zoom = Integer.parseInt(pref.getString("pref_zoomminlevel", "10"));
                    if (zoom > mOsmStandardView.getRenderer().ZOOM_MINLEVEL) {
                        setZoomLevel(zoom);
                        setTitle();

                    }
                    return true;
                }
            });

            zoomIn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    if (mOsmStandardView.getRenderer().ZOOM_MAXLEVEL > mOsmStandardView.getZoomLevel()) {
                        setZoomLevel(mOsmStandardView.getZoomLevel() + 1);
                    }

                    setTitle();
                }
            });
            zoomIn.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                    final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    final int zoom = Integer.parseInt(pref.getString("pref_zoommaxlevel", Integer.toString(mOsmStandardView.getRenderer().ZOOM_MAXLEVEL)));
                    if (zoom > mOsmStandardView.getRenderer().ZOOM_MINLEVEL) {
                        setZoomLevel(zoom);
                        setTitle();
                    }
                    return true;
                }
            });

            buttonMeasure.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    setMeasureDistance(!CoreInfoHandler.getInstance().isMeasureDistance(), true);
                }

            });

            buttonAutoFollow.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    setAutoFollow(true);
                }
            });

            buttonCompass.setOnClickListener(new OnClickListener() {

                // @Override
                @Override
                public void onClick(final View v) {

                    setCompass(!mCompassEnabled, true);

                }
            });

            buttonTilt.setOnClickListener(new OnClickListener() {

                // @Override
                @Override
                public void onClick(final View v) {

                    setTilt(!mTiltEnabled, true);

                }
            });

            currentPositionTextView = (TextView) infobarView.findViewById(R.id.CurrentPos);
            currentPositionTextView.setText("Current location unknown");

            infoLayer.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    CoreInfoHandler.getInstance().incCoordFormatId();

                    updateInfoWindow(true, true, true, true, false);

                }

            });

            infoLayer.setLongClickable(true);
            infoLayer.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {

                    Ut.copyTextToClipboard(getActivity(), (String) currentPositionTextView.getText());

                    Toast.makeText(getActivity(), "position copied to clipboard", Toast.LENGTH_LONG).show();

                    return true;
                }

            });

            screenCenterTextView = (TextView) infobarView.findViewById(R.id.ScreenPos);
            screenCenterTextView.setText("Screencenter:");

            targetTextView = (TextView) infobarView.findViewById(R.id.TargetPos);
            screenCenterTextView.setText("no target defined");

            mapInfoTextView = (TextView) infobarView.findViewById(R.id.MapInfo);
            gpsInfoTextView = (TextView) infobarView.findViewById(R.id.GPSInfo);

            // }

        }

        /* ScaleBarView */
        if (sharedPreferences.getBoolean("pref_showscalebar", true)) {
            final ImageView ivScalebar = new ImageView(getActivity());
            final ScaleBarDrawable dr = new ScaleBarDrawable(getActivity(), mOsmStandardView, Integer.parseInt(sharedPreferences.getString("pref_units", "0")));
            ivScalebar.setImageDrawable(dr);
            final RelativeLayout.LayoutParams scaleParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            scaleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            scaleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rl.addView(ivScalebar, scaleParams);
        }

        mDrivingDirectionUp = sharedPreferences.getBoolean("pref_drivingdirectionup", true);

        mNorthDirectionUp = sharedPreferences.getBoolean("pref_northdirectionup", true);

        mDistanceCircles = sharedPreferences.getBoolean("pref_overlays_distcircle", true);

        registerAccellerationListener();
        registerOrientationListener();
        registerLocationListener();

        restoreUIState();

        return view;
    }

    @Override
    public void onDestroy() {

        deregisterAccellerationListener();
        deregisterOrientationListener();
        deregisterLocationListener();

        // mCurrentTrackOverlay.unbindService();

        try {
            mOsmStandardView.freeDatabases();
        }
        catch (final Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();

    }

    boolean justRun = false;

    private void resume() {

        if (mTrackOverlay != null) {
            mTrackOverlay.setStopDraw(false);
        }

        if (mRouteOverlay != null) {
            mRouteOverlay.setStopDraw(false);
        }
        if (mDistanceCircleOverlay != null) {
            mDistanceCircleOverlay.setStopDraw(!CoreInfoHandler.getInstance().isMeasureDistance() && (mDistanceCircles));
        }
        if (mLastLocOverlay != null) {
            mLastLocOverlay.setStopDraw(false);
        }

        // is there a command to execute ?
        if (CoreInfoHandler.getInstance().getMapCmd() > MapFragment.MAP_CMD_NO) {
            switch (CoreInfoHandler.getInstance().getMapCmd()) {
                case MAP_CMD_CURR_LOC: {
                    setAutoFollow(true, true);
                    setLastKnownLocation();
                    break;
                }
                case MAP_CMD_SHOW_POI: {
                    setAutoFollow(false, true);
                    focusOnPOIPoint();
                    break;
                }
                case MAP_CMD_SHOW_ROUTE: {
                    setAutoFollow(false, true);
                    // mRouteOverlay.refreshRoute();

                    break;
                }
                case MAP_CMD_SHOW_TRACK: {
                    setAutoFollow(false, true);
                    // mTrackOverlay.refreshTrack();
                    break;
                }
                case MAP_CMD_SHOW_SEARCH: {
                    setAutoFollow(false, true);
                    focusOnTopoSearchResult();
                    break;
                }
                case MAP_CMD_SHOW_EXTERNAL: {
                    setAutoFollow(false, true);
                    focusOnExternalResult();
                    break;
                }
            }
            justRun = true;
        }
        else {
            if (justRun) {
                justRun = false;
            }
            else {
                // otherwise we just restore our old position.......
                final SharedPreferences uiState = getActivity().getPreferences(0);
                final int lon = uiState.getInt("Longitude", -1);
                final int lat = uiState.getInt("Latitude", -1);
                @SuppressWarnings("unused")
                final int zoom = uiState.getInt("ZoomLevel", 16);

                final boolean compassEnabled = uiState.getBoolean("CompassEnabled", true);
                final boolean autofollowEnabled = uiState.getBoolean("AutoFollow", true);
                final boolean tiltEnabled = uiState.getBoolean("TiltEnabled", true);
                final boolean measureDistance = uiState.getBoolean("MeasureDistance", true);

                CoreInfoHandler.getInstance().setUseCurrentTarget(uiState.getBoolean("use_Target", true));

                if ((lat != -1) && (lon != -1)) {
                    mOsmStandardView.getController().animateTo(lat, lon, OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED);
                }

                setTilt(tiltEnabled, false);
                setMeasureDistance(measureDistance, false);
                setAutoFollow(autofollowEnabled, true);
                setCompass(compassEnabled, false);
                checkZoomButtons();
            }
        }

    }

    private void focusOnPOIPoint() {
        if (CoreInfoHandler.getInstance().getCurrentPoiPoint() != null) {
            mOsmStandardView.getController().animateTo(CoreInfoHandler.getInstance().getCurrentPoiPoint(),
                    OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED, OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
                    OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
        }
        if (mOsmStandardView.getZoomLevel() < mOsmStandardView.getRenderer().ZOOM_MAXLEVEL) {
            setZoomLevel(mOsmStandardView.getRenderer().ZOOM_MAXLEVEL);
        }
        CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_NO);
    }

    private void focusOnTrack() {
        if (CoreInfoHandler.getInstance().getMapCmd() == MapFragment.MAP_CMD_SHOW_TRACK) {
            // mTrackOverlay.setSelectTrackById(CoreInfoHandler.getInstance()
            // .getCurrentTrackId());

            final Track track = mTrackOverlay.getTrack();
            if (track != null) {
                mOsmStandardView.getController().animateTo(track.getFirstGeoPoint(), OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
                        OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
                CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_NO);
            }
        }
    }

    private void focusOnRoute() {

        if (CoreInfoHandler.getInstance().getMapCmd() == MapFragment.MAP_CMD_SHOW_ROUTE) {
            mRouteOverlay.setSelectRouteById(CoreInfoHandler.getInstance().getCurrentRouteId());

            final Route route = mRouteOverlay.getSelectedRoute();
            if (route != null) {
                mOsmStandardView.getController().animateTo(route.getGeoPoints().get(0), OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
                        OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
                CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_NO);
            }
        }
    }

    private void focusOnTopoSearchResult() {

        if (CoreInfoHandler.getInstance().getMapCmd() == MapFragment.MAP_CMD_SHOW_SEARCH) {
            final Toponym toponym = CoreInfoHandler.getInstance().getCurrentToponym();

            if (toponym != null) {

                final GeoPoint point = new GeoPoint((int) (toponym.getLatitude() * 1E6), (int) (toponym.getLongitude() * 1E6));

                moveToTopoSearchResult(point, toponym.getName(), 17);
            }
            CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_NO);
        }
    }

    private void focusOnExternalResult() {
        if (CoreInfoHandler.getInstance().getMapCmd() == MapFragment.MAP_CMD_SHOW_EXTERNAL) {

            final ExternalPointOverlay overlay = CoreInfoHandler.getInstance().getExternalPointOverlay();

            if (overlay != null) {

                final GeoPoint point = overlay.getPoiPoint(0).getGeoPoint();

                String title = overlay.getPoiPoint(0).getTitle();
                if (title == null) {
                    title = "Target";
                }

                moveToTopoSearchResult(point, title, 17);
            }
            CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_NO);
        }

    }

    private void pause() {
        final SharedPreferences uiState = getActivity().getPreferences(0);
        final SharedPreferences.Editor editor = uiState.edit();
        editor.putString("MapName", mOsmStandardView.getRenderer().ID);
        editor.putInt("Latitude", mOsmStandardView.getMapCenterLatitudeE6());
        editor.putInt("Longitude", mOsmStandardView.getMapCenterLongitudeE6());
        editor.putInt("ZoomLevel", mOsmStandardView.getZoomLevel());
        editor.putBoolean("CompassEnabled", mCompassEnabled);
        editor.putBoolean("TiltEnabled", mTiltEnabled);
        editor.putBoolean("ShowAddress", mShowAddressOrCoords);

        editor.putBoolean("AutoFollow", CoreInfoHandler.getInstance().isAutoFollow());
        editor.putBoolean("MeasureDistance", CoreInfoHandler.getInstance().isMeasureDistance());

        editor.putString("app_version", Ut.getAppVersion(getActivity()));

        final GeoPoint targetPoint = CoreInfoHandler.getInstance().getCurrentTarget();
        if (targetPoint != null) {
            editor.putInt("TargetLatitude", targetPoint.getLatitudeE6());
            editor.putInt("TargetLongitude", targetPoint.getLongitudeE6());
        }
        else {
            editor.remove("TargetLatitude");
            editor.remove("TargetLongitude");
        }

        editor.putBoolean("use_Target", CoreInfoHandler.getInstance().isUseCurrentTarget());

        if (mPoiOverlay != null) {
            editor.putInt("curShowPoiId", mPoiOverlay.getTapIndex());
        }
        else {
            editor.remove("curShowPoiId");
        }
        editor.commit();

        if (mTrackOverlay != null) {
            mTrackOverlay.setStopDraw(true);
        }
        if (mRouteOverlay != null) {
            mRouteOverlay.setStopDraw(true);
        }
        if (mDistanceCircleOverlay != null) {
            mDistanceCircleOverlay.setStopDraw(true);
        }
        if (mLastLocOverlay != null) {
            mLastLocOverlay.setStopDraw(true);
        }
    }

    private LocationListener getLocationListener() {
        if (locationListener == null) {
            locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(final String provider, final int status, final Bundle extras) {
                }

                @Override
                public void onProviderEnabled(final String provider) {
                }

                @Override
                public void onProviderDisabled(final String provider) {
                }

                @Override
                public void onLocationChanged(final Location location) {
                    locationChanged(location);
                }

            };
        }
        return locationListener;
    }

    int lastZoomLevelb4Move = 0;

    private void locationChanged(final Location location) {

        if (FilteringLocationHelper.useLocation(location)) {

            if (mLastLocOverlay != null) {
                mLastLocOverlay.refresh();
            }
            if (location.hasSpeed()) {
                mLastSpeed = location.getSpeed();

                if (zoomOnSpeed == true) {
                    int changeZoom = 0;
                    if ((mLastSpeed > 0.5) && (CoreInfoHandler.getInstance().isAutoFollow())) {

                        final int speedkmh = (int) (mLastSpeed * 3.6);
                        if (speedkmh > 3) {
                            if ((speedkmh > 15) && (speedkmh < 40)) {
                                changeZoom = mOsmStandardView.getRenderer().ZOOM_MAXLEVEL;
                            }
                            else if ((speedkmh > 40) && (speedkmh < 80)) {
                                changeZoom = mOsmStandardView.getRenderer().ZOOM_MAXLEVEL - 1;
                                // } else if (speedkmh < 60) {
                                // changeZoom =
                                // mOsmStandardView.getRenderer().ZOOM_MAXLEVEL
                                // - 2;
                                // } else if (speedkmh < 90) {
                                // changeZoom =
                                // mOsmStandardView.getRenderer().ZOOM_MAXLEVEL
                                // - 3;
                            }
                            else // if (speedkmh > 90)
                            {
                                changeZoom = mOsmStandardView.getRenderer().ZOOM_MAXLEVEL - 2;
                            }
                        }
                        if (changeZoom > 0) {
                            if (lastZoomLevelb4Move == 0) {
                                lastZoomLevelb4Move = mOsmStandardView.getZoomLevel();
                            }
                            if (changeZoom != mOsmStandardView.getZoomLevel()) {
                                mOsmStandardView.setZoomLevel(changeZoom);
                            }
                        }

                    }
                    else {
                        if (lastZoomLevelb4Move > 0) {
                            mOsmStandardView.setZoomLevel(lastZoomLevelb4Move);
                            lastZoomLevelb4Move = 0;
                        }
                    }
                }
            }

            if (mOsmStandardView.getVisibility() == View.VISIBLE) {
                if (CoreInfoHandler.getInstance().isAutoFollow()) {
                    if (mDrivingDirectionUp) {
                        if (location.getSpeed() > 0.5) {
                            mOsmStandardView.setBearing(location.getBearing());
                        }
                    }

                    mOsmStandardView.getController().animateTo(TypeConverter.locationToGeoPoint(location),
                            OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED, OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
                            OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
                }
                else {
                    mOsmStandardView.postInvalidate();
                }

                updateInfoWindow(true, false, true, false, true);
            }
        }
    }

    private void updateInfoWindow(final boolean updatefield1, final boolean updatefield2, final boolean updatefield3, final boolean updatefield4,
            final boolean updatefield5) {

        if (updatefield1) { // current
            if ((currentPositionTextView != null) && (!mShowAddressOrCoords)) {
                if (CoreInfoHandler.getInstance().getCurrentLocation() != null) {
                    // GeoPoint.FORMAT_DM
                    currentPositionTextView.setText("L "
                            + GeoMathUtil.formatLocation(CoreInfoHandler.getInstance().getCurrentLocation(), CoreInfoHandler.getInstance().getCoordFormatId()));
                }
                else {
                    if (currentPositionTextView != null) {
                        currentPositionTextView.setText(R.string.info_currentLocation);
                    }
                }
            }
        }
        if (updatefield2) { // screen
            if (screenCenterTextView != null) {
                screenCenterTextView.setText("C "
                        + GeoMathUtil.formatGeoPoint(mOsmStandardView.getMapCenter(), CoreInfoHandler.getInstance().getCoordFormatId()));
            }
        }
        if (updatefield4) { // target
            if (CoreInfoHandler.getInstance().getCurrentTarget() != null) {
                if (targetTextView != null) {
                    targetTextView.setText("T "
                            + GeoMathUtil.formatGeoPoint(CoreInfoHandler.getInstance().getCurrentTarget(), CoreInfoHandler.getInstance().getCoordFormatId()));
                }
            }
            else {
                if (targetTextView != null) {
                    targetTextView.setText(R.string.info_TargetLocation);
                }
            }
        }

        if (updatefield5) { // info
            if (CoreInfoHandler.getInstance().getCurrentLocation() != null) {
                if (gpsInfoTextView != null) {
                    final Location loc = CoreInfoHandler.getInstance().getCurrentLocation();

                    // altitude
                    String info = "↑"
                            + (loc.hasAltitude() ? GeoMathUtil.twoDecimalFormat.format(loc.getAltitude()) : "?")
                            + " "
                            + CoreInfoHandler.getInstance().getMainActivity().getResources().getStringArray(R.array.distance_unit_title)[CoreInfoHandler
                                    .getInstance().getDistanceUnitFormatId()] + "   ";

                    // speed
                    info += (loc.hasSpeed() ? GeoMathUtil.twoDecimalFormat.format(GeoMathUtil.convertSpeed(loc.getSpeed(), CoreInfoHandler.getInstance()
                            .getSpeedFormatId())) : "?")
                            + " "
                            + CoreInfoHandler.getInstance().getMainActivity().getResources().getStringArray(R.array.speed_unit_title)[CoreInfoHandler
                                    .getInstance().getSpeedFormatId()];
                    // // acc
                    // info += "  +/- "
                    // + (loc.hasAccuracy() ? GeoMathUtil.twoDecimalFormat
                    // .format(loc.getAccuracy()) : "?")
                    // + " "
                    // + CoreInfoHandler
                    // .getInstance()
                    // .getMainActivity()
                    // .getResources()
                    // .getStringArray(R.array.distance_unit_title)[CoreInfoHandler
                    // .getInstance().getDistanceUnitFormatId()];

                    // dist
                    if (CoreInfoHandler.getInstance().getCurrentTarget() != null) {

                        final GeoPoint target = CoreInfoHandler.getInstance().getCurrentTarget();

                        info += "  ↔ "
                                + GeoMathUtil.getHumanDistanceString(
                                        GeoMathUtil.distanceTo(loc.getLatitude(), loc.getLongitude(), target.getLatitude(), target.getLongitude()),
                                        CoreInfoHandler.getInstance().getDistanceUnitFormatId());
                    }

                    gpsInfoTextView.setText(info);
                }
            }
        }

        if (updatefield3) {
            if (mapInfoTextView != null) {

                String newTitle = mOsmStandardView.getRenderer().NAME + "(" + (1 + mOsmStandardView.getZoomLevel()) + ")";

                final Location location = CoreInfoHandler.getInstance().getCurrentLocation();
                if ((location != null) && (location.hasAccuracy())) {
                    newTitle += "  +/- "
                            + (location.hasAccuracy() ? GeoMathUtil.getHumanDistanceString(location.getAccuracy(), CoreInfoHandler.getInstance()
                                    .getDistanceUnitFormatId()) : "?")
                            + " "
                            + CoreInfoHandler.getInstance().getMainActivity().getResources().getStringArray(R.array.distance_unit_title)[CoreInfoHandler
                                    .getInstance().getDistanceUnitFormatId()];
                }
                mapInfoTextView.setText(newTitle);

            }
        }
    }

    private SensorEventListener getAccelerationListener() {
        if (accellerationListener == null) {
            accellerationListener = new SensorEventListener() {

                @Override
                public void onSensorChanged(final SensorEvent event) {
                    // TODO Auto-generated method stub

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

                private int iOrientation = -1;

                @Override
                public void onSensorChanged(final SensorEvent event) {
                    sensorChanged(event);

                }

                float lastBearing = -1;

                private void sensorChanged(final SensorEvent event) {

                    if (iOrientation < 0) {
                        iOrientation = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                    }

                    final float updatedBearing = updateBearing(event.values[0]) + (90 * iOrientation);

                    if (updatedBearing != lastBearing) {
                        compass.setBearing(updatedBearing);
                        if (mTiltEnabled) {
                            if (mNorthDirectionUp) {
                                if ((mDrivingDirectionUp == false) || (mLastSpeed == 0)) {
                                    mOsmStandardView.setBearing(updatedBearing);
                                    lastBearing = updatedBearing;
                                    mOsmStandardView.postInvalidate();
                                }
                            }
                        }
                    }

                    if (compass.getVisibility() == View.VISIBLE) {
                        compass.postInvalidate();
                    }
                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {

                }
            };
        }
        return orientationListener;
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

    private void setCompass(final boolean enabled, final boolean showToast) {
        mCompassEnabled = enabled;
        buttonCompass.setChecked(enabled);

        if (mCompassEnabled) {
            compass.enableCompass();
        }
        else {
            compass.disableCompass();
        }

        mOsmStandardView.invalidate();
        if (showToast) {
            Toast.makeText(getActivity(), (mCompassEnabled) ? R.string.compass_enabled : R.string.compass_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void setTilt(final boolean enabled, final boolean showToast) {
        mTiltEnabled = enabled;
        buttonTilt.setChecked(enabled);

        // JF: reset north up
        if (!mTiltEnabled) {
            mOsmStandardView.setBearing(0);
            compass.setBearing(0);
            mOsmStandardView.invalidate();
        }

        if (showToast) {
            Toast.makeText(getActivity(), (mTiltEnabled) ? R.string.tilt_enabled : R.string.tilt_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    public void setBearing(final String bearing) {
        bearingTextView.setText(bearing);
    }

    public void setAutoFollow(final boolean autoFollow) {
        setAutoFollow(autoFollow, false);
    }

    public void setAutoFollow(final boolean autoFollow, final boolean supressToast) {

        CoreInfoHandler.getInstance().setAutoFollow(autoFollow);

        if (buttonAutoFollow != null) {

            buttonAutoFollow.setChecked(autoFollow);

            if (autoFollow) {
                if (!supressToast) {
                    Toast.makeText(getActivity(), (autoFollow) ? R.string.auto_follow_enabled : R.string.auto_follow_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (autoFollow) {
            setLastKnownLocation();
        }
    }

    private void setMeasureDistance(final boolean enabled, final boolean showToast) {
        CoreInfoHandler.getInstance().setMeasureDistance(enabled);

        if (mDistanceCircleOverlay != null) {
            mDistanceCircleOverlay.setStopDraw(!enabled);
        }

        buttonMeasure.setChecked(enabled);

        mOsmStandardView.invalidate();

        if (showToast) {
            Toast.makeText(getActivity(), (enabled) ? R.string.measure_enabled : R.string.measure_disabled, Toast.LENGTH_SHORT).show();
        }

    }

    private OpenStreetMapRendererInfo getRendererInfo(final Resources aRes, final SharedPreferences aPref, final String aName) {

        final OpenStreetMapRendererInfo RendererInfo = new OpenStreetMapRendererInfo(aRes, aName);

        RendererInfo.LoadFromResources(aName, PreferenceManager.getDefaultSharedPreferences(getActivity()));

        return RendererInfo;
    }

    private void restoreUIState() {
        final SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);

        final OpenStreetMapRendererInfo rendererInfo = getRendererInfo(getResources(), settings, settings.getString("MapName", "mapnik"));

        if (!mOsmStandardView.setRenderer(rendererInfo)) {
            mOsmStandardView.setRenderer(getRendererInfo(getResources(), settings, "mapnik"));
        }
        // if (osmvMinimap != null) {
        // osmvMinimap.setRenderer(mOsmStandardView.getRenderer());
        // }

        initializeOverlays();

        setZoomLevel(settings.getInt("ZoomLevel", 10));
        mOsmStandardView.setMapCenter(settings.getInt("Latitude", 48), settings.getInt("Longitude", 16));

        final int targetLat = settings.getInt("TargetLatitude", 0);
        final int targetLon = settings.getInt("TargetLongitude", 0);

        if ((targetLat != 0) && (targetLon != 0)) {
            final GeoPoint target = new GeoPoint(targetLat, targetLon);
            CoreInfoHandler.getInstance().setCurrentTarget(target);
        }

        mCompassEnabled = settings.getBoolean("CompassEnabled", false);
        setCompass(mCompassEnabled, false);

        CoreInfoHandler.getInstance().setAutoFollow(settings.getBoolean("AutoFollow", true));

        buttonAutoFollow.setPressed(CoreInfoHandler.getInstance().isAutoFollow());

        // tilt
        mTiltEnabled = settings.getBoolean("TiltEnabled", false);
        setTilt(mTiltEnabled, false);

        setMeasureDistance(settings.getBoolean("MeasureDistance", false), false);

        mShowAddressOrCoords = settings.getBoolean("ShowAddress", false);

        setTitle();

        if (mPoiOverlay != null) {
            mPoiOverlay.setTapIndex(settings.getInt("curShowPoiId", -1));
        }

    }

    private void initializeOverlays() {

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mOsmStandardView.getOverlays().clear();

        // final boolean showViennaParking =
        // pref.getBoolean("pref_overlays_vkpz",
        // false);
        //
        // if (showViennaParking) {
        // if (mViennaShortParkingAreaOverlay == null) {
        //
        // mViennaShortParkingAreaOverlay = new ViennaParkraumOverlay(
        // getActivity(), CoreInfoHandler.getInstance()
        // .getPoiManager(getActivity()));
        // }
        // mOsmv.getOverlays().add(mViennaShortParkingAreaOverlay);
        // }

        final boolean showGrid = pref.getBoolean("pref_overlays_geogrid", false);

        if (showGrid) {
            if (mGeoGridOverlay == null) {
                mGeoGridOverlay = new GeoGridOverlay(getActivity());
            }
            mGeoGridOverlay.setStopDraw(!showGrid);
            mOsmStandardView.getOverlays().add(mGeoGridOverlay);
        }

        final boolean showDistanceCircle = pref.getBoolean("pref_overlays_distcircle", true);

        if (showDistanceCircle) {
            if (mDistanceCircleOverlay == null) {
                mDistanceCircleOverlay = new DistanceCircleOverlay(getActivity());
            }
            mDistanceCircleOverlay.setStopDraw(!showDistanceCircle && CoreInfoHandler.getInstance().isMeasureDistance());
            mOsmStandardView.getOverlays().add(mDistanceCircleOverlay);
        }

        final boolean showLastLocations = pref.getBoolean("pref_overlays_lastloc", true);
        if (showLastLocations) {
            if (mLastLocOverlay == null) {
                mLastLocOverlay = new LastLocationCircleOverlay(getActivity());
            }
            mLastLocOverlay.setStopDraw(!showLastLocations);
            mOsmStandardView.getOverlays().add(mLastLocOverlay);
        }

        /* SingleLocation-Overlay */

        if (mMyLocationOverlay == null) {
            mMyLocationOverlay = new MyLocationOverlay(getActivity());
        }
        mOsmStandardView.getOverlays().add(mMyLocationOverlay);

        if (CoreInfoHandler.getInstance().getExternalPointOverlay() == null) {
            CoreInfoHandler.getInstance().setExternalPointOverlay(new ExternalPointOverlay(getActivity(), null));
        }
        mOsmStandardView.getOverlays().add(CoreInfoHandler.getInstance().getExternalPointOverlay());

        if (mTrackOverlay == null) {
            mTrackOverlay = new TrackOverlay(getActivity());
            CoreInfoHandler.getInstance().setTrackOverlay(mTrackOverlay);
        }
        mOsmStandardView.getOverlays().add(mTrackOverlay);

        if (mRouteOverlay == null) {
            mRouteOverlay = new RouteOverlay(getActivity(), new BasePointOverlay.OnItemTapListener<PoiPoint>() {

                @Override
                public boolean onItemTap(final int aIndex, final PoiPoint aItem) {

                    mPoiIndex = -1;
                    mRouteIndex = aIndex;

                    if (mRouteOverlay.getTapIndex() < 0) // no display
                    {
                        final AsyncTask<PoiPoint, Void, PoiPoint> task = new AsyncTask<PoiPoint, Void, PoiPoint>() {

                            @Override
                            protected void onPostExecute(final PoiPoint result) {

                                final QuickAction quickAction = quickActionSelected;
                                quickAction.show(mOsmStandardView, mOsmStandardView.mTouchDownX, mOsmStandardView.mTouchDownY);
                            }

                            @Override
                            protected PoiPoint doInBackground(final PoiPoint... params) {

                                return params[0];
                            }
                        };

                        task.execute(aItem);
                    }
                    return true;
                }

                @Override
                public boolean onItemLongPress(final int aIndex, final PoiPoint aItem) {
                    // TODO modify route
                    return false;
                }
            });

            CoreInfoHandler.getInstance().setRouteOverlay(mRouteOverlay);

        }
        mOsmStandardView.getOverlays().add(mRouteOverlay);

        if (mCurrentTrackOverlay == null) {
            mCurrentTrackOverlay = new CurrentTrackOverlay(MapFragment.this.getActivity(), mOsmStandardView);
        }
        mOsmStandardView.getOverlays().add(mCurrentTrackOverlay);

        // pref.getBoolean("pref_hidepoi", false) ignored by now !!
        if (mPoiOverlay == null) {
            mPoiOverlay = new PoiOverlay(MapFragment.this.getActivity(),

            new BasePointOverlay.OnItemTapListener<PoiPoint>() {

                @Override
                public boolean onItemTap(final int aIndex, final PoiPoint aItem) {

                    mRouteIndex = -1; // ????
                    mPoiIndex = aIndex;
                    if (mPoiOverlay.getTapIndex() < 0) // no display
                    {
                        mMode = getSherlockActivity().startActionMode(new ActionModePOI());

                        final AsyncTask<PoiPoint, Void, PoiPoint> task = new AsyncTask<PoiPoint, Void, PoiPoint>() {

                            @Override
                            protected void onPostExecute(final PoiPoint result) {

                                final QuickAction quickAction = quickActionSelected;
                                quickAction.show(mOsmStandardView, mOsmStandardView.mTouchDownX, mOsmStandardView.mTouchDownY);

                                // Toast.makeText(getActivity(), R.string.NYI,
                                // Toast.LENGTH_LONG).show();
                            }

                            @Override
                            protected PoiPoint doInBackground(final PoiPoint... params) {

                                return params[0];
                            }
                        };

                        task.execute(aItem);
                    }
                    return true;
                }

                @Override
                public boolean onItemLongPress(final int aIndex, final PoiPoint aItem) {
                    // TODO Auto-generated method stub
                    return false;
                }
            }, pref);

        }
        CoreInfoHandler.getInstance().setPoiOverlay(mPoiOverlay);
        mOsmStandardView.getOverlays().add(mPoiOverlay);

    }

    private void setupQuickActions() {
        quickActionStd = new QuickAction(getActivity());
        quickActionSelected = new QuickAction(getActivity());

        setupQuickAction(quickActionStd, false);
        setupQuickAction(quickActionSelected, true);
    }

    private void setupQuickAction(final QuickAction quickAction, final boolean itemSelected) {

        // ------
        final ActionItem address = new ActionItem();

        address.setTitle(getResources().getString(R.string.menu_getAddress));
        address.setIcon(getResources().getDrawable(R.drawable.menu_address));
        quickAction.addActionItem(address);
        // ------
        // final ActionItem item = new ActionItem();
        //
        // item.setTitle(getResources().getString(R.string.menu_vkpz));
        // item.setIcon(getResources().getDrawable(R.drawable.menu_wikipedia));
        //
        // quickAction.addActionItem(item);

        // ------
        final ActionItem shareLocation = new ActionItem();

        shareLocation.setTitle(getResources().getString(R.string.menu_shareLocation));
        shareLocation.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(shareLocation);

        // ------
        final ActionItem wikipedia = new ActionItem();

        wikipedia.setTitle(getResources().getString(R.string.menu_getWikiPedia));
        wikipedia.setIcon(getResources().getDrawable(R.drawable.menu_wikipedia));

        quickAction.addActionItem(wikipedia);

        // ------
        final ActionItem addWaypoint = new ActionItem();

        addWaypoint.setTitle(getResources().getString(R.string.menu_addpoi));
        addWaypoint.setIcon(getResources().getDrawable(R.drawable.menu_add));
        quickAction.addActionItem(addWaypoint);

        if (itemSelected) { // ------
            final ActionItem editWaypoint = new ActionItem();

            editWaypoint.setTitle(getResources().getString(R.string.menu_edit));
            editWaypoint.setIcon(getResources().getDrawable(R.drawable.menu_edit));
            quickAction.addActionItem(editWaypoint);

            // ------
            final ActionItem deleteWaypoint = new ActionItem();

            deleteWaypoint.setTitle(getResources().getString(R.string.menu_delete));
            deleteWaypoint.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));
            quickAction.addActionItem(deleteWaypoint);

            final ActionItem navigateWaypoint = new ActionItem();

            deleteWaypoint.setTitle(getResources().getString(R.string.menu_goto));
            // deleteWaypoint.setIcon(getResources().getDrawable(
            // R.drawable.));
            quickAction.addActionItem(navigateWaypoint);

        }

        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final int pos) {
                if (pos == 0) {
                    // handleContextItemSelected(R.id.menu_show);//
                    // WKPZ!
                    handleContextItemSelected(R.id.menu_getAddress);
                }
                else if (pos == 1) {
                    handleContextItemSelected(R.id.menu_shareLocation);
                }
                else if (pos == 2) {
                    handleContextItemSelected(R.id.menu_getWikiPedia);
                }
                else if (pos == 3) {
                    handleContextItemSelected(R.id.menu_addpoi);
                }
                else if (pos == 4) {
                    handleContextItemSelected(R.id.menu_editpoi);
                }
                else if (pos == 5) {
                    handleContextItemSelected(R.id.menu_deletepoi);
                }
                else if (pos == 6) {
                    handleContextItemSelected(R.id.menu_gotopoi);
                }
                else if (pos == 7) {

                }
            }
        });

    }

    private void handleContextItemSelected(final int id) {

        switch (id) {
            case R.id.menu_addpoi: {
                final GeoPoint point = mOsmStandardView.getTouchDownPoint();

                addPoi(point);

                break;
            }
            case R.id.menu_editpoi: { // edit poi or route
                if (mPoiIndex > -1) {
                    final PoiPoint poi = mPoiOverlay.getPoiPoint(mPoiIndex);

                    editPoi(poi);

                }
                else if (mRouteIndex > -1) {

                    editRoute();
                }
                break;
            }
            case R.id.menu_deletepoi: { // delete poi or route

                if (mPoiIndex > -1) {
                    deletePoi();
                }
                break;
            }
            case R.id.menu_hide: {
                if (mPoiIndex > -1) {
                    hidePoi();
                }
                break;
            }
            case R.id.menu_getWikiPedia: {
                final GeoPoint point = mOsmStandardView.getTouchDownPoint();

                searchWiki(point);

                break;
            }
            case R.id.menu_getAddress: {

                showAddress(mOsmStandardView.mTouchDownX, mOsmStandardView.mTouchDownY);
                break;
            }
            case R.id.menu_shareLocation: {

                PoiPoint poi = null;

                if (mPoiIndex > -1) { // share poi
                    poi = mPoiOverlay.getPoiPoint(mPoiIndex);
                }
                if (mRouteIndex > -1) { // share routepoi
                    poi = mRouteOverlay.getSelectedRoute().getPoints().get(mRouteIndex);
                }
                if (poi == null) {
                    final GeoPoint gpoint = getTouchdownPoint();

                    poi = new PoiPoint("Target", "touchdownpoint", gpoint, -1);
                }
                if (poi != null) {
                    shareLocation(poi);
                }

                break;
            }
            case R.id.menu_gotopoi: {
                PoiPoint poi1 = (mPoiIndex > -1) ? mPoiOverlay.getPoiPoint(mPoiIndex) : mRouteOverlay.getSelectedRoute().getPoints().get(mRouteIndex);

                if (poi1 == null) {

                    CoreInfoHandler.getInstance().getDBManager(getActivity()).deletePoisOfCategoryTarget();

                    final GeoPoint gpoint = getTouchdownPoint();
                    poi1 = new PoiPoint("Target", "touchdownpoint", gpoint, -1);
                    poi1.setCategoryId(DBConstants.POI_CATEGORY_TARGET);

                    CoreInfoHandler.getInstance().getDBManager(getActivity()).updatePoi(poi1);
                }

                gotoPoint(poi1);

                break;
            }
        }
        mRouteIndex = -1;
        mPoiIndex = -1;
    }

    private void gotoCoords() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final GeoPoint center = CoreInfoHandler.getInstance().getCurrentMapCenter();

        final DialogFragment newFragment = CoordsDialogFragment.newInstance(getActivity(), center, "goto Location", R.string.dialogTitleRouting);
        newFragment.show(ft, "dialog");

    }

    private void gotoPoint(final PoiPoint point) {

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final DialogFragment newFragment = RoutingDialogFragment.newInstance(point.getTitle(), point.getDescr(), point.getGeoPoint().getLatitude(), point
                .getGeoPoint().getLongitude(), R.string.dialogTitleRouting);
        newFragment.show(ft, "dialog");

    }

    private void shareLocation(final PoiPoint poi) {
        if (poi != null) {
            final Intent intent = Ut.shareLocation(poi.getGeoPoint().getLatitude(), poi.getGeoPoint().getLongitude(), poi.getTitle() + "\n" + poi.getDescr(),
                    getActivity());

            if (intent != null) {
                startActivity(intent);
            }

            // Toast.makeText(this, R.string.NYI,
            // Toast.LENGTH_LONG).show();
        }
        else { // share directly from map
            final OpenStreetMapViewProjection pj = mOsmStandardView.getProjection();
            final GeoPoint gpoint = pj.fromPixels(mOsmStandardView.mTouchDownX, mOsmStandardView.mTouchDownY, mOsmStandardView.getBearing());

            final Intent intent = Ut.shareLocation(gpoint.getLatitude(), gpoint.getLongitude(), "Map Location", getActivity());

            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    private void searchWiki(final GeoPoint point) {
        CoreInfoHandler.getInstance().setCurrentSearchPoint(point);

        // final SharedPreferences uiState = getActivity().getPreferences(0);
        // final SharedPreferences.Editor editor = uiState.edit();

        final int page = FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_WIKI);

        // editor.putInt("PageInFlipper", page);
        // editor.commit();

        if (Ut.isMultiPane(getActivity())) {
            if (CoreInfoHandler.getInstance().getViewPager() != null) {
                CoreInfoHandler.getInstance().getViewPager().setCurrentItem(page, true);
            }
        }
        else {
            showData(page);
        }

    }

    private void hidePoi() {
        final PoiPoint poi = mPoiOverlay.getPoiPoint(mPoiIndex);
        poi.setHidden(true);
        CoreInfoHandler.getInstance().getDBManager(MapFragment.this.getActivity()).updatePoi(poi);
        mOsmStandardView.invalidate();
    }

    private void deletePoi() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning_delete_all_routes).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int id) {

                final PoiPoint poiPoint = mPoiOverlay.getPoiPoint(mPoiIndex);
                if (poiPoint != null) {
                    CoreInfoHandler.getInstance().getDBManager(MapFragment.this.getActivity()).deletePoi(poiPoint.getId());
                    mOsmStandardView.invalidate();
                }
                else {
                    Ut.e(" failed to retrieve PoiPoint for deletetion");
                }
                // TODO: remove from overlay

                dialog.dismiss();

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void addPoi(final GeoPoint point) {
        final DialogFragment newFragment = PoiDialogFragment.newInstance(-1, "new POI", "", point.getLatitude(), point.getLongitude(), R.string.dialogTitlePOI);
        showDialog(newFragment);
    }

    private void editPoi(final PoiPoint poi) {
        final DialogFragment dialogFragment = PoiDialogFragment.newInstance(poi.getId(), poi.getTitle(), poi.getDescr(), poi.getGeoPoint().getLatitude(), poi
                .getGeoPoint().getLongitude(), R.string.dialogTitlePOI);
        showDialog(dialogFragment);

        mOsmStandardView.invalidate();

    }

    private void editRoute() {
        final DialogFragment dialogFragment = RouteDialogFragment.newInstance(mRouteOverlay.getSelectedRoute().getId(), mRouteOverlay.getSelectedRoute()
                .getName(), mRouteOverlay.getSelectedRoute().getDescr(), R.string.dialogTitleRoute);
        showDialog(dialogFragment);

    }

    private void setLastKnownLocation() {
        final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        final Location loc1 = lm.getLastKnownLocation("gps");
        final Location loc2 = lm.getLastKnownLocation("network");

        final boolean boolGpsEnabled = lm.isProviderEnabled(OpenStreetMapConstants.GPS);
        final boolean boolNetworkEnabled = lm.isProviderEnabled(OpenStreetMapConstants.NETWORK);
        String str = "";
        Location loc = null;

        if ((loc1 == null) && (loc2 != null)) {
            loc = loc2;
        }
        else if ((loc1 != null) && (loc2 == null)) {
            loc = loc1;
        }
        else if ((loc1 == null) && (loc2 == null)) {
            loc = null;
        }
        else {
            loc = loc1.getTime() > loc2.getTime() ? loc1 : loc2;
        }

        if (boolGpsEnabled) {
        }
        else if (boolNetworkEnabled) {
            str = getString(R.string.message_gpsdisabled);
        }
        else if (loc == null) {
            str = getString(R.string.message_locationunavailable);
        }
        else {
            str = getString(R.string.message_lastknownlocation);
        }

        if (str.length() > 0) {
            Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
        }

        if (loc != null) {

            CoreInfoHandler.getInstance().setCurrentLocation(loc);

            updateInfoWindow(true, false, false, false, true);

            mOsmStandardView.getController().animateTo(TypeConverter.locationToGeoPoint(loc), OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
                    OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
        }
    }

    private float updateBearing(final float newBearing) {
        float dif = newBearing - mLastBearing;
        // find difference between new and current position
        if (Math.abs(dif) > 180) {
            dif = 360 - dif;
        }
        // if difference is bigger than 180 degrees,
        // it's faster to rotate in opposite direction
        if (Math.abs(dif) < 1) {
            return mLastBearing;
        }
        // if difference is less than 1 degree, leave things as is
        if (Math.abs(dif) >= 90) {
            return mLastBearing = newBearing;
        }
        // if difference is bigger than 90 degrees, just update it
        mLastBearing += 90 * Math.signum(dif) * Math.pow(Math.abs(dif) / 90, 2);
        // bearing is updated proportionally to the square of the difference
        // value
        // sign of difference is paid into account
        // if difference is 90(max. possible) it is updated exactly by 90
        while (mLastBearing > 360) {
            mLastBearing -= 360;
        }
        while (mLastBearing < 0) {
            mLastBearing += 360;
        }

        if (bearingTextView != null) {
            bearingTextView.setText(mLastBearing + "°");
        }
        // prevent bearing overrun/underrun
        return mLastBearing;
    }

    private void setTitle() {

        updateInfoWindow(false, false, true, false, false);
    }

    private class MyCallbackHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            boolean resultHandled = false;
            switch (msg.what) {

                case TrackOverlay.TRACK_MAPPED: {
                    focusOnTrack();
                    resultHandled = true;
                    break;
                }
                case RouteOverlay.ROUTE_MAPPED: {
                    focusOnRoute();
                    resultHandled = true;
                    break;
                }
                case R.id.map_center_changed: {

                    CoreInfoHandler.getInstance().setCurrentMapCenter(mOsmStandardView.getMapCenter());

                    updateInfoWindow(false, true, false, false, false);

                    resultHandled = true;
                    break;
                }
                case R.id.user_moved_map: {
                    resultHandled = true;
                    setAutoFollow(false);
                    break;
                }
                case R.id.tap_on_map: {
                    resultHandled = true;

                    final QuickAction quickAction = quickActionStd;
                    quickAction.show(mOsmStandardView, mOsmStandardView.mTouchDownX, mOsmStandardView.mTouchDownY);

                    // Toast.makeText(MainMapActivity.this, R.string.NYI,
                    // Toast.LENGTH_LONG)
                    // .show();
                    break;
                }
                case R.id.tap_on_poi: {
                    resultHandled = true;
                    break;
                }
                case R.id.tap_on_routepoi: {
                    resultHandled = true;
                    break;
                }
                case R.id.set_title: {
                    resultHandled = true;
                    setTitle();
                    break;
                }
                case OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE: {
                    resultHandled = true;
                    if (msg.obj != null) {
                        Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }

            if (!resultHandled) {
                super.handleMessage(msg);
            }
        }
    }

    private GeoPoint getTouchdownPoint() {
        GeoPoint gpoint;
        final OpenStreetMapViewProjection pj = mOsmStandardView.getProjection();

        gpoint = pj.fromPixels(mOsmStandardView.mTouchDownX, mOsmStandardView.mTouchDownY, mOsmStandardView.getBearing());
        return gpoint;
    }

    private void showAddress(final int x, final int y) {

        final AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            GeoPoint gpoint;

            @Override
            protected void onPostExecute(final String address) {

                quickActionAddress = new QuickAction(getActivity());

                final ActionItem addressItem = new ActionItem();

                addressItem.setClickable(true);
                addressItem.setTitle(address);
                addressItem.setIcon(getResources().getDrawable(R.drawable.menu_address));

                quickActionAddress.addActionItem(addressItem);

                quickActionAddress.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

                    @Override
                    public void onItemClick(final int pos) {
                        if (pos == 0) {
                            final PoiPoint targetPoint = new PoiPoint("Address", address, gpoint, -1);
                            gotoPoi(targetPoint);
                        }
                    }
                });

                quickActionAddress.show(mOsmStandardView, x, y);
            }

            @Override
            protected String doInBackground(final Void... params) {

                gpoint = getTouchdownPoint();
                final String currentAddress = Ut.getAddress(getActivity(), gpoint.getLatitude(), gpoint.getLongitude());

                return currentAddress;
            }
        };

        task.execute((Void) null);

    }

    public void moveToTopoSearchResult(final GeoPoint searchTarget, final String address, final int zoomLevel) {

        if (zoomLevel > -1) {
            setZoomLevel(zoomLevel);
        }
        mOsmStandardView.getController().animateTo(searchTarget, OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
                OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH, OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);

    }

    private void showDialog(final DialogFragment dialog) {
        final FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        final Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            transaction.remove(prev);
        }

        transaction.addToBackStack(null);
        dialog.show(transaction, "dialog");
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean result = false;
        switch (item.getItemId()) {

            case (R.id.menu_mapselected): {
                final String mapname = (String) item.getTitleCondensed();
                if (mapname != null) {

                    final OpenStreetMapRendererInfo RendererInfo = getRendererInfo(getResources(), getActivity().getPreferences(Context.MODE_PRIVATE),
                            (String) item.getTitleCondensed());
                    mOsmStandardView.setRenderer(RendererInfo);
                    // if (osmvMinimap != null) {
                    // osmvMinimap.setRenderer(RendererInfo);
                    // }
                    initializeOverlays();

                    setTitle();
                }
                result = true;
                break;
            }
            case (R.id.compass): {
                setCompass(!mCompassEnabled, true);
                result = true;
                break;
            }
            case (R.id.mylocation): {
                gotoMyLocation();
                result = true;
                break;
            }
            case (R.id.tilt): {
                setTilt(!mTiltEnabled, true);
                result = true;
                break;
            }
        }
        return result;
    }

    private void gotoMyLocation() {
        setAutoFollow(true);
        // setLastKnownLocation();
    }

    // @Override
    // public void onCreateContextMenu(ContextMenu menu, View v,
    // ContextMenuInfo menuInfo) {
    //
    // super.onCreateContextMenu(menu, v, menuInfo);
    //
    // if (v == buttonSelectMap) {
    // menu.setHeaderTitle("choose Map");
    // //TODO: cache !! this will be called on every btn click but never changes
    // createMapMenu(menu);
    //
    // }
    //
    // }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.map_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final Menu submenu = menu.findItem(R.id.mapselector).getSubMenu();

        submenu.clear();
        createMapMenu(submenu);
    }

    private List<MapMenuItemInfo> getMapMenuItems() {
        List<MapMenuItemInfo> menuItemInfos = null;

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = fac.newSAXParser();
            if (parser != null) {
                final InputStream in = getResources().openRawResource(R.raw.predefmaps);

                final PredefMapsParser pmp = new PredefMapsParser(pref);
                parser.parse(in, pmp);
                menuItemInfos = pmp.getMapMenuItemInfoList();

            }
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        return menuItemInfos;
    }

    private void createMapMenu(final Menu menu) {

        final List<MapMenuItemInfo> menuItemInfos = getMapMenuItems();

        Collections.sort(menuItemInfos, new Comparator<MapMenuItemInfo>() {

            @Override
            public int compare(final MapMenuItemInfo lhs, final MapMenuItemInfo rhs) {

                return lhs.getName().compareTo(rhs.getName());
            }
        });

        if (menuItemInfos != null) {
            for (final MapMenuItemInfo mii : menuItemInfos) {
                final MenuItem item = menu.add(Menu.NONE, R.id.menu_mapselected, Menu.NONE, mii.getName());
                item.setTitleCondensed(mii.getId());
            }
        }
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

    public class GetAddressTask extends AddressTask {

        @Override
        protected void onPostExecute(final Address address) {

            if (address != null) {
                updateAddressField(address);
            }

            // fade in display
            infoLayer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));
            infoLayer.setVisibility(View.VISIBLE);

        }
    }

    public static String formatAddress(final Address address) {
        String adddr = "";
        if (address != null) {

            adddr = address.getAddressLine(0);

            if (address.getAddressLine(1) != null) {
                adddr += " " + address.getAddressLine(2);
            }
            if (address.getAddressLine(2) != null) {
                adddr += " " + address.getAddressLine(3);
            }
            if (address.getLocality() != null) {
                adddr += " " + address.getLocality();
            }
            if (address.getCountryName() != null) {
                adddr += " " + address.getCountryName();
            }
        }
        else {
            adddr = "?";
        }
        return adddr;
    }

    public void updateAddressField(final Address address) {
        currentPositionTextView.setText(formatAddress(address));

    }

    private void checkZoomButtons() {
        if (zoomIn != null) {
            zoomIn.setVisibility(mOsmStandardView.getRenderer().ZOOM_MAXLEVEL > mOsmStandardView.getZoomLevel() ? View.VISIBLE : View.INVISIBLE);
        }
        if (zoomOut != null) {
            zoomOut.setVisibility((mOsmStandardView.getZoomLevel() > 2) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void setZoomLevel(final int zoom) {
        mOsmStandardView.setZoomLevel(zoom);
        checkZoomButtons();
    }

    private void gotoPoi(final PoiPoint point) {
        if (point != null) {

            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

            if (prev != null) {
                ft.remove(prev);
            }

            ft.addToBackStack(null);

            final DialogFragment newFragment = RoutingDialogFragment.newInstance(point.getTitle(), point.getDescr(), point.getGeoPoint().getLatitude(), point
                    .getGeoPoint().getLongitude(), R.string.dialogTitleRouting);
            newFragment.show(ft, "dialog");

        }

    }

    private void showData(final int page) {
        if (!Ut.isMultiPane(getActivity())) {
            final Intent intent = new Intent(getActivity(), PagerActivity.class);

            final Bundle bundle = new Bundle();
            bundle.putInt("PageInFlipper", page);
            intent.putExtras(bundle);

            startActivityForResult(intent, MainActivity.ACTIVITY_ID_DATA);
        }

    }

    private final class ActionModePOI implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {

            menu.add(getResources().getString(R.string.menu_getAddress)).setIcon(R.drawable.menu_address).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(getResources().getString(R.string.menu_shareLocation)).setIcon(R.drawable.menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(getResources().getString(R.string.menu_getWikiPedia)).setIcon(R.drawable.menu_wikipedia).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(getResources().getString(R.string.menu_addpoi)).setIcon(R.drawable.menu_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(getResources().getString(R.string.menu_edit)).setIcon(R.drawable.menu_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(getResources().getString(R.string.menu_delete)).setIcon(android.R.drawable.ic_menu_delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(getResources().getString(R.string.menu_goto)).setIcon(R.drawable.menu_navi).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            // Used to put dark icons on light action bar
            // boolean isLight = SampleList.THEME ==
            // R.style.Theme_Sherlock_Light;
            //
            // menu.add("Save")
            // .setIcon(isLight ? R.drawable.ic_compose_inverse :
            // R.drawable.ic_compose)
            // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            //
            // menu.add("Search")
            // .setIcon(isLight ? R.drawable.ic_search_inverse :
            // R.drawable.ic_search)
            // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            //
            // menu.add("Refresh")
            // .setIcon(isLight ? R.drawable.ic_refresh_inverse :
            // R.drawable.ic_refresh)
            // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            //
            // menu.add("Save")
            // .setIcon(isLight ? R.drawable.ic_compose_inverse :
            // R.drawable.ic_compose)
            // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            //
            // menu.add("Search")
            // .setIcon(isLight ? R.drawable.ic_search_inverse :
            // R.drawable.ic_search)
            // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            //
            // menu.add("Refresh")
            // .setIcon(isLight ? R.drawable.ic_refresh_inverse :
            // R.drawable.ic_refresh)
            // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            // Toast.makeText(ActionModes.this, "Got click: " + item,
            // Toast.LENGTH_SHORT).show();
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(final ActionMode mode) {
        }
    }

}
