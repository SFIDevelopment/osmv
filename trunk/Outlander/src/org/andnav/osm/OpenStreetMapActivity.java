// Created by plusminus on 00:14:42 - 02.10.2008
package org.andnav.osm;

import org.andnav.osm.util.constants.OpenStreetMapConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Baseclass for Activities who want to contribute to the OpenStreetMap Project.
 * 
 * @author Nicolas Gramlich
 */
public abstract class OpenStreetMapActivity extends SherlockFragmentActivity implements OpenStreetMapConstants {

    // ===========================================================
    // Constants
    // ===========================================================

    protected static final String PROVIDER_NAME  = LocationManager.GPS_PROVIDER;

    // ===========================================================
    // Fields
    // ===========================================================

    protected MapLocationListener mLocationListener, mNetListener;

    // protected RouteRecorder mRouteRecorder = new RouteRecorder();

    protected boolean             mDoGPSRecordingAndContributing;

    protected LocationManager     mLocationManager;

    public int                    mNumSatellites = OpenStreetMapConstants.NOT_SET;

    private boolean               mGPSFastUpdate = false;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Calls
     * <code>onCreate(final Bundle savedInstanceState, final boolean pDoGPSRecordingAndContributing)</code>
     * with <code>pDoGPSRecordingAndContributing == true</code>.<br/>
     * That means it automatically contributes to the OpenStreetMap Project in
     * the background.
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        onCreate(savedInstanceState, true);
    }

    /**
     * Called when the activity is first created. Registers LocationListener.
     * 
     * @param savedInstanceState
     * @param pDoGPSRecordingAndContributing
     *            If <code>true</code>, it automatically contributes to the
     *            OpenStreetMap Project in the background.
     */
    public void onCreate(final Bundle savedInstanceState, final boolean pDoGPSRecordingAndContributing) {
        super.onCreate(savedInstanceState);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mGPSFastUpdate = pref.getBoolean("pref_gpsfastupdate", true);

        // register location listener
        initLocation();
    }

    protected LocationManager getLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }

    private void getBestProvider() {
        int minTime = 0;
        int minDistance = 0;

        if (!mGPSFastUpdate) {
            minTime = 2000;
            minDistance = 20;
        }

        if (mLocationListener != null)
        {
            getLocationManager().removeUpdates(mLocationListener);
        }
        if (mNetListener != null) {
            getLocationManager().removeUpdates(mNetListener);
        }

        if (getLocationManager().isProviderEnabled(OpenStreetMapConstants.GPS)) {
            getLocationManager().requestLocationUpdates(OpenStreetMapConstants.GPS, minTime, minDistance, mLocationListener);

            try {
                if (getLocationManager().isProviderEnabled(OpenStreetMapConstants.NETWORK)) {
                    mNetListener = new MapLocationListener();
                    getLocationManager().requestLocationUpdates(OpenStreetMapConstants.NETWORK, minTime, minDistance, mNetListener);
                }
            }
            catch (final Exception e) {
                Log.e(OpenStreetMapConstants.DEBUGTAG, "isProviderEnabled(NETWORK) exception");
                e.printStackTrace();
            }

        }
        else if (getLocationManager().isProviderEnabled(OpenStreetMapConstants.NETWORK)) {
            getLocationManager().requestLocationUpdates(OpenStreetMapConstants.NETWORK, minTime, minDistance, mLocationListener);
        }
    }

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {

                                                            public void onReceive(Context context, Intent intent) {

                                                                // String action
                                                                // =
                                                                // intent.getAction();

                                                                Location loc = intent.getParcelableExtra(LOC_UPDATE_LOC);
                                                                if (loc != null) {
                                                                    onLocationChanged(loc);
                                                                }
                                                                Log.d(OpenStreetMapConstants.DEBUGTAG, "location received from service");
                                                            }
                                                        };

    private void initLocation() {
        // mLocationListener = new MapLocationListener();

        // register receiver for messages from service
        LocalBroadcastManager.getInstance(this).registerReceiver(locationBroadcastReceiver

        , new IntentFilter(LOC_UPDATE_EVENT));

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    public abstract void onLocationLost();

    public abstract void onLocationChanged(final Location pLoc);

    public abstract void onStatusChanged(String provider, int status, Bundle extras);

    /**
     * Called when activity is destroyed. Unregisters LocationListener.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //
        // getLocationManager().removeUpdates(mLocationListener);
        // if (mNetListener != null) {
        // getLocationManager().removeUpdates(mNetListener);
        // }

    }

    // ===========================================================
    // Methods
    // ===========================================================

    @Override
    protected void onStart() {
//        getBestProvider();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mLocationListener != null) {
            getLocationManager().removeUpdates(mLocationListener);
        }
        if (mNetListener != null) {
            getLocationManager().removeUpdates(mNetListener);
        }
        super.onStop();
    }

    protected void statusChanged(final String provider, final int status, final Bundle b) {
        // Log.e(DEBUGTAG, "onStatusChanged provider = " + provider +
        // " status = " + status + " satellites = " + b.getInt("satellites",
        // NOT_SET));
        if (mNetListener != null) {
            if (provider.equals(OpenStreetMapConstants.GPS) && (status == LocationProvider.AVAILABLE)) {
                getLocationManager().removeUpdates(mNetListener);
                mNetListener = null;
                Log.e(OpenStreetMapConstants.DEBUGTAG, "Stop NETWORK listener");
            }
        }
        if (mNetListener == null) {
            OpenStreetMapActivity.this.onStatusChanged(provider, status, b);
        }
        else if ((mNetListener != null) && provider.equals(OpenStreetMapConstants.NETWORK)) {
            OpenStreetMapActivity.this.onStatusChanged(provider, status, b);
        }

    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * Logs all Location-changes to <code>mRouteRecorder</code>.
     * 
     * @author plusminus
     */
    private class MapLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location loc) {
            if (loc != null) {
                // if(OpenStreetMapActivity.this.mDoGPSRecordingAndContributing)
                // OpenStreetMapActivity.this.mRouteRecorder.add(loc,
                // OpenStreetMapActivity.this.mNumSatellites);

                OpenStreetMapActivity.this.onLocationChanged(loc);
            }
            else {
                onLocationLost();
            }
        }

        @Override
        public void onStatusChanged(final String a, final int status, final Bundle b) {
            mNumSatellites = b.getInt("satellites", OpenStreetMapConstants.NOT_SET); // LATER
                                                                                     // Check
                                                                                     // on
            // an actual
            // device
            // Log.e(DEBUGTAG, "onStatusChanged status = " + status +
            // " satellites = " + b.getInt("satellites", NOT_SET));
            statusChanged(a, status, b);
        }

        @Override
        public void onProviderEnabled(final String a) {
            Log.e(OpenStreetMapConstants.DEBUGTAG, "onProviderEnabled");
            getBestProvider();
        }

        @Override
        public void onProviderDisabled(final String a) {
            Log.e(OpenStreetMapConstants.DEBUGTAG, "onProviderDisabled");
            getBestProvider();
        }
    }
}
