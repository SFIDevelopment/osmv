package org.outlander.fragments;

import java.util.ArrayList;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.andnav.osm.util.GeoPoint;
import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.outlander.R;
import org.outlander.constants.DBConstants;
import org.outlander.model.PoiPoint;
import org.outlander.overlays.PoiOverlay;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public class ToponymSearchResultFragment extends SherlockListFragment implements PageChangeNotifyer {

    int                         mPositionChecked = 0;
    int                         mPositionShown   = -1;
    private QuickAction         mQuickAction;
    private boolean             isInitialized    = false;
    private ToponymSearchResult searchResult     = null;
    private String              searchString;
    private ProgressDialog      dlgWait;
    private TextView            nrOfEntries;

    public static ToponymSearchResultFragment newInstance(final int num) {
        final ToponymSearchResultFragment f = new ToponymSearchResultFragment();

        // Supply num input as an argument.
        final Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    private ToponymSearchResult searchForLocations(final String searchString) {
        ToponymSearchResult searchResult = null;
        try {
            if (Ut.isInternetConnectionAvailable(getActivity())) {
                // String language = PreferenceManager
                // .getDefaultSharedPreferences(this).getString(
                // "pref_googlelanguagecode", "en");

                WebService.setUserName(WebService.USERNAME);
                searchResult = WebService.search(searchString, null, null, null, 0);

                addLocationsToOverlay(searchResult);
            }
        }
        catch (final Exception e) {
            Ut.e("Toposearch failed with " + e.getMessage());
        }

        return searchResult;
    }

    private void addLocationsToOverlay(final ToponymSearchResult searchResult) {

        CoreInfoHandler.getInstance().getDBManager(getActivity()).deletePoisOfCategoryTopo();

        GeoPoint gpoint = null;
        final List<PoiPoint> points = new ArrayList<PoiPoint>();
        for (final Toponym toponym : searchResult.getToponyms()) {
            final PoiPoint point = new PoiPoint();

            gpoint = new GeoPoint(toponym.getLatitude(), toponym.getLongitude());

            point.setGeoPoint(gpoint);
            point.setTitle(toponym.getName());
            point.setDescr(toponym.getCountryName());
            point.setIconId(R.drawable.poiyellow);
            point.setCategoryId(DBConstants.POI_CATEGORY_TOPO);

            points.add(point);

            CoreInfoHandler.getInstance().getDBManager(getActivity()).updatePoi(point);

        }
        sendMsgToOverlay(PoiOverlay.POI_REFRESH, -1);

        // CoreInfoHandler.getInstance().getRouteOverlay().refresh();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.headerlist, null);

        final ImageView icon = (ImageView) view.findViewById(R.id.menuButton);
        final TextView header = (TextView) view.findViewById(R.id.caption1);
        final Button btnMenu = (Button) view.findViewById(R.id.button_menu);
        btnMenu.setVisibility(View.GONE);

        icon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                getActivity().openOptionsMenu();
            }
        });

        header.setText(R.string.tab_topo);

        nrOfEntries = (TextView) view.findViewById(R.id.caption2);
        nrOfEntries.setText(R.string.EntriesInCategory);

        header.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                getActivity().openOptionsMenu();
            }
        });

        restoreSavedState(savedInstanceState);

        return view;

    }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceTopoList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceTopoList", -1);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreSavedState(savedInstanceState);
        if (!isInitialized) {

            mQuickAction = new QuickAction(getActivity());
            setupQuickAction(mQuickAction);
            isInitialized = true;
        }
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoiceTopoList", mPositionChecked);
        outState.putInt("shownChoiceTopoList", mPositionShown);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        mPositionChecked = (int) id;
        mQuickAction.show(v);
    }

    private void resume() {
        fillData();
    }

    private void sendMsgToOverlay(String cmd, int id) {
        Intent intent = new Intent(cmd);
        if (id > -1)
            intent.putExtra(PoiOverlay.POI_ID, id);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    public void fillData() {
        // changed ?
        if (((CoreInfoHandler.getInstance().getTopoSearchString() != null) && (searchString == null))
                || ((CoreInfoHandler.getInstance().getTopoSearchString() != null) && (searchString != null) && (!CoreInfoHandler.getInstance()
                        .getTopoSearchString().equals(searchString)))) {
            searchString = CoreInfoHandler.getInstance().getTopoSearchString();

            if (searchString != null) {
                final GetData asyncTask = new GetData();
                dlgWait = Ut.ShowWaitDialog(getActivity(), 0);
                asyncTask.execute(searchString);
            }
        }
    }

    public class GetData extends AsyncTask<String, Void, ToponymSearchResult> {

        @Override
        protected ToponymSearchResult doInBackground(final String... params) {

            searchResult = searchForLocations(params[0]);

            return searchResult;
        }

        @Override
        protected void onPostExecute(final ToponymSearchResult searchResult) {
            try {

                if (!isDetached()) {
                    if (nrOfEntries != null) {
                        final String newHeaderDescr = getString(R.string.EntriesInCategory)
                                + ((searchResult != null) ? searchResult.getTotalResultsCount() : 0);
                        nrOfEntries.setText(newHeaderDescr);
                    }
                }
                final ListAdapter adapter = new BaseAdapter() {

                    GeoPoint               point;
                    private LayoutInflater inflater = null;

                    @Override
                    public int getCount() {
                        return (searchResult != null) ? searchResult.getToponyms().size() : 0;// searchResult.getTotalResultsCount();
                    }

                    @Override
                    public long getItemId(final int position) {
                        return position;
                    }

                    @Override
                    public View getView(final int position, View convertView, final ViewGroup parent) {

                        ViewHolder holder = null;

                        if (convertView == null) {
                            if (inflater == null) {
                                inflater = LayoutInflater.from(getActivity());
                            }
                            convertView = inflater.inflate(R.layout.list_item, null);
                            holder = new ViewHolder();
                            holder.textView1 = (TextView) convertView.findViewById(android.R.id.text1);
                            holder.textView2 = (TextView) convertView.findViewById(android.R.id.text2);
                            holder.icon1 = (ImageView) convertView.findViewById(R.id.ImageView01);
                            holder.icon2 = (ImageView) convertView.findViewById(R.id.ImageView02);

                            holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
                            holder.checkbox.setVisibility(View.GONE);

                            convertView.setTag(holder);
                        }
                        else {
                            holder = (ViewHolder) convertView.getTag();
                        }
                        final Toponym toponym = searchResult.getToponyms().get(position);

                        if (point == null) {
                            point = new GeoPoint(toponym.getLatitude(), toponym.getLongitude());
                        }
                        else {
                            point.setLatitude(toponym.getLatitude());
                            point.setLongitude(toponym.getLongitude());
                        }
                        final String locationtext = GeoMathUtil.formatGeoPoint(point, CoreInfoHandler.getInstance().getCoordFormatId());

                        holder.textView1.setText(toponym.getName() + " [" + toponym.getCountryName() + "]");
                        holder.textView2.setText(locationtext);
                        holder.icon2.setImageResource(R.drawable.icon);
                        holder.icon1.setImageResource(R.drawable.poiyellow);

                        return convertView;
                    }

                    @Override
                    public Object getItem(final int position) {
                        return searchResult.getToponyms().get(position);
                    }
                };

                setListAdapter(adapter);

                if ((searchResult != null) && ((searchResult.getToponyms() != null) && (searchResult.getToponyms().size() > 0))) {
                    showTopoResultAuto(searchResult.getToponyms().get(0));
                }

            }
            catch (final Exception x) {
                Ut.d("Toponym onPostExecute: " + x.toString());
            }

            if (dlgWait != null) {
                dlgWait.dismiss();
            }

        }

    }

    public static class ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView groupHeader;
        ImageView       icon1;
        ImageView       icon2;
        CheckBox        checkbox;
    }

    private void setupQuickAction(final QuickAction quickAction) {

        // ------
        ActionItem item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_show));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        // ------
        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_getWikiPedia));
        item.setIcon(getResources().getDrawable(R.drawable.menu_wikipedia));

        quickAction.addActionItem(item);

        // ------
        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_shareLocation));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_addpoi));
        item.setIcon(getResources().getDrawable(R.drawable.menu_add));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_showexternalpoi));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final int pos) {
                if (pos == 0) {
                    handleContextItemSelected(R.id.menu_show);
                }
                else if (pos == 1) {
                    handleContextItemSelected(R.id.menu_getWikiPedia);
                }
                else if (pos == 2) {
                    handleContextItemSelected(R.id.menu_shareLocation);
                }
                else if (pos == 3) {
                    handleContextItemSelected(R.id.menu_addpoi);
                }
                else if (pos == 4) {
                    handleContextItemSelected(R.id.menu_showexternalpoi);
                }
            }
        });

    }

    private void handleContextItemSelected(final int id) {

        final Toponym toponym = searchResult.getToponyms().get(mPositionChecked);

        switch (id) {
            case R.id.menu_show:

                showTopoResult(toponym);

                break;

            case R.id.menu_getWikiPedia: {

                searchWiki(toponym);

                break;
            }

            case R.id.menu_addpoi:

                addPoi(toponym);

                break;

            case R.id.menu_showexternalpoi:
                showExternalInformation(toponym);
                break;
            case R.id.menu_shareLocation: {

                final Intent intent = Ut.shareLocation(toponym.getLatitude(), toponym.getLongitude(), toponym.getName() + "\n" + toponym.getCountryName(),
                        getActivity());

                if (intent != null) {
                    startActivity(intent);
                }
                // Toast.makeText(this, R.string.NYI, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private void searchWiki(final Toponym toponym) {

        final GeoPoint point = new GeoPoint(toponym.getLatitude(), toponym.getLongitude());

        CoreInfoHandler.getInstance().setCurrentSearchPoint(point);
        CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_WIKI));
    }

    private void addPoi(final Toponym toponym) {
        final DialogFragment newFragment = PoiDialogFragment.newInstance(-1, toponym.getName(), toponym.getCountryName(), toponym.getLatitude(),
                toponym.getLongitude(), R.string.dialogTitlePOI);
        showDialog(newFragment);
    }

    private void showTopoResultAuto(final Toponym toponym) {

        if (Ut.isMultiPane(getActivity())) {
            showTopoResult(toponym);
        }
    }

    private void showTopoResult(final Toponym toponym) {

        CoreInfoHandler.getInstance().setCurrentToponym(toponym);
        CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_SHOW_SEARCH);

        if (Ut.isMultiPane(getActivity())) {
            CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));
        }
        else {

            getActivity().finish();
        }

    }

    private void showExternalInformation(final Toponym toponym) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + toponym.getLatitude() + "," + toponym.getLongitude() + "?z=18"));
            startActivity(intent);
        }
        catch (final ActivityNotFoundException x) {
            Toast.makeText(getActivity(), R.string.no_activity, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void pageGetsActivated() {
        resume();
    }

    @Override
    public void pageGetsDeactivated() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        resume();
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

}
