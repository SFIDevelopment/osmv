// Created by plusminus on 22:01:11 - 29.09.2008
package org.outlander.overlays;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.outlander.R;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.utils.img.NinePatch;
import org.outlander.utils.img.NinePatchDrawable;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.preference.PreferenceManager;

/**
 * 
 * @author Nicolas Gramlich
 * @author Jim Fandango
 * 
 */
public class MyLocationOverlay extends OpenStreetMapViewOverlay {
    // ===========================================================
    // Constants
    // ===========================================================

    final static int                MIN_DIST             = 5;
    final static int                MIN_ZOOM             = 7;
    protected final static int      METER_IN_PIXEL       = 156412;     // ???
    // ===========================================================
    // Fields
    // ===========================================================

    protected final Paint           mPaint               = new Paint();

    private final Drawable          mArrow;
    private final Drawable          mStatic;
    // private final Drawable mMapCenter;
    // private Drawable mStop;
    /** Coordinates the feet of the person are located. */
    // protected final static android.graphics.Point person_hotspot = new
    // android.graphics.Point(
    // 24, 39);

    private final int               mPrefAccuracy;

    private final Paint             mPaintAccuracyFill;
    private final Paint             mPaintAccuracyBorder;

    private final Paint             mDistanceLinePaint;
    private final Paint             mTargetLinePaint;

    private final NinePatchDrawable mButton;

    private final boolean           mNeedCrosshair;
    private final Paint             mPaintCross          = new Paint();
    private final static int        mCrossSize           = 8;

    Path                            pathCenter           = new Path();
    Path                            pathTarget           = new Path();
    Point                           screenLocationCoords = new Point();
    Point                           screenTargetCoords   = new Point();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MyLocationOverlay(final Context ctx) {

        mArrow = ctx.getResources().getDrawable(R.drawable.direction_arrow);
        mStatic = ctx.getResources().getDrawable(R.drawable.location);

        mPaintAccuracyFill = new Paint();
        mPaintAccuracyFill.setAntiAlias(true);
        mPaintAccuracyFill.setStrokeWidth(2);
        mPaintAccuracyFill.setStyle(Paint.Style.FILL);
        mPaintAccuracyFill.setColor(ctx.getResources().getColor(
                R.color.accuracyfill));

        mPaintAccuracyBorder = new Paint(mPaintAccuracyFill);
        mPaintAccuracyBorder.setStyle(Paint.Style.STROKE);
        mPaintAccuracyBorder.setColor(ctx.getResources().getColor(
                R.color.accuracyborder));

        mPaintCross.setAntiAlias(true);

        final SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        mPrefAccuracy = Integer.parseInt(pref.getString("pref_accuracy", "1")
                .replace("\"", ""));

        mNeedCrosshair = pref.getBoolean("pref_crosshair", true);

        mPaintCross.setAntiAlias(true);
        mPaintCross.setStrokeWidth(1);
        mPaintCross.setStyle(Paint.Style.STROKE);
        mPaintCross.setColor(pref.getInt("color_distance",

        ctx.getResources().getColor(R.color.distance)));

        mDistanceLinePaint = new Paint();
        mDistanceLinePaint.setAntiAlias(true);
        mDistanceLinePaint.setStrokeWidth(2);
        mDistanceLinePaint.setStyle(Paint.Style.STROKE);
        mDistanceLinePaint.setColor(pref.getInt("color_distance", ctx
                .getResources().getColor(R.color.distance)));
        mDistanceLinePaint.setPathEffect(new DashPathEffect(
                new float[] { 5, 2 }, 0));

        mTargetLinePaint = new Paint(mDistanceLinePaint);
        mTargetLinePaint.setColor(pref.getInt("color_track_current", ctx
                .getResources().getColor(R.color.currenttrack)));
        mTargetLinePaint.setPathEffect(new PathDashPathEffect(RouteOverlay
                .makePathDash(2), 12, 12, PathDashPathEffect.Style.ROTATE));

        final Bitmap mBubbleBitmap = BitmapFactory.decodeResource(
                ctx.getResources(), R.drawable.popup_button);
        final byte[] chunk = { 8, 8, 31, 28 }; // left,top,right,bottom
        mButton = new NinePatchDrawable(new NinePatch(mBubbleBitmap, chunk, ""));

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
        return;
    }

    @Override
    public void onDraw(final Canvas c, final OpenStreetMapView osmv) {

        final GeoPoint myLocationPoint = CoreInfoHandler.getInstance()
                .getCurrentLocationAsGeoPoint();

        final GeoPoint mapCenter = CoreInfoHandler.getInstance()
                .getCurrentMapCenter();

        // final GeoPoint mapCenter = osmv.getMapCenter();

        final boolean drawDistanceToCenter = (CoreInfoHandler.getInstance()
                .isMeasureDistance()
                && (CoreInfoHandler.getInstance().isAutoFollow() == false)
                && ((mapCenter != null) && (myLocationPoint != null))
                && (mapCenter.distanceTo(myLocationPoint) > MIN_DIST) && (osmv
                .getZoomLevel() >= MIN_ZOOM));

        if (myLocationPoint != null) {
            final OpenStreetMapViewProjection pj = osmv.getProjection();

            pj.toPixels(myLocationPoint, screenLocationCoords);

            final Location myLocation = CoreInfoHandler.getInstance()
                    .getCurrentLocation();
            final float accuracy = myLocation.getAccuracy();
            final float speed = myLocation.getSpeed();
            final float bearing = myLocation.getBearing();

            if ((mPrefAccuracy != 0)
                    && (((accuracy > 0) && (mPrefAccuracy == 1)) || ((mPrefAccuracy > 1) && (accuracy >= mPrefAccuracy)))) {
                final int pixelRadius = (int) ((osmv.mTouchScale * accuracy) / ((float) METER_IN_PIXEL / (1 << osmv
                        .getZoomLevel())));

                c.drawCircle(screenLocationCoords.x, screenLocationCoords.y,
                        pixelRadius, mPaintAccuracyFill);

                c.drawCircle(screenLocationCoords.x, screenLocationCoords.y,
                        pixelRadius, mPaintAccuracyBorder);
            }

            c.save();
            pj.toPixels(mapCenter, screenCenterCoords);
            if (speed == 0) {
                c.rotate(osmv.getBearing(), screenLocationCoords.x,
                        screenLocationCoords.y);

                drawDistanceLines(c, drawDistanceToCenter, pj,
                        screenLocationCoords);

                drawCrossHair(c, screenCenterCoords);

                // // ??
                // c.drawCircle(screenLocationCoords.x, screenLocationCoords.y,
                // 10, mPaintAccuracyFill);

                mStatic.setBounds(
                        screenLocationCoords.x
                                - (mStatic.getMinimumWidth() / 2),
                        screenLocationCoords.y
                                - (mStatic.getMinimumHeight() / 2),
                        screenLocationCoords.x
                                + (mStatic.getMinimumWidth() / 2),
                        screenLocationCoords.y
                                + (mStatic.getMinimumHeight() / 2));
                mStatic.draw(c);

            } else {
                c.rotate(bearing, screenLocationCoords.x,
                        screenLocationCoords.y);

                drawDistanceLines(c, drawDistanceToCenter, pj,
                        screenLocationCoords);
                drawCrossHair(c, screenCenterCoords);

                mArrow.setBounds(
                        screenLocationCoords.x - (mArrow.getMinimumWidth() / 2),
                        screenLocationCoords.y
                                - (mArrow.getMinimumHeight() / 2),
                        screenLocationCoords.x + (mArrow.getMinimumWidth() / 2),
                        screenLocationCoords.y
                                + (mArrow.getMinimumHeight() / 2));
                mArrow.draw(c);
            }
            c.restore();

        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    final Point screenCenterCoords = new Point();

    void drawDinstance(final Canvas c, final OpenStreetMapViewProjection pj,
            final GeoPoint target) {

        pj.toPixels(target, screenCenterCoords);

        c.drawLine(screenLocationCoords.x, screenLocationCoords.y,
                screenCenterCoords.x, screenCenterCoords.y, mDistanceLinePaint);
        pathCenter.reset();
        pathCenter.moveTo(screenLocationCoords.x, screenLocationCoords.y);
        pathCenter.lineTo(screenCenterCoords.x, screenCenterCoords.y);

        drawDistanceInfo(c, screenCenterCoords, pathCenter, target, false);

    }

    private void drawDistanceLines(final Canvas c,
            final boolean drawDistanceToCenter,
            final OpenStreetMapViewProjection pj,
            final Point screenLocationCoords) {

        // position to screenscenter

        if (drawDistanceToCenter) {

            drawDinstance(c, pj, CoreInfoHandler.getInstance()
                    .getCurrentMapCenter());
        }

        // draw distance to target

        if ((CoreInfoHandler.getInstance().getCurrentTarget() != null)
                && (CoreInfoHandler.getInstance().isUseCurrentTarget()))

        {
            drawDinstance(c, pj, CoreInfoHandler.getInstance()
                    .getCurrentTarget());
        }

    }

    private void drawCrossHair(final Canvas c, final Point screenCenterCoords) {
        if (mNeedCrosshair) {
            final int x = screenCenterCoords.x;
            final int y = screenCenterCoords.y;

            c.drawLine(x - mCrossSize, y, x + mCrossSize, y, mPaintCross);
            c.drawLine(x, y - mCrossSize, x, y + mCrossSize, mPaintCross);
        }
    }

    // at t he moment - to mapcenter
    void drawDistanceInfo(final Canvas c, final Point screenCenterCoords,
            final Path path, final GeoPoint target, final boolean showInfoBox) {

        final GeoPoint myLocation = CoreInfoHandler.getInstance()
                .getCurrentLocationAsGeoPoint();

        final int distance = myLocation.distanceTo(target);

        final String distanceText = GeoMathUtil.getHumanDistanceString(
                distance, CoreInfoHandler.getInstance()
                        .getDistanceUnitFormatId());

        if (showInfoBox) {

            final double bearing = GeoMathUtil.azimuthTo(myLocation,
                    CoreInfoHandler.getInstance().getCurrentMapCenter());

            final int textToRight = 10, widthRightCut = 2, textPadding = 4, maxButtonWidth = 240;

            final int coordFormt = CoreInfoHandler.getInstance()
                    .getCoordFormatId();
            final BasePointOverlay.TextWriter distanceTxt = new BasePointOverlay.TextWriter(
                    maxButtonWidth - textToRight, 12, distanceText);
            final BasePointOverlay.TextWriter twDescr = new BasePointOverlay.TextWriter(
                    maxButtonWidth - textToRight, 12, "bearing +"
                            + (int) bearing + "°");

            // GeoPoint.FORMAT_DM
            final BasePointOverlay.TextWriter twCoord = new BasePointOverlay.TextWriter(
                    maxButtonWidth - textToRight, 10,
                    GeoMathUtil.formatGeoPoint(CoreInfoHandler.getInstance()
                            .getCurrentMapCenter(), coordFormt));

            final int buttonHeight = 10 + distanceTxt.getHeight()
                    + twDescr.getHeight() + twCoord.getHeight()
                    + (3 * textPadding);

            final int buttonWidth = Math.max(twCoord.getWidth(),
                    Math.max(distanceTxt.getWidth(), twDescr.getWidth()))
                    + textToRight + widthRightCut + widthRightCut;

            mButton.setBounds(screenCenterCoords.x - (buttonWidth >> 1),
                    screenCenterCoords.y + (MyLocationOverlay.mCrossSize / 2),
                    screenCenterCoords.x + (buttonWidth >> 1),
                    screenCenterCoords.y + buttonHeight
                            + (MyLocationOverlay.mCrossSize / 2));

            mButton.draw(c);

            distanceTxt.Draw(c, mButton.getBounds().left + textToRight,
                    mButton.getBounds().top + textPadding);

            twDescr.Draw(
                    c,
                    mButton.getBounds().left + textToRight,
                    mButton.getBounds().top + textPadding
                            + distanceTxt.getHeight() + textPadding);

            twCoord.Draw(
                    c,
                    mButton.getBounds().left + textToRight,
                    mButton.getBounds().top + textPadding
                            + distanceTxt.getHeight() + textPadding
                            + twDescr.getHeight() + textPadding);
        }
        c.drawTextOnPath(distanceText, path, 30, 12, mPaintCross);

    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
