package org.outlander.overlays;

import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.outlander.R;
import org.outlander.io.db.CachedLocationsDatabaseHelper;
import org.outlander.model.LocationPoint;
import org.outlander.utils.geo.GeoMathUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class LastLocationCircleOverlay extends OpenStreetMapViewOverlay {

    private boolean             mStopDraw            = false;

    private List<LocationPoint> lastLocations;

    private final GeoPoint      center               = new GeoPoint(0.0, 0.0);

    private final int[]         colorDefs;

    Paint                       mPaint, mPaintFill;
    Context                     context;
    final static int            radius               = 3;
    Point                       screenLocationCoords = new Point();

    public LastLocationCircleOverlay(final Context context) {
        super();

        this.context = context;
        colorDefs = context.getResources().getIntArray(R.array.lastloccolors);
        //
        // color = new Color[colorDefs.length];
        //
        // for (int i = 0; i < colorDefs.length; i++) {
        // color[i] = new Color();
        //
        // int a = (colorDefs[i] >> 16) & 0xFF;
        // int r = (colorDefs[i] >> 16) & 0xFF;
        // int g = (colorDefs[i] >> 8) & 0xFF;
        // int b = (colorDefs[i] >> 0) & 0xFF;
        //
        // color[i] = Color.argb(a, r, g, b);
        // }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mPaintFill = new Paint();
        mPaintFill.setAntiAlias(true);
        mPaintFill.setStrokeWidth(2);
        mPaintFill.setStyle(Paint.Style.FILL);

    }

    public void setStopDraw(final boolean stopDrawing) {
        mStopDraw = stopDrawing;
    }

    @Override
    protected void onDraw(final Canvas c, final OpenStreetMapView osmv) {
        if (!mStopDraw) {

            if ((lastLocations != null) && (lastLocations.size() > 0)) {
                final OpenStreetMapViewProjection pj = osmv.getProjection();

                int i = 0;
                final LocationPoint lastpoint = null;
                for (final LocationPoint point : lastLocations) {
                    if (point != null) {// draw only if distance will be visible
                        if ((lastpoint == null)
                                || ((lastpoint != null) && (GeoMathUtil.distanceApart(lastpoint.getLatitude(), lastpoint.getLongitude(), point.getLatitude(),
                                        point.getLongitude()) > MyLocationOverlay.MIN_DIST))) {
                            center.setLatitude(point.getLatitude());
                            center.setLongitude(point.getLongitude());

                            pj.toPixels(center, screenLocationCoords);

                            // set different colors
                            // int color = context.getResources().getColor(
                            // colorDefs[i]);
                            mPaint.setColor(colorDefs[i]);
                            mPaintFill.setColor(colorDefs[i]);

                            c.drawCircle(screenLocationCoords.x, screenLocationCoords.y, radius, mPaintFill);

                            c.drawCircle(screenLocationCoords.x, screenLocationCoords.y, radius, mPaint);

                        }
                        i++;
                    }
                }
                //
            }
        }
    }

    // has to be called from outside...
    public void refresh() {
        lastLocations = CachedLocationsDatabaseHelper.getInstance(context).retrieveLatestLocationPoints();
    }

    @Override
    protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {

    }

}
