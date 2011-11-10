package at.the.gogo.parkoid.fragments;

import java.util.Iterator;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.speech.SpeakItOut;

public class OverviewFragment extends LocationListenerFragment {


    private TextView  kpzHeaderTitle;
    
    // private AddressListFragment adressFragment;
    private ListView  addressList;


    public static OverviewFragment newInstance() {
        final OverviewFragment f = new OverviewFragment();

        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.overview, null);

        initializeGUI(view);
        
        kpzHeaderTitle = (TextView) view.findViewById(R.id.kpz_list_header);

        addressList = (ListView) view.findViewById(R.id.kpz_list);

        // adressFragment = (AddressListFragment) getFragmentManager()
        // .findFragmentById(R.id.kpz_list);

        return view;
    }

   

//    @Override
//    protected void updateLocation() {
//
//
//        super.updateLocation();
//    }
//

    @Override
    public void updateInfoList(final Boolean inZone) {

        super.updateInfoList(inZone);
        
        if (inZone != null) {

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
                        caption = (String) vkpz.getProperties().get("type");
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

    private void pause() {
        // NOOP for now
    }

    private void resume() {
        // NOOP for now
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overview_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean result = false;
        return result;
    }

}
