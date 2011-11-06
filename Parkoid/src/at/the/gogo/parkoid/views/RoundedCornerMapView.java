package at.the.gogo.parkoid.views;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RoundedCornerMapView extends MapView {

    Path                      clipPath      = null;

    final public static float CORNER_RADIUS = 10.0f;

    public RoundedCornerMapView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedCornerMapView(final Context context, final int tileSizePixels) {
        super(context, tileSizePixels, new DefaultResourceProxyImpl(context));
    }

    public RoundedCornerMapView(final Context context,
            final int tileSizePixels, final ResourceProxy resourceProxy) {
        super(context, tileSizePixels, resourceProxy, null);
    }

    public RoundedCornerMapView(final Context context,
            final int tileSizePixels, final ResourceProxy resourceProxy,
            final MapTileProviderBase aTileProvider) {
        super(context, tileSizePixels, resourceProxy, aTileProvider, null);
    }

    private void initializeClipPath(final float cornerRadius) {
        clipPath = new Path();
        final RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, cornerRadius, cornerRadius,
                Path.Direction.CW);
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        if (clipPath == null) {
            initializeClipPath(RoundedCornerMapView.CORNER_RADIUS);
        }

        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        if (clipPath == null) {
            initializeClipPath(RoundedCornerMapView.CORNER_RADIUS);
        }

        canvas.clipPath(clipPath);

        super.onDraw(canvas);

    }

}
