package org.outlander.activities;

import org.outlander.fragments.PoiListFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PoiListActivity extends FragmentActivity {

    public static final String RESPONSE_POINTID            = "pointid";
    public static final String RESPONSE_TABPAGE            = "tabpage";
    public static final String RESPONSE_TABPAGEPOI         = "poitab";
    public static final String RESPONSE_TABPAGE_CMD        = "cmd";    // common
    public static final String RESPONSE_TABPAGE_CMD_SHOW   = "show";
    public static final String RESPONSE_TABPAGE_CMD_FOLLOW = "follow";

    //
    // private PoiManager mPoiManager;
    // private SharedPreferences pref;
    // private int coordFormt;
    // private ProgressDialog dlgWait;
    // private QuickAction mQuickAction;
    // private int clickedItemId = -1;
    // private final HashMap<Integer, Integer> mItemIds = new HashMap<Integer,
    // Integer>(); // list

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        } else {
            final PoiListFragment pois = new PoiListFragment();
            pois.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, pois).commit();
        }

        // this.setContentView(R.layout.route_list);
        // registerForContextMenu(getExpandableListView());
        //
        // mHandler = new SimpleInvalidationHandler();

    }

    // position,
    // id

    // @Override
    // protected void onCreate(final Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    //
    // pref = PreferenceManager.getDefaultSharedPreferences(this);
    // coordFormt = Integer.parseInt(pref.getString("pref_coords", "1"));
    //
    // registerForContextMenu(getExpandableListView());
    //
    // mQuickAction = new QuickAction(this);
    // setupQuickAction(mQuickAction);
    // }
    //
    // @Override
    // protected void onStop() {
    // // mPoiManager.FreeDatabases();
    // // mPoiManager = null;
    // super.onStop();
    // }
    //
    // @Override
    // protected void onResume() {
    // FillData();
    // super.onResume();
    // }
    //
    // @Override
    // protected Dialog onCreateDialog(final int id, final Bundle args) {
    // switch (id) {
    // case R.id.dialog_wait: {
    // dlgWait = new ProgressDialog(this);
    // dlgWait.setMessage("Please wait while loading...");
    // dlgWait.setIndeterminate(true);
    // dlgWait.setCancelable(false);
    // return dlgWait;
    // }
    // }
    // return null;
    // }
    //
    // public static class ViewHolder {
    // public TextView textView1;
    // public TextView textView2;
    // public TextView textView3;
    // ImageView icon1;
    // ImageView icon2;
    // }
    //
    // private int calcId(final int groupPosition, final int childPosition) {
    // return (int) (groupPosition << 4) + childPosition;
    // }
    //
    // private void FillData() {
    //
    // showDialog(R.id.dialog_wait);
    //
    // if (mPoiManager == null) {
    // mPoiManager = new PoiManager(this);
    // }
    //
    // final Cursor cursor = mPoiManager.getGeoDatabase()
    // .getPoiCategoryListCursor();
    //
    // startManagingCursor(cursor);
    //
    // final ExpandableListAdapter adapter = new SimpleCursorTreeAdapter(this,
    // cursor, R.layout.group_layout, new String[] { "name" },
    // new int[] { android.R.id.text1 }, R.layout.poilist_item,
    // new String[] { "name" }, new int[] { android.R.id.text1 }) {
    //
    // private int pendingId;
    //
    // /**
    // * returns *realId*....
    // */
    // @Override
    // public long getChildId(final int groupPosition,
    // final int childPosition) {
    //
    // final long id = mItemIds.get(calcId(groupPosition,
    // childPosition));
    // return id;
    //
    // }
    //
    // @Override
    // public View getChildView(final int groupPosition,
    // final int childPosition, final boolean isLastChild,
    // final View convertView, final ViewGroup parent) {
    //
    // pendingId = calcId(groupPosition, childPosition);
    //
    // return super.getChildView(groupPosition, childPosition,
    // isLastChild, convertView, parent);
    // }
    //
    // @Override
    // public View newChildView(final Context context,
    // final Cursor cursor, final boolean isLastChild,
    // final ViewGroup parent) {
    //
    // final PoiCategory category = mPoiManager.getPoiCategory(cursor
    // .getInt(cursor.getColumnIndex("categoryid")));
    //
    // final LayoutInflater inflater = LayoutInflater.from(context);
    // final View view = inflater.inflate(R.layout.poilist_item,
    // parent, false);
    //
    // mItemIds.put(pendingId,
    // cursor.getInt(cursor.getColumnIndex("pointid")));
    //
    // final String name = cursor.getString(cursor
    // .getColumnIndex("name"));
    //
    // final ViewHolder vhc = new ViewHolder();
    // vhc.textView1 = (TextView) view
    // .findViewById(android.R.id.text1);
    // vhc.textView2 = (TextView) view
    // .findViewById(android.R.id.text2);
    // vhc.textView3 = (TextView) view.findViewById(R.id.infotext3);
    // vhc.icon1 = (ImageView) view.findViewById(R.id.ImageView01);
    // vhc.icon2 = (ImageView) view.findViewById(R.id.ImageView02);
    //
    // /**
    // * Next set the name of the entry.
    // */
    //
    // vhc.textView1.setText(name);
    //
    // final double lat = cursor.getDouble(cursor
    // .getColumnIndex("lat"));
    // final double lon = cursor.getDouble(cursor
    // .getColumnIndex("lon"));
    // final String coords = GeoMathUtil.formatGeoPoint(new GeoPoint(
    // (int) (1E6 * lat), (int) (1E6 * lon)), coordFormt);
    //
    // vhc.textView3.setText(coords);
    //
    // final String descr = cursor.getString(cursor
    // .getColumnIndex("descr"));
    // vhc.textView2.setText(descr);
    // vhc.icon1.setImageResource(category.IconId);
    // vhc.icon2.setImageResource(R.drawable.list_poi);
    //
    // return view;
    //
    // }
    //
    // @Override
    // public View newGroupView(final Context context,
    // final Cursor cursor, final boolean isExpanded,
    // final ViewGroup parent) {
    //
    // final String groupName = cursor.getString(cursor
    // .getColumnIndex("name"));
    //
    // final LayoutInflater inflater = LayoutInflater.from(context);
    // final View view = inflater.inflate(R.layout.group_layout,
    // parent, false);
    //
    // final ViewHolder vhg = new ViewHolder();
    // vhg.textView1 = (TextView) view.findViewById(R.id.tvGroup);
    // vhg.icon1 = (ImageView) view.findViewById(R.id.ImageView01);
    //
    // final int imageResourceId = cursor.getInt(cursor
    // .getColumnIndex("iconid"));
    //
    // vhg.icon1.setImageResource(imageResourceId);
    // vhg.textView1.setText(groupName);
    // return view;
    // }
    //
    // @Override
    // protected Cursor getChildrenCursor(final Cursor groupCursor) {
    //
    // final int categoryId = groupCursor.getInt(groupCursor
    // .getColumnIndex("_id"));
    // final Cursor cursor = mPoiManager.getGeoDatabase()
    // .getPoiListForCategory(categoryId);
    // startManagingCursor(cursor);
    // return cursor;
    // }
    // };
    //
    // getExpandableListView().setOnChildClickListener(
    // new OnChildClickListener() {
    // public boolean onChildClick(ExpandableListView parent,
    // View v, int groupPosition, int childPosition,
    // long id) {
    //
    // clickedItemId = mItemIds.get(calcId(groupPosition,
    // childPosition));
    //
    // mQuickAction.show(getExpandableListView());
    // return true;
    // }
    //
    // });
    //
    // setListAdapter(adapter);
    //
    // dlgWait.dismiss();
    //
    // }
    //
    // private void setupQuickAction(final QuickAction quickAction) {
    //
    // // ------
    // ActionItem item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_edit));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_edit));
    // quickAction.addActionItem(item);
    //
    // // ------
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_show));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
    // quickAction.addActionItem(item);
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_goto));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
    // quickAction.addActionItem(item);
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_shareLocation));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_share));
    // quickAction.addActionItem(item);
    //
    // // ------
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_delete));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_delete));
    // quickAction.addActionItem(item);
    //
    // // ------
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_showexternalpoi));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
    // quickAction.addActionItem(item);
    //
    // quickAction
    // .setOnActionItemClickListener(new QuickAction.OnActionItemClickListener()
    // {
    //
    // public void onItemClick(final int pos) {
    // if (pos == 0) {
    // handleContextItemSelected(R.id.menu_editpoi);
    // } else if (pos == 1) {
    // handleContextItemSelected(R.id.menu_show);
    // } else if (pos == 2) {
    // handleContextItemSelected(R.id.menu_gotopoi);
    // } else if (pos == 3) {
    // handleContextItemSelected(R.id.menu_shareLocation);
    // } else if (pos == 4) {
    // handleContextItemSelected(R.id.menu_deletepoi);
    // } else if (pos == 5) {
    // handleContextItemSelected(R.id.menu_showexternalpoi);
    // }
    // }
    // });
    //
    // }
    //
    // private void handleContextItemSelected(final int id) {
    //
    // final PoiPoint poi = mPoiManager.getPoiPoint(clickedItemId);
    //
    // switch (id) {
    // case R.id.menu_editpoi: {
    // startActivity((new Intent(this, PoiActivity.class)).putExtra(
    // RESPONSE_POINTID, clickedItemId));
    // break;
    // }
    // case R.id.menu_show: {
    // final Intent intent = (new Intent())
    // .putExtra(RESPONSE_POINTID, clickedItemId)
    // .putExtra(RESPONSE_TABPAGE, RESPONSE_TABPAGEPOI)
    // .putExtra(RESPONSE_TABPAGE_CMD,
    // RESPONSE_TABPAGE_CMD_SHOW);
    //
    // if (getParent() != null) {
    // getParent().setResult(RESULT_OK, intent);
    // } else {
    // setResult(RESULT_OK, intent);
    // }
    // finish();
    // break;
    // }
    // case R.id.menu_gotopoi: {
    // final Intent intent = (new Intent())
    // .putExtra(RESPONSE_POINTID, clickedItemId)
    // .putExtra(RESPONSE_TABPAGE, RESPONSE_TABPAGEPOI)
    // .putExtra(RESPONSE_TABPAGE_CMD,
    // RESPONSE_TABPAGE_CMD_FOLLOW);
    //
    // if (getParent() != null) {
    // getParent().setResult(RESULT_OK, intent);
    // } else {
    // setResult(RESULT_OK, intent);
    // }
    // finish();
    // break;
    // }
    // case R.id.menu_shareLocation: {
    //
    // break;
    // }
    // case R.id.menu_deletepoi: {
    // mPoiManager.deletePoi(clickedItemId);
    // FillData();
    // break;
    // }
    // case R.id.menu_showexternalpoi:
    // try {
    // final Intent intent = new Intent(Intent.ACTION_VIEW,
    // Uri.parse("geo:" + poi.GeoPoint.getLatitude() + ","
    // + poi.GeoPoint.getLongitude() + "?z=18"));
    // startActivity(intent);
    // } catch (final ActivityNotFoundException x) {
    // Toast.makeText(this, R.string.no_activity,
    // Toast.LENGTH_LONG).show();
    // }
    // }
    //
    // }
    //
    // @Override
    // public boolean onCreateOptionsMenu(final Menu menu) {
    // super.onCreateOptionsMenu(menu);
    //
    // final MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.poilist_menu, menu);
    //
    // return true;
    // }
    //
    // @Override
    // public boolean onOptionsItemSelected(final MenuItem item) {
    // super.onOptionsItemSelected(item);
    //
    // switch (item.getItemId()) {
    // case R.id.menu_addpoi:
    // startActivity((new Intent(this, PoiActivity.class)));
    // return true;
    // case R.id.menu_categorylist:
    // startActivity((new Intent(this, PoiCategoryListActivity.class)));
    // return true;
    // case R.id.menu_importpoi:
    // startActivityForResult((new Intent(this,
    // ImportPoiActivity.class)), R.id.menu_importpoi);
    // return true;
    // case R.id.menu_deleteall:
    // showDialog(R.id.menu_deleteall);
    // return true;
    // }
    //
    // return true;
    // }
    //
    // @Override
    // protected Dialog onCreateDialog(final int id) {
    // switch (id) {
    // case R.id.menu_deleteall:
    // return new AlertDialog.Builder(this)
    // // .setIcon(R.drawable.alert_dialog_icon)
    // .setTitle(R.string.warning_delete_all_poi)
    // .setPositiveButton(android.R.string.yes,
    // new DialogInterface.OnClickListener() {
    // public void onClick(
    // final DialogInterface dialog,
    // final int whichButton) {
    // mPoiManager.DeleteAllPoi();
    // FillData();
    // }
    // })
    // .setNegativeButton(android.R.string.no,
    // new DialogInterface.OnClickListener() {
    // public void onClick(
    // final DialogInterface dialog,
    // final int whichButton) {
    //
    // /* User clicked Cancel so do some stuff */
    // }
    // }).create();
    // }
    // ;
    //
    // return super.onCreateDialog(id);
    // }
    //
    // @Override
    // public void onCreateContextMenu(final ContextMenu menu, final View v,
    // final ContextMenuInfo menuInfo) {
    //
    // // final int pointid = (int) ((ExpandableListContextMenuInfo)
    // // menuInfo).id;
    // // final PoiPoint poi = mPoiManager.getPoiPoint(pointid);
    // //
    // // menu.setHeaderTitle(poi.Title);
    // //
    // // menu.add(0, R.id.menu_gotopoi, 0, getText(R.string.menu_goto));
    // // menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
    // // if (poi.Hidden) {
    // // menu.add(0, R.id.menu_show, 0, getText(R.string.menu_show));
    // // } else {
    // // menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
    // // }
    // // menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
    // // menu.add(0, R.id.menu_publishpoi, 0,
    // // getText(R.string.menu_publishpoi));
    // // menu.add(0, R.id.menu_toradar, 0, getText(R.string.menu_toradar));
    // //
    // // super.onCreateContextMenu(menu, v, menuInfo);
    // }
    //
    // @Override
    // public boolean onContextItemSelected(final MenuItem item) {
    // // final int pointid = (int) ((ExpandableListContextMenuInfo) item
    // // .getMenuInfo()).id;
    // // final PoiPoint poi = mPoiManager.getPoiPoint(pointid);
    // //
    // // switch (item.getItemId()) {
    // // case R.id.menu_editpoi:
    // // startActivity((new Intent(this, PoiActivity.class)).putExtra(
    // // RESPONSE_POINTID, pointid));
    // // break;
    // // case R.id.menu_gotopoi: {
    // // final Intent intent = (new Intent()).putExtra(RESPONSE_POINTID,
    // // pointid)
    // // .putExtra(RESPONSE_TABPAGE, RESPONSE_TABPAGEPOI);
    // //
    // // if (getParent() != null) {
    // // getParent().setResult(RESULT_OK, intent);
    // // } else {
    // // setResult(RESULT_OK, intent);
    // // }
    // // finish();
    // // }
    // // break;
    // // case R.id.menu_deletepoi:
    // // mPoiManager.deletePoi(pointid);
    // // FillData();
    // // break;
    // // case R.id.menu_showexternalpoi:
    // // try {
    // // final Intent intent = new Intent(Intent.ACTION_VIEW,
    // // Uri.parse("geo:" + poi.GeoPoint.getLatitude() + ","
    // // + poi.GeoPoint.getLongitude() + "?z=18"));
    // // startActivity(intent);
    // // } catch (final ActivityNotFoundException x) {
    // // Toast.makeText(this, R.string.no_activity,
    // // Toast.LENGTH_LONG).show();
    // // }
    // // case R.id.menu_publishpoi:
    // // // {
    // // // try
    // // // {
    // // // final Intent intent = new Intent(Intent.ACTION_VIEW,
    // // // Uri.parse("geo:"
    // // //
    // // +poi.GeoPoint.getLatitude()+","+poi.GeoPoint.getLongitude()+"?z=18"));
    // // // startActivity(intent);
    // // // }
    // // // catch (ActivityNotFoundException x)
    // // // {
    // // //
    // // // }
    // // // }
    // // //
    // // break;
    // //
    // // case R.id.menu_hide:
    // // poi.Hidden = true;
    // // mPoiManager.updatePoi(poi);
    // // FillData();
    // // break;
    // // case R.id.menu_show:
    // // poi.Hidden = false;
    // // mPoiManager.updatePoi(poi);
    // // FillData();
    // // break;
    // // case R.id.menu_toradar:
    // // try {
    // // final Intent i = new Intent(
    // // "com.google.android.radar.SHOW_RADAR");
    // // i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    // // i.putExtra("name", poi.Title);
    // // i.putExtra("latitude",
    // // (poi.GeoPoint.getLatitudeE6() / 1000000f));
    // // i.putExtra("longitude",
    // // (poi.GeoPoint.getLongitudeE6() / 1000000f));
    // // startActivity(i);
    // // } catch (final Exception e) {
    // // Toast.makeText(this, R.string.message_noradar,
    // // Toast.LENGTH_LONG).show();
    // // }
    // // break;
    // // }
    // //
    // return super.onContextItemSelected(item);
    // }
    //
    // // @Override
    // // protected void onListItemClick(ListView l, View v, int position, long
    // id)
    // // {
    // // super.onListItemClick(l, v, position, id);
    // // }
    //
    // @Override
    // protected void onActivityResult(final int requestCode,
    // final int resultCode, final Intent data) {
    //
    // switch (requestCode) {
    // case R.id.menu_importpoi:
    // if (resultCode == RESULT_OK) {
    // final int pois = data.getIntExtra(
    // ImportPoiActivity.RESPONSE_NRPOIS, 0);
    // final int routes = data.getIntExtra(
    // ImportPoiActivity.RESPONSE_NRROUTES, 0);
    // final int tracks = data.getIntExtra(
    // ImportPoiActivity.RESPONSE_NRTRACKS, 0);
    // Toast.makeText(
    // this,
    // "Pois imported:   " + pois + "\n"
    // + "Routes imported: " + routes + "\n"
    // + "Tracks imported: " + tracks + "\n",
    // Toast.LENGTH_LONG).show();
    // FillData();
    // }
    // break;
    // }
    // super.onActivityResult(requestCode, resultCode, data);
    // }
}
