package at.the.gogo.parkoid.fragments;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.map.ParkingCarItem;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.Position;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.json.ParseWKPZ;
import at.the.gogo.parkoid.util.speech.SpeakItOut;
import at.the.gogo.parkoid.util.webservices.VKPZQuery;
import at.the.gogo.parkoid.util.webservices.YahooGeocoding;

public abstract class LocationListenerFragment extends Fragment implements
        PageChangeNotifyer {

    private LocationListener listener;
    private boolean          initialized = false;

    protected boolean        updateAddress;
    protected boolean        updateVPZ;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setUpdateAddress(true);
        setUpdateVPZ(true);

        listener = new LocationListener() {

            @Override
            public void onStatusChanged(final String provider,
                    final int status, final Bundle extras) {

                // getActivity().getPreferences(0);
            }

            @Override
            public void onProviderEnabled(final String provider) {

            }

            @Override
            public void onProviderDisabled(final String provider) {

            }

            @Override
            public void onLocationChanged(final Location location) {

                final SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());

                // ugly
                final boolean autoupdate = sharedPreferences.getBoolean(
                        "pref_autoupdate", false);

                if (CoreInfoHolder.getInstance().getAccuracyWanted() == 0) {
                    final int accuracy = sharedPreferences.getInt(
                            "pref_gps_accuracy", 30);
                    CoreInfoHolder.getInstance().setAccuracyWanted(accuracy);
                }

                if ((autoupdate) || (!initialized)) {
                    updateLocation();
                    initialized = true;
                }
            }
        };

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

    private void pause() {
        CoreInfoHolder.getInstance().deregisterLocationListener(listener);
    }

    private void resume() {
        CoreInfoHolder.getInstance().registerLocationListener(listener);
        updateLocation();
    }

    protected void updateLocation() {
        if (Util.isInternetConnectionAvailable(getActivity())) {
            // request Address

            final GetAddressTask asyncTask1 = new GetAddressTask();
            asyncTask1.execute(CoreInfoHolder.getInstance()
                    .getLastKnownLocation());

            final CheckVKPZTask asyncTask2 = new CheckVKPZTask();
            asyncTask2.execute(CoreInfoHolder.getInstance()
                    .getLastKnownLocation());

            // check
        } else {
            Toast.makeText(getActivity(), R.string.message_inet_notavailable,
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String formatAddress(final GeoCodeResult address) {
        String adddr = "";
        if (address != null) {

            adddr = address.getLine1();

            if (address.getLine2() != null) {
                adddr += " " + address.getLine2();
            }
            if (address.getLine3() != null) {
                adddr += " " + address.getLine3();
            }
            if (address.getCity() != null) {
                adddr += " " + address.getCity();
            }
            if (address.getCountry() != null) {
                adddr += " " + address.getCountry();
            }
        } else {
            adddr = CoreInfoHolder.getInstance().getContext()
                    .getText(R.string.current_location_unknown).toString();
        }
        return adddr;
    }

    public abstract void updateAddressField(final GeoCodeResult address);

    public abstract void updateInfoList(final Boolean inZone);

    public class GetAddressTask extends
            AsyncTask<Location, Void, GeoCodeResult> {

        @Override
        protected GeoCodeResult doInBackground(final Location... params) {
            if (isUpdateAddress()) {
                if (params[0] != null) {
                    if (Util.isInternetConnectionAvailable(getActivity())) {
                        final GeoCodeResult result = YahooGeocoding
                                .reverseGeoCode(params[0].getLatitude(),
                                        params[0].getLongitude());

                        CoreInfoHolder.getInstance()
                                .setLastKnownAddress(result);

                    }
                } else if (Util.DEBUGMODE) {
                    final GeoCodeResult result = YahooGeocoding.reverseGeoCode(
                            48.208336, 16.372223);
                    CoreInfoHolder.getInstance().setLastKnownAddress(result);

                }
            }
            return CoreInfoHolder.getInstance().getLastKnownAddress();
        }

        @Override
        protected void onPostExecute(final GeoCodeResult address) {

            updateAddressField(address);
        }
    }

    private double getPrefAccuracy() {
        return CoreInfoHolder.getInstance().getAccuracyWanted();
    }

    public class CheckVKPZTask extends AsyncTask<Location, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final Location... params) {
            boolean inZone = false;
            if (isUpdateVPZ()) {
                Map<String, Map<String, String>> rawMeat = null;

                double lat = 0;
                double lon = 0;
                int acc = 0;

                if (params[0] != null) {

                    acc = (int) (params[0].hasAccuracy() ? Math.max(
                            getPrefAccuracy(), params[0].getAccuracy())
                            : getPrefAccuracy());

                    lat = params[0].getLatitude();
                    lon = params[0].getLongitude();

                } else if (Util.DEBUGMODE) {

                    lat = 48.208336;
                    lon = 16.372223;
                    acc = 300;
                }

                if ((lat != 0) && (lon != 0)) {
                    CoreInfoHolder.getInstance().setAccuracyUsed(acc);
                    rawMeat = VKPZQuery.getZones(lat, lon, acc, 0);
                }

                if ((rawMeat != null) && (rawMeat.size() > 0)) {
                    final Iterator<String> iterator = rawMeat.keySet()
                            .iterator();
                    while (iterator.hasNext()) {
                        final String key = iterator.next();

                        final Map<String, String> map = rawMeat.get(key);
                        final Iterator<String> iterator2 = map.keySet()
                                .iterator();
                        Util.dd("position :" + lat + " " + lon + " acc: " + acc);
                        while (iterator2.hasNext()) {
                            final String key2 = iterator2.next();
                            Util.dd("key :" + key2 + " | value : "
                                    + map.get(key2));
                        }
                    }
                }

                CoreInfoHolder.getInstance().setVKPZCurrentList(
                        ParseWKPZ.parseWebserviceData(rawMeat, false));

                inZone = (rawMeat != null) && (rawMeat.size() > 0);
            } else {
                inZone = ((CoreInfoHolder.getInstance().getVKPZCurrentList() != null) && (CoreInfoHolder
                        .getInstance().getVKPZCurrentList().size() > 0));
            }
            return inZone;
        }

        @Override
        protected void onPostExecute(final Boolean inZone) {
            updateInfoList(inZone);
        }
    }

    public boolean isUpdateAddress() {
        return updateAddress;
    }

    public void setUpdateAddress(final boolean updateAddress) {
        this.updateAddress = updateAddress;
    }

    public boolean isUpdateVPZ() {
        return updateVPZ;
    }

    public void setUpdateVPZ(final boolean updateVPZ) {
        this.updateVPZ = updateVPZ;
    }

    @Override
    public void pageGetsActivated() {
        resume();
    }

    @Override
    public void pageGetsDeactivated() {
        pause();
    }

    protected void navigateToCar() {

        final List<Position> locations = CoreInfoHolder.getInstance()
                .getDbManager().getLastLocationsList();

        if ((locations != null) && (locations.size() > 0)) {
            Position carLocation = locations.get(locations.size() - 1);

            NaviToCarTask task = new NaviToCarTask();
            task.execute(carLocation);

        } else {
            Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                    R.string.warning_no_parkingSlot, Toast.LENGTH_SHORT).show();
        }
    }

    public static class NaviToCarTask extends
            AsyncTask<Position, Void, GeoCodeResult> {

        Position carLocation;

        @Override
        protected void onPostExecute(final GeoCodeResult address) {

            String info = (address != null) ? "Ihr Auto befindet sich hier:\n"
                    + LocationListenerFragment.formatAddress(address)
                    : "Info nicht verfügbar"; // nasty

            if (address != null) {
                String carLocationTxt = CoreInfoHolder.getInstance()
                        .getContext().getText(R.string.tts_location_car)
                        .toString()
                        + address.getLine1();

                if (CoreInfoHolder.getInstance().isSpeakit()) {
                    SpeakItOut.speak(carLocationTxt);

                    SpeakItOut.speak(CoreInfoHolder.getInstance().getContext()
                            .getText(R.string.tts_navigator).toString());
                }
                CoreInfoHolder
                        .getInstance()
                        .getContext()
                        .startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri
                                        .parse("google.navigation:ll="
                                                + carLocation.getLatitude()
                                                + ","
                                                + carLocation.getLongitude()
                                                + "&mode=w")));
            }

            Toast.makeText(CoreInfoHolder.getInstance().getContext(), info,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected GeoCodeResult doInBackground(Position... params) {

            carLocation = params[0];

            GeoCodeResult address = null;
            if (Util.isInternetConnectionAvailable(CoreInfoHolder.getInstance()
                    .getContext())) {
                address = YahooGeocoding.reverseGeoCode(
                        params[0].getLatitude(), params[0].getLongitude());
            }
            return address;
        }
    }

}
