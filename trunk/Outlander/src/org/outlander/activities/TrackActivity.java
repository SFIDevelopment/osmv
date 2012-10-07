package org.outlander.activities;

import org.outlander.R;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiPoint;
import org.outlander.model.Track;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TrackActivity extends Activity {
    EditText          mName, mDescr;
    private Track     mTrack;
    private DBManager mPoiManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.track);

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
            mTrack = new Track();
            mName.setText(extras.getString("name"));
            mDescr.setText(extras.getString("descr"));
        } else {
            mTrack = mPoiManager.getTrack(id);

            if (mTrack == null) {
                finish();
            }

            mName.setText(mTrack.Name);
            mDescr.setText(mTrack.Descr);
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
                        TrackActivity.this.finish();
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
        mTrack.Name = mName.getText().toString();
        mTrack.Descr = mDescr.getText().toString();

        mPoiManager.updateTrack(mTrack, false);
        finish();

        Toast.makeText(TrackActivity.this, R.string.message_saved,
                Toast.LENGTH_SHORT).show();
    }

}
