package org.outlander.activities;

import org.outlander.R;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiPoint;
import org.outlander.model.RouteCategory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RouteCategoryActivity extends Activity {
    EditText              mTitle;
    EditText              mDescr;
    private RouteCategory mRouteCategory;
    private DBManager     mPoiManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.routecategory);

        if (mPoiManager == null) {
            mPoiManager = new DBManager(this);
        }

        mTitle = (EditText) findViewById(R.id.Title);
        mDescr = (EditText) findViewById(R.id.Descr);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        final int id = extras.getInt("id", PoiPoint.EMPTY_ID());

        if (id < 0) {
            mRouteCategory = new RouteCategory();
            mTitle.setText(extras.getString("title"));
            mDescr.setText(extras.getString("descr"));
        } else {
            mRouteCategory = mPoiManager.getRouteCategory(id);

            if (mRouteCategory == null) {
                finish();
            }

            mTitle.setText(mRouteCategory.Title);
            mDescr.setText(mRouteCategory.Description);
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
                        RouteCategoryActivity.this.finish();
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
        mRouteCategory.Title = mTitle.getText().toString();
        mRouteCategory.Description = mDescr.getText().toString();

        // currently not used
        mRouteCategory.IconId = 0;
        mRouteCategory.MinZoom = 14;
        mRouteCategory.Hidden = false;

        mPoiManager.updateRouteCategory(mRouteCategory);
        finish();

        Toast.makeText(this, R.string.message_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            // ???
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
