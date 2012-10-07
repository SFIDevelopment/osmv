package org.outlander.activities;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.model.PoiPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.geo.GeoMathUtil;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class PoiActivity extends Activity {
    EditText         mTitle, mLat, mLon, mDescr;
    Spinner          mSpinner;
    CheckBox         mHidden;
    private PoiPoint mPoiPoint;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.poi);

        mTitle = (EditText) findViewById(R.id.Title);
        mLat = (EditText) findViewById(R.id.Lat);
        mLon = (EditText) findViewById(R.id.Lon);
        mDescr = (EditText) findViewById(R.id.Descr);
        mHidden = (CheckBox) findViewById(R.id.Hidden);

        mSpinner = (Spinner) findViewById(R.id.spinnerCategory);
        final Cursor c = CoreInfoHandler.getInstance().getDBManager(null)
                .getGeoDatabase().getPoiCategoryListCursor();
        startManagingCursor(c);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, c,
                new String[] { "name" }, new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        final int id = extras.getInt("pointid", PoiPoint.EMPTY_ID());

        if (id < 0) {
            mPoiPoint = new PoiPoint();
            mTitle.setText(extras.getString("title"));
            mSpinner.setSelection(0);
            mLat.setText(GeoMathUtil.formatGeoCoord(extras.getDouble("lat")));
            mLon.setText(GeoMathUtil.formatGeoCoord(extras.getDouble("lon")));
            mDescr.setText(extras.getString("descr"));
            mHidden.setChecked(false);
        } else {
            mPoiPoint = CoreInfoHandler.getInstance().getDBManager(null)
                    .getPoiPoint(id);

            if (mPoiPoint == null) {
                finish();
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

        ((Button) findViewById(R.id.saveButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        doSaveAction();
                    }
                });
        ((Button) findViewById(R.id.discardButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        PoiActivity.this.finish();
                    }
                });
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                doSaveAction();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doSaveAction() {
        mPoiPoint.setTitle(mTitle.getText().toString());
        mPoiPoint.setCategoryId((int) mSpinner.getSelectedItemId());
        mPoiPoint.setDescr(mDescr.getText().toString());
        mPoiPoint.setGeoPoint(GeoPoint.from2DoubleString(mLat.getText()
                .toString(), mLon.getText().toString()));
        mPoiPoint.setHidden(mHidden.isChecked());

        CoreInfoHandler.getInstance().getDBManager(null).updatePoi(mPoiPoint);
        finish();

        Toast.makeText(PoiActivity.this, R.string.message_saved,
                Toast.LENGTH_SHORT).show();
    }

}
