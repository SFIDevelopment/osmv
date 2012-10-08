package org.outlander.utils;

import java.util.HashMap;
import java.util.List;

import org.outlander.model.ProximityTarget;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.AsyncTask;

public class ProximityHandler extends BroadcastReceiver {

    // ===========================================================
    // Constants
    // ===========================================================

    private static final String                   PROXIMITY_ALERT   = "PROXIMITY_ALERT";

    private final LocationManager                 locationManager;
    private final Context                         context;

    private final HashMap<Integer, PendingIntent> pendindingIntents = new HashMap<Integer, PendingIntent>();

    public ProximityHandler(final Context context, final LocationManager locationManager) {
        super();

        this.context = context;
        this.locationManager = locationManager;

        context.registerReceiver(this, new IntentFilter(ProximityHandler.PROXIMITY_ALERT));
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(this);
    }

    public void registerProximityTargetsFromDB() {
        final ProximitaTargetTask importer = new ProximitaTargetTask();

        importer.execute((Void) null);
    }

    private void registerAllKnownTargets() {

        final List<ProximityTarget> allTargets = CoreInfoHandler.getInstance().getDBManager(context).getProximityTargetList();

        for (final ProximityTarget target : allTargets) {
            registerProximityTarget(target);
        }
    }

    private void registerProximityTarget(final ProximityTarget proximityTarget) {

        final Intent intent = new Intent(ProximityHandler.PROXIMITY_ALERT);
        intent.putExtra("targetId", proximityTarget.getId());
        final PendingIntent proximityIntent = PendingIntent.getBroadcast(context, proximityTarget.getId(), intent, 0);

        pendindingIntents.put(proximityTarget.getId(), proximityIntent);

        if (locationManager != null) {
            locationManager.addProximityAlert(proximityTarget.GeoPoint.getLatitude(), proximityTarget.GeoPoint.getLongitude(), 100, -1, proximityIntent);
        }
    }

    public void unregisterAllProximityTargets() {

        for (final Integer id : pendindingIntents.keySet()) {
            unregisterProximityTarget(id);
        }
    }

    // private void unregisterProximityTarget(final ProximityTarget
    // proximityTarget) {
    // unregisterProximityTarget(proximityTarget.getId());
    // }

    private void unregisterProximityTarget(final int id) {
        if (locationManager != null) {
            locationManager.removeProximityAlert(pendindingIntents.get(id));
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String key = LocationManager.KEY_PROXIMITY_ENTERING;

        final Boolean entering = intent.getBooleanExtra(key, false);

        if (entering) {
            Ut.d("Proximity on entering - received");
        }
    }

    class ProximitaTargetTask extends AsyncTask<Void, Void, Void> {

        ProximitaTargetTask() {
            super();
        }

        @Override
        protected Void doInBackground(final Void... params) {

            registerAllKnownTargets();

            return null;
        }

    }

}
