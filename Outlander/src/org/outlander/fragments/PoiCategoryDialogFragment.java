package org.outlander.fragments;

import org.outlander.R;
import org.outlander.activities.PoiIconSetActivity;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiCategory;
import org.outlander.model.PoiPoint;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class PoiCategoryDialogFragment extends SherlockDialogFragment {

    EditText            mTitle;
    CheckBox            mHidden;
    ImageView           mIcon;
    SeekBar             mMinZoom;
    private PoiCategory mPoiCategory;
    private DBManager   mPoiManager;

    static public PoiCategoryDialogFragment newInstance(final int id, final String title, final int dialogTitle, final DBManager poiManager) {
        final PoiCategoryDialogFragment fragment = new PoiCategoryDialogFragment();

        final Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("title", title);
        args.putInt("dialogTitle", dialogTitle);

        fragment.setArguments(args);

        fragment.mPoiManager = poiManager;

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

        final View v = inflater.inflate(R.layout.poicategory, container, false);

        mTitle = (EditText) v.findViewById(R.id.Title);
        mHidden = (CheckBox) v.findViewById(R.id.Hidden);
        mIcon = (ImageView) v.findViewById(R.id.ImageIcon);
        mMinZoom = (SeekBar) v.findViewById(R.id.MinZoom);

        Bundle extras = getArguments();
        if (extras == null) {
            extras = new Bundle();
        }
        final int id = extras.getInt("id", PoiPoint.EMPTY_ID());

        if (id < 0) {
            mPoiCategory = new PoiCategory();
            mTitle.setText(extras.getString("title"));
            mHidden.setChecked(false);
            mIcon.setImageResource(mPoiCategory.IconId);
            mMinZoom.setProgress(14);
        }
        else {
            mPoiCategory = mPoiManager.getPoiCategory(id);

            if (mPoiCategory == null) {
                dismiss();
            }

            mTitle.setText(mPoiCategory.Title);
            mHidden.setChecked(mPoiCategory.Hidden);
            mIcon.setImageResource(mPoiCategory.IconId);
            mMinZoom.setProgress(mPoiCategory.MinZoom);
        }

        ((Button) v.findViewById(R.id.saveButton)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                doSaveAction();
            }
        });
        ((Button) v.findViewById(R.id.discardButton)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });

        mIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                doSelectIcon();
            }
        });
        return v;
    }

    protected void doSelectIcon() {
        startActivityForResult(new Intent(getActivity(), PoiIconSetActivity.class), R.id.ImageIcon);
    }

    // @Override
    // public void onDestroy() {
    // mPoiManager.FreeDatabases();
    // super.onDestroy();
    // }

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
        mPoiCategory.Title = mTitle.getText().toString();
        mPoiCategory.Hidden = mHidden.isChecked();
        mPoiCategory.MinZoom = mMinZoom.getProgress();

        mPoiManager.updatePoiCategory(mPoiCategory);
        dismiss();

        Toast.makeText(getActivity(), R.string.message_saved, Toast.LENGTH_SHORT).show();
    }
}
