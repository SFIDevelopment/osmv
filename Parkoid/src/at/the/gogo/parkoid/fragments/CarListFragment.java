package at.the.gogo.parkoid.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.Car;
import at.the.gogo.parkoid.util.CoreInfoHolder;

public class CarListFragment extends ListFragment implements PageChangeNotifyer {

    int             mPositionChecked = 0;
    int             mPositionShown   = -1;
    Car             carSelected;
    private boolean isInitialized    = false;

    public static CarListFragment newInstance() {
        final CarListFragment f = new CarListFragment();

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
        final View view = inflater.inflate(R.layout.list, null);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.car_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceList", -1);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
       v.showContextMenu();
    }

    
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {

        final int id = (int) ((AdapterView.AdapterContextMenuInfo) menuInfo).id;
        carSelected = CoreInfoHolder.getInstance().getDbManager().getCar(id);

        menu.setHeaderTitle(carSelected.getName() + " ("
                + carSelected.getLicence() + ")");

        menu.add(0, R.id.menu_editcar, 0, getText(R.string.menu_editcar));
        menu.add(0, R.id.menu_deletecar, 0, getText(R.string.menu_deletecar));

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addcar: {
                addCar();
                break;
            }
            case R.id.deleteallcars: {
                deleteAllCars();
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {

        boolean result = false;
        switch (item.getItemId()) {
            case R.id.menu_editcar: {
                editCar(carSelected.getId());
                result = true;
                break;
            }
            case R.id.menu_deletecar: {
                deleteCar(carSelected.getId());
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreSavedState(savedInstanceState);

        if (!isInitialized) {

            // NOOP for now
            isInitialized = true;
        }
        registerForContextMenu(getListView());

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onResume() {        
        super.onResume();
        resume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoiceList", mPositionChecked);
        outState.putInt("shownChoiceList", mPositionShown);
    }

    public void refreshData(final boolean forceRefresh) {
        // refresh = forceRefresh;
        fillData();
    }

    public void fillData() {

        final GetDataTask asyncTask = new GetDataTask();
        asyncTask.execute((Void) null);
    }

    public static class ViewHolder {
        public TextView[] textView = new TextView[7];

        ImageView         icon1;
        ImageView         icon2;
    }

    final static String[] COLUMNS = { "name", "licence" };
    final static int[]    FIELDS  = { R.id.carName, R.id.carLicence };

    public class GetDataTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(final Void... params) {

            final Cursor cursor = CoreInfoHolder.getInstance().getDbManager()
                    .getDatabase().getCarListCursor();
            getActivity().startManagingCursor(cursor);
            return cursor;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(final Cursor cursor) {

            if (cursor != null) {
                setListAdapter(new SimpleCursorAdapter(getActivity(),
                        R.layout.car_item, cursor, CarListFragment.COLUMNS,
                        CarListFragment.FIELDS));
            }
        }
    }

    private void deleteCar(final int id) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(R.string.warning_delete_car)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogYES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {

                                // CoreInfoHolder.getInstance()
                                // .getDbManager(getActivity()).beginTransaction();

                                CoreInfoHolder.getInstance().getDbManager()
                                        .deleteCar(carSelected.getId());
                                // CoreInfoHolder.getInstance()
                                // .getDbManager(getActivity()).commitTransaction();

                                dialog.dismiss();
                                fillData();
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

    private void editCar(final int id) {
        final DialogFragment df = CarFragment.newInstance(carSelected.getId(),
                carSelected.getName(), carSelected.getLicence(),
                R.string.dlg_car_title, this);
        df.show(getFragmentManager(), "Auto");
    }

    private void addCar() {
        final DialogFragment df = CarFragment.newInstance(-1, "", "",
                R.string.dlg_car_title, this);
        df.show(getFragmentManager(), "Auto");
    }

    private void deleteAllCars() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(R.string.warning_delete_all_cars)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogYES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {

                                CoreInfoHolder.getInstance().getDbManager()
                                        .deleteAllCars();

                                dialog.dismiss();
                                fillData();
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

    private void pause() {
        // NOOP for now
    }

    private void resume() {
        fillData();
    }

    @Override
    public void pageGetsActivated() {
        resume();
    }

    @Override
    public void pageGetsDeactivated() {
        pause();
    }

}
