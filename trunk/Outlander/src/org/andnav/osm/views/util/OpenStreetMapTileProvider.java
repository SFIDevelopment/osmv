// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.outlander.R;
import org.outlander.utils.Ut;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Nicolas Gramlich
 */
public class OpenStreetMapTileProvider implements OpenStreetMapConstants, OpenStreetMapViewConstants {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected Bitmap                           mLoadingMapTile;
    protected Context                          mCtx;
    protected OpenStreetMapTileCache           mTileCache;
    public OpenStreetMapTileFilesystemProvider mFSTileProvider;
    protected OpenStreetMapTileDownloader      mTileDownloader;
    private final Handler                      mLoadCallbackHandler = new LoadCallbackHandler();
    private final Handler                      mDownloadFinishedListenerHander;
    protected OpenStreetMapRendererInfo        mRendererInfo;

    // ===========================================================
    // Constructors
    // ===========================================================

    public OpenStreetMapTileProvider(final Context ctx, final Handler aDownloadFinishedListener, final OpenStreetMapRendererInfo aRendererInfo,
            final int iMapTileCacheSize) {
        mCtx = ctx;
        try {
            mLoadingMapTile = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.maptile_loading);
        }
        catch (final OutOfMemoryError e) {
            Ut.w("OutOfMemoryError");
            mLoadingMapTile = null;
            e.printStackTrace();
        }
        mTileCache = new OpenStreetMapTileCache(iMapTileCacheSize);
        mFSTileProvider = new OpenStreetMapTileFilesystemProvider(ctx, 4 * 1024 * 1024, mTileCache, aRendererInfo.TILE_SOURCE_TYPE == 0 ? aRendererInfo.ID
                : null); // 4MB
                         // FSCache
        mTileDownloader = new OpenStreetMapTileDownloader(ctx, mFSTileProvider);
        mDownloadFinishedListenerHander = aDownloadFinishedListener;
        mRendererInfo = aRendererInfo;

        switch (aRendererInfo.TILE_SOURCE_TYPE) {
            case 0:
                mTileDownloader.setCacheDatabase(aRendererInfo.CacheDatabaseName());
                break;
            case 3:
            case 4:
            case 5:
                mFSTileProvider.setUserMapFile(aRendererInfo.BASEURL, aRendererInfo.TILE_SOURCE_TYPE, aDownloadFinishedListener);
                aRendererInfo.ZOOM_MAXLEVEL = mFSTileProvider.getZoomMaxInCacheFile();
                aRendererInfo.ZOOM_MINLEVEL = mFSTileProvider.getZoomMinInCacheFile();
                break;
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public boolean setRender(final OpenStreetMapRendererInfo aRenderer, final Handler callback) {
        boolean ret = true;
        mRendererInfo = aRenderer;
        switch (aRenderer.TILE_SOURCE_TYPE) {
            case 0:
                mTileDownloader.setCacheDatabase(aRenderer.CacheDatabaseName());
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                ret = mFSTileProvider.setUserMapFile(aRenderer.BASEURL, aRenderer.TILE_SOURCE_TYPE, new SimpleInvalidationHandler());
                aRenderer.ZOOM_MAXLEVEL = mFSTileProvider.getZoomMaxInCacheFile();
                aRenderer.ZOOM_MINLEVEL = mFSTileProvider.getZoomMinInCacheFile();
                break;
        }
        return ret;
    }

    private class SimpleInvalidationHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case OpenStreetMapTileFilesystemProvider.INDEXIND_SUCCESS_ID:
                    mRendererInfo.ZOOM_MAXLEVEL = mFSTileProvider.getZoomMaxInCacheFile();
                    mRendererInfo.ZOOM_MINLEVEL = mFSTileProvider.getZoomMinInCacheFile();
                    break;
            }

            Message.obtain(mDownloadFinishedListenerHander, msg.what, msg.obj).sendToTarget();
        }
    }

    public Bitmap getMapTile(final String aTileURLString) {
        return getMapTile(aTileURLString, 0, 0, 0, 0);
    }

    public Bitmap getMapTile(final String aTileURLString, final int aTypeCash, final int x, final int y, final int z) {
        return getMapTile(aTileURLString, aTypeCash, mLoadingMapTile, x, y, z);
    }

    public Bitmap getMapTile(final String aTileURLString, final int storeType, final Bitmap aLoadingMapTile, final int x, final int y, final int z) {
        // Log.d(DEBUGTAG, "getMapTile "+aTileURLString);

        final Bitmap ret = mTileCache.getMapTile(aTileURLString);
        if (ret != null) {
            if (OpenStreetMapViewConstants.DEBUGMODE) {
                Log.i(OpenStreetMapConstants.DEBUGTAG, "MapTileCache succeded for: " + aTileURLString);
            }
        }
        else {
            if (OpenStreetMapViewConstants.DEBUGMODE) {
                Log.i(OpenStreetMapConstants.DEBUGTAG, "Cache failed, trying from FS.");
            }
            try {
                if (storeType == 5) {
                    mFSTileProvider.loadMapTileFromSQLite(aTileURLString, mLoadCallbackHandler, x, y, z);
                }
                else if (storeType == 4) {
                    mFSTileProvider.loadMapTileFromTAR(aTileURLString, mLoadCallbackHandler);
                }
                else if (storeType == 3) { // MapNav files
                    mFSTileProvider.loadMapTileFromMNM(aTileURLString, mLoadCallbackHandler, x, y, z);
                }
                else {
                    mFSTileProvider.loadMapTileToMemCacheAsync(aTileURLString, mLoadCallbackHandler);
                }

                // ret = aLoadingMapTile;
            }
            catch (final Exception e) {
                if (OpenStreetMapViewConstants.DEBUGMODE) {
                    Log.d(OpenStreetMapConstants.DEBUGTAG, "Error(" + e.getClass().getSimpleName() + ") loading MapTile from Filesystem: "
                            + OpenStreetMapTileNameFormatter.format(aTileURLString));
                }
            }
            if (ret == null) { /*
                                * FS did not contain the MapTile, we need to
                                * download it asynchronous.
                                */
                if (OpenStreetMapViewConstants.DEBUGMODE) {
                    Log.i(OpenStreetMapConstants.DEBUGTAG, "Requesting Maptile for download.");
                }
                // ret = aLoadingMapTile;

                mTileDownloader.requestMapTileAsync(aTileURLString, mLoadCallbackHandler, x, y, z);
            }
        }
        return ret;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private class LoadCallbackHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            final int what = msg.what;
            switch (what) {
                case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
                    mDownloadFinishedListenerHander.sendEmptyMessage(OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID);
                    if (OpenStreetMapViewConstants.DEBUGMODE) {
                        Log.i(OpenStreetMapConstants.DEBUGTAG, "MapTile download success.");
                    }
                    break;
                case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_FAIL_ID:
                    if (OpenStreetMapViewConstants.DEBUGMODE) {
                        Log.e(OpenStreetMapConstants.DEBUGTAG, "MapTile download error.");
                    }
                    break;

                case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
                    mDownloadFinishedListenerHander.sendEmptyMessage(OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID);
                    if (OpenStreetMapViewConstants.DEBUGMODE) {
                        Log.i(OpenStreetMapConstants.DEBUGTAG, "MapTile fs->cache success.");
                    }
                    break;
                case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_FAIL_ID:
                    if (OpenStreetMapViewConstants.DEBUGMODE) {
                        Log.e(OpenStreetMapConstants.DEBUGTAG, "MapTile download error.");
                    }
                    break;
            }
        }
    }

    public void preCacheTile(final String aTileURLString) {
        getMapTile(aTileURLString);
    }

    public void freeDatabases() {
        mFSTileProvider.freeDatabases();
    }

    public void commitCache() {
        mTileCache.Commit();
    }
}
