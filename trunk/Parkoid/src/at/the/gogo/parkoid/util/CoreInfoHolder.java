package at.the.gogo.parkoid.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.ViewPager;
import at.the.gogo.parkoid.map.ParkStripIconOverlay;
import at.the.gogo.parkoid.map.ParkingCarOverlay;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.db.DBManager;

public class CoreInfoHolder {

    private DBManager                       dbManager;
    private GeoCodeResult                   lastKnownAddress;
    private Location                        lastKnownLocation;

    private LocationListener                locationListener;
    private List<LocationListener>          subLocationListeners;
    private Map<String, ViennaKurzParkZone> vkpzInfoCurrentList; // current
    private Map<String, ViennaKurzParkZone> vkpzInfoCacheList;   // all cached

    private ViewPager                       pager;
    private int                             accuracyUsed;
    private int                             accuracyWanted;
    private Context                         context;

    private ParkingCarOverlay               parkingCarOverlay;
    private ParkStripIconOverlay            parkStripIconOverlay;
    private TextToSpeech                    mTts;
    private boolean                         speakit;

    private boolean                         speechRecoAvailable;

    private static CoreInfoHolder           instance;

    public static CoreInfoHolder getInstance() {
        if (CoreInfoHolder.instance == null) {
            CoreInfoHolder.instance = new CoreInfoHolder();
        }
        return CoreInfoHolder.instance;
    }

    public void setPager(final ViewPager pager) {
        this.pager = pager;
    }

    public ViewPager getPager() {
        return pager;
    }

    public void gotoPage(final int pageId) {
        pager.setCurrentItem(pageId); // , true
    }

    public DBManager getDbManager() {

        if (dbManager == null) {
            dbManager = new DBManager(getContext());
        }
        return dbManager;
    }

    public void setDbManager(final DBManager dbManager) {
        this.dbManager = dbManager;
    }

    private List<LocationListener> getSubLocationListeners() {
        if (subLocationListeners == null) {
            subLocationListeners = new ArrayList<LocationListener>();
        }
        return subLocationListeners;
    }

    public void registerLocationListener(
            final LocationListener sensorEventListener) {

        getSubLocationListeners().add(sensorEventListener);
    }

    public void deregisterLocationListener(
            final LocationListener sensorEventListener) {

        getSubLocationListeners().remove(sensorEventListener);
    }

    public LocationListener getLocationListener() {
        if (locationListener == null) {
            locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(final String provider,
                        final int status, final Bundle extras) {
                    if (getSubLocationListeners() != null) {
                        for (final LocationListener listener : getSubLocationListeners()) {
                            listener.onStatusChanged(provider, status, extras);
                        }
                    }
                }

                @Override
                public void onProviderEnabled(final String provider) {
                    if (getSubLocationListeners() != null) {
                        for (final LocationListener listener : getSubLocationListeners()) {
                            listener.onProviderEnabled(provider);
                        }
                    }
                }

                @Override
                public void onProviderDisabled(final String provider) {
                    if (getSubLocationListeners() != null) {
                        for (final LocationListener listener : getSubLocationListeners()) {
                            listener.onProviderDisabled(provider);
                        }
                    }
                }

                @Override
                public void onLocationChanged(final Location location) {

                    setLastKnownLocation(location);

                    if (getSubLocationListeners() != null) {
                        for (final LocationListener listener : getSubLocationListeners()) {
                            listener.onLocationChanged(location);
                        }
                    }
                }
            };
        }
        return locationListener;
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(final Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public Map<String, ViennaKurzParkZone> getVKPZCurrentList() {
        return vkpzInfoCurrentList;
    }

    public void setVKPZCurrentList(
            final Map<String, ViennaKurzParkZone> currentList) {
        vkpzInfoCurrentList = currentList;
    }

    public GeoCodeResult getLastKnownAddress() {
        return lastKnownAddress;
    }

    public void setLastKnownAddress(final GeoCodeResult lastKnownAddress) {
        this.lastKnownAddress = lastKnownAddress;
    }

    public Map<String, ViennaKurzParkZone> getVKPZCacheList() {
        if (vkpzInfoCacheList == null) {
            vkpzInfoCacheList = new HashMap<String, ViennaKurzParkZone>();
        }
        return vkpzInfoCacheList;
    }

    public void setVKPZCacheList(final Map<String, ViennaKurzParkZone> kpzList) {
        vkpzInfoCacheList = kpzList;
    }

    public int getAccuracyUsed() {
        return accuracyUsed;
    }

    public void setAccuracyUsed(final int accuracyUsed) {
        this.accuracyUsed = accuracyUsed;
    }

    public int getAccuracyWanted() {
        return accuracyWanted;
    }

    public void setAccuracyWanted(final int accuracyWanted) {
        this.accuracyWanted = accuracyWanted;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public ParkingCarOverlay getParkingCarOverlay() {
        return parkingCarOverlay;
    }

    public void setParkingCarOverlay(final ParkingCarOverlay parkingCarOverlay) {
        this.parkingCarOverlay = parkingCarOverlay;
    }

    public TextToSpeech getTts() {
        return mTts;
    }

    public void setTts(final TextToSpeech mTts) {
        this.mTts = mTts;
    }

    public boolean isSpeakit() {
        return speakit;
    }

    public void setSpeakit(final boolean speakit) {
        this.speakit = speakit;
    }

    public boolean isSpeechRecoAvailable() {
        return speechRecoAvailable;
    }

    public void setSpeechRecoAvailable(final boolean speechRecoAvailable) {
        this.speechRecoAvailable = speechRecoAvailable;
    }

    public void setParkStripOverlay(
            final ParkStripIconOverlay parkStripIconOverlay) {
        this.parkStripIconOverlay = parkStripIconOverlay;
    }

    public ParkStripIconOverlay getParkStripOverlay() {
        return parkStripIconOverlay;
    }

}
