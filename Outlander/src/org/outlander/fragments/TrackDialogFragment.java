package org.outlander.fragments;

import org.outlander.R;
import org.outlander.chart.ChartViewFactory;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiPoint;
import org.outlander.model.Track;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class TrackDialogFragment extends SherlockDialogFragment {

    EditText          mName, mDescr;
    private Track     track;
    private DBManager mPoiManager;

    static public TrackDialogFragment newInstance(final int trackid, final String name, final String descr, final int dialogTitle) {
        final TrackDialogFragment fragment = new TrackDialogFragment();

        final Bundle args = new Bundle();
        args.putInt("trackid", trackid);
        args.putString("name", name);
        args.putString("descr", descr);
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

        final View v = inflater.inflate(R.layout.track, container, false);

        if (mPoiManager == null) {
            mPoiManager = new DBManager(getActivity());
        }

        mName = (EditText) v.findViewById(R.id.Name);
        mDescr = (EditText) v.findViewById(R.id.Descr);

        Bundle extras = getArguments();
        if (extras == null) {
            extras = new Bundle();
        }

        final int id = extras.getInt("trackid", PoiPoint.EMPTY_ID());

        if (id < 0) {
            track = new Track();
            mName.setText(extras.getString("name"));
            mDescr.setText(extras.getString("descr"));
        }
        else {
            track = mPoiManager.getTrack(id);

            if (track == null) {
                dismiss();
            }

            final View graphView = ChartViewFactory.getTrackChartView(getActivity(), track);

            final LinearLayout layout = (LinearLayout) v.findViewById(R.id.chart);
            layout.addView(graphView);

            mName.setText(track.Name);
            mDescr.setText(track.Descr);
        }

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

    @Override
    public void onDestroy() {
        mPoiManager.freeDatabases();
        super.onDestroy();
    }

    // @Override
    // public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    // switch (keyCode) {
    // case KeyEvent.KEYCODE_BACK: {
    // doSaveAction();
    // return true;
    // }
    // }
    // return super.onKeyDown(keyCode, event);
    // }

    private void doSaveAction() {
        track.Name = mName.getText().toString();
        track.Descr = mDescr.getText().toString();

        mPoiManager.updateTrack(track, true);
        dismiss();

        Toast.makeText(getActivity(), R.string.message_saved, Toast.LENGTH_SHORT).show();
    }
}
