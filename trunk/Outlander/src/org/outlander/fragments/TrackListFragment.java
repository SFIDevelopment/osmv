package org.outlander.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.outlander.R;
import org.outlander.io.XML.SimpleXML;
import org.outlander.model.Track;
import org.outlander.model.TrackPoint;
import org.outlander.overlays.TrackOverlay;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TrackListFragment extends SherlockListFragment implements PageChangeNotifyer {

    int                               mPositionChecked = 0;
    int                               mPositionShown   = -1;

    private QuickAction               mQuickAction;
    private int                       selectedItemId   = -1;
    private TextView                  nrOfEntries;
    private boolean                   isInitialized    = false;

    protected ExecutorService         mThreadPool      = Executors.newFixedThreadPool(2);

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

    public static TrackListFragment newInstance() {
        final TrackListFragment f = new TrackListFragment();

        // // Supply num input as an argument.
        // Bundle args = new Bundle();
        // args.putInt("num", num);
        // f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        restoreSavedState(savedInstanceState);

        final View view = inflater.inflate(R.layout.headerlist, null);

        final ImageView icon = (ImageView) view.findViewById(R.id.menuButton);
        final TextView header = (TextView) view.findViewById(R.id.caption1);
        final Button btnMenu = (Button) view.findViewById(R.id.button_menu);
        btnMenu.setVisibility(View.GONE);

        LinearLayout ll = (LinearLayout) view.findViewById(R.id.header1);
        ll.setBackgroundResource(R.drawable.box_header_blue);
        ll = (LinearLayout) view.findViewById(R.id.header2);
        ll.setBackgroundResource(R.drawable.box_header_blue);
        ll = (LinearLayout) view.findViewById(R.id.header3);
        ll.setBackgroundResource(R.drawable.box_header_blue);

        
        icon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                // getActivity().openOptionsMenu();
                CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));

            }
        });

        header.setText(R.string.tab_tracks);

        nrOfEntries = (TextView) view.findViewById(R.id.caption2);
        nrOfEntries.setText(R.string.EntriesInCategory);

        header.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                getActivity().openOptionsMenu();
            }
        });

        return view;

        // return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceTrackList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceTrackList", -1);
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

        outState.putInt("curChoiceTrackList", mPositionChecked);
        outState.putInt("shownChoiceTracckList", mPositionShown);
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
        inflater.inflate(R.menu.tracklist_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void resume() {
        fillData();
    }

    public void fillData() {
        final Cursor c = CoreInfoHandler.getInstance().getDBManager(getActivity()).getGeoDatabase().getTrackListCursor();

        getActivity().startManagingCursor(c);

        if (nrOfEntries != null) {
            final String newHeaderDescr = getString(R.string.EntriesInCategory) + (c.getCount());
            nrOfEntries.setText(newHeaderDescr);
        }

        // final SharedPreferences sharedPreferences = PreferenceManager
        // .getDefaultSharedPreferences(getActivity());
        //
        // final int metric = Integer.parseInt(sharedPreferences.getString(
        // "pref_units", "0"));
        //
        // final boolean useImperialUnits = (metric == 1);

        final ListAdapter adapter = new SimpleCursorAdapter(getActivity(),
        // android.R.layout.simple_list_item_2,
                R.layout.list_item, c, new String[] { "name", "descr" }, new int[] { android.R.id.text1, android.R.id.text2 }) {

            LayoutInflater inflater       = null;
            long           currentGroupId = -1;

            @Override
            public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {

                if (inflater == null) {
                    inflater = LayoutInflater.from(context);
                }

                // check if we reached a new group ( resultset is grouped by
                // groupid )

                final long groupId = cursor.getLong(cursor.getColumnIndex("categoryid"));

                final View view;
                if (groupId == currentGroupId) {
                    // simple item
                    view = inflater.inflate(R.layout.poilist_item, parent, false);

                    // TODO: migrate
                    view.findViewById(R.id.header).setVisibility(View.GONE);

                }
                else {
                    // item with header
                    view = inflater.inflate(R.layout.poilist_item_groupheader, parent, false);
                }

                final String name = cursor.getString(cursor.getColumnIndex("name"));

                final ViewHolder vhc = new ViewHolder();
                vhc.textView1 = (TextView) view.findViewById(android.R.id.text1);
                vhc.textView2 = (TextView) view.findViewById(android.R.id.text2);
                vhc.textView3 = (TextView) view.findViewById(R.id.infotext3);
                vhc.groupHeader = (TextView) view.findViewById(R.id.groupheader);
                vhc.icon1 = (ImageView) view.findViewById(R.id.ImageView01);
                vhc.icon2 = (ImageView) view.findViewById(R.id.ImageView02);

                if (groupId != currentGroupId) {
                    final Cursor c1 = CoreInfoHandler.getInstance().getDBManager(getActivity()).getGeoDatabase().getTrackCategory((int) groupId);
                    if (c1.moveToFirst()) {
                        final String groupName = c1.getString(c1.getColumnIndex("name"));
                        vhc.groupHeader.setText(groupName);
                    }
                    c1.close();
                }

                vhc.textView1.setText(name);
                final double distance = cursor.getFloat(cursor.getColumnIndex("distance"));

                final long time = cursor.getLong(cursor.getColumnIndex("time")) *1000; // is in Seconds !!

                final float avgspeed = cursor.getFloat(cursor.getColumnIndex("avgspeed"));

                final String descr = cursor.getString(cursor.getColumnIndex("descr"));

                vhc.textView2.setText(descr);

                vhc.icon2.setImageResource(R.drawable.map_pin_holed_blue);
                vhc.icon1.setVisibility(View.GONE);
                // vhc.icon1.setImageResource(cursor.getInt(cursor
                // .getColumnIndex("image")));

                // get all points for statistics
                final int trackId = cursor.getInt(cursor.getColumnIndex("_id"));
                final Cursor c2 = CoreInfoHandler.getInstance().getDBManager(getActivity()).getGeoDatabase().getTrackPoints(trackId);
                String stat = " T " + GeoMathUtil.formatElapsedTime(time); // "# waypoints: "
                                                                           // +
                                                                           // c2.getCount();

                stat += " | ↔ " + GeoMathUtil.getHumanDistanceString(distance, CoreInfoHandler.getInstance().getDistanceUnitFormatId());
                stat += " | Δ "
                        + String.format(Locale.getDefault(), "%.2f",GeoMathUtil.convertSpeed(avgspeed, CoreInfoHandler.getInstance().getSpeedFormatId()))
                                + " "
                                + CoreInfoHandler.getInstance().getMainActivity().getResources().getStringArray(R.array.speed_unit_title)[CoreInfoHandler
                                        .getInstance().getSpeedFormatId()];

                vhc.textView3.setText(stat);

                c2.close();

                final CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);

                final int show = cursor.getInt(cursor.getColumnIndex("show"));
                cb.setChecked(show == 1);

                cb.setOnClickListener(new OnClickListener() {

                    int itemId = trackId;

                    @Override
                    public void onClick(final View v) {

                        unselectCurrentTrack(itemId);

                        CoreInfoHandler.getInstance().setCurrentTrackId(itemId);

                        CoreInfoHandler.getInstance().getDBManager(getActivity()).setTrackChecked(itemId, cb.isChecked());
                        // refreshTrack();
                        getListView().invalidateViews();
                    }
                });

                currentGroupId = groupId;

                return view;
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

    private void setupQuickAction(final QuickAction quickAction) {

        ActionItem item = new ActionItem();
        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_show));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        // ------

        item.setTitle(getResources().getString(R.string.menu_edit));
        item.setIcon(getResources().getDrawable(R.drawable.menu_edit));
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

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_exporttoearth));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_exporttoigc));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final int pos) {
                if (pos == 0) {
                    handleContextItemSelected(R.id.menu_show);
                }
                else if (pos == 1) {
                    handleContextItemSelected(R.id.menu_editpoi);
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
                else if (pos == 5) {
                    handleContextItemSelected(R.id.menu_exporttoearth);
                }
                else if (pos == 6) {
                    handleContextItemSelected(R.id.menu_exporttoigctrack);
                }
            }
        });

    }

    private void handleContextItemSelected(final int id) {

        final Track track = CoreInfoHandler.getInstance().getDBManager(getActivity()).getTrack(selectedItemId);

        switch (id) {
            case R.id.menu_editpoi: {
                editTrack(track);
                break;
            }

            case R.id.menu_show: {
                showTrack(track);
                break;
            }
            case R.id.menu_deletepoi: {
                deleteTrack();
                break;
            }
            case R.id.menu_exporttogpxpoi: {
                doExportTrackGPX(selectedItemId);
                break;
            }
            case R.id.menu_exporttokmlpoi: {
                doExportTrackKML(selectedItemId);
                break;
            }
            case R.id.menu_exporttoearth: {
                showInGoogleEarth(selectedItemId);
                break;
            }
            case R.id.menu_exporttoigctrack: {
                Toast.makeText(getActivity(), R.string.NYI, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        super.onOptionsItemSelected(item);
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.menu_deletealltracks: {
                deleteAll();
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
        CoreInfoHandler.getInstance().getDBManager(getActivity()).setTracksChecked(show);
        resume();
    }

    private void deleteAll() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning_delete_all_tracks).setCancelable(false)
                .setPositiveButton(R.string.dialogYES, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {

                        CoreInfoHandler.getInstance().getDBManager(getActivity()).deleteAllTracks();

                        sendMsgToOverlay(); // if shown track is deleted one

                        dialog.dismiss();

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

    private void unselectCurrentTrack(final int trackId) {
        if (CoreInfoHandler.getInstance().getCurrentTrackId() == selectedItemId) {
            // CoreInfoHandler.getInstance().getTrackOverlay().clearTrack();
            CoreInfoHandler.getInstance().setCurrentTrackId(-1);
        }

    }

    private void deleteTrack() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning_delete_track).setCancelable(false).setPositiveButton(R.string.dialogYES, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int id) {

                unselectCurrentTrack(selectedItemId);

                CoreInfoHandler.getInstance().getDBManager(getActivity()).deleteTrack(selectedItemId);

                sendMsgToOverlay(); // if shown track is deleted one

                fillData();
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

    /**
     * if in tablet mode refresh visible tracks on the fly
     * 
     * @param track
     */
    // private void refreshTrack() {
    //
    // if (Ut.isMultiPane(getActivity())) {
    //
    // CoreInfoHandler.getInstance().setMapCmd(
    // MapFragment.MAP_CMD_SHOW_TRACK);
    // CoreInfoHandler.getInstance().getMapFragment().refresh(); // TODO:
    // // ??
    // // does
    // // this
    // // work
    // // in
    // // nontablet
    // // mode
    // // 9
    // } else {
    // CoreInfoHandler.getInstance().getTrackOverlay().refresh();
    // }
    // }

    private void sendMsgToOverlay() {
        Intent intent = new Intent(TrackOverlay.TRACK_CMD);
        intent.putExtra(TrackOverlay.TRACK_CMD, TrackOverlay.TRACK_REFRESH);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void showTrack(final Track track) {

        CoreInfoHandler.getInstance().getDBManager(getActivity()).setTrackChecked(selectedItemId, true);

        CoreInfoHandler.getInstance().setCurrentTrackId(track.getId());

        sendMsgToOverlay();

        CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_SHOW_TRACK);
        if (Ut.isMultiPane(getActivity())) {
            CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));
        }
        else {
            // getActivity().finishActivity(MainActivity.ACTIVITY_ID_DATA);
            getActivity().finish();
        }
    }

    private void editTrack(final Track track) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final DialogFragment newFragment = TrackDialogFragment.newInstance(track.getId(), track.Name, track.Descr, R.string.dialogTitleTrack);
        newFragment.show(ft, "dialog");
    }

    private File getExportFile(int trackId,boolean usekml)
    {
        final File folder = Ut.getTschekkoMapsExportDir(getActivity());
        final String filename = folder.getAbsolutePath() + "/track" + trackId + (usekml ? ".kml" : ".gpx");
        final File file = new File(filename);

        return file;
    }
    
    private void doExportTrackKML(final int id) {
        // showDialog(R.id.dialog_wait);
        final int trackid = id;

        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                final Track track = CoreInfoHandler.getInstance().getDBManager(getActivity()).getTrack(trackid);

                final SimpleXML xml = new SimpleXML("kml");
                xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
                xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");

                final SimpleXML Placemark = xml.createChild("Placemark");
                Placemark.createChild("name").setText(track.Name);
                Placemark.createChild("description").setText(track.Descr);
                final SimpleXML LineString = Placemark.createChild("LineString");
                final SimpleXML coordinates = LineString.createChild("coordinates");
                final StringBuilder builder = new StringBuilder();

                for (final TrackPoint tp : track.getPoints()) {
                    builder.append(tp.getLongitude()).append(",").append(tp.getLatitude()).append(",").append(tp.alt).append(" ");
                }
                coordinates.setText(builder.toString().trim());
                
                final File file = TrackListFragment.this.getExportFile(track.getId(),true);

                FileOutputStream out;
                try {
                    file.createNewFile();
                    out = new FileOutputStream(file);
                    final OutputStreamWriter wr = new OutputStreamWriter(out);
                    wr.write(SimpleXML.saveXml(xml));
                    wr.close();
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0, file.getName()).sendToTarget();
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

    private void doExportTrackGPX(final int id) {
        // showDialog(R.id.dialog_wait);
        final int trackid = id;

        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                final Track track = CoreInfoHandler.getInstance().getDBManager(getActivity()).getTrack(trackid);

                final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                final SimpleXML xml = new SimpleXML("gpx");
                xml.setAttr("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
                xml.setAttr("xmlns", "http://www.topografix.com/GPX/1/1");
                xml.setAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                xml.setAttr("creator", "Outlander");
                xml.setAttr("version", "1.1");

                final SimpleXML meta = xml.createChild("metadata");
                meta.createChild("name").setText(track.Name);
                meta.createChild("desc").setText(track.Descr);
                final SimpleXML autor = meta.createChild("author");
                autor.createChild("name").setText("RMaps");

                final SimpleXML trk = xml.createChild("trk");
                final SimpleXML trkseg = trk.createChild("trkseg");
                SimpleXML trkpt = null;
                for (final TrackPoint tp : track.getPoints()) {
                    trkpt = trkseg.createChild("trkpt");
                    trkpt.setAttr("lat", Double.toString(tp.getLatitude()));
                    trkpt.setAttr("lon", Double.toString(tp.getLongitude()));
                    trkpt.createChild("ele").setText(Double.toString(tp.alt));
                    trkpt.createChild("time").setText(formatter.format(tp.date));
                }

                final File file = TrackListFragment.this.getExportFile(track.getId(),false);
                FileOutputStream out;
                try {
                    file.createNewFile();
                    out = new FileOutputStream(file);
                    final OutputStreamWriter wr = new OutputStreamWriter(out);
                    wr.write(SimpleXML.saveXml(xml));
                    wr.close();
                    Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0, file.getName()).sendToTarget();
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

    @Override
    public void pageGetsActivated() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pageGetsDeactivated() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh() {
        resume();
    }

    private void showInGoogleEarth(final int trackid)
    {
        final Track track = CoreInfoHandler.getInstance().getDBManager(getActivity()).getTrack(trackid);
        
        final File file = TrackListFragment.this.getExportFile(track.getId(),true);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.google-earth.kml+xml");
        intent.putExtra("com.google.earth.EXTRA.tour_feature_id", track.Name);
        startActivity(intent);
    }
    
}
