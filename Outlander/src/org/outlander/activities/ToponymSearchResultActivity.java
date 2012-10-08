package org.outlander.activities;

import org.outlander.fragments.ToponymSearchResultFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ToponymSearchResultActivity extends FragmentActivity {

    public final static String QUERYTERMKEY     = "searchString";
    public final static String RESPONSE_LAT     = "lat";
    public final static String RESPONSE_LON     = "lon";
    public final static String RESPONSE_ADDRESS = "address";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        }
        else {
            final ToponymSearchResultFragment fragment = new ToponymSearchResultFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

    // private ProgressDialog dlgWait;
    //
    // private ToponymSearchResult searchResult = null;
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
    // private ToponymSearchResult searchForLocations(final String searchString)
    // {
    // ToponymSearchResult searchResult = null;
    // try {
    // if (Ut.isInternetConnectionAvailable(this)) {
    // // String language = PreferenceManager
    // // .getDefaultSharedPreferences(this).getString(
    // // "pref_googlelanguagecode", "en");
    //
    // WebService.setUserName(WebService.USERNAME);
    // searchResult = WebService.search(searchString, null, null,
    // null, 0);
    // }
    // } catch (final Exception e) {
    // Ut.e("Toposearch failed with " + e.getMessage());
    // }
    //
    // return searchResult;
    // }
    //
    // @Override
    // protected void onCreate(final Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // // this.setContentView(R.layout.searchresult);
    // registerForContextMenu(getListView());
    //
    // // getListView().
    //
    // Bundle extras = getIntent().getExtras();
    // if (extras == null) {
    // extras = new Bundle();
    // }
    // final String searchString = extras.getString(QUERYTERMKEY);
    //
    // // mHandler = new SimpleInvalidationHandler();
    //
    // showDialog(R.id.dialog_wait);
    // searchResult = searchForLocations(searchString);
    // dlgWait.dismiss();
    //
    // if (searchResult != null) {
    // addHeader();
    // } else {
    // setResult(RESULT_CANCELED, (new Intent()));
    // finish();
    // }
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
    // + ((searchResult != null) ? searchResult
    // .getTotalResultsCount() : 0);
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
    // final SharedPreferences pref = PreferenceManager
    // .getDefaultSharedPreferences(this);
    //
    // final int coordFormt = Integer.parseInt(pref.getString("pref_coords",
    // "1"));
    //
    // final ListAdapter adapter = new BaseAdapter() {
    //
    // GeoPoint point;
    // private final LayoutInflater mInflater = (LayoutInflater)
    // getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //
    // public int getCount() {
    // return (searchResult != null) ? searchResult.getToponyms()
    // .size() : 0;// searchResult.getTotalResultsCount();
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
    // final Toponym toponym = searchResult.getToponyms()
    // .get(position);
    //
    // if (point == null) {
    // point = new GeoPoint((int) (toponym.getLatitude() * 1E6),
    // (int) (toponym.getLongitude() * 1E6));
    // } else {
    // point.setLatitude(toponym.getLatitude());
    // point.setLatitude(toponym.getLongitude());
    // }
    // final String locationtext = GeoMathUtil.formatGeoPoint(point,
    // coordFormt);
    //
    // holder.textView1.setText(toponym.getName() + " ["
    // + toponym.getCountryName() + "]");
    // holder.textView2.setText(locationtext);
    // holder.icon2.setImageResource(R.drawable.icon);
    //
    // return convertView;
    // }
    //
    // public Object getItem(final int position) {
    // return searchResult.getToponyms().get(position);
    // }
    // };
    //
    // setListAdapter(adapter);
    // }
    //
    // @Override
    // public void onCreateContextMenu(final ContextMenu menu, final View v,
    // final ContextMenuInfo menuInfo) {
    //
    // // super.onCreateContextMenu(menu, v, menuInfo);
    // final AdapterView.AdapterContextMenuInfo info =
    // (AdapterView.AdapterContextMenuInfo) menuInfo;
    // final Toponym toponym = searchResult.getToponyms().get(
    // info.position - 1); // why
    // // -1
    // // ??
    //
    // menu.setHeaderTitle(toponym.getName());
    // menu.add(0, R.id.menu_openurl, 0, getText(R.string.menu_goto));
    //
    // // if (wpa.getWikipediaUrl() != null) {
    // // menu.setHeaderTitle(wpa.getTitle());
    // // menu.add(0, R.id.menu_openurl, 0, getText(R.string.menu_openurl));
    // // } else {
    // // Toast.makeText(this, getString(R.string.url_missing),
    // // Toast.LENGTH_LONG).show();
    // // }
    // }
    //
    // @Override
    // public boolean onContextItemSelected(final MenuItem item) {
    //
    // final AdapterView.AdapterContextMenuInfo info =
    // (AdapterView.AdapterContextMenuInfo) item
    // .getMenuInfo();
    //
    // final Toponym toponym = searchResult.getToponyms().get(
    // info.position - 1); // ???
    //
    // switch (item.getItemId()) {
    // case R.id.menu_openurl:
    //
    // setResult(
    // RESULT_OK,
    // (new Intent())
    // .putExtra(RESPONSE_ADDRESS, toponym.getName())
    // .putExtra(RESPONSE_LAT,
    // (int) (toponym.getLatitude() * 1E6))
    // .putExtra(RESPONSE_LON,
    // (int) (toponym.getLongitude() * 1E6)));
    // finish();
    //
    // // try {
    // // final Intent viewIntent = new Intent(
    // // "android.intent.action.VIEW", Uri.parse(wpa
    // // .getWikipediaUrl()));
    // // startActivity(viewIntent);
    // // } catch (Exception x) {
    // // Toast.makeText(this, getString(R.string.url_open_problem),
    // // Toast.LENGTH_LONG).show();
    // // }
    // break;
    // }
    // return super.onContextItemSelected(item);
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
    // // @Override
    // // protected void onListItemClick(final ListView l, final View v,
    // // final int position, final long id) {
    // //
    // // // Object item = getListView().getItemAtPosition(position);
    // // super.onListItemClick(l, v, position, id);
    // // }

}
