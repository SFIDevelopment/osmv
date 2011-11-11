package at.the.gogo.parkoid.fragments;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
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
    private boolean          initialized    = false;

    protected boolean        updateAddress;
    protected boolean        updateVPZ;

    private TextView         currentAddress;

    private ImageView        parkButton;
    private TextView         locationCaption;

    boolean                  kpzStateChange = false;
    boolean                  kpzLastState;
    boolean                  kpzFirstTime   = true;

    boolean                  autoupdate;

    protected void initializeGUI(final View view) {
        currentAddress = (TextView) view.findViewById(R.id.currentAddress);
        locationCaption = (TextView) view.findViewById(R.id.locationCaption);

        currentAddress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                kpzFirstTime = true;
                updateLocation();
            }
        });

        // ((Button) view.findViewById(R.id.buy_ticket))
        // .setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(final View v) {
        // buyParkschein();
        // }
        // });
        //
        // ((Button) view.findViewById(R.id.check_location))
        // .setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(final View v) {
        // updateLocation();
        // }
        // });

        parkButton = (ImageView) view.findViewById(R.id.parkButton);
        registerForContextMenu(parkButton);
        parkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                parkButton.showContextMenu();

            }
        });

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setUpdateAddress(true);
        setUpdateVPZ(true);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(CoreInfoHolder.getInstance()
                        .getContext());

        // ugly
        autoupdate = sharedPreferences.getBoolean("pref_autoupdate", false);

        if (CoreInfoHolder.getInstance().getAccuracyWanted() == 0) {
            final int accuracy = sharedPreferences.getInt("pref_gps_accuracy",
                    30);
            CoreInfoHolder.getInstance().setAccuracyWanted(accuracy);
        }

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

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(getText(R.string.app_name));

        menu.add(0, R.id.menu_buyTicket, 0, getText(R.string.menu_buyTicket));
        menu.add(0, R.id.menu_saveLocation, 0,
                getText(R.string.menu_saveLocation));
        menu.add(0, R.id.navigateToCar, 0, getText(R.string.menu_navigateToCar));
        menu.add(0, R.id.deleteParinkingInfo, 0,
                getText(R.string.menu_deleteParkingInfo));

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.menu_buyTicket: {
                buyParkschein();
                saveLocation();
                result = true;
                break;
            }
            case R.id.menu_saveLocation: {
                saveLocation();
                result = true;
                break;
            }
            case R.id.navigateToCar: {
                navigateToCar();
                result = true;
                break;
            }
        }

        if (!result) {
            result = super.onContextItemSelected(item);
        }
        return result;
    }

    protected void updateLocation() {
        if (Util.isInternetConnectionAvailable(CoreInfoHolder.getInstance()
                .getContext())) {
            // request Address
            final GetAddressTask asyncTask1 = new GetAddressTask();
            asyncTask1.execute(CoreInfoHolder.getInstance()
                    .getLastKnownLocation());

            // request Zones
            final CheckVKPZTask asyncTask2 = new CheckVKPZTask();
            asyncTask2.execute(CoreInfoHolder.getInstance()
                    .getLastKnownLocation());

            // check
        } else {
            Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                    R.string.message_inet_notavailable, Toast.LENGTH_LONG)
                    .show();
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

    public void updateAddressField(final GeoCodeResult address) {
        currentAddress.setText(formatAddress(address));

        final Location location = CoreInfoHolder.getInstance()
                .getLastKnownLocation();
        if ((location != null) && (location.hasAccuracy())) {
            final String newTitle = getText(R.string.current_location)
                    + " (+/-" + Math.round(location.getAccuracy()) + "m)";
            locationCaption.setText(newTitle);
        } else {
            Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                    R.string.current_location_empty, Toast.LENGTH_SHORT).show();
        }

    }

    public void updateInfoList(final Boolean inZone) {
        if (inZone != null) {
            if (inZone) {
                parkButton.setImageResource(R.drawable.parken_danger);
            } else {
                parkButton.setImageResource(R.drawable.parken);
            }
        } else {
            parkButton.setImageResource(R.drawable.parken_unknown);
        }

        kpzStateChange = (kpzLastState != inZone) || kpzFirstTime;
        kpzLastState = inZone;
        kpzFirstTime = false;

        // speech support
        if (kpzStateChange) {
            if (CoreInfoHolder.getInstance().isSpeakit()) {
                SpeakItOut.speak(getText(
                        (inZone) ? R.string.tts_near_kpz
                                : R.string.tts_no_near_kpz).toString());

                final GeoCodeResult lastAddress = CoreInfoHolder.getInstance()
                        .getLastKnownAddress();

                if (lastAddress != null) {
                    String currLocText = "";
                    if (onlyCoords(lastAddress.getLine1())) {

                        currLocText = getText(R.string.tts_location_unknown)
                                .toString()
                                + getText(R.string.tts_location_coords)
                                        .toString() + lastAddress.getLine1();

                    } else {
                        currLocText = getText(R.string.tts_location_current)
                                .toString() + lastAddress.getLine1();
                    }
                    SpeakItOut.speak(currLocText);
                }
            }
        }
        // if (inZone)
        // {
        // // switch to parkplatzliste
        // CoreInfoHolder.getInstance().gotoPage(1);
        // }
    }

    private boolean onlyCoords(final String text) {
        boolean result = true;

        // It can't contain only numbers if it's null or empty...
        if ((text != null) && (text.length() > 0)) {

            for (int i = 0; i < text.length(); i++) {

                // If we find a non-digit character we return false.
                if (!(Character.isDigit(text.charAt(i))
                        || Character.isWhitespace(text.charAt(i)) || (text
                            .charAt(i) == ','))) {
                    result = false;
                    break;
                }
            }
        } else {
            result = false;
        }

        return result;
    }

    public class GetAddressTask extends
            AsyncTask<Location, Void, GeoCodeResult> {

        @Override
        protected GeoCodeResult doInBackground(final Location... params) {
            if (isUpdateAddress()) {
                if (params[0] != null) {
                    if (Util.isInternetConnectionAvailable(CoreInfoHolder
                            .getInstance().getContext())) {
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
            final Position carLocation = locations.get(locations.size() - 1);

            final NaviToCarTask task = new NaviToCarTask();
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

            final String info = (address != null) ? "Ihr Auto befindet sich hier:\n"
                    + LocationListenerFragment.formatAddress(address)
                    : "Info nicht verfügbar"; // nasty

            if (address != null) {
                final String carLocationTxt = CoreInfoHolder.getInstance()
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
        protected GeoCodeResult doInBackground(final Position... params) {

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

    private void saveLocation() {
        if (CoreInfoHolder.getInstance().getLastKnownLocation() != null) {
            // save car location
            final Location loc = CoreInfoHolder.getInstance()
                    .getLastKnownLocation();

            // TODO: at the moment we can dont care about cars so we just only
            // support one slotty ......
            final List<Position> lastPosList = CoreInfoHolder.getInstance()
                    .getDbManager().getLastLocationsList();

            if ((lastPosList != null) && (lastPosList.size() > 0)) {
                final Position pos = lastPosList.get(lastPosList.size() - 1);

                pos.setLatitude(loc.getLatitude());
                pos.setLongitude(loc.getLongitude());
                pos.setDatum(new Date(loc.getTime()));

                CoreInfoHolder.getInstance().getDbManager().updateLocation(pos);

            } else {
                CoreInfoHolder
                        .getInstance()
                        .getDbManager()
                        .addLocation(-1, loc.getLatitude(), loc.getLongitude(),
                                new Date(loc.getTime()));
            }
            if (CoreInfoHolder.getInstance().isSpeakit()) {
                SpeakItOut.speak(CoreInfoHolder.getInstance().getContext()
                        .getText(R.string.tts_location_saved).toString());
            }

            Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                    R.string.current_location_saved, Toast.LENGTH_SHORT).show();

        } else {

            if (Util.DEBUGMODE) {
                CoreInfoHolder
                        .getInstance()
                        .getDbManager()
                        .addLocation(-1, 48.208336, 16.372223,
                                new Date(System.currentTimeMillis()));

                Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                        "DEBUG position saved", Toast.LENGTH_SHORT).show();

            }
            Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                    R.string.current_location_empty, Toast.LENGTH_SHORT).show();
        }

        // update overlay
        CoreInfoHolder.getInstance().getParkingCarOverlay().refresh();
    }

    private void showParkschwein() {
        final DialogFragment df = ParkscheinFragment
                .newInstance(R.string.dlg_sms_title);
        df.show(getFragmentManager(), getText(R.string.dlg_sms_title)
                .toString());
    }

    private void buyParkschein() {
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(CoreInfoHolder.getInstance()
                        .getContext());

        final boolean check = sharedPreferences.getBoolean(
                "pref_sms_plausibility", true);

        if (check) {
            plausibilityCheck();
        } else {
            showParkschwein();
        }
    }

    private void plausibilityCheck() {

        // sunday ?
        // after 22h ?

        final GeoCodeResult address = CoreInfoHolder.getInstance()
                .getLastKnownAddress();
        if ((address != null) && (address.getCountry() != null)
                && (!address.getCountry().equalsIgnoreCase("Austria"))) {
            proceedDlg(R.string.sms_location_check1);
        } else if ((address != null) && (address.getCity() != null)
                && (!address.getCity().equalsIgnoreCase("Vienna"))) {
            proceedDlg(R.string.sms_location_check2);
        } else {
            // if our list is empty we are definitly not in kpz !?
            if ((CoreInfoHolder.getInstance().getVKPZCurrentList() == null)
                    || (CoreInfoHolder.getInstance().getVKPZCurrentList()
                            .size() == 0)) {
                proceedDlg(R.string.sms_location_check3);
            } else {
                showParkschwein();
            }

        }

        // return trotzdem;
    }

    private boolean result;

    private boolean proceedDlg(final int text) {
        new AlertDialog.Builder(getActivity())
                // .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.app_name)
                .setMessage(text)
                .setPositiveButton(R.string.sms_trotzdem_YES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int whichButton) {

                                showParkschwein();
                                result = true;
                            }
                        })
                .setNegativeButton(R.string.SMSNO,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int whichButton) {

                                result = false;
                            }
                        }).create().show();
        return result;
    }

}
