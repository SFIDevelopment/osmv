package org.outlander.utils;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.geonames.Toponym;
import org.outlander.R;
import org.outlander.activities.MainActivity;
import org.outlander.fragments.MapFragment;
import org.outlander.fragments.PageChangeNotifyer;
import org.outlander.io.db.CachedLocationsDatabaseHelper;
import org.outlander.io.db.DBManager;
import org.outlander.model.PanoramioItem;
import org.outlander.overlays.ExternalPointOverlay;
import org.outlander.overlays.PoiOverlay;
import org.outlander.overlays.RouteOverlay;
import org.outlander.overlays.TrackOverlay;
import org.outlander.utils.geo.GeoMathUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;

public class CoreInfoHandler {

    // ===========================================================
    // Constants
    // ===========================================================

    final static private String    PREF_COORD             = "pref_coords";
    final static private String    PREF_SPEED             = "pref_speed";
    final static private String    PREF_UNITS             = "pref_units";

    SensorEventListener            orientationListener;
    SensorEventListener            accellerationListener;
    List<SensorEventListener>      subOrientationListeners;
    List<SensorEventListener>      subAccellerationListeners;

    LocationListener               locationListener;
    List<LocationListener>         subLocationListeners;

    List<PanoramioItem>            panoramioItems;

    Handler                        callbackHandler;

    GeoPoint                       currentTarget;                         // for
                                                                           // navigation
    GeoPoint                       currentMapCenter;                      // center
                                                                           // of
                                                                           // screen
    GeoPoint                       currentSearchPoint;                    // user
                                                                           // click
                                                                           // point
    GeoPoint                       currentPoiPoint;                       // selected
                                                                           // POI
    Location                       currentLocation;                       // gps
                                                                           // position

    MainActivity                   mainActivity;

    boolean                        useCurrentTarget;

    private ExternalPointOverlay   mExternalPointOverlay;
    private TrackOverlay           trackOverlay;
    private RouteOverlay           routeOverlay;
    private PoiOverlay             poiOverlay;

    ViewPager                      viewPager;

    int[]                          pageSet;
    PageChangeNotifyer             pages[];
    MapFragment                    mapFragment;

    String                         topoSearchString;
    Toponym                        currentToponym;

    int                            currentRouteId         = -1;           // for
                                                                           // display
                                                                           // focus
    int                            currentTrackId         = -1;
    int                            currentRouteToFollowId = -1;

    private int                    coordFormatId          = -1;
    private int                    speedFormatId          = -1;
    private int                    unitFormatId           = -1;

    int                            mapCmd                 = -1;
    int                            oldPage                = -1;           // page
                                                                           // we
    // JUMPED from !
    private boolean                mAutoFollow;

    private boolean                mMeasureDistance;                      // from
                                                                           // current
                                                                           // position
                                                                           // to
                                                                           // screeencenter

    // ===========================================================
    // Fields
    // ===========================================================

    private DBManager              mDBManager;

    private static CoreInfoHandler instance;

    private CoreInfoHandler() {
    }

    public static CoreInfoHandler getInstance() {
        if (CoreInfoHandler.instance == null) {
            CoreInfoHandler.instance = new CoreInfoHandler();
        }
        return CoreInfoHandler.instance;
    }

    public DBManager getDBManager(final Context context) {
        if ((mDBManager == null) && (context != null)) {
            mDBManager = new DBManager(context);
        }
        return mDBManager;
    }

    public void setDBManager(final DBManager poiManager) {
        mDBManager = poiManager;
    }

    public SensorEventListener getOrientationListener() {
        if (orientationListener == null) {
            orientationListener = new SensorEventListener() {

                @Override
                public void onSensorChanged(final SensorEvent event) {

                    for (final SensorEventListener listener : getSubOrientationListeners()) {
                        listener.onSensorChanged(event);
                    }

                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {

                    for (final SensorEventListener listener : getSubOrientationListeners()) {
                        listener.onAccuracyChanged(sensor, accuracy);
                    }

                }
            };
        }
        return orientationListener;
    }

    private List<SensorEventListener> getSubOrientationListeners() {
        if (subOrientationListeners == null) {
            subOrientationListeners = new ArrayList<SensorEventListener>();
        }
        return subOrientationListeners;
    }

    public void registerOrientationListener(final SensorEventListener sensorEventListener) {

        getSubOrientationListeners().add(sensorEventListener);
    }

    public void deregisterOrientationListener(final SensorEventListener sensorEventListener) {

        getSubOrientationListeners().remove(sensorEventListener);
    }

    public SensorEventListener getAccellerationListener() {
        if (accellerationListener == null) {
            accellerationListener = new SensorEventListener() {

                @Override
                public void onSensorChanged(final SensorEvent event) {

                    for (final SensorEventListener listener : getSubAccellerationListeners()) {
                        listener.onSensorChanged(event);
                    }

                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int accuracy) {

                    for (final SensorEventListener listener : getSubAccellerationListeners()) {
                        listener.onAccuracyChanged(sensor, accuracy);
                    }

                }
            };
        }
        return accellerationListener;
    }

    private List<SensorEventListener> getSubAccellerationListeners() {
        if (subAccellerationListeners == null) {
            subAccellerationListeners = new ArrayList<SensorEventListener>();
        }
        return subAccellerationListeners;
    }

    public void registerAccellerationListener(final SensorEventListener sensorEventListener) {

        getSubAccellerationListeners().add(sensorEventListener);
    }

    public void deregisterAccellerationListener(final SensorEventListener sensorEventListener) {

        getSubAccellerationListeners().remove(sensorEventListener);
    }

    public LocationListener getLocationListener() {
        if (locationListener == null) {
            locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(final String provider, final int status, final Bundle extras) {

                    for (final LocationListener listener : getSubLocationListeners()) {
                        if (listener != null) {
                            listener.onStatusChanged(provider, status, extras);
                        }
                    }

                }

                @Override
                public void onProviderEnabled(final String provider) {

                    Ut.d("Location Provider enabled: " + provider);

                    for (final LocationListener listener : getSubLocationListeners()) {
                        if (listener != null) {
                            listener.onProviderEnabled(provider);
                        }
                    }

                }

                @Override
                public void onProviderDisabled(final String provider) {

                    Ut.d("Location Provider disabled: " + provider);

                    for (final LocationListener listener : getSubLocationListeners()) {
                        if (listener != null) {
                            listener.onProviderDisabled(provider);
                        }

                    }

                }

                @Override
                public void onLocationChanged(final Location location) {

                    CachedLocationsDatabaseHelper.getInstance(getMainActivity()).insertLocationPoint(location.getLatitude(), location.getLongitude(),
                            location.getAltitude(), location.getAccuracy(), location.getTime(), location.getProvider());

                    currentLocation = location;

                    for (final LocationListener listener : getSubLocationListeners()) {
                        if (listener != null) {
                            listener.onLocationChanged(location);
                        }
                    }

                }
            };
        }
        return locationListener;
    }

    private List<LocationListener> getSubLocationListeners() {
        if (subLocationListeners == null) {
            subLocationListeners = new ArrayList<LocationListener>();
        }
        return subLocationListeners;
    }

    public void registerLocationListener(final LocationListener sensorEventListener) {

        getSubLocationListeners().add(sensorEventListener);
    }

    public void deregisterLocationListener(final LocationListener sensorEventListener) {

        getSubLocationListeners().remove(sensorEventListener);
    }

    public void switchToPage(final int pageId) {
        if (callbackHandler != null) {
            Message.obtain(callbackHandler, R.id.switch_page, pageId).sendToTarget();

        }
    }

    public GeoPoint getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(final GeoPoint currentTarget) {
        this.currentTarget = currentTarget;
    }

    public GeoPoint getCurrentMapCenter() {
        return currentMapCenter;
    }

    public void setCurrentMapCenter(final GeoPoint currentMapCenter) {
        this.currentMapCenter = currentMapCenter;
    }

    // public Handler getCallbackHandler() {
    // return callbackHandler;
    // }
    //
    // public void setCallbackHandler(final Handler callbackHandler) {
    // this.callbackHandler = callbackHandler;
    // }

    public GeoPoint getCurrentSearchPoint() {
        return currentSearchPoint;
    }

    public void setCurrentSearchPoint(final GeoPoint currentSearchPoint) {
        this.currentSearchPoint = currentSearchPoint;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public void setViewPager(final ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public int[] getPageSet() {
        return pageSet;
    }

    public void setPageSet(final int[] pageSet) {
        this.pageSet = pageSet;
        pages = new PageChangeNotifyer[pageSet.length];
    }

    public PageChangeNotifyer getPageChangeNotifyer(final int ix) {
        return pages[ix];
    }

    public void setPageChangeNotifyer(final int ix, final PageChangeNotifyer pcn) {
        pages[ix] = pcn;
    }

    public void gotoPage(final int pageIndex) {
        if (pageIndex > -1) {
            oldPage = getViewPager().getCurrentItem();
            getViewPager().setCurrentItem(pageIndex);
        }
        else {
            // if (FragmentFactory.FRAG_ID_MAP == pageIndex) {
            getMapFragment().refresh(); // !!! hardwired !!
            // }
        }
    }

    public void gotoOldPage() {
        gotoPage(getOldPage());
    }

    public int getOldPage() {
        return oldPage;
    }

    public String getTopoSearchString() {
        return topoSearchString;
    }

    public void setTopoSearchString(final String topoSearchString) {
        this.topoSearchString = topoSearchString;
    }

    public int getMapCmd() {
        return mapCmd;
    }

    public void setMapCmd(final int mapCmd) {
        this.mapCmd = mapCmd;
    }

    public Toponym getCurrentToponym() {
        return currentToponym;
    }

    public void setCurrentToponym(final Toponym currentToponym) {
        this.currentToponym = currentToponym;
    }

    public int getCurrentRouteId() {
        return currentRouteId;
    }

    public void setCurrentRouteId(final int currentRoute) {
        currentRouteId = currentRoute;
    }

    public int getCurrentTrackId() {
        return currentTrackId;
    }

    public void setCurrentTrackId(final int currentTrack) {
        currentTrackId = currentTrack;
    }

    public boolean isAutoFollow() {
        return mAutoFollow;
    }

    public void setAutoFollow(final boolean mAutoFollow) {
        this.mAutoFollow = mAutoFollow;
    }

    public Location getCurrentLocation() {
        
        if (currentLocation == null)
        {
            if (Debug.isDebuggerConnected()) {
                currentLocation = new Location("GPS");
                currentLocation.setLatitude(48);
                currentLocation.setLongitude(16);
                currentLocation.setAccuracy(25);
                currentLocation.setSpeed(2);                
            }
            
        }
        
        return currentLocation;
    }

    public GeoPoint getCurrentLocationAsGeoPoint() {

        if (currentLocation == null) {
            if (Debug.isDebuggerConnected()) {
                return new GeoPoint(48, 16);
            }
        }

        return (currentLocation != null) ? new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()) : null;
    }

    public void setCurrentLocation(final Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public GeoPoint getCurrentPoiPoint() {
        return currentPoiPoint;
    }

    public void setCurrentPoiPoint(final GeoPoint currentPoiPoint) {
        this.currentPoiPoint = currentPoiPoint;
    }

    public int getCoordFormatId() {

        if (coordFormatId < 0) {
            coordFormatId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getMainActivity()).getString(CoreInfoHandler.PREF_COORD, "1"));
        }
        return coordFormatId;
    }

    public int incCoordFormatId() {
        setCoordFormatId(coordFormatId + 1);
        return coordFormatId;
    }

    public void setCoordFormatId(final int coordFormatId) {
        this.coordFormatId = coordFormatId;

        if (this.coordFormatId >= GeoMathUtil.MAX_FORMAT) {
            this.coordFormatId = 0;
        }
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getMainActivity()).edit();
        editor.putString(CoreInfoHandler.PREF_COORD, Integer.toString(this.coordFormatId));
        editor.commit();
    }

    public int getDistanceUnitFormatId() {

        if (unitFormatId < 0) {
            unitFormatId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getMainActivity()).getString(CoreInfoHandler.PREF_UNITS, "0"));
        }

        // // bugfix workaround
        // if (this.unitFormatId >=
        // GeoMathUtil.distanceConversionFactors.length) {
        // setDistanceUnitFormatId(0);
        // }

        return unitFormatId;
    }

    public int incDistanceUnitFormatId() {
        setDistanceUnitFormatId(unitFormatId + 1);
        return unitFormatId;
    }

    public void setDistanceUnitFormatId(final int unitFormatId) {
        this.unitFormatId = unitFormatId;

        if (this.unitFormatId >= GeoMathUtil.distanceConversionFactors.length) {
            this.unitFormatId = 0;
        }
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getMainActivity()).edit();
        editor.putString(CoreInfoHandler.PREF_UNITS, Integer.toString(this.unitFormatId));
        editor.commit();
    }

    public int getSpeedFormatId() {
        if (speedFormatId < 0) {
            speedFormatId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getMainActivity()).getString(CoreInfoHandler.PREF_SPEED, "1"));
        }
        return speedFormatId;
    }

    public int incSpeedFormatId() {
        setSpeedFormatId(speedFormatId + 1);
        return speedFormatId;
    }

    public void setSpeedFormatId(final int speedFormatId) {
        this.speedFormatId = speedFormatId;

        if (this.speedFormatId >= GeoMathUtil.speedConversionFactors.length) {
            this.speedFormatId = 0;
        }
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getMainActivity()).edit();
        editor.putString(CoreInfoHandler.PREF_SPEED, Integer.toString(this.speedFormatId));
        editor.commit();
    }

    public int getCurrentRouteToFollowId() {
        return currentRouteToFollowId;
    }

    public void setCurrentRouteToFollowId(final int currentRouteToFollowId) {
        this.currentRouteToFollowId = currentRouteToFollowId;
    }

    public boolean isMeasureDistance() {
        return mMeasureDistance;
    }

    public void setMeasureDistance(final boolean mMeasureDistance) {
        this.mMeasureDistance = mMeasureDistance;
    }

    public ExternalPointOverlay getExternalPointOverlay() {
        return mExternalPointOverlay;
    }

    public void setExternalPointOverlay(final ExternalPointOverlay mExternalPointOverlay) {
        this.mExternalPointOverlay = mExternalPointOverlay;
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    public void setMapFragment(final MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public List<PanoramioItem> getPanoramioItems() {
        return panoramioItems;
    }

    public void setPanoramioItems(final List<PanoramioItem> panoramioItems) {
        this.panoramioItems = panoramioItems;
    }

    public RouteOverlay getRouteOverlay() {
        return routeOverlay;
    }

    public void setRouteOverlay(final RouteOverlay routeOverlay) {
        this.routeOverlay = routeOverlay;
    }

    public TrackOverlay getTrackOverlay() {
        return trackOverlay;
    }

    public void setTrackOverlay(final TrackOverlay trackOverlay) {
        this.trackOverlay = trackOverlay;
    }

    public PoiOverlay getPoiOverlay() {
        return poiOverlay;
    }

    public void setPoiOverlay(final PoiOverlay poiOverlay) {
        this.poiOverlay = poiOverlay;
    }

    public boolean isUseCurrentTarget() {
        return useCurrentTarget;
    }

    public void setUseCurrentTarget(final boolean useCurrentTarget) {
        this.useCurrentTarget = useCurrentTarget;
    }

}
