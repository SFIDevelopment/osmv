// Created by plusminus on 17:45:56 - 25.09.2008
package org.andnav.osm.views;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.controller.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileDownloader;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;
import org.andnav.osm.views.util.Util;
import org.andnav.osm.views.util.VersionedGestureDetector;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.outlander.R;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class OpenStreetMapView extends View implements OpenStreetMapConstants, OpenStreetMapViewConstants {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private final SimpleInvalidationHandler        mSimpleInvalidationHandler;
    protected int                                  mLatitudeE6                  = 0, mLongitudeE6 = 0;
    protected int                                  mZoomLevel                   = 0;
    private float                                  mBearing                     = 0;
    private boolean                                mActionMoveDetected;
    private boolean                                mStopMoveDetecting;

    protected OpenStreetMapRendererInfo            mRendererInfo;
    protected final OpenStreetMapTileProvider      mTileProvider;

    protected final GestureDetector                mGestureDetector             = new GestureDetector(new OpenStreetMapViewGestureDetectorListener());
    private final VersionedGestureDetector         mVGestureDetector            = VersionedGestureDetector.newInstance(new GestureCallback());

    protected final List<OpenStreetMapViewOverlay> mOverlays                    = new ArrayList<OpenStreetMapViewOverlay>();

    protected final Paint                          mPaint                       = new Paint();
    public int                                     mTouchDownX;
    public int                                     mTouchDownY;
    public int                                     mTouchMapOffsetX;
    public int                                     mTouchMapOffsetY;
    public double                                  mTouchScale;
    private double                                 mTouchDiagonalSize;

    private OpenStreetMapView                      mMiniMap, mMaxiMap;

    private OpenStreetMapViewController            mController;
    private int                                    mMiniMapOverriddenVisibility = OpenStreetMapConstants.NOT_SET;
    private int                                    mMiniMapZoomDiff             = OpenStreetMapConstants.NOT_SET;
    private Handler                                mMainActivityCallbackHandler;

    private OpenStreetMapViewProjection            currentProjection;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Standard Constructor for {@link OpenStreetMapView}.
     * 
     * @param context
     * @param aRendererInfo
     *            pass a {@link OpenStreetMapRendererInfo} you like.
     */
    public OpenStreetMapView(final Context context, final OpenStreetMapRendererInfo aRendererInfo) {
        super(context);
        mSimpleInvalidationHandler = new SimpleInvalidationHandler();
        mRendererInfo = aRendererInfo;
        mTileProvider = new OpenStreetMapTileProvider(context, mSimpleInvalidationHandler, aRendererInfo, OpenStreetMapViewConstants.CACHE_MAPTILECOUNT_DEFAULT);
        mPaint.setAntiAlias(true);
        mTouchScale = 1;

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public Handler getHandler() {
        return mSimpleInvalidationHandler;
    }

    public Handler getCallbackHandler() {
        return mMainActivityCallbackHandler;
    }

    /**
     * @param context
     * @param aRendererInfo
     *            pass a {@link OpenStreetMapRendererInfo} you like.
     * @param osmv
     *            another {@link OpenStreetMapView}, to share the TileProvider
     *            with.<br/>
     *            May significantly improve the render speed, when using the
     *            same {@link OpenStreetMapRendererInfo}.
     */
    // public OpenStreetMapView(final Context context, final
    // OpenStreetMapRendererInfo aRendererInfo,
    // final OpenStreetMapView aMapToShareTheTileProviderWith) {
    // super(context);
    // this.mRendererInfo = aRendererInfo;
    // this.mTileProvider = aMapToShareTheTileProviderWith.mTileProvider;
    // this.mPaint.setAntiAlias(true);
    // }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * This MapView takes control of the {@link OpenStreetMapView} passed as
     * parameter.<br />
     * I.e. it zoomes it to x levels less than itself and centers it the same
     * coords.<br />
     * Its pretty useful when the MiniMap uses the same TileProvider.
     * 
     * @see OpenStreetMapView.OpenStreetMapView(
     * @param aOsmvMinimap
     * @param aZoomDiff
     *            3 is a good Value. Pass {@link OpenStreetMapViewConstants}
     *            .NOT_SET to disable auto-zooming of the minimap.
     */
    public void setMiniMap(final OpenStreetMapView aOsmvMinimap, final int aZoomDiff) {
        mMiniMapZoomDiff = aZoomDiff;
        mMiniMap = aOsmvMinimap;
        aOsmvMinimap.setMaxiMap(this);

        // Synchronize the Views.
        this.setMapCenter(mLatitudeE6, mLongitudeE6);
        setZoomLevel(getZoomLevel());
    }

    public boolean hasMiniMap() {
        return mMiniMap != null;
    }

    /**
     * @return {@link View}.GONE or {@link View}.VISIBLE or {@link View}
     *         .INVISIBLE or {@link OpenStreetMapViewConstants}.NOT_SET
     */
    public int getOverrideMiniMapVisiblity() {
        return mMiniMapOverriddenVisibility;
    }

    /**
     * Use this method if you want to make the MiniMap visible i.e.: always or
     * never. Use {@link View}.GONE , {@link View}.VISIBLE, {@link View}
     * .INVISIBLE. Use {@link OpenStreetMapViewConstants}.NOT_SET to reset this
     * feature.
     * 
     * @param aVisiblity
     */
    public void setOverrideMiniMapVisiblity(final int aVisiblity) {
        switch (aVisiblity) {
            case View.GONE:
            case View.VISIBLE:
            case View.INVISIBLE:
                if (mMiniMap != null) {
                    mMiniMap.setVisibility(aVisiblity);
                }
            case NOT_SET:
                setZoomLevel(mZoomLevel);
                break;
            default:
                throw new IllegalArgumentException("See javadoc of this method !!!");
        }
        mMiniMapOverriddenVisibility = aVisiblity;
    }

    protected void setMaxiMap(final OpenStreetMapView aOsmvMaxiMap) {
        mMaxiMap = aOsmvMaxiMap;
    }

    public OpenStreetMapViewController getController() {
        if (mController != null) {
            return mController;
        }
        else {
            return mController = new OpenStreetMapViewController(this);
        }
    }

    /**
     * You can add/remove/reorder your Overlays using the List of
     * {@link OpenStreetMapViewOverlay}. The first (index 0) Overlay gets drawn
     * first, the one with the highest as the last one.
     */
    public List<OpenStreetMapViewOverlay> getOverlays() {
        return mOverlays;
    }

    public double getLatitudeSpan() {
        return getDrawnBoundingBoxE6().getLongitudeSpanE6() / 1E6;
    }

    public int getLatitudeSpanE6() {
        return getDrawnBoundingBoxE6().getLatitudeSpanE6();
    }

    public double getLongitudeSpan() {
        return getDrawnBoundingBoxE6().getLatitudeSpanE6() / 1E6;
    }

    public int getLongitudeSpanE6() {
        return getDrawnBoundingBoxE6().getLatitudeSpanE6();
    }

    public BoundingBoxE6 getDrawnBoundingBoxE6() {
        return getBoundingBox(getWidth(), getHeight());
    }

    public BoundingBoxE6 getVisibleBoundingBoxE6() {
        // final ViewParent parent = this.getParent();
        // if(parent instanceof RotateView){
        // final RotateView par = (RotateView)parent;
        // return getBoundingBox(par.getMeasuredWidth(),
        // par.getMeasuredHeight());
        // }else{
        return getBoundingBox(getWidth(), getHeight());
        // }
    }

    private BoundingBoxE6 getBoundingBox(final int pViewWidth, final int pViewHeight) {
        /*
         * Get the center MapTile which is above this.mLatitudeE6 and
         * this.mLongitudeE6 .
         */
        final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(mLatitudeE6, mLongitudeE6, mZoomLevel, null, mRendererInfo.PROJECTION);

        final BoundingBoxE6 tmp = Util.getBoundingBoxFromMapTile(centerMapTileCoords, mZoomLevel, mRendererInfo.PROJECTION);

        final int mLatitudeSpan_2 = (int) ((1.0f * tmp.getLatitudeSpanE6() * pViewHeight) / mRendererInfo.getTileSizePx(mZoomLevel)) / 2;
        final int mLongitudeSpan_2 = (int) ((1.0f * tmp.getLongitudeSpanE6() * pViewWidth) / mRendererInfo.getTileSizePx(mZoomLevel)) / 2;

        final int north = mLatitudeE6 + mLatitudeSpan_2;
        final int south = mLatitudeE6 - mLatitudeSpan_2;
        final int west = mLongitudeE6 - mLongitudeSpan_2;
        final int east = mLongitudeE6 + mLongitudeSpan_2;

        return new BoundingBoxE6(north, east, south, west);
    }

    /**
     * This class is only meant to be used during on call of onDraw(). Otherwise
     * it may produce strange results.
     * 
     * @return
     */
    public OpenStreetMapViewProjection getProjection() {
        if (currentProjection == null) {
            currentProjection = new OpenStreetMapViewProjection();
        }
        return currentProjection;
    }

    public void setMapCenter(final GeoPoint aCenter) {
        this.setMapCenter(aCenter.getLatitudeE6(), aCenter.getLongitudeE6());
    }

    public void setMapCenter(final double aLatitude, final double aLongitude) {
        this.setMapCenter((int) (aLatitude * 1E6), (int) (aLongitude * 1E6));
    }

    public void setMapCenter(final int aLatitudeE6, final int aLongitudeE6) {
        this.setMapCenter(aLatitudeE6, aLongitudeE6, true);
    }

    protected void setMapCenter(final int aLatitudeE6, final int aLongitudeE6, final boolean doPassFurther) {
        mLatitudeE6 = aLatitudeE6;
        mLongitudeE6 = aLongitudeE6;

        if (doPassFurther && (mMiniMap != null)) {
            mMiniMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);
        }
        else if (mMaxiMap != null) {
            mMaxiMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);
        }

        Message.obtain(mMainActivityCallbackHandler, R.id.map_center_changed).sendToTarget();

        this.postInvalidate();
    }

    public void setBearing(final float aBearing) {
        mBearing = aBearing;
    }

    public float getBearing() {
        return mBearing;
    }

    public boolean setRenderer(final OpenStreetMapRendererInfo aRenderer) {
        mRendererInfo = aRenderer;
        final boolean ret = mTileProvider.setRender(aRenderer, mSimpleInvalidationHandler);

        if (mZoomLevel > aRenderer.ZOOM_MAXLEVEL) {
            mZoomLevel = aRenderer.ZOOM_MAXLEVEL;
        }
        if (mZoomLevel < aRenderer.ZOOM_MINLEVEL) {
            mZoomLevel = aRenderer.ZOOM_MINLEVEL;
        }

        setZoomLevel(mZoomLevel); // Invalidates the map and zooms to
                                  // the maximum level of the
                                  // renderer.
        return ret;
    }

    public OpenStreetMapRendererInfo getRenderer() {
        return mRendererInfo;
    }

    /**
     * @param aZoomLevel
     *            between 0 (equator) and 18/19(closest), depending on the
     *            Renderer chosen.
     */
    public void setZoomLevel(final int aZoomLevel) {
        mZoomLevel = Math.max(mRendererInfo.ZOOM_MINLEVEL, Math.min(mRendererInfo.ZOOM_MAXLEVEL, aZoomLevel));

        if (mMiniMap != null) {
            if (mZoomLevel < mMiniMapZoomDiff) {
                if (mMiniMapOverriddenVisibility == OpenStreetMapConstants.NOT_SET) {
                    mMiniMap.setVisibility(View.INVISIBLE);
                }
            }
            else {
                if ((mMiniMapOverriddenVisibility == OpenStreetMapConstants.NOT_SET) && (mMiniMap.getVisibility() != View.VISIBLE)) {
                    mMiniMap.setVisibility(View.VISIBLE);
                }
                if (mMiniMapZoomDiff != OpenStreetMapConstants.NOT_SET) {
                    mMiniMap.setZoomLevel(mZoomLevel - mMiniMapZoomDiff);
                }
            }
        }
        this.postInvalidate();
    }

    /**
     * Zooms in if possible.
     */
    public void zoomIn() {

        // final String nextBelowMaptileUrlString =
        // this.mRendererInfo.getTileURLString(Util
        // .getMapTileFromCoordinates(this.mLatitudeE6, this.mLongitudeE6,
        // this.mZoomLevel + 1,
        // null), this.mZoomLevel + 1);
        // this.mTileProvider.preCacheTile(nextBelowMaptileUrlString);

        setZoomLevel(mZoomLevel + 1);
    }

    /**
     * Zooms out if possible.
     */
    public void zoomOut() {
        setZoomLevel(mZoomLevel - 1);
    }

    /**
     * @return the current ZoomLevel between 0 (equator) and 18/19(closest),
     *         depending on the Renderer chosen.
     */
    public int getZoomLevel() {
        return mZoomLevel;
    }

    public GeoPoint getMapCenter() {
        return new GeoPoint(mLatitudeE6, mLongitudeE6);
    }

    public int getMapCenterLatitudeE6() {
        return mLatitudeE6;
    }

    public int getMapCenterLongitudeE6() {
        return mLongitudeE6;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    public void onLongPress(final MotionEvent e) {
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            if (osmvo.onLongPress(e, this)) {
                mActionMoveDetected = true;
                return;
            }
        }
    }

    public boolean onSingleTapUp(final MotionEvent e) {
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            if (osmvo.onSingleTapUp(e, this)) {

                return true;
            }
        }
        Message.obtain(mMainActivityCallbackHandler, R.id.tap_on_map).sendToTarget();

        return false;
    }

    public boolean onDoubleTap(final MotionEvent e) {
        if (mBearing != 0) {
            mBearing = 0;
            Message.obtain(mMainActivityCallbackHandler, R.id.user_moved_map).sendToTarget();
        }
        else {
            final GeoPoint newCenter = getProjection().fromPixels(e.getX(), e.getY());
            this.setMapCenter(newCenter);

            zoomIn();
            Message.obtain(mMainActivityCallbackHandler, R.id.set_title).sendToTarget();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        // Log.e(DEBUGTAG, "onKeyDown keyCode="+keyCode);
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            if (osmvo.onKeyDown(keyCode, event, this)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            if (osmvo.onKeyUp(keyCode, event, this)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event) {
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            if (osmvo.onTrackballEvent(event, this)) {
                return true;
            }
        }
        return super.onTrackballEvent(event);
    }

    public boolean canCreateContextMenu() {
        return !mActionMoveDetected;
    }

    public GeoPoint getTouchDownPoint() {
        return getProjection().fromPixels(mTouchDownX, mTouchDownY, mBearing);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            if (osmvo.onTouchEvent(event, this)) {
                return true;
            }
        }

        mGestureDetector.onTouchEvent(event);
        mVGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    // private Bitmap bitmap;
    // private Canvas canvas;
    //
    // protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    // if (bitmap != null) {
    // bitmap .recycle();
    // }
    // canvas= new Canvas();
    // bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    // canvas.setBitmap(bitmap);
    // }
    //
    // public void destroy() {
    // if (bitmap != null) {
    // bitmap.recycle();
    // }
    // }

    @Override
    public void onDraw(final Canvas c) {

        final long startMs = System.currentTimeMillis();

        /*
         * Do some calculations and drag attributes to local variables to save
         * some performance.
         */
        final int zoomLevel = mZoomLevel;
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();
        final int tileSizePxNotScale = mRendererInfo.getTileSizePx(mZoomLevel);
        final int tileSizePx = (int) (tileSizePxNotScale * mTouchScale);

        c.save();
        final float aRotateToAngle = 360 - mBearing;
        c.rotate(aRotateToAngle, viewWidth / 2, viewHeight / 2);

        // c.drawRGB(255, 255, 255);

        final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(mLatitudeE6, mLongitudeE6, zoomLevel, null, mRendererInfo.PROJECTION);

        /*
         * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
         * the center MapTile. So in the end we can determine which MapTiles we
         * additionally need next to the centerMapTile.
         */
        final Point upperLeftCornerOfCenterMapTileNotScale = getUpperLeftCornerOfCenterMapTileInScreen(centerMapTileCoords, tileSizePxNotScale, null);

        final int centerMapTileScreenLeftNotScale = upperLeftCornerOfCenterMapTileNotScale.x;
        final int centerMapTileScreenTopNotScale = upperLeftCornerOfCenterMapTileNotScale.y;
        final int centerMapTileScreenRightNotScale = centerMapTileScreenLeftNotScale + tileSizePxNotScale;
        final int centerMapTileScreenBottomNotScale = centerMapTileScreenTopNotScale + tileSizePxNotScale;

        final Point upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(centerMapTileCoords, tileSizePx, null);
        final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile.x;
        final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile.y;

        /*
         * Calculate the amount of tiles needed for each side around the center
         * one.
         */

        final int iDelta = (mBearing > 0) ? 1 : 0;
        final int additionalTilesNeededToLeftOfCenter = iDelta + (int) Math.ceil((float) centerMapTileScreenLeftNotScale / tileSizePxNotScale); // i.e.

        final int additionalTilesNeededToRightOfCenter = iDelta + (int) Math.ceil((float) (viewWidth - centerMapTileScreenRightNotScale) / tileSizePxNotScale);
        final int additionalTilesNeededToTopOfCenter = iDelta + (int) Math.ceil((float) centerMapTileScreenTopNotScale / tileSizePxNotScale); // i.e.

        final int additionalTilesNeededToBottomOfCenter = iDelta
                + (int) Math.ceil((float) (viewHeight - centerMapTileScreenBottomNotScale) / tileSizePxNotScale);

        final int mapTileUpperBound = mRendererInfo.getTileUpperBound(zoomLevel);

        final int[] mapTileCoords = new int[] { centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX],
                centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] };

        /* Draw all the MapTiles (from the upper left to the lower right). */
        for (int y = -additionalTilesNeededToTopOfCenter; y <= additionalTilesNeededToBottomOfCenter; y++) {
            for (int x = -additionalTilesNeededToLeftOfCenter; x <= additionalTilesNeededToRightOfCenter; x++) {
                /*
                 * Add/substract the difference of the tile-position to the one
                 * of the center.
                 */
                mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] = GeoMathUtil.mod(
                        centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] + y, mapTileUpperBound);
                mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] = GeoMathUtil.mod(
                        centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] + x, mapTileUpperBound);
                /* Construct a URLString, which represents the MapTile. */
                final String tileURLString = mRendererInfo.getTileURLString(mapTileCoords, zoomLevel);
                Ut.d("onDraw: " + tileURLString);

                /* Draw the MapTile 'i tileSizePx' above of the centerMapTile */
                final Bitmap currentMapTile = mTileProvider.getMapTile(tileURLString, mRendererInfo.TILE_SOURCE_TYPE,
                        mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX], mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX],
                        zoomLevel);
                if (currentMapTile != null) {
                    final int tileLeft = mTouchMapOffsetX + centerMapTileScreenLeft + (x * tileSizePx);
                    final int tileTop = mTouchMapOffsetY + centerMapTileScreenTop + (y * tileSizePx);

                    final Rect r = new Rect(tileLeft, tileTop, tileLeft + tileSizePx, tileTop + tileSizePx);

                    if (!currentMapTile.isRecycled()) {
                        c.drawBitmap(currentMapTile, null, r, mPaint);
                    }

                    if (OpenStreetMapViewConstants.DEBUGMODE) {
                        c.drawLine(tileLeft, tileTop, tileLeft + tileSizePx, tileTop, mPaint);
                        c.drawLine(tileLeft, tileTop, tileLeft, tileTop + tileSizePx, mPaint);
                        c.drawText(
                                "y x = "
                                        + mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
                                        + " "
                                        + mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
                                        + " zoom "
                                        + zoomLevel
                                        + " "
                                        + mRendererInfo.getQRTS(mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX],
                                                mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX], zoomLevel), tileLeft + 5, tileTop + 15,
                                mPaint);
                    }
                }
            }
        }

        mTileProvider.commitCache();

        /* Draw all Overlays. */
        currentProjection = null;
        for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
            osmvo.onManagedDraw(c, this);
        }

        mPaint.setStyle(Style.STROKE);
        if (mMaxiMap != null) {
            c.drawRect(0, 0, viewWidth - 1, viewHeight - 1, mPaint);
        }

        c.restore();

        // c.drawLine(viewWidth/2, 0, viewWidth/2, viewHeight, this.mPaint);
        // c.drawLine(0, viewHeight/2, viewWidth, viewHeight/2, this.mPaint);
        // c.drawCircle(viewWidth/2, viewHeight/2, 100, this.mPaint);
        // c.drawLine(viewWidth/2-100, viewHeight/2-100, viewWidth/2+100,
        // viewHeight/2+100, this.mPaint);
        // c.drawLine(viewWidth/2+100, viewHeight/2-100, viewWidth/2-100,
        // viewHeight/2+100, this.mPaint);

        if (OpenStreetMapViewConstants.DEBUGMODE) {
            final long endMs = System.currentTimeMillis();
            Log.i(OpenStreetMapConstants.DEBUGTAG, "Rendering overall: " + (endMs - startMs) + "ms");
        }
    }

    // @Override
    // public void onDraw( Canvas c) {
    //
    // final long startMs = System.currentTimeMillis();
    //
    // /*
    // * Do some calculations and drag attributes to local variables to save
    // * some performance.
    // */
    // final int zoomLevel = mZoomLevel;
    // final int viewWidth = getWidth();
    // final int viewHeight = getHeight();
    // final int tileSizePxNotScale = mRendererInfo.getTileSizePx(mZoomLevel);
    // final int tileSizePx = (int) (tileSizePxNotScale * mTouchScale);
    //
    // canvas.save();
    // final float aRotateToAngle = 360 - mBearing;
    // canvas.rotate(aRotateToAngle, viewWidth / 2, viewHeight / 2);
    //
    // // c.drawRGB(255, 255, 255);
    //
    // final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(
    // mLatitudeE6, mLongitudeE6, zoomLevel, null,
    // mRendererInfo.PROJECTION);
    //
    // /*
    // * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
    // * the center MapTile. So in the end we can determine which MapTiles we
    // * additionally need next to the centerMapTile.
    // */
    // final Point upperLeftCornerOfCenterMapTileNotScale =
    // getUpperLeftCornerOfCenterMapTileInScreen(
    // centerMapTileCoords, tileSizePxNotScale, null);
    //
    // final int centerMapTileScreenLeftNotScale =
    // upperLeftCornerOfCenterMapTileNotScale.x;
    // final int centerMapTileScreenTopNotScale =
    // upperLeftCornerOfCenterMapTileNotScale.y;
    // final int centerMapTileScreenRightNotScale =
    // centerMapTileScreenLeftNotScale
    // + tileSizePxNotScale;
    // final int centerMapTileScreenBottomNotScale =
    // centerMapTileScreenTopNotScale
    // + tileSizePxNotScale;
    //
    // final Point upperLeftCornerOfCenterMapTile =
    // getUpperLeftCornerOfCenterMapTileInScreen(
    // centerMapTileCoords, tileSizePx, null);
    // final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile.x;
    // final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile.y;
    //
    // /*
    // * Calculate the amount of tiles needed for each side around the center
    // * one.
    // */
    //
    // final int iDelta = (mBearing > 0) ? 1 : 0;
    // final int additionalTilesNeededToLeftOfCenter = iDelta
    // + (int) Math.ceil((float) centerMapTileScreenLeftNotScale
    // / tileSizePxNotScale); // i.e.
    // final int additionalTilesNeededToRightOfCenter = iDelta
    // + (int) Math
    // .ceil((float) (viewWidth - centerMapTileScreenRightNotScale)
    // / tileSizePxNotScale);
    // final int additionalTilesNeededToTopOfCenter = iDelta
    // + (int) Math.ceil((float) centerMapTileScreenTopNotScale
    // / tileSizePxNotScale); // i.e.
    // final int additionalTilesNeededToBottomOfCenter = iDelta
    // + (int) Math
    // .ceil((float) (viewHeight - centerMapTileScreenBottomNotScale)
    // / tileSizePxNotScale);
    //
    // final int mapTileUpperBound = mRendererInfo
    // .getTileUpperBound(zoomLevel);
    // final int[] mapTileCoords = new int[] {
    // centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX],
    // centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
    // };
    //
    // /* Draw all the MapTiles (from the upper left to the lower right). */
    // for (int y = -additionalTilesNeededToTopOfCenter; y <=
    // additionalTilesNeededToBottomOfCenter; y++) {
    // for (int x = -additionalTilesNeededToLeftOfCenter; x <=
    // additionalTilesNeededToRightOfCenter; x++) {
    // /*
    // * Add/substract the difference of the tile-position to the one
    // * of the center.
    // */
    // mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] =
    // GeoMathUtil
    // .mod(centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
    // + y, mapTileUpperBound);
    // mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] =
    // GeoMathUtil
    // .mod(centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
    // + x, mapTileUpperBound);
    // /* Construct a URLString, which represents the MapTile. */
    // final String tileURLString = mRendererInfo.getTileURLString(
    // mapTileCoords, zoomLevel);
    // Ut.dd("onDraw: " + tileURLString);
    //
    // /* Draw the MapTile 'i tileSizePx' above of the centerMapTile */
    // final Bitmap currentMapTile = mTileProvider
    // .getMapTile(
    // tileURLString,
    // mRendererInfo.TILE_SOURCE_TYPE,
    // mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX],
    // mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX],
    // zoomLevel);
    // if (currentMapTile != null) {
    // final int tileLeft = mTouchMapOffsetX
    // + centerMapTileScreenLeft + (x * tileSizePx);
    // final int tileTop = mTouchMapOffsetY
    // + centerMapTileScreenTop + (y * tileSizePx);
    // final Rect r = new Rect(tileLeft, tileTop, tileLeft
    // + tileSizePx, tileTop + tileSizePx);
    // if (!currentMapTile.isRecycled()) {
    // canvas.drawBitmap(currentMapTile, null, r, mPaint);
    // }
    //
    // if (OpenStreetMapViewConstants.DEBUGMODE) {
    // canvas.drawLine(tileLeft, tileTop, tileLeft + tileSizePx,
    // tileTop, mPaint);
    // canvas.drawLine(tileLeft, tileTop, tileLeft, tileTop
    // + tileSizePx, mPaint);
    // canvas.drawText(
    // "y x = "
    // + mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
    // + " "
    // + mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
    // + " zoom "
    // + zoomLevel
    // + " "
    // + mRendererInfo
    // .getQRTS(
    // mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX],
    // mapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX],
    // zoomLevel),
    // tileLeft + 5, tileTop + 15, mPaint);
    // }
    // }
    // }
    //
    //
    // }
    //
    // mTileProvider.commitCash();
    //
    // /* Draw all Overlays. */
    // currentProjection = null;
    // for (final OpenStreetMapViewOverlay osmvo : mOverlays) {
    // osmvo.onManagedDraw(canvas, this);
    // }
    //
    // mPaint.setStyle(Style.STROKE);
    // if (mMaxiMap != null) {
    // canvas.drawRect(0, 0, viewWidth - 1, viewHeight - 1, mPaint);
    // }
    //
    // canvas.restore();
    //
    // c.drawBitmap(bitmap,
    // new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()),
    // new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()), null);
    //
    //
    // // c.drawLine(viewWidth/2, 0, viewWidth/2, viewHeight, this.mPaint);
    // // c.drawLine(0, viewHeight/2, viewWidth, viewHeight/2, this.mPaint);
    // // c.drawCircle(viewWidth/2, viewHeight/2, 100, this.mPaint);
    // // c.drawLine(viewWidth/2-100, viewHeight/2-100, viewWidth/2+100,
    // // viewHeight/2+100, this.mPaint);
    // // c.drawLine(viewWidth/2+100, viewHeight/2-100, viewWidth/2-100,
    // // viewHeight/2+100, this.mPaint);
    //
    // if (OpenStreetMapViewConstants.DEBUGMODE) {
    // final long endMs = System.currentTimeMillis();
    // Log.i(OpenStreetMapConstants.DEBUGTAG, "Rendering overall: "
    // + (endMs - startMs) + "ms");
    // }
    // }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * @param centerMapTileCoords
     * @param tileSizePx
     * @param reuse
     *            just pass null if you do not have a Point to be 'recycled'.
     */
    private Point getUpperLeftCornerOfCenterMapTileInScreen(final int[] centerMapTileCoords, final int tileSizePx, final Point reuse) {
        final Point out = (reuse != null) ? reuse : new Point();

        final int viewWidth = getWidth();
        final int viewWidth_2 = viewWidth / 2;
        final int viewHeight = getHeight();
        final int viewHeight_2 = viewHeight / 2;

        /*
         * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
         * the center MapTile. So in the end we can determine which MapTiles we
         * additionally need next to the centerMapTile.
         */
        final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(centerMapTileCoords, mZoomLevel, mRendererInfo.PROJECTION);
        final float[] relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(mLatitudeE6, mLongitudeE6, null);

        final int centerMapTileScreenLeft = viewWidth_2
                - (int) (0.5f + (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] * tileSizePx));
        final int centerMapTileScreenTop = viewHeight_2
                - (int) (0.5f + (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] * tileSizePx));

        out.set(centerMapTileScreenLeft, centerMapTileScreenTop);
        return out;
    }

    public void setCallbackHandler(final Handler callbackHandler) {
        mMainActivityCallbackHandler = callbackHandler;
    }

    public void freeDatabases() {
        mTileProvider.freeDatabases();
    }

    /**
     * This class may return valid results until the underlying
     * {@link OpenStreetMapView} gets modified in any way (i.e. new center).
     * 
     * @author Nicolas Gramlich
     */
    public class OpenStreetMapViewProjection {

        final int           viewWidth;
        final int           viewHeight;
        final BoundingBoxE6 bb;
        final int           zoomLevel;
        final int           tileSizePx;
        final int[]         centerMapTileCoords;
        final Point         upperLeftCornerOfCenterMapTile;

        public OpenStreetMapViewProjection() {
            viewWidth = getWidth();
            viewHeight = getHeight();

            /*
             * Do some calculations and drag attributes to local variables to
             * save some performance.
             */
            zoomLevel = mZoomLevel; // LATER Draw to
                                    // attributes and so
                                    // make it only
                                    // 'valid' for a
                                    // short time.
            tileSizePx = (int) (mRendererInfo.getTileSizePx(mZoomLevel) * mTouchScale);

            /*
             * Get the center MapTile which is above this.mLatitudeE6 and
             * this.mLongitudeE6 .
             */
            centerMapTileCoords = Util.getMapTileFromCoordinates(mLatitudeE6, mLongitudeE6, zoomLevel, null, mRendererInfo.PROJECTION);
            upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(centerMapTileCoords, tileSizePx, null);

            bb = getDrawnBoundingBoxE6();
        }

        /**
         * Converts x/y ScreenCoordinates to the underlying GeoPoint.
         * 
         * @param x
         * @param y
         * @return GeoPoint under x/y.
         */
        public GeoPoint fromPixels(float x, float y) {
            /* Subtract the offset caused by touch. */
            // Log.d(DEBUGTAG,
            // "x = "+x+" mTouchMapOffsetX = "+OpenStreetMapView.this.mTouchMapOffsetX+"   ");

            x -= mTouchMapOffsetX;
            y -= mTouchMapOffsetY;

            // int xx =
            // centerMapTileCoords[0]*tileSizePx+(int)x-upperLeftCornerOfCenterMapTile.x;
            // int asd = Util.x2lon(xx, zoomLevel, tileSizePx);
            final GeoPoint p = bb.getGeoPointOfRelativePositionWithLinearInterpolation(x / viewWidth, y / viewHeight);

            // Log.d(DEBUGTAG,
            // "lon "+p.getLongitudeE6()+" "+xx+" "+asd+" OffsetX = "+OpenStreetMapView.this.mTouchMapOffsetX);
            // Log.d(DEBUGTAG,
            // "    "+centerMapTileCoords[0]+" "+tileSizePx+" "+x+" "+upperLeftCornerOfCenterMapTile.x);
            // p.setLongitudeE6(asd);

            // for(int i =0; i<=tileSizePx*(1<<zoomLevel); i++){int Q =
            // Util.x2lon(i, zoomLevel, tileSizePx);Log.d(DEBUGTAG,
            // "lon "+i+" "+Q);}

            return p;
        }

        public GeoPoint fromPixels(final float x, final float y, final double bearing) {
            final int x1 = (int) (x - (getWidth() / 2));
            final int y1 = (int) (y - (getHeight() / 2));
            final double hypot = Math.hypot(x1, y1);
            final double angle = -1 * Math.signum(y1) * Math.toDegrees(Math.acos(x1 / hypot));
            final double angle2 = angle - bearing;
            final int x2 = (int) (Math.cos(Math.toRadians(angle2)) * hypot);
            final int y2 = (int) (Math.sin(Math.toRadians(angle2 - 180)) * hypot);

            return fromPixels(((getWidth() / 2) + x2), ((getHeight() / 2) + y2));
        }

        private static final int EQUATORCIRCUMFENCE = 40075676; // 40075004;

        public float metersToEquatorPixels(final float aMeters) {
            return (aMeters / OpenStreetMapViewProjection.EQUATORCIRCUMFENCE) * mRendererInfo.getTileSizePx(mZoomLevel);
        }

        /**
         * Converts a GeoPoint to its ScreenCoordinates. <br/>
         * <br/>
         * <b>CAUTION</b> ! Conversion currently has a large error on
         * <code>zoomLevels <= 7</code>.<br/>
         * The Error on ZoomLevels higher than 7, the error is below
         * <code>1px</code>.<br/>
         * LATER: Add a linear interpolation to minimize this error.
         * 
         * <PRE>
         * Zoom     Error(m)    Error(px)
         * 11   6m  1/12px
         * 10   24m     1/6px
         * 8    384m    1/2px
         * 6    6144m   3px
         * 4    98304m  10px
         * </PRE>
         * 
         * @param in
         *            the GeoPoint you want the onScreenCoordinates of.
         * @param reuse
         *            just pass null if you do not have a Point to be
         *            'recycled'.
         * @return the Point containing the approximated ScreenCoordinates of
         *         the GeoPoint passed.
         */
        public Point toPixels(final GeoPoint in, final Point reuse) {
            return toPixels(in, reuse, true);
        }

        public Point toPixels(final GeoPoint in, final double bearing, final Point reuse) {
            final Point point = toPixels(in, reuse, true);
            final Point out = (reuse != null) ? reuse : new Point();

            final int x1 = point.x - (getWidth() / 2);
            final int y1 = point.y - (getHeight() / 2);
            final double hypot = Math.hypot(x1, y1);
            final double angle = -1 * Math.signum(y1) * Math.toDegrees(Math.acos(x1 / hypot));
            final double angle2 = angle + bearing;
            final int x2 = (int) (Math.cos(Math.toRadians(angle2)) * hypot);
            final int y2 = (int) (Math.sin(Math.toRadians(angle2 - 180)) * hypot);

            out.set((getWidth() / 2) + x2, (getHeight() / 2) + y2);
            return out;
        }

        protected Point toPixels(final GeoPoint in, final Point reuse, final boolean doGudermann) {

            final Point out = (reuse != null) ? reuse : new Point();

            final int[] underGeopointTileCoords = Util.getMapTileFromCoordinates(in.getLatitudeE6(), in.getLongitudeE6(), zoomLevel, null,
                    mRendererInfo.PROJECTION);

            /*
             * Calculate the Latitude/Longitude on the left-upper ScreenCoords
             * of the MapTile.
             */
            final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(underGeopointTileCoords, zoomLevel, mRendererInfo.PROJECTION);

            final float[] relativePositionInCenterMapTile;
            if (doGudermann && (zoomLevel < 7)) {
                relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(in.getLatitudeE6(),
                        in.getLongitudeE6(), null);
            }
            else {
                relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(in.getLatitudeE6(), in.getLongitudeE6(),
                        null);
            }

            final int tileDiffX = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
                    - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX];
            final int tileDiffY = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
                    - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX];
            final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x - (tileSizePx * tileDiffX);
            final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y - (tileSizePx * tileDiffY);

            final int x = underGeopointTileScreenLeft
                    + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] * tileSizePx);
            final int y = underGeopointTileScreenTop + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] * tileSizePx);

            /* Add up the offset caused by touch. */
            out.set(x + mTouchMapOffsetX, y + mTouchMapOffsetY);
            return out;
        }

        public Point toPixels2(final GeoPoint in) {

            final Point out = new Point();
            final boolean doGudermann = true;

            final int[] underGeopointTileCoords = Util.getMapTileFromCoordinates(in.getLatitudeE6(), in.getLongitudeE6(), zoomLevel, null,
                    mRendererInfo.PROJECTION);

            /*
             * Calculate the Latitude/Longitude on the left-upper ScreenCoords
             * of the MapTile.
             */
            final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(underGeopointTileCoords, zoomLevel, mRendererInfo.PROJECTION);

            final float[] relativePositionInCenterMapTile;
            if (doGudermann && (zoomLevel < 7)) {
                relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(in.getLatitudeE6(),
                        in.getLongitudeE6(), null);
            }
            else {
                relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(in.getLatitudeE6(), in.getLongitudeE6(),
                        null);
            }

            final int tileDiffX = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
                    - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX];
            final int tileDiffY = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
                    - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX];
            final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x - (tileSizePx * tileDiffX);
            final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y - (tileSizePx * tileDiffY);

            final int x = underGeopointTileScreenLeft
                    + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] * tileSizePx);
            final int y = underGeopointTileScreenTop + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] * tileSizePx);

            /* Add up the offset caused by touch. */
            out.set(x, y);
            return out;
        }

        public Path toPixels(final List<GeoPoint> in, final Path reuse) {
            return toPixels(in, reuse, true);
        }

        protected Path toPixels(final List<GeoPoint> in, final Path reuse, final boolean doGudermann) throws IllegalArgumentException {
            if (in.size() < 2) {
                throw new IllegalArgumentException("List of GeoPoints needs to be at least 2.");
            }

            final Path out = (reuse != null) ? reuse : new Path();

            int i = 0;
            for (final GeoPoint gp : in) {
                i++;
                final int[] underGeopointTileCoords = Util.getMapTileFromCoordinates(gp.getLatitudeE6(), gp.getLongitudeE6(), zoomLevel, null,
                        mRendererInfo.PROJECTION);

                /*
                 * Calculate the Latitude/Longitude on the left-upper
                 * ScreenCoords of the MapTile.
                 */
                final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(underGeopointTileCoords, zoomLevel, mRendererInfo.PROJECTION);

                final float[] relativePositionInCenterMapTile;
                if (doGudermann && (zoomLevel < 7)) {
                    relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(gp.getLatitudeE6(),
                            gp.getLongitudeE6(), null);
                }
                else {
                    relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(gp.getLatitudeE6(),
                            gp.getLongitudeE6(), null);
                }

                final int tileDiffX = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
                        - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX];
                final int tileDiffY = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
                        - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX];
                final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x - (tileSizePx * tileDiffX);
                final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y - (tileSizePx * tileDiffY);

                final int x = underGeopointTileScreenLeft
                        + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] * tileSizePx);
                final int y = underGeopointTileScreenTop
                        + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] * tileSizePx);

                /* Add up the offset caused by touch. */
                if (i == 0) {
                    out.moveTo(x + mTouchMapOffsetX, y + mTouchMapOffsetY);
                }
                else {
                    out.lineTo(x + mTouchMapOffsetX, y + mTouchMapOffsetY);
                }
            }

            return out;
        }

        public Path toPixelsTrackPoints(final List<? extends GeoPoint> in, final Point baseCoord, final GeoPoint baseLocation, final List<Point> screenCoords)
                throws IllegalArgumentException {
            if (in.size() < 2) {
                return null;
                // throw new
                // IllegalArgumentException("List of GeoPoints needs to be at least 2.");
            }

            final Path out = new Path();
            final boolean doGudermann = true;

            int i = 0;
            int lastX = 0, lastY = 0;
            for (final GeoPoint tp : in) {
                final int[] underGeopointTileCoords = Util.getMapTileFromCoordinates(tp.getLatitudeE6(), tp.getLongitudeE6(), zoomLevel, null,
                        mRendererInfo.PROJECTION);

                /*
                 * Calculate the Latitude/Longitude on the left-upper
                 * ScreenCoords of the MapTile.
                 */
                final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(underGeopointTileCoords, zoomLevel, mRendererInfo.PROJECTION);

                final float[] relativePositionInCenterMapTile;
                if (doGudermann && (zoomLevel < 7)) {
                    relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(tp.getLatitudeE6(),
                            tp.getLongitudeE6(), null);
                }
                else {
                    relativePositionInCenterMapTile = bb.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(tp.getLatitudeE6(),
                            tp.getLongitudeE6(), null);
                }

                final int tileDiffX = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX]
                        - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX];
                final int tileDiffY = centerMapTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX]
                        - underGeopointTileCoords[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX];
                final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x - (tileSizePx * tileDiffX);
                final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y - (tileSizePx * tileDiffY);

                final int x = underGeopointTileScreenLeft
                        + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] * tileSizePx);
                final int y = underGeopointTileScreenTop
                        + (int) (relativePositionInCenterMapTile[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] * tileSizePx);

                /* Add up the offset caused by touch. */
                if (i == 0) {
                    out.setLastPoint(x, y);
                    lastX = x;
                    lastY = y;
                    baseCoord.x = x;
                    baseCoord.y = y;
                    baseLocation.setCoordsE6(tp.getLatitudeE6(), tp.getLongitudeE6());
                    i++;
                }
                else {
                    if ((Math.abs(lastX - x) > 5) || (Math.abs(lastY - y) > 5)) {
                        out.lineTo(x, y);
                        lastX = x;
                        lastY = y;
                        i++;
                    }
                }
                if (screenCoords != null) {
                    screenCoords.add(new Point(x, y));
                }
            }

            return out;
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private class GestureCallback implements VersionedGestureDetector.OnGestureListener {

        @Override
        public void onDown(final MotionEvent event) {
            mActionMoveDetected = false;
            mStopMoveDetecting = false;
            mTouchDownX = (int) event.getX();
            mTouchDownY = (int) event.getY();
            // invalidate();
        }

        @Override
        public void onMove(final MotionEvent event, final int count, final float x1, final float y1, final float x2, final float y2) {
            if ((Math.max(Math.abs(mTouchDownX - event.getX()), Math.abs(mTouchDownY - event.getY())) > 6) && !mStopMoveDetecting) {
                mActionMoveDetected = true;
                final float aRotateToAngle = 360 - mBearing;
                mTouchMapOffsetX = (int) (Math.sin(Math.toRadians(aRotateToAngle)) * (event.getY() - mTouchDownY))
                        + (int) (Math.cos(Math.toRadians(aRotateToAngle)) * (event.getX() - mTouchDownX));
                mTouchMapOffsetY = (int) (Math.cos(Math.toRadians(aRotateToAngle)) * (event.getY() - mTouchDownY))
                        - (int) (Math.sin(Math.toRadians(aRotateToAngle)) * (event.getX() - mTouchDownX));

                if (count > 1) {
                    final double DiagonalSize = Math.hypot((x1 - x2), (y1 - y2));
                    mTouchScale = (DiagonalSize / mTouchDiagonalSize);
                }

                // update center on the fly
                mTouchDownY = (int) event.getY();
                mTouchDownX = (int) event.getX();
                setMapCenter(newCenter());

                // invalidate();

                Message.obtain(mMainActivityCallbackHandler, R.id.user_moved_map).sendToTarget();
            }
        }

        private GeoPoint newCenter() {
            final int viewWidth_2 = getWidth() >> 1;
            final int viewHeight_2 = getHeight() >> 1;
            final GeoPoint newCenter = getProjection().fromPixels(viewWidth_2, viewHeight_2);

            mTouchMapOffsetX = 0;
            mTouchMapOffsetY = 0;

            return newCenter;
        }

        @Override
        public void onUp(final MotionEvent event) {
            mActionMoveDetected = false;
            mStopMoveDetecting = true;

            final GeoPoint newCenter = newCenter();
            setMapCenter(newCenter); // Calls invalidate
        }

        @Override
        public void onDown2(final MotionEvent event, final float x1, final float y1, final float x2, final float y2) {
            mTouchDiagonalSize = Math.hypot((x1 - x2), (y1 - y2));
            mActionMoveDetected = true;
        }

        @Override
        public void onUp2(final MotionEvent event) {
            if (mTouchScale > 1) {
                setZoomLevel((getZoomLevel() + (int) Math.round(mTouchScale)) - 1);
            }
            else {
                setZoomLevel((getZoomLevel() - (int) Math.round(1 / mTouchScale)) + 1);
            }
            mTouchScale = 1;

            mActionMoveDetected = false;
            mStopMoveDetecting = true;
            final GeoPoint newCenter2 = newCenter();
            setMapCenter(newCenter2); // Calls invalidate

            Message.obtain(mMainActivityCallbackHandler, R.id.set_title).sendToTarget();
        }

    }

    private class SimpleInvalidationHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
                case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
                    OpenStreetMapView.this.invalidate();
                    break;
                case OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE:
                    Message.obtain(mMainActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE, msg.obj).sendToTarget();
                    break;
                case OpenStreetMapTileFilesystemProvider.INDEXIND_SUCCESS_ID:
                    if (mZoomLevel > mRendererInfo.ZOOM_MAXLEVEL) {
                        mZoomLevel = mRendererInfo.ZOOM_MAXLEVEL;
                    }
                    if (mZoomLevel < mRendererInfo.ZOOM_MINLEVEL) {
                        mZoomLevel = mRendererInfo.ZOOM_MINLEVEL;
                    }

                    Message.obtain(mMainActivityCallbackHandler, R.id.set_title).sendToTarget();

                    OpenStreetMapView.this.invalidate();
                    break;
            }
        }
    }

    private class OpenStreetMapViewGestureDetectorListener implements OnGestureListener, OnDoubleTapListener {

        @Override
        public boolean onDown(final MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
            // LATER Could be used for smoothly 'scroll-out' the map on a fast
            // motion.
            return false;
        }

        @Override
        public void onLongPress(final MotionEvent e) {
            OpenStreetMapView.this.onLongPress(e);
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(final MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            return OpenStreetMapView.this.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            return OpenStreetMapView.this.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(final MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(final MotionEvent e) {
            return false;
        }

    }

}
