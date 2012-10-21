package org.outlander.activities;

import org.outlander.fragments.RouteListFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class RouteListActivity extends SherlockFragmentActivity {

    public static final String RESPONSE_ROUTEID      = "routeid";
    public static final String RESPONSE_TABPAGEROUTE = "routetab";

    // private PoiManager mPoiManager;
    //
    // private ProgressDialog dlgWait;
    // protected ExecutorService mThreadPool = Executors
    // .newFixedThreadPool(2);
    //
    // private QuickAction mQuickAction;
    // private int clickedItemId = -1;
    // private final HashMap<Integer, Integer> mItemIds = new HashMap<Integer,
    // Integer>(); // list

    // private SimpleInvalidationHandler mHandler;

    // public class SimpleInvalidationHandler extends Handler {
    //
    // @Override
    // public void handleMessage(final Message msg) {
    // switch (msg.what) {
    //
    // case R.id.menu_exporttogpxpoi:
    // if (msg.arg1 == 0) {
    // Toast.makeText(
    // RouteListActivity.this,
    // getString(R.string.message_error) + " "
    // + (String) msg.obj, Toast.LENGTH_LONG)
    // .show();
    // } else {
    // Toast.makeText(
    // RouteListActivity.this,
    // getString(R.string.message_trackexported) + " "
    // + (String) msg.obj, Toast.LENGTH_LONG)
    // .show();
    // }
    // break;
    // }
    // }
    // }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        }
        else {
            final RouteListFragment routes = new RouteListFragment();
            routes.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, routes).commit();
        }

        // this.setContentView(R.layout.route_list);
        // registerForContextMenu(getExpandableListView());
        //
        // mHandler = new SimpleInvalidationHandler();

    }

    // @Override
    // protected void onResume() {
    // // FillData();
    // super.onResume();
    // }

    // @Override
    // protected void onStop() {
    // mPoiManager.FreeDatabases();
    // mPoiManager = null;
    // super.onStop();
    // }

    // ←↑→↓↔↨

    // private int calcId(final int groupPosition, final int childPosition) {
    // return (int) (groupPosition << 4) + childPosition;
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
    // private void FillData() {
    // if (mPoiManager == null) {
    // mPoiManager = new PoiManager(this);
    // }
    // final Cursor cursor = mPoiManager.getGeoDatabase()
    // .getRouteCategoryListCursor();
    // if (cursor != null) {
    // startManagingCursor(cursor);
    //
    // final ExpandableListAdapter adapter = new SimpleCursorTreeAdapter(
    // this, cursor, R.layout.group_layout,
    // new String[] { "name" }, new int[] { android.R.id.text1 },
    // R.layout.poilist_item, new String[] { "name" },
    // new int[] { android.R.id.text1 }) {
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
    // final LayoutInflater inflater = LayoutInflater
    // .from(context);
    // final View view = inflater.inflate(R.layout.poilist_item,
    // parent, false);
    //
    // final String name = cursor.getString(cursor
    // .getColumnIndex("name"));
    //
    // final ViewHolder vhc = new ViewHolder();
    // vhc.textView1 = (TextView) view
    // .findViewById(android.R.id.text1);
    // vhc.textView2 = (TextView) view
    // .findViewById(android.R.id.text2);
    // vhc.textView3 = (TextView) view
    // .findViewById(R.id.infotext3);
    // vhc.icon1 = (ImageView) view.findViewById(R.id.ImageView01);
    // vhc.icon2 = (ImageView) view.findViewById(R.id.ImageView02);
    //
    // vhc.textView1.setText(name);
    //
    // final String descr = cursor.getString(cursor
    // .getColumnIndex("descr"));
    //
    // vhc.textView2.setText(descr);
    // vhc.icon2.setImageResource(R.drawable.list_route);
    // vhc.icon1.setImageResource(cursor.getInt(cursor
    // .getColumnIndex("image")));
    //
    // // get all points for statistics
    // final long routeId = cursor.getLong(cursor
    // .getColumnIndex("_id"));
    //
    // final Cursor c2 = mPoiManager.getGeoDatabase()
    // .getRoutePoints(routeId);
    // final String stat = "# waypoints: " + c2.getCount();
    // // + length
    //
    // mItemIds.put(pendingId, (int) routeId);
    //
    // vhc.textView3.setText(stat);
    //
    // c2.close();
    // return view;
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
    // final LayoutInflater inflater = LayoutInflater
    // .from(context);
    // final View view = inflater.inflate(R.layout.group_layout,
    // parent, false);
    //
    // final ViewHolder vhg = new ViewHolder();
    // vhg.textView1 = (TextView) view.findViewById(R.id.tvGroup);
    //
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
    // .getRoutesForCategory(categoryId);
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
    // }
    // ;
    // }

    // @Override
    // public boolean onCreateOptionsMenu(final Menu menu) {
    // super.onCreateOptionsMenu(menu);
    //
    // final MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.routelist_menu, menu);
    //
    // return true;
    // }
    //
    // @Override
    // public boolean onOptionsItemSelected(final MenuItem item) {
    // super.onOptionsItemSelected(item);
    //
    // switch (item.getItemId()) {
    // case R.id.menu_importpoi:
    // startActivityForResult((new Intent(this,
    // ImportPoiActivity.class)), R.id.menu_importpoi);
    //
    // case R.id.menu_categorylist:
    // startActivity((new Intent(this, RouteCategoryListActivity.class)));
    // break;
    // case R.id.menu_addroute:
    // Toast.makeText(this, R.string.NYI, Toast.LENGTH_SHORT).show();
    // break;
    // case R.id.menu_deleteall:
    // Toast.makeText(this, R.string.NYI, Toast.LENGTH_SHORT).show();
    // break;
    // }
    // return true;
    // }

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
    // item.setTitle(getResources().getString(R.string.menu_exporttogpx));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
    // quickAction.addActionItem(item);
    //
    // // ------
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_exporttokml));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_share));
    // quickAction.addActionItem(item);
    //
    // // ------
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
    // handleContextItemSelected(R.id.menu_deletepoi);
    // } else if (pos == 3) {
    // handleContextItemSelected(R.id.menu_exporttogpxpoi);
    // } else if (pos == 4) {
    // handleContextItemSelected(R.id.menu_exporttokmlpoi);
    // }
    // }
    // });
    //
    // }
    //
    // private void handleContextItemSelected(final int id) {
    //
    // // final PoiPoint poi = mPoiManager.getPoiPoint(clickedItemId);
    //
    // switch (id) {
    // case R.id.menu_editpoi: {
    // startActivity((new Intent(this, RouteActivity.class)).putExtra(
    // "id", clickedItemId));
    // break;
    // }
    // case R.id.menu_gotopoi: {
    // mPoiManager.setRouteChecked(id);
    //
    // final Intent intent = (new Intent()).putExtra(RESPONSE_ROUTEID,
    // id).putExtra(PoiListActivity.RESPONSE_TABPAGE,
    // RESPONSE_TABPAGEROUTE);
    //
    // if (getParent() != null) {
    // getParent().setResult(RESULT_OK, intent);
    // } else {
    // setResult(RESULT_OK, intent);
    // }
    // finish();
    // break;
    // }
    // case R.id.menu_deletepoi:
    // mPoiManager.deleteRoute(id);
    // FillData();
    // break;
    // case R.id.menu_exporttogpxpoi:
    // DoExportRouteGPX(id);
    // break;
    // case R.id.menu_exporttokmlpoi:
    // DoExportRouteKML(id);
    // break;
    //
    // }
    // }

    // private void DoExportRouteKML(final int id) {
    // showDialog(R.id.dialog_wait);
    // final int routeid = id;
    //
    // mThreadPool.execute(new Runnable() {
    // public void run() {
    // final Route route = mPoiManager.getRoute(routeid);
    //
    // final File folder = Ut
    // .getTschekkoMapsExportDir(RouteListActivity.this);
    //
    // // ExportTrack.exportTrackAsKML(track,folder,mHandler);
    //
    // final SimpleXML xml = new SimpleXML("kml");
    // xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
    // xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");
    //
    // final SimpleXML Placemark = xml.createChild("Placemark");
    // Placemark.createChild("name").setText(route.Name);
    // Placemark.createChild("description").setText(route.Descr);
    // final SimpleXML LineString = Placemark
    // .createChild("LineString");
    // final SimpleXML coordinates = LineString
    // .createChild("coordinates");
    // final StringBuilder builder = new StringBuilder();
    //
    // for (final PoiPoint tp : route.getPoints()) {
    // builder.append(tp.GeoPoint.getLongitude()).append(",")
    // .append(tp.GeoPoint.getLatitude()).append(",")
    // .append(tp.Alt).append(" ");
    // }
    // coordinates.setText(builder.toString().trim());
    //
    // final String filename = folder.getAbsolutePath() + "/Route-"
    // + route.Name + ".kml";
    // final File file = new File(filename);
    // FileOutputStream out;
    // try {
    // file.createNewFile();
    // out = new FileOutputStream(file);
    // final OutputStreamWriter wr = new OutputStreamWriter(out);
    // wr.write(SimpleXML.saveXml(xml));
    // wr.close();
    // Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0,
    // filename).sendToTarget();
    // } catch (final FileNotFoundException e) {
    // Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0,
    // e.getMessage()).sendToTarget();
    // e.printStackTrace();
    // } catch (final IOException e) {
    // Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0,
    // e.getMessage()).sendToTarget();
    // e.printStackTrace();
    // }
    // dlgWait.dismiss();
    // }
    // });
    //
    // }
    //
    // private void DoExportRouteGPX(final int id) {
    // showDialog(R.id.dialog_wait);
    // final int routeid = id;
    //
    // mThreadPool.execute(new Runnable() {
    // public void run() {
    // final Route route = mPoiManager.getRoute(routeid);
    //
    // // SimpleDateFormat formatter = new SimpleDateFormat(
    // // "yyyy-MM-dd'T'HH:mm:ss'Z'");
    // final SimpleXML xml = new SimpleXML("gpx");
    // xml.setAttr("xsi:schemaLocation",
    // "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
    // xml.setAttr("xmlns", "http://www.topografix.com/GPX/1/1");
    // xml.setAttr("xmlns:xsi",
    // "http://www.w3.org/2001/XMLSchema-instance");
    // xml.setAttr("creator",
    // "RMaps - http://code.google.com/p/robertprojects/");
    // xml.setAttr("version", "1.1");
    //
    // final SimpleXML meta = xml.createChild("metadata");
    // meta.createChild("name").setText(route.Name);
    // meta.createChild("desc").setText(route.Descr);
    // final SimpleXML autor = meta.createChild("author");
    // autor.createChild("name").setText("RMaps");
    //
    // final SimpleXML rte = xml.createChild("rte");
    // for (final PoiPoint tp : route.getPoints()) {
    //
    // final SimpleXML rtpt = rte.createChild("rtept");
    // rtpt.setAttr("lat",
    // Double.toString(tp.GeoPoint.getLatitude()));
    // rtpt.setAttr("lon",
    // Double.toString(tp.GeoPoint.getLongitude()));
    // rtpt.createChild("ele").setText(Double.toString(tp.Alt));
    // rtpt.createChild("desc").setText(tp.Descr);
    // }
    //
    // final File folder = Ut
    // .getTschekkoMapsExportDir(RouteListActivity.this);
    // final String filename = folder.getAbsolutePath() + "/Route-"
    // + route.Name + ".gpx";
    // final File file = new File(filename);
    // FileOutputStream out;
    // try {
    // file.createNewFile();
    // out = new FileOutputStream(file);
    // final OutputStreamWriter wr = new OutputStreamWriter(out);
    // wr.write(SimpleXML.saveXml(xml));
    // wr.close();
    // Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0,
    // filename).sendToTarget();
    // } catch (final FileNotFoundException e) {
    // Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0,
    // e.getMessage()).sendToTarget();
    // e.printStackTrace();
    // } catch (final IOException e) {
    // Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0,
    // e.getMessage()).sendToTarget();
    // e.printStackTrace();
    // }
    //
    // dlgWait.dismiss();
    // }
    // });
    //
    // }
    //
    // @Override
    // protected Dialog onCreateDialog(final int id) {
    // switch (id) {
    // case R.id.dialog_wait: {
    // dlgWait = new ProgressDialog(this);
    // dlgWait.setMessage("Please wait...");
    // dlgWait.setIndeterminate(true);
    // dlgWait.setCancelable(false);
    // return dlgWait;
    // }
    // }
    // return super.onCreateDialog(id);
    // }

    // @Override
    // public boolean onChildClick(final ExpandableListView parent, final View
    // v,
    // final int groupPosition, final int childPosition, final long id) {
    // Ut.dd("pos=" + groupPosition);
    // Ut.dd("id=" + id);
    // mPoiManager.setRouteChecked((int) id);
    // FillData();
    //
    // return true;// super.onChildClick(parent, v, groupPosition,
    // // childPosition, id);
    // }

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
    // "POIs imported:   " + pois + "\n"
    // + // move to strings.xml
    // "Routes imported: " + routes + "\n"
    // + "Tracks imported: " + tracks + "\n",
    // Toast.LENGTH_LONG).show();
    // FillData();
    // }
    // break;
    // }
    // super.onActivityResult(requestCode, resultCode, data);
    // }
}
