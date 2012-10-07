package org.outlander.overlays;

import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Point;

public class CompassOverlay extends OpenStreetMapViewOverlay {

    protected final Picture mCompassFrame   = new Picture();
    protected final Picture mCompassRose    = new Picture();
    private final Matrix    mCompassMatrix  = new Matrix();

    // actual compass value. Note: this one is only changed when an actual
    // compass value
    // is being read, so a check >= 0 is valid
    private float           mAzimuth        = -1.0f;

    private float           mCompassCenterX = 35.0f;
    private float           mCompassCenterY = 35.0f;
    private final float     mCompassRadius  = 20.0f;

    // private final float COMPASS_FRAME_CENTER_X;
    // private final float COMPASS_FRAME_CENTER_Y;
    private final float     COMPASS_ROSE_CENTER_X;
    private final float     COMPASS_ROSE_CENTER_Y;

    protected final Paint   mPaint          = new Paint();
    protected final Paint   mCirclePaint    = new Paint();

    boolean                 drawCompass     = false;

    int                     mScale          = 1;

    public CompassOverlay() {
        mCirclePaint.setARGB(0, 100, 100, 128);
        mCirclePaint.setAntiAlias(true);

        createCompassFramePicture();
        createCompassRosePicture();

        // COMPASS_FRAME_CENTER_X = (mCompassFrame.getWidth() / 2) - 0.5f;
        // COMPASS_FRAME_CENTER_Y = (mCompassFrame.getHeight() / 2) - 0.5f;
        COMPASS_ROSE_CENTER_X = (mCompassRose.getWidth() / 2) - 0.5f;
        COMPASS_ROSE_CENTER_Y = (mCompassRose.getHeight() / 2) - 0.5f;

    }

    public void setCompassCenter(final float x, final float y) {
        mCompassCenterX = x;
        mCompassCenterY = y;
    }

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {

        if ((isCompassEnabled()) && (mAzimuth >= 0.0f)) {
            final float centerX = mCompassCenterX * mScale;
            final float centerY = (mCompassCenterY * mScale)
                    + (c.getHeight() - osmv.getHeight());

            // mCompassMatrix.setTranslate(-COMPASS_FRAME_CENTER_X,
            // -COMPASS_FRAME_CENTER_Y);
            // mCompassMatrix.postTranslate(centerX, centerY);

            c.save();
            c.rotate(360 - osmv.getBearing(), centerX, centerY);

            c.drawPicture(mCompassFrame);

            c.restore();

            // c.save();
            // c.setMatrix(mCompassMatrix);
            // c.drawPicture(mCompassFrame);

            // Note - all azimuths are offset by -90 when in landscape mode.
            // This is because the
            // hardware does not change orientation when physically flipped, but
            // Android changes the
            // screen coordinates therefore it will be off by 90 degrees. This
            // assumes that Android only
            // allows two screen rotations - 0 degrees (portrait) and 90 degrees
            // (landscape) and does
            // not permit 180 or 270 degrees (upside-down portrait and
            // upside-down landscape
            // respectively). This is probably a bad assumption, so maybe there
            // is a better way to do
            // this. SensorManager.remapCoordinateSystem might be able to help.

            final int azimuthRotationOffset = (osmv.getResources()
                    .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? -90
                    : 0);

            mCompassMatrix.setRotate(-mAzimuth + azimuthRotationOffset,
                    COMPASS_ROSE_CENTER_X, COMPASS_ROSE_CENTER_Y);
            mCompassMatrix.postTranslate(-COMPASS_ROSE_CENTER_X,
                    -COMPASS_ROSE_CENTER_Y);
            mCompassMatrix.postTranslate(centerX, centerY);

            c.setMatrix(mCompassMatrix);
            c.drawPicture(mCompassRose);
            c.restore();
        }

    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
        // TODO Auto-generated method stub

    }

    public void setAzimuth(final float azimuth) {
        mAzimuth = azimuth;
    }

    /**
     * If enabled, the map is receiving orientation updates and drawing your
     * location on the map.
     * 
     * @return true if enabled, false otherwise
     */

    public boolean isCompassEnabled() {
        return drawCompass;
    }

    public void enableCompass() {
        drawCompass = true;
    }

    public void setStopDraw(final boolean stopDraw) {
        drawCompass = !stopDraw;
    }

    /**
     * Disable orientation updates
     */

    public void disableCompass() {
        mAzimuth = -1.0f;
        drawCompass = false;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private Point calculatePointOnCircle(final float centerX,
            final float centerY, final float radius, final float degrees) {
        // for trigonometry, 0 is pointing east, so subtract 90
        // compass degrees are the wrong way round
        final double dblRadians = Math.toRadians(-degrees + 90);

        final int intX = (int) (radius * Math.cos(dblRadians));
        final int intY = (int) (radius * Math.sin(dblRadians));

        return new Point((int) centerX + intX, (int) centerY - intY);
    }

    private void drawTriangle(final Canvas canvas, final float x,
            final float y, final float radius, final float degrees,
            final Paint paint) {
        canvas.save();
        final Point point = calculatePointOnCircle(x, y, radius, degrees);
        canvas.rotate(degrees, point.x, point.y);
        final Path p = new Path();
        p.moveTo(point.x - (2 * mScale), point.y);
        p.lineTo(point.x + (2 * mScale), point.y);
        p.lineTo(point.x, point.y - (5 * mScale));
        p.close();
        canvas.drawPath(p, paint);
        canvas.restore();
    }

    private void createCompassFramePicture() {
        // The inside of the compass is white and transparent
        final Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        innerPaint.setStyle(Style.FILL);
        innerPaint.setAlpha(200);

        // The outer part (circle and little triangles) is gray and transparent
        final Paint outerPaint = new Paint();
        outerPaint.setColor(Color.GRAY);
        outerPaint.setAntiAlias(true);
        outerPaint.setStyle(Style.STROKE);
        outerPaint.setStrokeWidth(2.0f);
        outerPaint.setAlpha(200);

        final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
        final int center = picBorderWidthAndHeight / 2;

        final Canvas canvas = mCompassFrame.beginRecording(
                picBorderWidthAndHeight, picBorderWidthAndHeight);

        // draw compass inner circle and border
        canvas.drawCircle(center, center, mCompassRadius * mScale, innerPaint);
        canvas.drawCircle(center, center, mCompassRadius * mScale, outerPaint);

        // Draw little triangles north, south, west and east (don't move)
        // to make those move use "-bearing + 0" etc. (Note: that would mean to
        // draw the triangles
        // in the onDraw() method)
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 0,
                outerPaint);
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 90,
                outerPaint);
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 180,
                outerPaint);
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 270,
                outerPaint);

        mCompassFrame.endRecording();
    }

    private void createCompassRosePicture() {
        // Paint design of north triangle (it's common to paint north in red
        // color)
        final Paint northPaint = new Paint();
        northPaint.setColor(0xFFA00000);
        northPaint.setAntiAlias(true);
        northPaint.setStyle(Style.FILL);
        northPaint.setAlpha(220);

        // Paint design of south triangle (black)
        final Paint southPaint = new Paint();
        southPaint.setColor(Color.BLACK);
        southPaint.setAntiAlias(true);
        southPaint.setStyle(Style.FILL);
        southPaint.setAlpha(220);

        // Create a little white dot in the middle of the compass rose
        final Paint centerPaint = new Paint();
        centerPaint.setColor(Color.WHITE);
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Style.FILL);
        centerPaint.setAlpha(220);

        // final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 *
        // mScale);
        final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
        final int center = picBorderWidthAndHeight / 2;

        final Canvas canvas = mCompassRose.beginRecording(
                picBorderWidthAndHeight, picBorderWidthAndHeight);

        // Blue triangle pointing north
        final Path pathNorth = new Path();
        pathNorth.moveTo(center, center - ((mCompassRadius - 3) * mScale));
        pathNorth.lineTo(center + (4 * mScale), center);
        pathNorth.lineTo(center - (4 * mScale), center);
        pathNorth.lineTo(center, center - ((mCompassRadius - 3) * mScale));
        pathNorth.close();
        canvas.drawPath(pathNorth, northPaint);

        // Red triangle pointing south
        final Path pathSouth = new Path();
        pathSouth.moveTo(center, center + ((mCompassRadius - 3) * mScale));
        pathSouth.lineTo(center + (4 * mScale), center);
        pathSouth.lineTo(center - (4 * mScale), center);
        pathSouth.lineTo(center, center + ((mCompassRadius - 3) * mScale));
        pathSouth.close();
        canvas.drawPath(pathSouth, southPaint);

        // Draw a little white dot in the middle
        canvas.drawCircle(center, center, 2, centerPaint);

        mCompassRose.endRecording();
    }

}
