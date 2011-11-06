package at.the.gogo.parkoid.fragments;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.Position;
import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;

public class OverviewFragment extends LocationListenerFragment {

    private TextView    currentAddress;

    // private boolean initialized = false;
    private ImageButton parkButton;
    private TextView    kpzHeaderTitle;
    private TextView    locationCaption;
    // private AddressListFragment adressFragment;
    private ListView    addressList;

    public static OverviewFragment newInstance() {
        final OverviewFragment f = new OverviewFragment();

        return f;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.overview, null);

        currentAddress = (TextView) view.findViewById(R.id.currentAddress);
        locationCaption = (TextView) view.findViewById(R.id.locationCaption);

        ((Button) view.findViewById(R.id.buy_ticket))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        buyParkschein();
                    }
                });

        ((Button) view.findViewById(R.id.check_location))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        updateLocation();
                    }
                });

        parkButton = (ImageButton) view.findViewById(R.id.parkButton);

        parkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveLocation();
            }
        });

        kpzHeaderTitle = (TextView) view.findViewById(R.id.kpz_list_header);

        addressList = (ListView) view.findViewById(R.id.kpz_list);

        // adressFragment = (AddressListFragment) getFragmentManager()
        // .findFragmentById(R.id.kpz_list);

        return view;
    }

    // @Override
    // public void onDestroyView() {
    //
    // try {
    // FragmentTransaction transaction = getSupportFragmentManager()
    // .beginTransaction();
    //
    // transaction.remove(adressFragment);
    // // transaction.remove(fragment);
    // // transaction.add(id, fragment);
    //
    // transaction.commitAllowingStateLoss();
    // } catch (Exception x) {
    //
    // Util.dd("Fragment TX failed with nested fragment");
    // }
    // super.onDestroyView();
    // }

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
            Toast.makeText(getActivity(), R.string.current_location_saved,
                    Toast.LENGTH_SHORT).show();

        } else {

            if (Util.DEBUGMODE) {
                CoreInfoHolder
                        .getInstance()
                        .getDbManager()
                        .addLocation(-1, 48.208336, 16.372223,
                                new Date(System.currentTimeMillis()));

                Toast.makeText(getActivity(), "DEBUG position saved",
                        Toast.LENGTH_SHORT).show();

            }
            Toast.makeText(getActivity(), R.string.current_location_empty,
                    Toast.LENGTH_SHORT).show();
        }

        // update overlay
        CoreInfoHolder.getInstance().getParkingCarOverlay().refresh();
    }

    @Override
    protected void updateLocation() {

        final Location location = CoreInfoHolder.getInstance()
                .getLastKnownLocation();
        if ((location != null) && (location.hasAccuracy())) {
            final String newTitle = getText(R.string.current_location)
                    + " (+/-" + Math.round(location.getAccuracy()) + "m)";
            locationCaption.setText(newTitle);
        }

        super.updateLocation();
    }

    private void showParkschwein() {
        final DialogFragment df = ParkscheinFragment
                .newInstance(R.string.dlg_sms_title);
        df.show(getFragmentManager(), getText(R.string.dlg_sms_title)
                .toString());
    }

    private void buyParkschein() {
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        final boolean check = sharedPreferences.getBoolean(
                "pref_sms_plausibility", true);

        if (check) {
            plausibilityCheck();
        } else {
            showParkschwein();
        }
    }

    private void plausibilityCheck() {
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

    @Override
    public void updateAddressField(final GeoCodeResult address) {
        currentAddress.setText(formatAddress(address));
    }

    @Override
    public void updateAddressList(final Boolean inZone) {

        if (inZone != null) {
            if (inZone) {
                parkButton.setImageResource(R.drawable.parken_danger);

            } else {
                parkButton.setImageResource(R.drawable.parken);
            }

            // fill list (again)
            refreshList(true);

            setkpzTitle(getText(
                    (inZone) ? R.string.near_kpz : R.string.no_near_kpz)
                    .toString());
            Toast.makeText(getActivity(),
                    (inZone) ? R.string.near_kpz : R.string.no_near_kpz,
                    Toast.LENGTH_SHORT).show();

        } else {
            setkpzTitle(getText(R.string.unknown_near_kpz).toString());
            parkButton.setImageResource(R.drawable.parken_unknown);
            Toast.makeText(getActivity(), R.string.unknown_near_kpz,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setkpzTitle(final String title) {
        final String newTitle = title + " (+/-"
                + CoreInfoHolder.getInstance().getAccuracyUsed() + "m)";
        kpzHeaderTitle.setText(newTitle);
    }

    public void refreshList(final boolean forceRefresh) {
        // refresh = forceRefresh;
        fillData();
    }

    public void fillData() {

        final GetAddressData asyncTask = new GetAddressData();
        asyncTask.execute((Void) null);
    }

    public static class ViewHolder {
        TextView  textView1;
        TextView  textView2;
        TextView  textView3;
        ImageView icon1;
        ImageView icon2;
    }

    public class GetAddressData extends
            AsyncTask<Void, Void, Map<String, ViennaKurzParkZone>> {

        @Override
        protected Map<String, ViennaKurzParkZone> doInBackground(
                final Void... params) {
            return CoreInfoHolder.getInstance().getVKPZCurrentList();
        }

        @Override
        protected void onPostExecute(
                final Map<String, ViennaKurzParkZone> entries) {

            final ListAdapter adapter = new BaseAdapter() {

                private LayoutInflater inflater = null;

                @Override
                public int getCount() {
                    return (entries != null) ? entries.size() : 0;
                }

                @Override
                public long getItemId(final int position) {
                    return position;
                }

                private ViennaKurzParkZone getElementAt(final int ix) {
                    ViennaKurzParkZone result = null;
                    final Iterator<String> iterator = entries.keySet()
                            .iterator();
                    int i = 0;

                    while ((i <= ix) && (iterator.hasNext())) {
                        final String key = iterator.next();
                        if (i == ix) {
                            result = entries.get(key);
                        }
                        i++;
                    }
                    return result;
                }

                // private String getKeyAt(final int ix) {
                // String result = null;
                // final Iterator<String> iterator = entries.keySet()
                // .iterator();
                // int i = 0;
                //
                // while ((i <= ix) && (iterator.hasNext())) {
                // final String key = iterator.next();
                // if (i == ix) {
                // result = key;
                // }
                // i++;
                // }
                // return result;
                // }

                @Override
                public View getView(final int position, View convertView,
                        final ViewGroup parent) {

                    ViewHolder holder = null;

                    if (convertView == null) {
                        if (inflater == null) {
                            inflater = LayoutInflater.from(getActivity());
                        }
                        convertView = inflater.inflate(R.layout.address_item,
                                null);

                        holder = new ViewHolder();

                        holder.textView1 = (TextView) convertView
                                .findViewById(R.id.AddressLine1);
                        holder.textView2 = (TextView) convertView
                                .findViewById(R.id.AddressLine2);
                        holder.textView3 = (TextView) convertView
                                .findViewById(R.id.AddressLine3);

                        holder.icon1 = (ImageView) convertView
                                .findViewById(R.id.imageView1);

                        convertView.setTag(holder);

                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    final ViennaKurzParkZone vkpz = getElementAt(position);

                    String caption = "";

                    if (vkpz.getProperties().containsKey("strasse")) {
                        caption = (String) vkpz.getProperties().get("strasse");
                        if (vkpz.getProperties().containsKey("geltungsbereich")) {
                            caption += " "
                                    + vkpz.getProperties().get(
                                            "geltungsbereich");
                        }

                    } else if (vkpz.getProperties().containsKey("bezirk")) {
                        caption = vkpz.getProperties().get("bezirk")
                                + ". Bezirk ";
                    } else {
                        caption = (String)vkpz.getProperties().get("type");
                    }

                    holder.textView1.setText(caption);
                    holder.textView2.setText("max Dauer: "
                            + vkpz.getProperties().get("dauer"));
                    holder.textView3.setText("Zeitraum: "
                            + vkpz.getProperties().get("zeitraum"));

                    return convertView;
                }

                @Override
                public Object getItem(final int position) {
                    return entries.get(position);
                }
            };
            addressList.setAdapter(adapter);
        }
    }

}
