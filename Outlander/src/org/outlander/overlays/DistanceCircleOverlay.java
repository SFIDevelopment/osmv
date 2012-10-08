package org.outlander.overlays;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.outlander.R;
import org.outlander.utils.CoreInfoHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class DistanceCircleOverlay extends OpenStreetMapViewOverlay {

    private boolean                 mStopDraw = false;
    private final SharedPreferences sharedPreferences;
    private final Paint             mPaintLine;
    private Paint                   mPaintFill;
    private final Paint             mPaintText;
    private int                     mLastZoom;
    private final boolean           fillCircle;

    int                             fillColor;

    // private List<Path> circles;

    int[]                           radius2   = { 500000, 1000000, 2000000 };
    int[]                           radius3   = { 100000, 300000, 600000 };
    int[]                           radius4   = { 80000, 100000, 15000 };
    int[]                           radius5   = { 100000, 200000 };
    int[]                           radius6   = { 25000, 35000, 70000 };
    int[]                           radius7   = { 10000, 30000, 50000 };
    int[]                           radius8   = { 5000, 15000, 25000 };
    int[]                           radius9   = { 5000, 15000 };
    int[]                           radius10  = { 1000, 4000, 8000 };
    int[]                           radius11  = { 2000, 4000 };
    int[]                           radius12  = { 500, 1000, 2000 };
    int[]                           radius13  = { 400, 600, 900 };
    int[]                           radius14  = { 200, 400, 600 };
    int[]                           radius15  = { 50, 100, 200 };
    int[]                           radius16  = { 30, 50, 100 };
    int[]                           radius17  = { 20, 40, 60 };

    int[][]                         radius    = { null, null, null, null, radius2, radius3, radius4, radius5, radius6, radius7, radius8, radius9, radius10,
            radius11, radius12, radius13, radius14, radius15, radius16, radius17 };

    public DistanceCircleOverlay(final Context context) {

        mLastZoom = -1;

        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStrokeWidth(3);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setTextSize(15);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // active & inactive (at the moment)
        mPaintLine.setColor(sharedPreferences.getInt("color_distance_circle", context.getResources().getColor(R.color.distancecircle)));

        fillCircle = sharedPreferences.getBoolean("color_distance_circle_fill", true);

        final DashPathEffect dashPath = new DashPathEffect(new float[] { 3, 3 }, (float) 1.0);

        mPaintLine.setPathEffect(dashPath);

        mPaintText = new Paint(mPaintLine);
        mPaintText.setPathEffect(null);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setStrokeWidth(1);

        if (fillCircle) {
            mPaintFill = new Paint(mPaintLine);
            mPaintFill.setStrokeWidth(2);
            mPaintFill.setStyle(Paint.Style.FILL);
            mPaintFill.setAlpha(20);
        }

    }

    public void setStopDraw(final boolean stopDrawing) {
        mStopDraw = stopDrawing;
    }

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {
        if (!mStopDraw) {
            if (osmv.getZoomLevel() != mLastZoom) {
                mLastZoom = osmv.getZoomLevel();
            }

            if ((mLastZoom >= 0) && (mLastZoom < radius.length)) {
                final int[] radiusse = radius[mLastZoom];

                if (radiusse != null) {
                    final GeoPoint myLocationPoint = CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint();

                    if (myLocationPoint != null) {
                        final OpenStreetMapViewProjection pj = osmv.getProjection();
                        final Point screenLocationCoords = new Point();
                        pj.toPixels(myLocationPoint, screenLocationCoords);

                        for (final int element : radiusse) {
                            final int radius = (int) ((osmv.mTouchScale * element) / ((float) MyLocationOverlay.METER_IN_PIXEL / (1 << mLastZoom)));

                            if (fillCircle) {
                                c.drawCircle(screenLocationCoords.x, screenLocationCoords.y, radius, mPaintFill);
                            }
                            c.drawCircle(screenLocationCoords.x, screenLocationCoords.y, radius, mPaintLine);

                            c.drawText(Integer.toString(element / 2) + " m", screenLocationCoords.x, screenLocationCoords.y + radius + 20, mPaintText);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {

    }

}
