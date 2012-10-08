package org.outlander.activities;

import org.outlander.fragments.CategoryListFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class RouteCategoryListActivity extends FragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        }
        else {
            final CategoryListFragment fragment = new CategoryListFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

    // private PoiManager mPoiManager;
    //
    // @Override
    // protected void onCreate(final Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // registerForContextMenu(getListView());
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
    // headerTxt.setText(R.string.category_routes);
    // }
    // }
    //
    // @Override
    // protected void onStart() {
    // mPoiManager = new PoiManager(this);
    // super.onStart();
    // }
    //
    // @Override
    // protected void onStop() {
    // mPoiManager.FreeDatabases();
    // mPoiManager = null;
    // super.onStop();
    // }
    //
    // @Override
    // protected void onResume() {
    // FillData();
    // super.onResume();
    // }
    //
    // private void FillData() {
    // final Cursor c = mPoiManager.getGeoDatabase()
    // .getRouteCategoryListCursor();
    // startManagingCursor(c);
    //
    // final ListAdapter adapter = new SimpleCursorAdapter(this,
    // // android.R.layout.simple_list_item_2,
    // R.layout.list_item, c, new String[] { "name", "descr" },
    // new int[] { android.R.id.text1, android.R.id.text2 }) {
    // };
    // ;
    // setListAdapter(adapter);
    // }
    //
    // @Override
    // public boolean onCreateOptionsMenu(final Menu menu) {
    // super.onCreateOptionsMenu(menu);
    //
    // final MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.categorylist_menu, menu);
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
    // startActivity((new Intent(this, RouteCategoryActivity.class)));
    // return true;
    // }
    //
    // return true;
    // }
    //
    // @Override
    // public void onCreateContextMenu(final ContextMenu menu, final View v,
    // final ContextMenuInfo menuInfo) {
    // final int id = (int) ((AdapterView.AdapterContextMenuInfo) menuInfo).id;
    // final RouteCategory category = mPoiManager.getRouteCategory(id);
    //
    // menu.setHeaderTitle(category.Title);
    //
    // menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
    // if (category.Hidden == true) {
    // menu.add(0, R.id.menu_show, 0, getText(R.string.menu_show));
    // } else {
    // menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
    // }
    // menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
    //
    // super.onCreateContextMenu(menu, v, menuInfo);
    // }
    //
    // @Override
    // public boolean onContextItemSelected(final MenuItem item) {
    // final int id = (int) ((AdapterView.AdapterContextMenuInfo) item
    // .getMenuInfo()).id;
    // final RouteCategory category = mPoiManager.getRouteCategory(id);
    //
    // switch (item.getItemId()) {
    // case R.id.menu_editpoi:
    // startActivity((new Intent(this, RouteCategoryActivity.class))
    // .putExtra("id", id));
    // break;
    // case R.id.menu_deletepoi:
    // mPoiManager.deleteRouteCategory(id);
    // FillData();
    // break;
    // case R.id.menu_hide:
    // category.Hidden = true;
    // mPoiManager.updateRouteCategory(category);
    // FillData();
    // break;
    // case R.id.menu_show:
    // category.Hidden = false;
    // mPoiManager.updateRouteCategory(category);
    // FillData();
    // break;
    // }
    //
    // return super.onContextItemSelected(item);
    // }

}
