package org.outlander.overlays;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.outlander.R;
import org.outlander.utils.Ut;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class GeoGridOverlay extends OpenStreetMapViewOverlay {

    private final Paint mPaint;
    private boolean     mStopDraw     = true;
    private final int   gridDegrees   = 10;
    Point               screenCoords1 = new Point();
    Point               screenCoords2 = new Point();

    public GeoGridOverlay(final Context context) {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth((float) 0.3);
        mPaint.setStyle(Paint.Style.STROKE);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // active & inactive (at the moment)
        mPaint.setColor(sharedPreferences.getInt("color_geogrid", context.getResources().getColor(R.color.grid)));
    }

    public void setStopDraw(final boolean stopdraw) {
        mStopDraw = stopdraw;
    }

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {
        if (mStopDraw) {
            return;
        }

        final OpenStreetMapViewProjection pj = osmv.getProjection();

        final GeoPoint p1 = new GeoPoint((double) 90, 0);
        final GeoPoint p2 = new GeoPoint((double) -90, 0);

        for (int lon = -180; lon < 180; lon += gridDegrees) {

            p1.setLongitude(lon);
            p2.setLongitude(lon);

            pj.toPixels(p1, screenCoords1);
            pj.toPixels(p2, screenCoords2);

            c.drawLine(screenCoords1.x, screenCoords1.y, screenCoords2.x, screenCoords2.y, mPaint);
        }

        // p1.setLongitude(-180.0);
        // p2.setLongitude(180.0);
        //
        // for (int lat = -90; lat < 90; lat += gridDegrees) {
        //
        // p1.setLatitude((double) lat);
        // p2.setLatitude((double) lat);
        //
        // pj.toPixels(p1, screenCoords1);
        // pj.toPixels(p2, screenCoords2);
        //
        // c.drawLine(screenCoords1.x, screenCoords1.y, screenCoords2.x,
        // screenCoords2.y, mPaint);
        // }

        Ut.d("Draw grid");
        //
        // // final long startMs = System.currentTimeMillis();
        //
        // if ((screenCoords.x != mBaseCoords.x)
        // && (screenCoords.y != mBaseCoords.y)) {
        // c.save();
        // c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y
        // - mBaseCoords.y);
        // c.drawPath(mPath, mPaint);
        // c.restore();
        // } else {
        // c.drawPath(mPath, mPaint);
        // }
    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
    }

}
