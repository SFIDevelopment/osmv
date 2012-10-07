package org.outlander.activities;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.util.FileUtils;
import org.outlander.R;
import org.outlander.io.XML.GpxParser;
import org.outlander.io.XML.KmlPoiParser;
import org.outlander.io.XML.ParserResults;
import org.outlander.io.db.DBManager;
import org.outlander.utils.Ut;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class ImportPoiActivity extends Activity {

    public static final String RESPONSE_NRPOIS   = "pois";
    public static final String RESPONSE_NRROUTES = "routes";
    public static final String RESPONSE_NRTRACKS = "tracks";

    EditText                   mFileName;
    Spinner                    mSpinnerRouteCat;
    Spinner                    mSpinnerPOICat;
    private DBManager          mPoiManager;

    private ProgressDialog     dlgWait;
    protected ExecutorService  mThreadPool       = Executors
                                                         .newFixedThreadPool(2);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        this.setContentView(R.layout.importpoi);

        if (mPoiManager == null) {
            mPoiManager = new DBManager(this);
        }

        mFileName = (EditText) findViewById(R.id.FileName);
        mFileName.setText(settings.getString("IMPORT_POI_FILENAME", Ut
                .getTschekkoMapsImportDir(this).getAbsolutePath()));

        mSpinnerPOICat = (Spinner) findViewById(R.id.spinnerPOICategory);
        final Cursor c = mPoiManager.getGeoDatabase()
                .getPoiCategoryListCursor();
        startManagingCursor(c);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, c,
                new String[] { "name" }, new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPOICat.setAdapter(adapter);

        mSpinnerRouteCat = (Spinner) findViewById(R.id.spinnerRouteCategory);
        final Cursor c2 = mPoiManager.getGeoDatabase()
                .getRouteCategoryListCursor();
        startManagingCursor(c2);
        final SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, c2,
                new String[] { "name" }, new int[] { android.R.id.text1 });
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRouteCat.setAdapter(adapter2);

        ((Button) findViewById(R.id.SelectFileBtn))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        doSelectFile();
                    }
                });
        ((Button) findViewById(R.id.ImportBtn))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        doImportPOI();
                    }
                });
        ((Button) findViewById(R.id.discardButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        ImportPoiActivity.this.finish();
                    }
                });
    }

    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args) {
        switch (id) {
            case R.id.dialog_wait: {
                dlgWait = new ProgressDialog(this);
                dlgWait.setMessage("Please wait while loading...");
                dlgWait.setIndeterminate(true);
                dlgWait.setCancelable(false);
                return dlgWait;
            }
        }
        return null;
    }

    protected void doSelectFile() {
        final Intent intent = new Intent(this, FileManagerActivity.class);
        intent.setData(Uri.parse(mFileName.getText().toString()));
        startActivityForResult(intent, R.id.ImportBtn);

    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case R.id.ImportBtn:
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

    private void doImportPOI() {
        final File file = new File(mFileName.getText().toString());

        if (!file.exists()) {
            Toast.makeText(this, R.string.message_fnf, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        showDialog(R.id.dialog_wait);

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                final int pOICategoryId = (int) mSpinnerPOICat
                        .getSelectedItemId();
                final int routeCategoryId = (int) mSpinnerRouteCat
                        .getSelectedItemId();
                final File file = new File(mFileName.getText().toString());

                final SAXParserFactory fac = SAXParserFactory.newInstance();
                SAXParser parser = null;
                try {
                    parser = fac.newSAXParser();
                } catch (final ParserConfigurationException e) {
                    Ut.d(e.toString());
                    // e.printStackTrace();
                } catch (final SAXException e) {
                    Ut.d(e.toString());
                    // e.printStackTrace();
                }

                final ParserResults results = new ParserResults();

                if (parser != null) {
                    mPoiManager.beginTransaction();
                    Ut.dd("Start parsing file " + file.getName());

                    try {
                        if (FileUtils.getExtension(file.getName())
                                .equalsIgnoreCase(".kml")) {
                            parser.parse(file, new KmlPoiParser(mPoiManager,
                                    pOICategoryId));
                        } else if (FileUtils.getExtension(file.getName())
                                .equalsIgnoreCase(".gpx")) {

                            parser.parse(file, new GpxParser(mPoiManager,
                                    pOICategoryId, routeCategoryId, results,
                                    false));
                        }

                        mPoiManager.commitTransaction();
                    } catch (final SAXException e) {

                        Ut.d(e.toString());
                        // e.printStackTrace();
                        mPoiManager.rollbackTransaction();
                    } catch (final IOException e) {

                        Ut.d(e.toString());
                        // e.printStackTrace();
                        mPoiManager.rollbackTransaction();
                    } catch (final IllegalStateException e) {
                    } catch (final OutOfMemoryError e) {
                        Ut.w("OutOfMemoryError");
                        mPoiManager.rollbackTransaction();
                    }
                    Ut.dd("Pois commited");
                }

                dlgWait.dismiss();

                final Intent intent = (new Intent())
                        .putExtra(ImportPoiActivity.RESPONSE_NRPOIS,
                                results.pointCounter)
                        .putExtra(ImportPoiActivity.RESPONSE_NRROUTES,
                                results.routeCounter)
                        .putExtra(ImportPoiActivity.RESPONSE_NRTRACKS,
                                results.trackCounter);
                setResult(Activity.RESULT_OK, intent);

                ImportPoiActivity.this.finish();
            };
        });

    }

    @Override
    protected void onDestroy() {
        mPoiManager.freeDatabases();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putString("IMPORT_POI_FILENAME", mFileName.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        final SharedPreferences uiState = getPreferences(0);
        final SharedPreferences.Editor editor = uiState.edit();
        editor.putString("IMPORT_POI_FILENAME", mFileName.getText().toString());
        editor.commit();
        super.onPause();
    }

}
