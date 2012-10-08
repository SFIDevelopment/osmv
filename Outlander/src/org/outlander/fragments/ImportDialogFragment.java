package org.outlander.fragments;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openintents.filemanager.FileManagerActivity;
import org.outlander.R;
import org.outlander.io.ImportTask;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ImportDialogFragment extends SherlockDialogFragment {

    public static final String RESPONSE_NRPOIS   = "pois";
    public static final String RESPONSE_NRROUTES = "routes";
    public static final String RESPONSE_NRTRACKS = "tracks";

    EditText                   mFileName;
    Spinner                    mSpinnerRouteCat;
    Spinner                    mSpinnerPOICat;

    // private ProgressDialog dlgWait;
    protected ExecutorService  mThreadPool       = Executors.newFixedThreadPool(2);

    static public ImportDialogFragment newInstance(final int dialogTitle) {
        final ImportDialogFragment fragment = new ImportDialogFragment();

        final Bundle args = new Bundle();
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
        final View v = inflater.inflate(R.layout.importpoi, container, false);

        final SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);

        mFileName = (EditText) v.findViewById(R.id.FileName);
        mFileName.setText(settings.getString("IMPORT_POI_FILENAME", Ut.getTschekkoMapsImportDir(getActivity()).getAbsolutePath()));

        mSpinnerPOICat = (Spinner) v.findViewById(R.id.spinnerPOICategory);
        final Cursor c = CoreInfoHandler.getInstance().getDBManager(getActivity()).getGeoDatabase().getPoiUserCategoryListCursor();
        getActivity().startManagingCursor(c);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, c, new String[] { "name" },
                new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPOICat.setAdapter(adapter);

        mSpinnerRouteCat = (Spinner) v.findViewById(R.id.spinnerRouteCategory);
        final Cursor c2 = CoreInfoHandler.getInstance().getDBManager(getActivity()).getGeoDatabase().getRouteUserCategoryListCursor();
        getActivity().startManagingCursor(c2);
        final SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, c2, new String[] { "name" },
                new int[] { android.R.id.text1 });
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRouteCat.setAdapter(adapter2);

        ((Button) v.findViewById(R.id.SelectFileBtn)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                doSelectFile();
            }
        });
        ((Button) v.findViewById(R.id.ImportBtn)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                doImportPOI();
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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1234:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    // obtain the filename
                    String filename = Uri.decode(data.getDataString());
                    if (filename != null) {
                        // Get rid of URI prefix:
                        if (filename.startsWith("file://")) {
                            filename = filename.substring(7);
                        }

                        mFileName.setText(filename);
                    }

                }
                break;
        }
    }

    protected void doSelectFile() {
        final Intent intent = new Intent(getActivity(), FileManagerActivity.class);
        intent.setData(Uri.parse(mFileName.getText().toString()));
        startActivityForResult(intent, 1234);

    }

    private void doImportPOI() {
        final File file = new File(mFileName.getText().toString());

        if (!file.exists()) {
            Toast.makeText(getActivity(), R.string.message_fnf, Toast.LENGTH_LONG).show();
            return;
        }

        final int pOICategoryId = (int) mSpinnerPOICat.getSelectedItemId();
        final int routeCategoryId = (int) mSpinnerRouteCat.getSelectedItemId();

        final ImportTask importer = new ImportTask(getActivity(), mFileName.getText().toString(), pOICategoryId, routeCategoryId, CoreInfoHandler.getInstance()
                .getDBManager(getActivity()));
        importer.execute((Void) null);

    }
}
