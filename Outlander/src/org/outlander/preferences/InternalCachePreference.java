package org.outlander.preferences;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.views.util.OpenStreetMapTileCache;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.outlander.R;
import org.outlander.utils.Ut;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InternalCachePreference extends Preference {

    private Button                                    btnClear;
    private final Context                             mCtx;
    private final File                                mDbFile;
    private final OpenStreetMapTileFilesystemProvider mFSTileProvider;
    private final ExecutorService                     mThreadExecutor = Executors.newSingleThreadExecutor();
    private ProgressDialog                            mProgressDialog;
    private final SimpleInvalidationHandler           mHandler;

    public InternalCachePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setWidgetLayoutResource(R.layout.preference_widget_btn_clear);

        mCtx = context;
        mFSTileProvider = new OpenStreetMapTileFilesystemProvider(context, 4 * 1024 * 1024, new OpenStreetMapTileCache(), null); // 4MB
                                                                                                                                 // FSCache
        mDbFile = context.getDatabasePath("osmaptilefscache_db");

        setSummary(String.format(mCtx.getString(R.string.pref_internalcache_summary),
                (int) (mDbFile.length() + mFSTileProvider.getCurrentFSCacheByteSize()) / 1024));

        mHandler = new SimpleInvalidationHandler();
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        btnClear = (Button) view.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new OnClickListener() {

            // @Override
            @Override
            public void onClick(final View v) {
                mProgressDialog = Ut.ShowWaitDialog(mCtx, 0);
                mThreadExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        mFSTileProvider.clearCurrentFSCache();
                        Message.obtain(mHandler).sendToTarget();
                        mProgressDialog.dismiss();

                    }
                });

            }
        });

    }

    private class SimpleInvalidationHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {

            InternalCachePreference.this.setSummary(String.format(mCtx.getString(R.string.pref_internalcache_summary),
                    (int) (mDbFile.length() + mFSTileProvider.getCurrentFSCacheByteSize()) / 1024));

        }
    }

}
