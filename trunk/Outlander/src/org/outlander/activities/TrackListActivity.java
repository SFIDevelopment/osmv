package org.outlander.activities;

import org.outlander.fragments.TrackListFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TrackListActivity extends FragmentActivity {

    public static final String RESPONSE_TRACKID      = "trackid";
    public static final String RESPONSE_TABPAGETRACK = "tracktab";

    // private PoiManager mPoiManager;
    //
    // private ProgressDialog dlgWait;
    // protected ExecutorService mThreadPool = Executors
    // .newFixedThreadPool(2);
    // private SimpleInvalidationHandler mHandler;

    // public class SimpleInvalidationHandler extends Handler {
    //
    // @Override
    // public void handleMessage(final Message msg) {
    // switch (msg.what) {
    // case R.id.tracks:
    // if (msg.arg1 == 0) {
    // Toast.makeText(TrackListActivity.this,
    // R.string.trackwriter_nothing, Toast.LENGTH_LONG)
    // .show();
    // } else {
    // Toast.makeText(TrackListActivity.this,
    // R.string.trackwriter_saved, Toast.LENGTH_LONG)
    // .show();
    // }
    //
    // FillData();
    // break;
    // case R.id.menu_exporttogpxpoi:
    // if (msg.arg1 == 0) {
    // Toast.makeText(
    // TrackListActivity.this,
    // getString(R.string.message_error) + " "
    // + (String) msg.obj, Toast.LENGTH_LONG)
    // .show();
    // } else {
    // Toast.makeText(
    // TrackListActivity.this,
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
            final TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }

        // this.setContentView(R.layout.route_list);
        // registerForContextMenu(getExpandableListView());
        //
        // mHandler = new SimpleInvalidationHandler();

    }

    // @Override
    // protected void onCreate(final Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // this.setContentView(R.layout.track_list);
    // registerForContextMenu(getListView());
    // mPoiManager = new PoiManager(this);
    //
    // mHandler = new SimpleInvalidationHandler();
    //
    // // ((Button) findViewById(R.id.startButton))
    // // .setOnClickListener(new OnClickListener() {
    // // public void onClick(final View v) {
    // // startService(new Intent("org.tschekkomap.trackwriter"));
    // // }
    // // });
    // // ((Button) findViewById(R.id.stopButton))
    // // .setOnClickListener(new OnClickListener() {
    // // public void onClick(final View v) {
    // // stopService(new Intent("org.tschekkomap.trackwriter"));
    // // doSaveTrack();
    // // }
    // // });
    // }

    // @Override
    // protected void onResume() {
    // FillData();
    // super.onResume();
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
    // final Cursor c = mPoiManager.getGeoDatabase().getTrackListCursor();
    // if (c != null) {
    // startManagingCursor(c);
    //
    // final ListAdapter adapter = new SimpleCursorAdapter(this,
    // R.layout.poilist_item, c, new String[] { "name", "descr",
    // "image" }, new int[] { android.R.id.text1,
    // android.R.id.text2, R.id.ImageView01 }) {
    //
    // ViewHolder viewHolder;
    //
    // @Override
    // public View newView(final Context context, final Cursor cursor,
    // final ViewGroup parent) {
    //
    // final Cursor c = getCursor();
    //
    // final LayoutInflater inflater = LayoutInflater
    // .from(context);
    //
    // final View v = inflater.inflate(R.layout.poilist_item,
    // parent, false);
    //
    // if (viewHolder == null) {
    // viewHolder = new ViewHolder();
    //
    // viewHolder.textView1 = (TextView) v
    // .findViewById(android.R.id.text1);
    // viewHolder.textView2 = (TextView) v
    // .findViewById(android.R.id.text2);
    // viewHolder.textView3 = (TextView) v
    // .findViewById(R.id.infotext3);
    //
    // viewHolder.icon1 = (ImageView) v
    // .findViewById(R.id.ImageView01);
    // viewHolder.icon2 = (ImageView) v
    // .findViewById(R.id.ImageView02);
    //
    // }
    //
    // final String name = c.getString(c.getColumnIndex("name"));
    //
    // /**
    // * Next set the name of the entry.
    // */
    //
    // viewHolder.textView1.setText(name);
    //
    // final String descr = c.getString(c.getColumnIndex("descr"));
    //
    // /**
    // * Next set the descr of the entry.
    // */
    //
    // viewHolder.textView2.setText(descr);
    // viewHolder.icon2.setImageResource(R.drawable.list_track);
    // viewHolder.icon1.setImageResource(c.getInt(c
    // .getColumnIndex("image")));
    //
    // // get all points for statistics
    // final long trackId = c.getLong(c.getColumnIndex("_id"));
    // final Cursor c2 = mPoiManager.getGeoDatabase()
    // .getTrackPoints(trackId);
    //
    // final String stat = "# trackpoints: " + c2.getCount();
    // // + duration + length
    //
    // viewHolder.textView3.setText(stat);
    //
    // c2.close();
    // return v;
    // }
    // };
    // setListAdapter(adapter);
    // }
    // ;
    // }
    //
    // @Override
    // public boolean onCreateOptionsMenu(final Menu menu) {
    // super.onCreateOptionsMenu(menu);
    //
    // final MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.tracklist, menu);
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
    // startActivity((new Intent(this, ImportPoiActivity.class)));
    // return true;
    // }
    //
    // return true;
    // }
    //
    // @Override
    // public void onCreateContextMenu(final ContextMenu menu, final View v,
    // final ContextMenuInfo menuInfo) {
    // // int pointid = (int)
    // // ((AdapterView.AdapterContextMenuInfo)menuInfo).id;
    // // PoiPoint poi = mPoiManager.getPoiPoint(pointid);
    // //
    // menu.add(0, R.id.menu_gotopoi, 0, getText(R.string.menu_goto_track));
    // menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
    // menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
    // menu.add(0, R.id.menu_exporttogpxpoi, 0,
    // getText(R.string.menu_exporttogpx));
    // menu.add(0, R.id.menu_exporttokmlpoi, 0,
    // getText(R.string.menu_exporttokml));
    // menu.add(0, R.id.menu_exporttoigctrack, 0,
    // getText(R.string.menu_exporttoigc));
    //
    // super.onCreateContextMenu(menu, v, menuInfo);
    // }
    //
    // @Override
    // public boolean onContextItemSelected(final MenuItem item) {
    // final int id = (int) ((AdapterView.AdapterContextMenuInfo) item
    // .getMenuInfo()).id;
    // // PoiPoint poi = mPoiManager.getPoiPoint(pointid);
    // //
    // switch (item.getItemId()) {
    // case R.id.menu_editpoi:
    // startActivity((new Intent(this, TrackActivity.class)).putExtra(
    // "id", id));
    // break;
    // case R.id.menu_gotopoi:
    // setResult(RESULT_OK,
    // (new Intent()).putExtra(RESPONSE_TRACKID, id));
    //
    // {
    // final Intent intent = (new Intent()).putExtra(
    // RESPONSE_TRACKID, id).putExtra(
    // PoiListActivity.RESPONSE_TABPAGE,
    // RESPONSE_TABPAGETRACK);
    //
    // if (getParent() != null) {
    // getParent().setResult(RESULT_OK, intent);
    // } else {
    // setResult(RESULT_OK, intent);
    // }
    // finish();
    // }
    // break;
    // case R.id.menu_deletepoi:
    // mPoiManager.deleteTrack(id);
    // FillData();
    // break;
    // case R.id.menu_exporttogpxpoi:
    // doExportTrackGPX(id);
    // break;
    // case R.id.menu_exporttokmlpoi:
    // doExportTrackKML(id);
    // break;
    // case R.id.menu_exporttoigctrack:
    // Toast.makeText(TrackListActivity.this, R.string.NYI,
    // Toast.LENGTH_LONG).show();
    // break;
    // }
    //
    // return super.onContextItemSelected(item);
    // }
    //
    // private void doExportTrackKML(final int id) {
    // showDialog(R.id.dialog_wait);
    // final int trackid = id;
    //
    // mThreadPool.execute(new Runnable() {
    // public void run() {
    // final Track track = mPoiManager.getTrack(trackid);
    //
    // final File folder = Ut
    // .getTschekkoMapsExportDir(TrackListActivity.this);
    //
    // // ExportTrack.exportTrackAsKML(track,folder,mHandler);
    //
    // final SimpleXML xml = new SimpleXML("kml");
    // xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
    // xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");
    //
    // final SimpleXML Placemark = xml.createChild("Placemark");
    // Placemark.createChild("name").setText(track.Name);
    // Placemark.createChild("description").setText(track.Descr);
    // final SimpleXML LineString = Placemark
    // .createChild("LineString");
    // final SimpleXML coordinates = LineString
    // .createChild("coordinates");
    // final StringBuilder builder = new StringBuilder();
    //
    // for (final TrackPoint tp : track.getPoints()) {
    // builder.append(tp.getLongitude()).append(",")
    // .append(tp.getLatitude()).append(",")
    // .append(tp.alt).append(" ");
    // }
    // coordinates.setText(builder.toString().trim());
    //
    // final String filename = folder.getAbsolutePath() + "/track"
    // + track.getId() + ".kml";
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
    // private void doExportTrackGPX(final int id) {
    // showDialog(R.id.dialog_wait);
    // final int trackid = id;
    //
    // mThreadPool.execute(new Runnable() {
    // public void run() {
    // final Track track = mPoiManager.getTrack(trackid);
    //
    // final SimpleDateFormat formatter = new SimpleDateFormat(
    // "yyyy-MM-dd'T'HH:mm:ss'Z'");
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
    // meta.createChild("name").setText(track.Name);
    // meta.createChild("desc").setText(track.Descr);
    // final SimpleXML autor = meta.createChild("author");
    // autor.createChild("name").setText("RMaps");
    //
    // final SimpleXML trk = xml.createChild("trk");
    // final SimpleXML trkseg = trk.createChild("trkseg");
    // SimpleXML trkpt = null;
    // for (final TrackPoint tp : track.getPoints()) {
    // trkpt = trkseg.createChild("trkpt");
    // trkpt.setAttr("lat", Double.toString(tp.getLatitude()));
    // trkpt.setAttr("lon", Double.toString(tp.getLongitude()));
    // trkpt.createChild("ele").setText(Double.toString(tp.alt));
    // trkpt.createChild("time")
    // .setText(formatter.format(tp.date));
    // }
    //
    // final File folder = Ut
    // .getTschekkoMapsExportDir(TrackListActivity.this);
    // final String filename = folder.getAbsolutePath() + "/track"
    // + trackid + ".gpx";
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
    //
    // @Override
    // protected void onListItemClick(final ListView l, final View v,
    // final int position, final long id) {
    // Ut.dd("pos=" + position);
    // Ut.dd("id=" + id);
    // mPoiManager.setTrackChecked((int) id);
    // FillData();
    // super.onListItemClick(l, v, position, id);
    // }

}
