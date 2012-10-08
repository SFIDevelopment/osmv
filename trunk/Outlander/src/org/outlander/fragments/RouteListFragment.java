package org.outlander.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.outlander.R;
import org.outlander.io.XML.SimpleXML;
import org.outlander.model.PoiCategory;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;
import org.outlander.model.RouteCategory;
import org.outlander.overlays.RouteOverlay;
import org.outlander.overlays.TrackOverlay;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.views.SectionedListViewAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class RouteListFragment extends SherlockListFragment implements PageChangeNotifyer {

    boolean                           mHasDetailsFrame;
    int                               mPositionChecked = 0;
    int                               mPositionShown   = -1;

    private QuickAction               mQuickAction;
    private int                       selectedItemId   = -1;
    private TextView                  nrOfEntries;
    private boolean                   isInitialized    = false;
    protected ExecutorService         mThreadPool      = Executors.newFixedThreadPool(2);

    boolean                           showDetails;

    private SimpleInvalidationHandler mHandler;

    public class SimpleInvalidationHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {

                case R.id.menu_exporttogpxpoi:
                    if (msg.arg1 == 0) {
                        Toast.makeText(getActivity(), getString(R.string.message_error) + " " + (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getActivity(), getString(R.string.message_trackexported) + " " + (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    public static RouteListFragment newInstance() {
        final RouteListFragment f = new RouteListFragment();

        // Bundle args = new Bundle();
        // args.putInt("num", num);
        // f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CoreInfoHandler.getInstance().getMainActivity());

        showDetails = sharedPreferences.getBoolean("pref_details_route", true);

        restoreSavedState(savedInstanceState);
        //
        final View view = inflater.inflate(R.layout.headerlist, null);

        final ImageView icon = (ImageView) view.findViewById(R.id.menuButton);
        final TextView header = (TextView) view.findViewById(R.id.caption1);
        final Button btnMenu = (Button) view.findViewById(R.id.button_menu);

        btnMenu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                showDetails = !showDetails;

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CoreInfoHandler.getInstance().getMainActivity());
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("pref_details_route", showDetails);
                editor.commit();

                getListView().invalidateViews();

            }
        });

        icon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                // getActivity().openOptionsMenu();

                // CoreInfoHandler
                // .getInstance()
                // .gotoPage(
                // FragmentFactory
                // .getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));

            }
        });

        header.setText(R.string.tab_routes);

        nrOfEntries = (TextView) view.findViewById(R.id.caption2);
        nrOfEntries.setText(R.string.EntriesInCategory);

        header.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                getActivity().openOptionsMenu();
            }
        });

        return view;

    }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceRouteList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceRouteList", -1);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreSavedState(savedInstanceState);

        if (!isInitialized) {
            // getListView().addHeaderView(headerView);
            mQuickAction = new QuickAction(getActivity());
            setupQuickAction(mQuickAction);
            isInitialized = true;
        }

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        // if (mHasDetailsFrame) {
        // // In dual-pane mode, the list view highlights the selected item.
        // getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // // Make sure our UI is in the correct state.
        // showDetails(mPositionChecked);
        // }
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoiceRouteList", mPositionChecked);
        outState.putInt("shownChoiceRouteList", mPositionShown);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        selectedItemId = (int) id;
        if (selectedItemId > -1) {
            mQuickAction.show(v);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.routelist_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    // @Override
    // public boolean onOptionsItemSelected(final MenuItem item) {
    // super.onOptionsItemSelected(item);
    //
    // switch (item.getItemId()) {
    // case R.id.menu_importpoi:
    // startActivityForResult((new Intent(getActivity(),
    // ImportPoiActivity.class)), R.id.menu_importpoi);
    //
    // case R.id.menu_routecategorylist:
    // startActivity((new Intent(getActivity(),
    // RouteCategoryListActivity.class)));
    // break;
    // case R.id.menu_addroute:
    // Toast.makeText(getActivity(), R.string.NYI, Toast.LENGTH_SHORT)
    // .show();
    // break;
    // case R.id.menu_deleteallroutes:
    // Toast.makeText(getActivity(), R.string.NYI, Toast.LENGTH_SHORT)
    // .show();
    // break;
    // }
    // return true;
    // }

    private void resume() {
        fillData();
    }

    public void fillData() {

        final List<Route> routeList = CoreInfoHandler.getInstance().getDBManager(getActivity()).getAllRoutes();

        final List<RouteCategory> catList = CoreInfoHandler.getInstance().getDBManager(getActivity()).getRouteCategories();

        if (nrOfEntries != null) {
            final String newHeaderDescr = getString(R.string.EntriesInCategory) + (routeList.size());
            nrOfEntries.setText(newHeaderDescr);
        }

        final ListAdapter adapter = new SectionedListViewAdapter() {

            LayoutInflater inflater = null;

            @Override
            public int getCount() {

                return routeList.size();
            }

            @Override
            public Object getItem(final int paramInt) {

                return routeList.get(paramInt);
            }

            @Override
            public long getItemId(final int paramInt) {

                return routeList.get(paramInt).getId();
            }

            @Override
            protected void onNextPageRequested(final int page) {

            }

            @Override
            protected void bindSectionHeader(final View view, final int position, final boolean displaySectionHeader) {

                if (displaySectionHeader) {
                    view.findViewById(R.id.header).setVisibility(View.VISIBLE);
                    final TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
                    lSectionTitle.setText(((PoiCategory) getSections()[getSectionForPosition(position)]).Title);
                }
                else {
                    view.findViewById(R.id.header).setVisibility(View.GONE);
                }

            }

            @Override
            public View getAmazingView(final int position, final View convertView, final ViewGroup parent) {

                View view = convertView;
                if (view == null) {
                    if (inflater == null) {
                        inflater = LayoutInflater.from(getActivity());
                    }

                    view = inflater.inflate(R.layout.poilist_item, null);
                }
                final Route route = (Route) getItem(position);

                final ViewHolder vhc = new ViewHolder();
                vhc.textView1 = (TextView) view.findViewById(android.R.id.text1);
                vhc.textView2 = (TextView) view.findViewById(android.R.id.text2);
                vhc.textView3 = (TextView) view.findViewById(R.id.infotext3);
                vhc.groupHeader = (TextView) view.findViewById(R.id.groupheader);
                vhc.icon1 = (ImageView) view.findViewById(R.id.ImageView01);
                vhc.icon2 = (ImageView) view.findViewById(R.id.ImageView02);

                vhc.textView1.setText(route.getName());
                vhc.icon1.setVisibility(View.GONE);
                //
                String descr = "";
                if (showDetails) {
                    descr = route.getDescr();
                }
                vhc.textView2.setText(descr);

                vhc.icon2.setImageResource(R.drawable.list_route);

                final double distance = GeoMathUtil.distance(route.getGeoPoints());
                //
                final String distanceText = GeoMathUtil.getHumanDistanceString(distance, CoreInfoHandler.getInstance().getDistanceUnitFormatId());
                //
                final String stat = "# Waypoints: " + route.getPoints().size() + " | ↔ " + distanceText;

                vhc.textView3.setText(stat);

                final CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);

                cb.setChecked(route.isShow());

                cb.setOnClickListener(new OnClickListener() {

                    int itemId = route.getId();

                    @Override
                    public void onClick(final View v) {

                        CoreInfoHandler.getInstance().getDBManager(getActivity()).setRouteChecked(itemId, ((CheckBox) v).isChecked());
                        refreshRoute();
                    }
                });

                return view;
            }

            @Override
            public void configurePinnedHeader(final View header, final int position, final int alpha) {

                final TextView lSectionHeader = (TextView) header;
                lSectionHeader.setText(((RouteCategory) getSections()[getSectionForPosition(position)]).Title);
                lSectionHeader.setBackgroundColor((alpha << 24) | (0xbbffbb));
                lSectionHeader.setTextColor((alpha << 24) | (0x000000));

            }

            @Override
            public int getPositionForSection(final int section) {
                int ix = 0;
                for (final Route route : routeList) {
                    if (route.getCategory() == section) {
                        break;
                    }
                    ix++;
                }

                return ix;
            }

            private int getSectionIx(final int categoryId) {
                int catId = 0;
                for (final RouteCategory cat : catList) {

                    if (cat.getId() == categoryId) {
                        break;
                    }
                    catId++;
                }
                return catId;
            }

            @Override
            public int getSectionForPosition(final int position) {

                return getSectionIx(routeList.get(position).getCategory());
            }

            @Override
            public Object[] getSections() {
                return catList.toArray();
            }

        };

        setListAdapter(adapter);
    }

    public static class ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView groupHeader;
        ImageView       icon1;
        ImageView       icon2;
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a whole new
     * activity in which it is displayed.
     */
    void showDetails(final int index) {
        mPositionChecked = index;

        if (mHasDetailsFrame) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            if (mPositionShown != mPositionChecked) {
                // If we are not currently showing a fragment for the new
                // position, we need to create and install a new one.
                // DetailsFragment df = DetailsFragment.newInstance(index);
                //
                // // Execute a transaction, replacing any existing fragment
                // // with this one inside the frame.
                // getFragmentManager()
                // .beginTransaction()
                // .replace(R.id.frame_details, df)
                // .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                // .commit();

                mPositionShown = index;
            }

        }
        else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            final Intent intent = new Intent();
            // intent.setClass(getActivity(), DetailsActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
        }
    }

    private void setupQuickAction(final QuickAction quickAction) {

        // ------
        ActionItem item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_edit));
        item.setIcon(getResources().getDrawable(R.drawable.menu_edit));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_show));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_delete));
        item.setIcon(getResources().getDrawable(R.drawable.menu_delete));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_exporttogpx));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_exporttokml));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------

        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final int pos) {
                if (pos == 0) {
                    handleContextItemSelected(R.id.menu_editpoi);
                }
                else if (pos == 1) {
                    handleContextItemSelected(R.id.menu_show);
                }
                else if (pos == 2) {
                    handleContextItemSelected(R.id.menu_deletepoi);
                }
                else if (pos == 3) {
                    handleContextItemSelected(R.id.menu_exporttogpxpoi);
                }
                else if (pos == 4) {
                    handleContextItemSelected(R.id.menu_exporttokmlpoi);
                }
            }
        });

    }

    // ---------------------------

    private void editRoute(final Route route) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final DialogFragment dialogFragment = RouteDialogFragment.newInstance(route.getId(), route.getName(), route.getDescr(), R.string.dialogTitleRoute);
        dialogFragment.show(ft, "RouteDialog");
    }

    private void sendMsgToOverlay(String cmd, int routeId) {
        Intent intent = new Intent(cmd);
        if (routeId > -1)
            intent.putExtra(RouteOverlay.ROUTE_ID, routeId);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    /**
     * if in tablet mode refresh visible tracks on the fly
     * 
     * @param track
     */
    private void refreshRoute() {

        // if (Ut.isMultiPane(getActivity())) {
        // CoreInfoHandler.getInstance().setMapCmd(
        // MapFragment.MAP_CMD_SHOW_ROUTE);
        // CoreInfoHandler.getInstance().getMapFragment().refresh();
        // } else {
        // CoreInfoHandler.getInstance().getRouteOverlay().refreshRoute();
        // }

        sendMsgToOverlay(RouteOverlay.ROUTE_REFRESH, -1);
    }

    private void showRoute(final Route route) {

        CoreInfoHandler.getInstance().getDBManager(getActivity()).setRouteChecked(selectedItemId, true);

        CoreInfoHandler.getInstance().setCurrentRouteId(route.getId());
        CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_SHOW_ROUTE);

        sendMsgToOverlay(RouteOverlay.ROUTE_FOCUS, route.getId());

        if (Ut.isMultiPane(getActivity())) {
            CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));
        }
        else {

            getActivity().finish();
        }
    }

    private void doExportRouteKML(final int id) {
        // showDialog(R.id.dialog_wait);
        final int routeid = id;

        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                final Route route = CoreInfoHandler.getInstance().getDBManager(getActivity()).getRoute(routeid);

                final File folder = Ut.getTschekkoMapsExportDir(RouteListFragment.this.getActivity());

                // ExportTrack.exportTrackAsKML(track,folder,mHandler);

                final SimpleXML xml = new SimpleXML("kml");
                xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
                xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");

                final SimpleXML Placemark = xml.createChild("Placemark");
                Placemark.createChild("name").setText(route.getName());
                Placemark.createChild("description").setText(route.getDescr());
                final SimpleXML LineString = Placemark.createChild("LineString");
                final SimpleXML coordinates = LineString.createChild("coordinates");
                final StringBuilder builder = new StringBuilder();

                for (final PoiPoint tp : route.getPoints()) {
                    builder.append(tp.getGeoPoint().getLongitude()).append(",").append(tp.getGeoPoint().getLatitude()).append(",").append(tp.getAlt())
                            .append(" ");
                }
                coordinates.setText(builder.toString().trim());

                final String filename = folder.getAbsolutePath() + "/Route-" + route.getName() + ".kml";
                final File file = new File(filename);
                FileOutputStream out;
                try {
                    file.createNewFile();
                    out = new FileOutputStream(file);
                    final OutputStreamWriter wr = new OutputStreamWriter(out);
                    wr.write(SimpleXML.saveXml(xml));
                    wr.close();
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0, filename).sendToTarget();
                }
                catch (final FileNotFoundException e) {
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
                    e.printStackTrace();
                }
                catch (final IOException e) {
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
                    e.printStackTrace();
                }
                // dlgWait.dismiss();
            }
        });

    }

    private void doExportRouteGPX(final int id) {
        // getActivity().showDialog(R.id.dialog_wait);
        final int routeid = id;

        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                final Route route = CoreInfoHandler.getInstance().getDBManager(getActivity()).getRoute(routeid);

                // SimpleDateFormat formatter = new SimpleDateFormat(
                // "yyyy-MM-dd'T'HH:mm:ss'Z'");
                final SimpleXML xml = new SimpleXML("gpx");
                xml.setAttr("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
                xml.setAttr("xmlns", "http://www.topografix.com/GPX/1/1");
                xml.setAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                xml.setAttr("creator", "RMaps - http://code.google.com/p/robertprojects/");
                xml.setAttr("version", "1.1");

                final SimpleXML meta = xml.createChild("metadata");
                meta.createChild("name").setText(route.getName());
                meta.createChild("desc").setText(route.getDescr());
                final SimpleXML autor = meta.createChild("author");
                autor.createChild("name").setText("RMaps");

                final SimpleXML rte = xml.createChild("rte");
                for (final PoiPoint tp : route.getPoints()) {

                    final SimpleXML rtpt = rte.createChild("rtept");
                    rtpt.setAttr("lat", Double.toString(tp.getGeoPoint().getLatitude()));
                    rtpt.setAttr("lon", Double.toString(tp.getGeoPoint().getLongitude()));
                    rtpt.createChild("ele").setText(Double.toString(tp.getAlt()));
                    rtpt.createChild("desc").setText(tp.getDescr());
                }

                final File folder = Ut.getTschekkoMapsExportDir(getActivity());
                final String filename = folder.getAbsolutePath() + "/Route-" + route.getName() + ".gpx";
                final File file = new File(filename);
                FileOutputStream out;
                try {
                    file.createNewFile();
                    out = new FileOutputStream(file);
                    final OutputStreamWriter wr = new OutputStreamWriter(out);
                    wr.write(SimpleXML.saveXml(xml));
                    wr.close();
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0, filename).sendToTarget();
                }
                catch (final FileNotFoundException e) {
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
                    e.printStackTrace();
                }
                catch (final IOException e) {
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
                    e.printStackTrace();
                }

                // dlgWait.dismiss();
            }
        });

    }

    // -------------------------------------

    private void deleteRoute(final Route route) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning_delete_route).setCancelable(false).setPositiveButton(R.string.dialogYES, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int id) {

                CoreInfoHandler.getInstance().getDBManager(getActivity()).deleteRoute(route.getId());
                refresh();

                sendMsgToOverlay(RouteOverlay.ROUTE_DEL, route.getId());

                dialog.dismiss();
            }
        }).setNegativeButton(R.string.dialogNO, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void addRoute() {
        Toast.makeText(getActivity(), R.string.NYI, Toast.LENGTH_SHORT).show();
    }

    private void deleteAllRoutes() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning_delete_all_routes).setCancelable(false)
                .setPositiveButton(R.string.dialogYES, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {

                        CoreInfoHandler.getInstance().getDBManager(getActivity()).deleteAllRoutes();

                        dialog.dismiss();
                        refreshRoute();
                        refresh();
                    }
                }).setNegativeButton(R.string.dialogNO, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void importRoutes() {
        final DialogFragment newFragment = ImportDialogFragment.newInstance(R.string.dialogTitleImport);
        CoreInfoHandler.getInstance().getMainActivity().showDialog(newFragment);
    }

    private void routeCategoryList() {
        CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_CAT));

    }

    private void handleContextItemSelected(final int id) {

        final Route route = CoreInfoHandler.getInstance().getDBManager(getActivity()).getRoute(selectedItemId);

        switch (id) {
            case R.id.menu_editpoi: {
                editRoute(route);
                break;
            }
            case R.id.menu_show: {
                showRoute(route);
                break;
            }
            case R.id.menu_deletepoi: {
                deleteRoute(route);
                break;
            }
            case R.id.menu_exporttogpxpoi:
                doExportRouteGPX(selectedItemId);
                break;
            case R.id.menu_exporttokmlpoi:
                doExportRouteKML(selectedItemId);
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.menu_addroute: {
                addRoute();
                result = true;
                break;
            }
            case R.id.menu_deleteallroutes: {
                deleteAllRoutes();
                result = true;
                break;
            }
            case R.id.menu_importpoi: {
                importRoutes();
                result = true;
                break;
            }
            case R.id.menu_routecategorylist: {
                routeCategoryList();
                result = true;
                break;
            }
            case R.id.menu_showall: {
                showHideAll(true);
                result = true;
                break;
            }
            case R.id.menu_hideall: {
                showHideAll(false);
                result = true;
                break;
            }
        }
        return result;
    }

    private void showHideAll(final boolean show) {
        CoreInfoHandler.getInstance().getDBManager(getActivity()).setRoutesChecked(show);
        resume();
        CoreInfoHandler.getInstance().getRouteOverlay().refresh();
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
        resume();
    }

}
