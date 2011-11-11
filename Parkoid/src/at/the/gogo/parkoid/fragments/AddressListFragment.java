package at.the.gogo.parkoid.fragments;

import java.util.Iterator;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.CoreInfoHolder;

public class AddressListFragment extends ListFragment {

    int                  mPositionChecked = 0;
    int                  mPositionShown   = -1;

    private boolean      isInitialized    = false;

    public static String site_title;

    public static AddressListFragment newInstance() {
        final AddressListFragment f = new AddressListFragment();

        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        // setHasOptionsMenu(true);
        AddressListFragment.site_title = getResources().getString(
                R.string.app_name);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        restoreSavedState(savedInstanceState);
        // final SharedPreferences pref = PreferenceManager
        // .getDefaultSharedPreferences(getActivity());
        //
        // activeSite = Integer.parseInt(pref.getString("pref_site", "0"));

        final View view = super.onCreateView(inflater, container,
                savedInstanceState);

        return view;
    }

    // @Override
    // public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // inflater.inflate(R.menu.main_option_menu, menu);
    // super.onCreateOptionsMenu(menu, inflater);
    // }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceList", -1);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreSavedState(savedInstanceState);

        if (!isInitialized) {

            // NOOP for now
            isInitialized = true;
        }

        // getListView().setCacheColorHint(Color.TRANSPARENT);

        // if (mHasDetailsFrame) {
        // In dual-pane mode, the list view highlights the selected item.
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // Make sure our UI is in the correct state.
        // showDetails(mPositionChecked);
        // }
    }

    @Override
    public void onResume() {
        fillData();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoiceList", mPositionChecked);
        outState.putInt("shownChoiceList", mPositionShown);
    }

    public void refreshData(final boolean forceRefresh) {
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

                private String getKeyAt(final int ix) {
                    String result = null;
                    final Iterator<String> iterator = entries.keySet()
                            .iterator();
                    int i = 0;

                    while ((i <= ix) && (iterator.hasNext())) {
                        final String key = iterator.next();
                        if (i == ix) {
                            result = key;
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
                            inflater = LayoutInflater.from(CoreInfoHolder
                                    .getInstance().getContext());
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

                    holder.textView1.setText(getKeyAt(position));
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
            setListAdapter(adapter);
        }
    }
}
