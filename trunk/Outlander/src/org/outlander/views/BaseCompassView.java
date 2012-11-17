package org.outlander.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class BaseCompassView extends View {

    boolean                 drawCompass = false;
    protected double        mAzimuth    = -1.0f;
    protected double        bearing     = 0;
    protected final Context mCtx;

    public BaseCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
    }

    public BaseCompassView(final Context ctx) {
        super(ctx);
        mCtx = ctx;
    }

    public void setBearing(final double bearing) {
        this.bearing = bearing;
    }

    public double getBearing() {
        return bearing;
    }

    public void setAzimuth(final double azimuth) {
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

}
