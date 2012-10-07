package org.outlander.fragments;

import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.outlander.R;
import org.outlander.model.PoiCategory;
import org.outlander.model.PoiPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.views.SectionedListViewAdapter;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

public class PoiListFragment extends SherlockListFragment implements
        PageChangeNotifyer {

    int                   mPositionChecked = 0;
    int                   mPositionShown   = -1;
    private QuickAction   mQuickAction;
    private TextView      nrOfEntries;
    private boolean       isInitialized    = false;
    private final boolean showDetails      = true;
    long                  selectedItemId   = -1;

    // protected ExecutorService mThreadPool = Executors
    // .newFixedThreadPool(2);

    public static PoiListFragment newInstance() {

        final PoiListFragment f = new PoiListFragment();
        //
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
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        restoreSavedState(savedInstanceState);

        final View view = inflater.inflate(R.layout.headerlist, null);

        final ImageView icon = (ImageView) view.findViewById(R.id.menuButton);
        final TextView header = (TextView) view.findViewById(R.id.caption1);
        final Button btnMenu = (Button) view.findViewById(R.id.button_menu);
        btnMenu.setVisibility(View.GONE);

        icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // getActivity().openOptionsMenu();

                // CoreInfoHandler
                // .getInstance()
                // .gotoPage(
                // FragmentFactory
                // .getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));
                //
            }
        });

        header.setText(R.string.tab_pois);

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
            mPositionChecked = savedInstanceState.getInt("curChoicePOIList", 0);
            mPositionShown = savedInstanceState
                    .getInt("shownChoicePOIList", -1);
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
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoicePOIList", mPositionChecked);
        outState.putInt("shownChoicePOIList", mPositionShown);
    }

    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        selectedItemId = id;
        mQuickAction.show(v);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.poilist_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void resume() {
        fillData();
    }

    private void fillData() {

        final List<PoiPoint> poiList = CoreInfoHandler.getInstance()
                .getDBManager(getActivity()).getPoiListUser();

        final List<PoiCategory> catList = CoreInfoHandler.getInstance()
                .getDBManager(getActivity()).getPoiUserCategoryList();

        if (nrOfEntries != null) {
            final String newHeaderDescr = getString(R.string.EntriesInCategory)
                    + (poiList.size());
            nrOfEntries.setText(newHeaderDescr);
        }

        final ListAdapter adapter = new SectionedListViewAdapter() {

            LayoutInflater inflater = null;

            @Override
            public int getCount() {

                return poiList.size();
            }

            @Override
            public Object getItem(final int paramInt) {

                return poiList.get(paramInt);
            }

            @Override
            public long getItemId(final int paramInt) {

                return poiList.get(paramInt).getId();
            }

            @Override
            protected void onNextPageRequested(final int page) {

            }

            @Override
            protected void bindSectionHeader(final View view,
                    final int position, final boolean displaySectionHeader) {

                if (displaySectionHeader) {
                    view.findViewById(R.id.header).setVisibility(View.VISIBLE);
                    final TextView lSectionTitle = (TextView) view
                            .findViewById(R.id.header);
                    lSectionTitle
                            .setText(((PoiCategory) getSections()[getSectionForPosition(position)]).Title);
                } else {
                    view.findViewById(R.id.header).setVisibility(View.GONE);
                }

            }

            @Override
            public View getAmazingView(final int position,
                    final View convertView, final ViewGroup parent) {

                View view = convertView;
                if (view == null) {
                    if (inflater == null) {
                        inflater = LayoutInflater.from(getActivity());
                    }

                    view = inflater.inflate(R.layout.poilist_item, null);
                }

                final PoiPoint poiPoint = (PoiPoint) getItem(position);

                final ViewHolder vhc = new ViewHolder();
                vhc.textView1 = (TextView) view
                        .findViewById(android.R.id.text1);
                vhc.textView2 = (TextView) view
                        .findViewById(android.R.id.text2);
                vhc.textView3 = (TextView) view.findViewById(R.id.infotext3);
                vhc.groupHeader = (TextView) view
                        .findViewById(R.id.groupheader);
                vhc.icon1 = (ImageView) view.findViewById(R.id.ImageView01);
                vhc.icon2 = (ImageView) view.findViewById(R.id.ImageView02);

                vhc.textView1.setText(poiPoint.getTitle());

                String text = GeoMathUtil.formatCoordinate(poiPoint
                        .getGeoPoint().getLatitude(), poiPoint.getGeoPoint()
                        .getLongitude(), CoreInfoHandler.getInstance()
                        .getCoordFormatId())
                        + (poiPoint.getAlt() > 0 ? " ↑ "
                                + GeoMathUtil.twoDecimalFormat
                                        .format(GeoMathUtil.convertDistance(
                                                poiPoint.getAlt(),
                                                CoreInfoHandler
                                                        .getInstance()
                                                        .getDistanceUnitFormatId()))
                                + CoreInfoHandler
                                        .getInstance()
                                        .getMainActivity()
                                        .getResources()
                                        .getStringArray(
                                                R.array.distance_unit_title)[CoreInfoHandler
                                        .getInstance()
                                        .getDistanceUnitFormatId()]
                                : "");

                if (CoreInfoHandler.getInstance().getCurrentLocation() != null) {
                    text += " → "
                            + GeoMathUtil.getHumanDistanceString(GeoMathUtil
                                    .distanceTo(poiPoint.getGeoPoint()
                                            .getLatitude(), poiPoint
                                            .getGeoPoint().getLongitude(),
                                            CoreInfoHandler.getInstance()
                                                    .getCurrentLocation()
                                                    .getLatitude(),
                                            CoreInfoHandler.getInstance()
                                                    .getCurrentLocation()
                                                    .getLongitude()),
                                    CoreInfoHandler.getInstance()
                                            .getDistanceUnitFormatId());
                }

                vhc.textView3.setText(text);

                vhc.textView2.setText(showDetails ? poiPoint.getDescr() : "");

                int imageResource = poiPoint.getIconId();
                if (imageResource < 0) {
                    imageResource = ((PoiCategory) getSections()[getSectionForPosition(position)]).IconId;
                }

                vhc.icon2.setImageResource(R.drawable.list_poi);
                vhc.icon1.setImageResource(imageResource);

                final CheckBox cb = (CheckBox) view
                        .findViewById(R.id.checkBox1);

                cb.setChecked(!poiPoint.isHidden());

                cb.setOnClickListener(new OnClickListener() {

                    int itemId = poiPoint.getId();

                    @Override
                    public void onClick(final View v) {

                        CoreInfoHandler.getInstance()
                                .getDBManager(getActivity())
                                .setPoiChecked(itemId, cb.isChecked());

                    }
                });

                vhc.icon1.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        Toast.makeText(getActivity(), R.string.NYI,
                                Toast.LENGTH_LONG).show();

                        // startActivityForResult(new Intent(getActivity(),
                        // PoiActivity.class), 1234);

                    }
                });
                return view;
            }

            @Override
            public void configurePinnedHeader(final View header,
                    final int position, final int alpha) {

                final TextView lSectionHeader = (TextView) header;
                lSectionHeader
                        .setText(((PoiCategory) getSections()[getSectionForPosition(position)]).Title);
                lSectionHeader.setBackgroundColor((alpha << 24) | (0xbbffbb));
                lSectionHeader.setTextColor((alpha << 24) | (0x000000));

            }

            @Override
            public int getPositionForSection(final int section) {

                int ix = 0;
                for (final PoiPoint point : poiList) {
                    if (point.getCategoryId() == section) {
                        break;
                    }
                    ix++;
                }

                return ix;
            }

            @Override
            public int getSectionForPosition(final int position) {

                return getSectionIx(poiList.get(position).getCategoryId());
            }

            private int getSectionIx(final int categoryId) {
                int catId = 0;
                for (final PoiCategory cat : catList) {

                    if (cat.getId() == categoryId) {
                        break;
                    }
                    catId++;
                }
                return catId;
            }

            @Override
            public Object[] getSections() {

                return catList.toArray();
            }

        };

        setListAdapter(adapter);
    }

    // ---------------------
    private void editPoi(final PoiPoint point) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final DialogFragment newFragment = PoiDialogFragment.newInstance(point
                .getId(), point.getTitle(), point.getDescr(), point
                .getGeoPoint().getLatitude(), point.getGeoPoint()
                .getLongitude(), R.string.dialogTitlePOI);
        newFragment.show(ft, "dialog");

    }

    private void showPoi(final PoiPoint point) {
        if (point != null) {
            CoreInfoHandler.getInstance().setCurrentPoiPoint(
                    point.getGeoPoint());
            CoreInfoHandler.getInstance().setMapCmd(
                    MapFragment.MAP_CMD_SHOW_POI);

            if (Ut.isMultiPane(getActivity())) {
                CoreInfoHandler
                        .getInstance()
                        .gotoPage(
                                FragmentFactory
                                        .getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));

            } else {

                getActivity().finish();
            }
        }

    }

    private void gotoPoi(final PoiPoint point) {
        if (point != null) {

            final FragmentTransaction ft = getFragmentManager()
                    .beginTransaction();
            final Fragment prev = getFragmentManager().findFragmentByTag(
                    "dialog");

            if (prev != null) {
                ft.remove(prev);
            }

            ft.addToBackStack(null);

            final DialogFragment newFragment = RoutingDialogFragment
                    .newInstance(point.getTitle(), point.getDescr(), point
                            .getGeoPoint().getLatitude(), point.getGeoPoint()
                            .getLongitude(), R.string.dialogTitleRouting);
            newFragment.show(ft, "dialog");

        }

    }

    private void deletePoi(final PoiPoint point) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(R.string.warning_delete_poi)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogYES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {

                                CoreInfoHandler.getInstance()
                                        .getDBManager(getActivity())
                                        .deletePoi(id);

                                // TODO: delete POI from Overlay

                                fillData();
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.dialogNO,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                dialog.cancel();
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void showPoiExternal(final PoiPoint point) {
        try {
            startActivity(Ut.showLocationExternal(point.getGeoPoint()
                    .getLatitude(), point.getGeoPoint().getLongitude()));
        } catch (final ActivityNotFoundException x) {
            Toast.makeText(getActivity(), R.string.no_activity,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void shareLocation(final PoiPoint point) {

        final Intent intent = Ut.shareLocation(point.getGeoPoint()
                .getLatitude(), point.getGeoPoint().getLongitude(),
                point.getTitle() + "\n" + point.getDescr(), getActivity());

        if (intent != null) {
            startActivity(intent);
        }
        // Toast.makeText(this, R.string.NYI, Toast.LENGTH_LONG).show();

    }

    private void importPois() {

        // CoreInfoHandler.getInstance().getMainActivity().showDialog(newFragment);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final DialogFragment newFragment = ImportDialogFragment
                .newInstance(R.string.dialogTitleImport);
        newFragment.show(ft, "dialog");

    }

    private void deleteAllPois() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(R.string.warning_delete_all_poi)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogYES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {

                                CoreInfoHandler.getInstance()
                                        .getDBManager(getActivity())
                                        .deleteAllPois();

                                dialog.dismiss();

                                PoiListFragment.this.refresh();

                            }
                        })
                .setNegativeButton(R.string.dialogNO,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                dialog.cancel();
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    private void poiCategoryList() {
        CoreInfoHandler
                .getInstance()
                .gotoPage(
                        FragmentFactory
                                .getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_CAT));

    }

    // ---------------------

    public static class ViewHolder {
        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView groupHeader;
        ImageView       icon1;
        ImageView       icon2;
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

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_goto));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_shareLocation));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_delete));
        item.setIcon(getResources().getDrawable(R.drawable.menu_delete));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_showexternalpoi));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        quickAction
                .setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

                    @Override
                    public void onItemClick(final int pos) {
                        if (pos == 0) {
                            handleContextItemSelected(R.id.menu_editpoi);
                        } else if (pos == 1) {
                            handleContextItemSelected(R.id.menu_show);
                        } else if (pos == 2) {
                            handleContextItemSelected(R.id.menu_gotopoi);
                        } else if (pos == 3) {
                            handleContextItemSelected(R.id.menu_shareLocation);
                        } else if (pos == 4) {
                            handleContextItemSelected(R.id.menu_deletepoi);
                        } else if (pos == 5) {
                            handleContextItemSelected(R.id.menu_showexternalpoi);
                        }
                    }
                });

    }

    private void handleContextItemSelected(final int id) {

        final PoiPoint point = CoreInfoHandler.getInstance()
                .getDBManager(getActivity()).getPoiPoint((int) selectedItemId);

        switch (id) {
            case R.id.menu_editpoi: {
                editPoi(point);
                break;
            }
            case R.id.menu_show: {
                showPoi(point);
                break;
            }
            case R.id.menu_gotopoi: {
                gotoPoi(point);
                break;
            }
            case R.id.menu_deletepoi: {
                deletePoi(point);
                break;
            }
            case R.id.menu_showexternalpoi: {
                showPoiExternal(point);
                break;
            }
            case R.id.menu_shareLocation: {
                shareLocation(point);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.menu_importpoi: {
                importPois();
                result = true;
                break;
            }
            case R.id.menu_addpoi: {
                Toast.makeText(getActivity(), R.string.NYI, Toast.LENGTH_SHORT)
                        .show();
                result = true;
                break;
            }
            case R.id.menu_poicategorylist: {
                poiCategoryList();
                result = true;
                break;
            }
            case R.id.menu_deleteallpois: {
                deleteAllPois();
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
        CoreInfoHandler.getInstance().getDBManager(getActivity())
                .setPoisChecked(show);
        resume();
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
