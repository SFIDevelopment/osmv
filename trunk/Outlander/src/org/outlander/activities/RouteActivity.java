package org.outlander.activities;

import org.outlander.R;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RouteActivity extends Activity {
    EditText          mName, mDescr;
    private Route     route;
    private DBManager mPoiManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.route);

        if (mPoiManager == null) {
            mPoiManager = new DBManager(this);
        }

        mName = (EditText) findViewById(R.id.Name);
        mDescr = (EditText) findViewById(R.id.Descr);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        final int id = extras.getInt("id", PoiPoint.EMPTY_ID());

        if (id < 0) {
            route = new Route();
            mName.setText(extras.getString("name"));
            mDescr.setText(extras.getString("descr"));
        } else {
            route = mPoiManager.getRoute(id);

            if (route == null) {
                finish();
            }

            mName.setText(route.getName());
            mDescr.setText(route.getDescr());
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
                        RouteActivity.this.finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        mPoiManager.freeDatabases();
        super.onDestroy();
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
        route.setName(mName.getText().toString());
        route.setDescr(mDescr.getText().toString());
        mPoiManager.updateRoute(route, false);
        finish();

        Toast.makeText(RouteActivity.this, R.string.message_saved,
                Toast.LENGTH_SHORT).show();
    }

}
