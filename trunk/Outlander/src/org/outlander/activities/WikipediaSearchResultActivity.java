package org.outlander.activities;

import org.outlander.fragments.WikipediaSearchResultFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class WikipediaSearchResultActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        }
        else {
            final WikipediaSearchResultFragment fragment = new WikipediaSearchResultFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

    //
    // private ProgressDialog dlgWait;
    // protected ExecutorService mThreadPool = Executors
    // .newFixedThreadPool(2);
    // // private SimpleInvalidationHandler mHandler;
    //
    // private List<WikipediaArticle> weblinks;
    //
    // // private class SimpleInvalidationHandler extends Handler {
    // //
    // // @Override
    // // public void handleMessage(final Message msg) {
    // // switch (msg.what) {
    // // case R.id.menu_exporttogpxpoi:
    // // if (msg.arg1 == 0) {
    // // Toast.makeText(
    // // WikipediaSearchResultActivity.this,
    // // getString(R.string.message_error) + " "
    // // + (String) msg.obj, Toast.LENGTH_LONG)
    // // .show();
    // // } else {
    // // Toast.makeText(
    // // WikipediaSearchResultActivity.this,
    // // getString(R.string.message_trackexported) + " "
    // // + (String) msg.obj, Toast.LENGTH_LONG)
    // // .show();
    // // }
    // // break;
    // // }
    // // }
    // // }
    //
    // private List<WikipediaArticle> SearchForArticles(final GeoPoint geoPoint)
    // {
    // List<WikipediaArticle> weblinks = null;
    // try {
    // if (Ut.isInternetConnectionAvailable(this)) {
    // final String language = PreferenceManager
    // .getDefaultSharedPreferences(this).getString(
    // "pref_googlelanguagecode", "en");
    // WebService.setUserName(WebService.USERNAME);
    // weblinks = WebService.findNearbyWikipedia(
    // geoPoint.getLatitude(), geoPoint.getLongitude(), 2,
    // language, 10);
    // }
    // } catch (final Exception e) {
    //
    // }
    //
    // return weblinks;
    // }
    //
    // @Override
    // protected void onCreate(final Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // // this.setContentView(R.layout.searchresult);
    // // registerForContextMenu(getListView());
    //
    // // getListView().
    //
    // Bundle extras = getIntent().getExtras();
    // if (extras == null) {
    // extras = new Bundle();
    // }
    // final double lat = extras.getDouble("lat", 0);
    // final double lon = extras.getDouble("lon", 0);
    //
    // final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
    //
    // // mHandler = new SimpleInvalidationHandler();
    //
    // showDialog(R.id.dialog_wait);
    // weblinks = SearchForArticles(gp);
    // dlgWait.dismiss();
    //
    // addHeader();
    // }
    //
    // private void addHeader() {
    // final ListView lv = getListView();
    // final LayoutInflater inflater = getLayoutInflater();
    // final View header = inflater.inflate(R.layout.list_header,
    // (ViewGroup) findViewById(R.id.LinearLayout01));
    // lv.addHeaderView(header, null, false);
    //
    // final TextView headerTxt = (TextView) header
    // .findViewById(android.R.id.text1);
    // if (headerTxt != null) {
    // headerTxt.setText(R.string.search_results);
    // }
    // final TextView detailsHeader = (TextView) header
    // .findViewById(android.R.id.text2);
    // if (detailsHeader != null) {
    // final String newHeaderDescr = getString(R.string.EntriesInCategory)
    // + ((weblinks != null) ? weblinks.size() : 0);
    // detailsHeader.setText(newHeaderDescr);
    // }
    // }
    //
    // @Override
    // protected void onResume() {
    // FillData();
    // super.onResume();
    // }
    //
    // public static class ViewHolder {
    // public TextView textView1;
    // public TextView textView2;
    // ImageView icon1;
    // ImageView icon2;
    // }
    //
    // private void FillData() {
    //
    // final ListAdapter adapter = new BaseAdapter() {
    //
    // private final LayoutInflater mInflater = (LayoutInflater)
    // getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //
    // public int getCount() {
    // return ((weblinks != null) ? weblinks.size() : 0);
    // }
    //
    // public long getItemId(final int position) {
    // return position;
    // }
    //
    // public View getView(final int position, View convertView,
    // final ViewGroup parent) {
    // // System.out.println("getView " + position + " " +
    // // convertView);
    //
    // ViewHolder holder = null;
    // if (convertView == null) {
    // convertView = mInflater.inflate(R.layout.list_item, null);
    // holder = new ViewHolder();
    // holder.textView1 = (TextView) convertView
    // .findViewById(android.R.id.text1);
    // holder.textView2 = (TextView) convertView
    // .findViewById(android.R.id.text2);
    // holder.icon1 = (ImageView) convertView
    // .findViewById(R.id.ImageView01);
    // holder.icon2 = (ImageView) convertView
    // .findViewById(R.id.ImageView02);
    // convertView.setTag(holder);
    // } else {
    // holder = (ViewHolder) convertView.getTag();
    // }
    //
    // holder.textView1.setText(weblinks.get(position).getTitle());
    // holder.textView2.setText(weblinks.get(position).getSummary());
    // holder.icon2.setImageResource(R.drawable.wikipedia);
    //
    // return convertView;
    // }
    //
    // public Object getItem(final int position) {
    // return weblinks.get(position);
    // }
    // };
    //
    // setListAdapter(adapter);
    // }
    //
    // private void openURL(final WikipediaArticle wpa) {
    // if (wpa.getWikipediaUrl() != null) {
    // try {
    // final Intent viewIntent = new Intent(
    // "android.intent.action.VIEW", Uri.parse(wpa
    // .getWikipediaUrl()));
    // startActivity(viewIntent);
    // } catch (final Exception x) {
    // Toast.makeText(WikipediaSearchResultActivity.this,
    // getString(R.string.url_open_problem), Toast.LENGTH_LONG)
    // .show();
    // }
    // } else {
    // Toast.makeText(WikipediaSearchResultActivity.this,
    // getString(R.string.url_missing), Toast.LENGTH_LONG).show();
    // }
    // }
    //
    // // @Override
    // // public void onCreateContextMenu(final ContextMenu menu, final View v,
    // // final ContextMenuInfo menuInfo) {
    // //
    // // // super.onCreateContextMenu(menu, v, menuInfo);
    // // final AdapterView.AdapterContextMenuInfo info =
    // // (AdapterView.AdapterContextMenuInfo) menuInfo;
    // // final WikipediaArticle wpa = weblinks.get(info.position);
    // //
    // // if (wpa.getWikipediaUrl() != null) {
    // // menu.setHeaderTitle(wpa.getTitle());
    // // menu.add(0, R.id.menu_openurl, 0, getText(R.string.menu_openurl));
    // // } else {
    // // Toast.makeText(WikipediaSearchResultActivity.this,
    // // getString(R.string.url_missing), Toast.LENGTH_LONG).show();
    // // }
    // // }
    // //
    // // @Override
    // // public boolean onContextItemSelected(final MenuItem item) {
    // //
    // // final AdapterView.AdapterContextMenuInfo info =
    // // (AdapterView.AdapterContextMenuInfo) item
    // // .getMenuInfo();
    // //
    // // final WikipediaArticle wpa = weblinks.get(info.position);
    // // //
    // // switch (item.getItemId()) {
    // // case R.id.menu_openurl:
    // // openURL(wpa);
    // // break;
    // // }
    // // return super.onContextItemSelected(item);
    // // }
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
    //
    // final WikipediaArticle wpa = weblinks.get(position-1); // ???
    //
    // openURL(wpa);
    // }

}
