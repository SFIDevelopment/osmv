package org.outlander.fragments;

import org.andnav.osm.util.GeoPoint;
import org.geonames.WebService;
import org.outlander.R;
import org.outlander.model.LocationPoint;
import org.outlander.model.PoiPoint;
import org.outlander.utils.AddressTask;
import org.outlander.utils.AltitudeTask;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;

import android.app.Dialog;
import android.database.Cursor;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class PoiDialogFragment extends SherlockDialogFragment {

    EditText         mTitle, mLat, mLon, mDescr;
    Spinner          mSpinner;
    CheckBox         mHidden;
    private PoiPoint mPoiPoint;

    static public PoiDialogFragment newInstance(final int pointid,
            final String title, final String descr, final double lat,
            final double lon, final int dialogTitle) {
        final PoiDialogFragment fragment = new PoiDialogFragment();

        final Bundle args = new Bundle();
        args.putInt("pointid", pointid);
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
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.poi, container, false);
        mTitle = (EditText) v.findViewById(R.id.Title);
        mLat = (EditText) v.findViewById(R.id.Lat);
        mLon = (EditText) v.findViewById(R.id.Lon);
        mDescr = (EditText) v.findViewById(R.id.Descr);
        mHidden = (CheckBox) v.findViewById(R.id.Hidden);

        mSpinner = (Spinner) v.findViewById(R.id.spinnerCategory);
        final Cursor c = CoreInfoHandler.getInstance().getDBManager(null)
                .getGeoDatabase().getPoiUserCategoryListCursor();
        getActivity().startManagingCursor(c);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(), android.R.layout.simple_spinner_item, c,
                new String[] { "name" }, new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        Bundle extras = getArguments(); // savedInstanceState; use to restore
                                        // visual state!
        if (extras == null) {
            extras = new Bundle();
        }
        final int id = extras.getInt("pointid", PoiPoint.EMPTY_ID());

        if (id < 0) {

            final double lat = extras.getDouble("lat");
            final double lon = extras.getDouble("lon");
            mPoiPoint = new PoiPoint();
            mTitle.setText(extras.getString("title"));
            mSpinner.setSelection(0);
            mLat.setText(GeoMathUtil.formatGeoCoord(lat));
            mLon.setText(GeoMathUtil.formatGeoCoord(lon));
            mDescr.setText(extras.getString("descr"));
            mHidden.setChecked(false);

            // get addr if...
            if (mDescr.getText().length() < 1) {

                final AddressTask task =

                new AddressTask() {
                    @Override
                    protected void onPostExecute(final Address address) {
                        mDescr.setText(MapFragment.formatAddress(address));
                    }
                };

                task.setContext(getActivity());
                task.execute(new LocationPoint(lat, lon));
            }
        } else {
            mPoiPoint = CoreInfoHandler.getInstance().getDBManager(null)
                    .getPoiPoint(id);

            if (mPoiPoint == null) {
                // finish();
                dismiss();
            }

            mTitle.setText(mPoiPoint.getTitle());
            for (int pos = 0; pos < mSpinner.getCount(); pos++) {
                if (mSpinner.getItemIdAtPosition(pos) == mPoiPoint
                        .getCategoryId()) {
                    mSpinner.setSelection(pos);
                    break;
                }
            }
            mLat.setText(GeoMathUtil.formatGeoCoord(mPoiPoint.getGeoPoint()
                    .getLatitude()));
            mLon.setText(GeoMathUtil.formatGeoCoord(mPoiPoint.getGeoPoint()
                    .getLongitude()));
            mDescr.setText(mPoiPoint.getDescr());
            mHidden.setChecked(mPoiPoint.isHidden());

        }

        // fix altitude ( if possible )
        if (mPoiPoint.getAlt() <= 0) {
            final AltitudeTask task =

            new AltitudeTask() {

                @Override
                protected void onPostExecute(final Integer altitude) {
                    mPoiPoint.setAlt(altitude);
                }
            };

            task.setContext(getActivity());
            task.execute(mPoiPoint);
        }

        ((Button) v.findViewById(R.id.saveButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        doSaveAction();
                        dismiss();
                    }
                });
        ((Button) v.findViewById(R.id.discardButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dismiss();
                    }
                });
        return v;
    }

    private void doSaveAction() {
        mPoiPoint.setTitle(mTitle.getText().toString());
        mPoiPoint.setCategoryId((int) mSpinner.getSelectedItemId());
        mPoiPoint.setDescr(mDescr.getText().toString());
        mPoiPoint.setGeoPoint(GeoPoint.from2DoubleString(mLat.getText()
                .toString(), mLon.getText().toString()));
        mPoiPoint.setHidden(mHidden.isChecked());

        final AsyncTask<PoiPoint, Void, PoiPoint> task = new AsyncTask<PoiPoint, Void, PoiPoint>() {

            @Override
            protected void onPostExecute(final PoiPoint result) {
                CoreInfoHandler.getInstance().getDBManager(null)
                        .updatePoi(result);
            }

            @Override
            protected PoiPoint doInBackground(final PoiPoint... params) {
                try {
                    if (params[0].getAlt() < 0) {
                        params[0].setAlt(WebService.astergdem(params[0]
                                .getGeoPoint().getLatitude(), params[0]
                                .getGeoPoint().getLongitude()));
                    }
                } catch (final Exception x) {
                    Ut.e("Webservicerequest for Altitude failed: "
                            + x.toString());
                }
                return params[0];
            }
        };

        task.execute(mPoiPoint);

        Toast.makeText(getActivity(), R.string.message_saved,
                Toast.LENGTH_SHORT).show();
    }

}
