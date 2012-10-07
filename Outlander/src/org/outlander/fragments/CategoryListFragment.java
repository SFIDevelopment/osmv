package org.outlander.fragments;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.outlander.R;
import org.outlander.model.Route;
import org.outlander.utils.CoreInfoHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CategoryListFragment extends SherlockListFragment implements
        PageChangeNotifyer {

    int                     mPositionChecked = 0;
    int                     mPositionShown   = -1;

    private QuickAction     mQuickAction;
    private int             selectedItemId   = -1;
    private View            headerView;
    private TextView        mDescription;
    private boolean         isInitialized    = false;
    private int             catType;

    public static final int CAT_TYPE_POI     = 0;
    public static final int CAT_TYPE_ROUTE   = 1;
    public static final int CAT_TYPE_TRACK   = 2;

    public static CategoryListFragment newInstance(final int catType) {
        final CategoryListFragment f = new CategoryListFragment();

        f.catType = catType;

        // Supply num input as an argument.
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
        //
        // View view = inflater.inflate(R.layout.list_with_header, container,
        // false);
        //
        // TextView textView = (TextView) view.findViewById(R.id.listheader);
        //
        // textView.setText(R.string.listheader_route);

        final View view = super.onCreateView(inflater, container,
                savedInstanceState);
        headerView = addHeader(inflater);

        return view;

        // return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceRouteList",
                    0);
            mPositionShown = savedInstanceState.getInt("shownChoiceRouteList",
                    -1);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreSavedState(savedInstanceState);

        if (!isInitialized) {
            getListView().addHeaderView(headerView);
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

        outState.putInt("curChoiceRouteList", mPositionChecked);
        outState.putInt("shownChoiceRouteList", mPositionShown);
    }

    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        selectedItemId = (int) id;
        if (selectedItemId >= 0) {
            mQuickAction.show(v);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.categorylist_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.menu_add: {
                addCategory();
                result = true;
                break;
            }
        }
        return result;
    }

    private void addCategory() {
        Toast.makeText(getActivity(), R.string.NYI, Toast.LENGTH_LONG).show();
    }

    private void resume() {
        fillData();
    }

    private View addHeader(final LayoutInflater inflater) {

        final View header = inflater.inflate(R.layout.list_header, null);

        final TextView headerTxt = (TextView) header
                .findViewById(android.R.id.text1);

        int headerId = 0;

        switch (catType) {
            case CAT_TYPE_POI:
                headerId = R.string.PoiCategoryTitle;
                break;
            case CAT_TYPE_ROUTE:
                headerId = R.string.RouteCategoryTitle;
                break;
            case CAT_TYPE_TRACK:
                headerId = R.string.TrackCategoryTitle;
                break;
        }

        if (headerTxt != null) {
            headerTxt.setText(headerId);
        }
        mDescription = (TextView) header.findViewById(android.R.id.text2);

        return header;
    }

    public void fillData() {
        Cursor c = null;
        switch (catType) {
            case CAT_TYPE_POI:
                c = CoreInfoHandler.getInstance().getDBManager(getActivity())
                        .getGeoDatabase().getPoiCategoryListCursor();
                break;
            case CAT_TYPE_ROUTE:
                c = CoreInfoHandler.getInstance().getDBManager(getActivity())
                        .getGeoDatabase().getRouteCategoryListCursor();
                break;
            case CAT_TYPE_TRACK:
                c = CoreInfoHandler.getInstance().getDBManager(getActivity())
                        .getGeoDatabase().getTrackCategoryListCursor();
                break;
        }

        // getLoaderManager().
        getActivity().startManagingCursor(c);

        if (mDescription != null) {
            final String newHeaderDescr = getString(R.string.EntriesInCategory)
                    + " " + (c.getCount());
            mDescription.setText(newHeaderDescr);
        }

        final ListAdapter adapter = new SimpleCursorAdapter(getActivity(),
                // android.R.layout.simple_list_item_2,
                R.layout.list_item, c, new String[] { "name", "descr" },
                new int[] { android.R.id.text1, android.R.id.text2 }) {

            LayoutInflater inflater = null;

            @Override
            public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {

                if (inflater == null) {
                    inflater = LayoutInflater.from(context);
                }

                final View view;
                // simple item
                view = inflater.inflate(R.layout.poilist_item, parent, false);

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

                final String name = cursor.getString(cursor
                        .getColumnIndex("name"));

                vhc.textView1.setText(name);

                final String descr = cursor.getString(cursor
                        .getColumnIndex("descr"));

                vhc.textView2.setText(descr);
                vhc.icon2.setImageResource(R.drawable.list_route);

                if (catType == CategoryListFragment.CAT_TYPE_POI) {
                    vhc.icon1.setImageResource(cursor.getInt(cursor
                            .getColumnIndex("image")));
                }

                // get all points for statistics
                final int catId = cursor.getInt(cursor.getColumnIndex("_id"));

                // get # of entry currently in this category
                int nrOfEntries = 0;
                switch (catType) {
                    case CAT_TYPE_POI:
                        nrOfEntries = CoreInfoHandler.getInstance()
                                .getDBManager(getActivity()).getGeoDatabase()
                                .getNrofPoiForCategory(catId);
                        break;
                    case CAT_TYPE_ROUTE:
                        nrOfEntries = CoreInfoHandler.getInstance()
                                .getDBManager(getActivity()).getGeoDatabase()
                                .getNrofRoutesForCategory(catId);
                        break;
                    case CAT_TYPE_TRACK:
                        nrOfEntries = CoreInfoHandler.getInstance()
                                .getDBManager(getActivity()).getGeoDatabase()
                                .getNrofTracksForCategory(catId);
                        break;
                }

                final String stat = "# Entries: " + nrOfEntries;
                vhc.textView3.setText(stat);
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

        // ------
        ActionItem item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_edit));
        item.setIcon(getResources().getDrawable(R.drawable.menu_edit));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_delete));
        item.setIcon(getResources().getDrawable(R.drawable.menu_delete));
        quickAction.addActionItem(item);

        // ------

        // ------

        quickAction
                .setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

                    @Override
                    public void onItemClick(final int pos) {
                        if (pos == 0) {
                            handleContextItemSelected(R.id.menu_editpoi);
                        } else if (pos == 1) {
                            handleContextItemSelected(R.id.menu_deletepoi);
                        }
                    }
                });

    }

    private void handleContextItemSelected(final int id) {

        final Route route = CoreInfoHandler.getInstance()
                .getDBManager(getActivity()).getRoute(selectedItemId);

        switch (id) {
            case R.id.menu_editpoi: {

                editCategory(route);

                break;
            }

            case R.id.menu_deletepoi: {

                deleteCategory(route);

                break;
            }

        }
    }

    private void editCategory(final Route route) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        final DialogFragment newFragment = RouteDialogFragment.newInstance(
                route.getId(), route.getName(), route.getDescr(),
                R.string.dialogTitleRoute);
        newFragment.show(ft, "dialog");

    }

    private void deleteCategory(final Route route) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(R.string.warning_delete_all_routes)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogYES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {

                                CoreInfoHandler.getInstance()
                                        .getDBManager(getActivity())
                                        .deleteRoute(id);
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

    @Override
    public void pageGetsActivated() {
    }

    @Override
    public void pageGetsDeactivated() {
    }

    @Override
    public void refresh() {
        resume();
    }
}
