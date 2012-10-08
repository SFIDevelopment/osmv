package org.outlander.fragments;

import org.outlander.R;
import org.outlander.activities.TurnRouteListActivity;
import org.outlander.model.LocationPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.SetTargetTask;
import org.outlander.utils.geo.GeoMathUtil;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class RoutingDialogFragment extends SherlockDialogFragment {

    RadioGroup radios;
    double     lat, lon;

    static public RoutingDialogFragment newInstance(final String title, final String descr, final double lat, final double lon, final int dialogTitle) {
        final RoutingDialogFragment fragment = new RoutingDialogFragment();

        final Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("descr", descr);
        args.putDouble("lat", lat);
        args.putDouble("lon", lon);

        args.putInt("dialogTitle", dialogTitle);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);

        final Bundle extras = getArguments();

        final int title = extras.getInt("dialogTitle");
        if (title != 0) {
            dialog.setTitle(title);
        }

        return dialog;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.choosenavmode, container, false);

        Bundle extras = getArguments(); // savedInstanceState; use to restore
                                        // visual state!
        if (extras == null) {
            extras = new Bundle();
        }

        radios = (RadioGroup) v.findViewById(R.id.radioGroupNavi);

        final EditText title = (EditText) v.findViewById(R.id.Title);
        final EditText latLon = (EditText) v.findViewById(R.id.LatLon);

        title.setText(extras.getString("title"));

        lat = extras.getDouble("lat");
        lon = extras.getDouble("lon");

        latLon.setText(GeoMathUtil.formatCoordinate(lat, lon, CoreInfoHandler.getInstance().getCoordFormatId()));

        ((Button) v.findViewById(R.id.saveButton)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                doSaveAction();
                dismiss();
            }
        });
        ((Button) v.findViewById(R.id.discardButton)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });
        return v;
    }

    private void doSaveAction() {
        switch (radios.getCheckedRadioButtonId()) {
            case R.id.radioCar: {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=" + lat + "," + lon)));
                break;
            }
            case R.id.radioFeet: {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=" + lat + "," + lon + "&mode=w")));
                break;
            }
            case R.id.radioInt: {

                final LocationPoint lp = new LocationPoint(lat, lon);

                new SetTargetTask(lp).execute(lp);

                //
                //
                //
                // point.setGeoPoint(gpoint);
                // point.setTitle(toponym.getName());
                // point.setDescr(toponym.getCountryName());
                // point.setIconId(R.drawable.poiyellow);
                // point.setCategoryId(DBConstants.POI_CATEGORY_TOPO);
                //
                // points.add(point);
                //
                // CoreInfoHandler.getInstance().getDBManager(getActivity())
                // .updatePoi(point);
                //
                //
                // CoreInfoHandler.getInstance().setCurrentTarget(
                // new GeoPoint(lat, lon));
                // CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_NO);
                // CoreInfoHandler
                // .getInstance()
                // .gotoPage(
                // FragmentFactory
                // .getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP));

                break;
            }
            case R.id.radioShowRoute: {

                // CoreInfoHandler.getInstance().setCurrentTarget(
                // new GeoPoint(lat, lon));

                final Intent intent = new Intent(getActivity(), TurnRouteListActivity.class);

                startActivity(intent);

                // Toast.makeText(getActivity(), R.string.NYI,
                // Toast.LENGTH_LONG)
                // .show();

                break;
            }
        }
    }

}
