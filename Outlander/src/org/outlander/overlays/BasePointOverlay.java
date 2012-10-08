package org.outlander.overlays;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.outlander.R;
import org.outlander.model.PoiPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.utils.img.NinePatch;
import org.outlander.utils.img.NinePatchDrawable;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.MotionEvent;

public abstract class BasePointOverlay extends OpenStreetMapViewOverlay {

    protected final Context               mCtx;

    private final NinePatchDrawable       mButton;
    private int                           mTapIndex;
    protected GeoPoint                    mLastMapCenter;
    protected int                         mLastZoom;

    protected OnItemTapListener<PoiPoint> mOnItemTapListener;
    protected List<PointInfo>             mItemList;
    protected SparseArray<MarkerInfo>     mBtnMap;                      // iconcache

    // protected SharedPreferences sharedPreferences;

    // Drawable markerShadow;

    public static final int               HOTSPOT_TYPE_CENTER       = 1;
    public static final int               HOTSPOT_TYPE_BOTTOMCENTER = 0;

    protected class PointInfo {

        PoiPoint poiPoint;
        Picture  picture      = null;
        Rect     curMarkerBounds;
        int      hotspotType;
        int      routeId;
        int      shadowIconId = -1;
        int      iconId       = -1;
    }

    protected class MarkerInfo {

        Drawable marker;
        Drawable markerShadow;
        int      mMarkerWidth;
        int      mMarkerHeight;
        Point    mMarkerHotSpot;
    }

    public int getTapIndex() {
        return mTapIndex;
    }

    public void setTapIndex(final int mTapIndex) {
        this.mTapIndex = mTapIndex;
    }

    public BasePointOverlay(final Context ctx, final OnItemTapListener<PoiPoint> onItemTapListener) {

        mCtx = ctx;

        final Bitmap mBubbleBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.popup_button);

        final byte[] chunk = { 8, 8, 31, 28 }; // left,top,right,bottom
        mButton = new NinePatchDrawable(new NinePatch(mBubbleBitmap, chunk, ""));
        mTapIndex = -1;

        mBtnMap = new SparseArray<MarkerInfo>();

        mOnItemTapListener = onItemTapListener;

        mLastMapCenter = null;
        mLastZoom = -1;
    }

    protected void setItemList(final List<PointInfo> itemList) {
        mItemList = itemList;
    }

    public void clear() {
        mItemList = null;
    }

    abstract public void requestPointsForArea(final double deltaX, final double deltaY);

    abstract protected void zoomLevelChanged();

    abstract protected void centerChanged();

    @Override
    public void onDraw(final Canvas c, final OpenStreetMapView mapView) {
        final OpenStreetMapViewProjection pj = mapView.getProjection();
        final Point curScreenCoords = new Point();

        boolean looseCenter = false;
        final GeoPoint center = mapView.getMapCenter();
        final GeoPoint lefttop = pj.fromPixels(0, 0);

        final double deltaX = Math.abs(center.getLongitude() - lefttop.getLongitude());

        final double deltaY = Math.abs(center.getLatitude() - lefttop.getLatitude());

        if ((mLastMapCenter == null) || (mLastZoom != mapView.getZoomLevel())) {
            looseCenter = true;
        }
        else if (((0.7 * deltaX) < Math.abs(center.getLongitude() - mLastMapCenter.getLongitude()))
                || ((0.7 * deltaY) < Math.abs(center.getLatitude() - mLastMapCenter.getLatitude()))) {
            looseCenter = true;
        }

        if ((mLastZoom != mapView.getZoomLevel())) {
            zoomLevelChanged();
        }

        if (looseCenter) {
            mLastMapCenter = center;
            mLastZoom = mapView.getZoomLevel();

            centerChanged();

            // get new points for display range
            requestPointsForArea(deltaX, deltaY);
        }

        if (mItemList != null) {

            /*
             * Draw in backward cycle, so the items with the least index are on
             * the front.
             */
            for (int i = mItemList.size() - 1; i >= 0; i--) {
                if (i != mTapIndex) {
                    final PointInfo item = mItemList.get(i);
                    pj.toPixels(item.poiPoint.getGeoPoint(), curScreenCoords);

                    c.save();
                    c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);

                    onDrawItem(c, i, curScreenCoords);

                    c.restore();
                }
            }

            if ((mTapIndex >= 0) && (mTapIndex < mItemList.size())) {
                final PointInfo item = mItemList.get(mTapIndex);
                pj.toPixels(item.poiPoint.getGeoPoint(), curScreenCoords);

                c.save();
                c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);

                onDrawItem(c, mTapIndex, curScreenCoords);

                c.restore();
            }
        }
    }

    protected void onDrawItem(final Canvas c, final int index, final Point screenCoords) {

        final PointInfo pointInfo = mItemList.get(index);
        final PoiPoint focusedItem = pointInfo.poiPoint;

        final Integer key = Integer.valueOf(focusedItem.getIconId());
        MarkerInfo markerInfo = null;
        markerInfo = mBtnMap.get(key);
        if (markerInfo == null) {
            markerInfo = new MarkerInfo();
            try {
                markerInfo.marker = mCtx.getResources().getDrawable(pointInfo.iconId);

                if ((pointInfo.shadowIconId > 0) && (pointInfo.picture == null)) {
                    markerInfo.markerShadow = mCtx.getResources().getDrawable(pointInfo.shadowIconId);
                }

            }
            catch (final Exception e) {
                markerInfo.marker = mCtx.getResources().getDrawable(R.drawable.poi);
            }

            if (pointInfo.picture != null) {
                markerInfo.mMarkerWidth = pointInfo.picture.getWidth();
                markerInfo.mMarkerHeight = pointInfo.picture.getHeight();
                markerInfo.mMarkerHotSpot = new Point(markerInfo.mMarkerWidth >> 1, markerInfo.mMarkerHeight >> 1);

            }
            else {
                markerInfo.mMarkerWidth = markerInfo.marker.getIntrinsicWidth();
                markerInfo.mMarkerHeight = markerInfo.marker.getIntrinsicHeight();
                markerInfo.mMarkerHotSpot = new Point(markerInfo.mMarkerWidth >> 1,
                        (mItemList.get(index).hotspotType == BasePointOverlay.HOTSPOT_TYPE_BOTTOMCENTER) ? markerInfo.mMarkerHeight
                                : markerInfo.mMarkerHeight >> 1);
            }

            mBtnMap.put(key, markerInfo); // cache for next time...
        }

        // draw description
        if (index == mTapIndex) {
            final int textToRight = 10, /* widthRightCut = 2, */textPadding = 4, maxButtonWidth = 240;

            final TextWriter twTitle = new TextWriter(maxButtonWidth - textToRight, 14, focusedItem.getTitle());
            final TextWriter twDescr = new TextWriter(maxButtonWidth - textToRight, 12, focusedItem.getDescr()); // TODO:
                                                                                                                 // limit
                                                                                                                 // text
            // !
            final int coordFormt = CoreInfoHandler.getInstance().getCoordFormatId();
            // GeoPoint.FORMAT_DM
            final TextWriter twCoord = new TextWriter(maxButtonWidth - textToRight, 10, GeoMathUtil.formatGeoPoint(focusedItem.getGeoPoint(), coordFormt));

            final int buttonHeight = 10 + twTitle.getHeight() + twDescr.getHeight() + twCoord.getHeight() + (3 * textPadding);
            final int buttonWidth = Math.max(twCoord.getWidth(), Math.max(twTitle.getWidth(), twDescr.getWidth())) + textToRight + textToRight;

            mButton.setBounds(screenCoords.x - (buttonWidth >> 1), screenCoords.y, screenCoords.x + (buttonWidth >> 1), screenCoords.y + buttonHeight);

            mButton.draw(c);

            twTitle.Draw(c, mButton.getBounds().left + textToRight, mButton.getBounds().top + textPadding);

            twDescr.Draw(c, mButton.getBounds().left + textToRight, mButton.getBounds().top + textPadding + twTitle.getHeight() + textPadding);

            twCoord.Draw(c, mButton.getBounds().left + textToRight,
                    mButton.getBounds().top + textPadding + twTitle.getHeight() + textPadding + twDescr.getHeight() + textPadding);
        }

        final PointInfo item = mItemList.get(index);
        if (item.curMarkerBounds == null) {
            item.curMarkerBounds = new Rect();
        }
        item.curMarkerBounds.left = screenCoords.x - markerInfo.mMarkerHotSpot.x;
        item.curMarkerBounds.right = item.curMarkerBounds.left + markerInfo.mMarkerWidth;
        item.curMarkerBounds.top = screenCoords.y - markerInfo.mMarkerHotSpot.y;
        item.curMarkerBounds.bottom = item.curMarkerBounds.top + markerInfo.mMarkerHeight;

        if (markerInfo.markerShadow != null) { // TODO: shadows should be drawn
                                               // seperatly
            markerInfo.markerShadow.setBounds(item.curMarkerBounds);
            markerInfo.markerShadow.draw(c);
        }

        if (item.picture != null) {
            c.drawPicture(item.picture, item.curMarkerBounds);
        }
        else {
            markerInfo.marker.setBounds(item.curMarkerBounds);
            markerInfo.marker.draw(c);
        }
    }

    public PoiPoint getPoiPoint(final int index) {

        PoiPoint result = null;

        try {
            final PointInfo pointInfo = mItemList.get(index);
            result = pointInfo.poiPoint;
        }
        catch (final Exception x) {
            Ut.e("getPoiPoint with index: " + index + " failed");
        }

        return result;
    }

    public int getMarkerAtPoint(final int eventX, final int eventY, final OpenStreetMapView mapView) {
        int index = -1;
        if (mItemList != null) {
            int i = 0;
            for (final PointInfo pointInfo : mItemList) {
                if (pointInfo.curMarkerBounds.contains(eventX, eventY)) {
                    index = i;
                    break;
                }
                i++;
            }
        }
        return index;
    }

    protected void removePoiByIndex(int ix) {
        List<PointInfo> tempList = new ArrayList<PointInfo>();

        if (mItemList != null) {
            int i = 0;
            for (final PointInfo pointInfo : mItemList) {
                if (ix != i) {
                    tempList.add(pointInfo);
                }
                i++;
            }
        }
        if (mTapIndex == ix) {
            mTapIndex = -1;
        }

        setItemList(tempList);
    }

    protected void removePoiById(int id) {
        List<PointInfo> tempList = new ArrayList<PointInfo>();

        if (mItemList != null) {
            int ix = 0;
            for (final PointInfo pointInfo : mItemList) {
                if (pointInfo.poiPoint.getId() != id) {
                    tempList.add(pointInfo);
                    if (ix == mTapIndex) {
                        mTapIndex = -1;
                    }
                }
                ix++;
            }
        }

        setItemList(tempList);

    }

    // for pois that are members of a route or something
    protected void removePoiBySourceId(int id) {
        List<PointInfo> tempList = new ArrayList<PointInfo>();

        if (mItemList != null) {
            int ix = 0;
            for (final PointInfo pointInfo : mItemList) {
                if (pointInfo.poiPoint.getPointSourceId() != id) {
                    tempList.add(pointInfo);
                    if (ix == mTapIndex) {
                        mTapIndex = -1;
                    }
                }
                ix++;
            }
        }

        setItemList(tempList);

    }

    @Override
    public boolean onSingleTapUp(final MotionEvent event, final OpenStreetMapView mapView) {

        final int index = getMarkerAtPoint((int) event.getX(), (int) event.getY(), mapView);
        if (index >= 0) {
            if (onTap(index)) {
                return true;
            }
        }

        return super.onSingleTapUp(event, mapView);
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final OpenStreetMapView mapView) {

        final int index = getMarkerAtPoint((int) event.getX(), (int) event.getY(), mapView);
        if (index >= 0) {
            if (onLongLongPress(index)) {
                return true;
            }
        }
        return super.onLongPress(event, mapView);
    }

    private boolean onLongLongPress(final int index) {
        if (mTapIndex == index) {
            mTapIndex = -1;
        }
        else {
            mTapIndex = index;
        }

        if (mTapIndex >= 0) {
            internalPointLongPressed(mItemList.get(index));
        }

        if (mOnItemTapListener != null) {
            return mOnItemTapListener.onItemLongPress(index, mItemList.get(index).poiPoint);
        }
        else {
            return true;
        }

    }

    protected boolean onTap(final int index) {
        if (mTapIndex == index) {
            mTapIndex = -1;
        }
        else {
            mTapIndex = index;
        }

        if (mTapIndex >= 0) {
            internalPointTapped(mItemList.get(index));
        }

        if (mOnItemTapListener != null) {
            return mOnItemTapListener.onItemTap(index, mItemList.get(index).poiPoint);
        }
        else {
            return true;
        }
    }

    protected void internalPointTapped(final PointInfo pointInfo) {

    }

    protected void internalPointLongPressed(final PointInfo pointInfo) {

    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
    }

    public static interface OnItemTapListener<P extends PoiPoint> {

        public boolean onItemTap(final int aIndex, final P aItem);

        public boolean onItemLongPress(final int aIndex, final P aItem);
    }

    public static class TextWriter {

        private final String   mText;
        private int            mMaxWidth;
        private final int      mMaxHeight;
        private final int      mTextSize;
        private final Paint    mPaint;
        private final String[] mLines;

        public TextWriter(final int aMaxWidth, final int aTextSize, final String aText) {
            mMaxWidth = aMaxWidth;
            mTextSize = aTextSize;
            mText = (aText != null ? aText : "");
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            // mPaint.setTypeface(Typeface.create((Typeface)null,
            // Typeface.BOLD));

            final float[] widths = new float[mText.length()];
            mPaint.setTextSize(mTextSize);
            mPaint.getTextWidths(mText, widths);

            final StringBuilder sb = new StringBuilder();
            int maxWidth = 0;
            int curLineWidth = 0;
            int lastStop = 0;
            int i;
            int lastwhitespace = 0;
            /*
             * Loop through the charwidth array and harshly insert a linebreak,
             * when the width gets bigger than DESCRIPTION_MAXWIDTH.
             */
            for (i = 0; i < widths.length; i++) {
                if (!Character.isLetter(mText.charAt(i)) && (mText.charAt(i) != ',')) {
                    lastwhitespace = i;
                }

                final float charwidth = widths[i];

                if ((curLineWidth + charwidth) > mMaxWidth) {
                    if (lastStop == lastwhitespace) {
                        i--;
                    }
                    else {
                        i = lastwhitespace;
                    }

                    sb.append(mText.subSequence(lastStop, i));
                    sb.append('\n');

                    lastStop = i;
                    maxWidth = Math.max(maxWidth, curLineWidth);
                    curLineWidth = 0;
                }

                curLineWidth += charwidth;
            }
            /* Add the last line to the rest to the buffer. */
            if (i != lastStop) {
                final String rest = mText.substring(lastStop, i);

                maxWidth = Math.max(maxWidth, (int) mPaint.measureText(rest));

                sb.append(rest);
            }
            mLines = sb.toString().split("\n");

            mMaxWidth = maxWidth;
            mMaxHeight = mLines.length * mTextSize;
        }

        public void Draw(final Canvas c, final int x, final int y) {
            for (int j = 0; j < mLines.length; j++) {
                c.drawText(mLines[j].trim(), x, y + (mTextSize * (j + 1)), mPaint);
            }
        }

        public int getWidth() {
            return mMaxWidth;
        }

        public int getHeight() {
            return mMaxHeight;
        }
    }

    public void fromPref(final SharedPreferences settings) {

    }

    public void toPref(final SharedPreferences.Editor editor) {

    }

}
