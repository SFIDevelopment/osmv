package org.outlander.activities;

import org.outlander.fragments.CategoryListFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PoiCategoryListActivity extends FragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        } else {
            final CategoryListFragment fragment = new CategoryListFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment).commit();
        }
    }

    //
    //
    //
    // private PoiManager mPoiManager;
    // private QuickAction mQuickAction;
    // private int mSelectedRow;
    // private final HashMap<Integer, Integer> mItemIds = new HashMap<Integer,
    // Integer>(); // list
    // // position,
    // // id
    //
    // @Override
    // protected void onCreate(final Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // registerForContextMenu(getListView());
    //
    // addHeader();
    //
    // mQuickAction = new QuickAction(this);
    // setupQuickAction(mQuickAction);
    // }
    //
    // private void addHeader() {
    // final ListView lv = getListView();
    // final LayoutInflater inflater = getLayoutInflater();
    // final View header = inflater.inflate(R.layout.list_header,
    // (ViewGroup) findViewById(R.id.LinearLayout01));
    //
    // lv.addHeaderView(header, null, false);
    //
    // final TextView headerTxt = (TextView) header
    // .findViewById(android.R.id.text1);
    // if (headerTxt != null) {
    // headerTxt.setText(R.string.category_pois);
    // }
    //
    // lv.setOnItemClickListener(new OnItemClickListener() {
    // public void onItemClick(AdapterView<?> parent, View view,
    // int position, long id) {
    // mSelectedRow = position; // set the selected row
    // mQuickAction.show(view);
    // }
    // });
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
    // item.setTitle(getResources().getString(R.string.menu_hide));
    // // item.setIcon(getResources().getDrawable(R.drawable.menu_));
    // quickAction.addActionItem(item);
    //
    // // ------R.string.menu_show
    // // ------R.string.menu_show
    //
    // item = new ActionItem();
    //
    // item.setTitle(getResources().getString(R.string.menu_delete));
    // item.setIcon(getResources().getDrawable(R.drawable.menu_delete));
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
    // handleContextItemSelected(R.id.menu_hide);
    // } else if (pos == 3) {
    // handleContextItemSelected(R.id.menu_deletepoi);
    // }
    // // else if (pos == 4) {
    // // handleContextItemSelected(R.id.menu_shareLocation);
    // // }
    // }
    // });
    //
    // }
    //
    // private void FillData() {
    //
    // final Cursor c = mPoiManager.getGeoDatabase()
    // .getPoiCategoryListCursor();
    // startManagingCursor(c);
    //
    // final ListAdapter adapter = new SimpleCursorAdapter(this,
    // // android.R.layout.simple_list_item_2,
    // R.layout.list_item, c, new String[] { "name", "iconid" },
    // new int[] { android.R.id.text1, R.id.ImageView02 }) {
    //
    // int listposition = 0;
    //
    // @Override
    // public View newView(final Context context, final Cursor cursor,
    // final ViewGroup parent) {
    // final Cursor c = getCursor();
    //
    // final LayoutInflater inflater = LayoutInflater.from(context);
    // final View v = inflater.inflate(R.layout.list_item, parent,
    // false);
    //
    // final String name = c.getString(c.getColumnIndex("name"));
    //
    // /**
    // * Next set the name of the entry.
    // */
    // final TextView name_text = (TextView) v
    // .findViewById(android.R.id.text1);
    // if (name_text != null) {
    // name_text.setText(name);
    // }
    //
    // final int categoryId = c.getInt(c.getColumnIndex("_id"));
    //
    // mItemIds.put(listposition, categoryId);
    // listposition++;
    //
    // final String descr = getString(R.string.EntriesInCategory)
    // + " "
    // + mPoiManager.getGeoDatabase().getNrofPoiForCategory(
    // categoryId) + " / "
    // + getString(R.string.EntriesVisibleFrom)
    // + c.getString(c.getColumnIndex("minzoom"));
    //
    // /**
    // * Next set the descr of the entry.
    // */
    // final TextView descr_text = (TextView) v
    // .findViewById(android.R.id.text2);
    // if (descr_text != null) {
    // descr_text.setText(descr);
    // }
    //
    // final ImageView trackIcon = (ImageView) v
    // .findViewById(R.id.ImageView02);
    // final int imageResourceId = c
    // .getInt(c.getColumnIndex("iconid"));
    // trackIcon.setImageResource(imageResourceId);
    //
    // // v.setOnClickListener(new View.OnClickListener() {
    // // public void onClick(View v) {
    // // mQuickAction.show(v);
    // // }
    // // });
    //
    // return v;
    // }
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
    // startActivity((new Intent(this, PoiCategoryActivity.class)));
    // return true;
    // }
    // return true;
    // }
    //
    // private void handleContextItemSelected(final int id) {
    //
    // int itemId = mItemIds.get(mSelectedRow);
    // final PoiCategory category = mPoiManager.getPoiCategory(itemId);
    //
    // switch (id) {
    // case R.id.menu_editpoi:
    // startActivity((new Intent(this, PoiCategoryActivity.class))
    // .putExtra("id", itemId));
    // break;
    // case R.id.menu_deletepoi:
    // mPoiManager.deletePoiCategory(itemId);
    // FillData();
    // break;
    // case R.id.menu_show:
    // category.Hidden = false;
    // mPoiManager.updatePoiCategory(category);
    // FillData();
    // break;
    // case R.id.menu_hide:
    // category.Hidden = true;
    // mPoiManager.updatePoiCategory(category);
    // FillData();
    // break;
    //
    // }
    // }
    //
    // // @Override
    // // public void onCreateContextMenu(final ContextMenu menu, final View v,
    // // final ContextMenuInfo menuInfo) {
    // // // final int id = (int) ((AdapterView.AdapterContextMenuInfo)
    // // // menuInfo).id;
    // // // final PoiCategory category = mPoiManager.getPoiCategory(id);
    // // //
    // // // menu.setHeaderTitle(category.Title);
    // // //
    // // // menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
    // // // if (category.Hidden == true) {
    // // // menu.add(0, R.id.menu_show, 0, getText(R.string.menu_show));
    // // // } else {
    // // // menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
    // // // }
    // // // menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
    // // //
    // // // super.onCreateContextMenu(menu, v, menuInfo);
    // // }
    //
    // // @Override
    // // public boolean onContextItemSelected(final MenuItem item) {
    // // final int id = (int) ((AdapterView.AdapterContextMenuInfo) item
    // // .getMenuInfo()).id;
    // //
    // // // final PoiCategory category = mPoiManager.getPoiCategory(id);
    // //
    // // return super.onContextItemSelected(item);
    // // }

}
