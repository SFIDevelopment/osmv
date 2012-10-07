package org.outlander.utils.img;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundedCornerImageView extends ImageView {

    public RoundedCornerImageView(final Context context) {
        super(context);

        // disable hw accelleration
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    // @Override
    // protected void onDraw(Canvas canvas) {
    // // super.onDraw(canvas);
    // Drawable drawable = getDrawable();
    //
    // Bitmap b = ((BitmapDrawable) drawable).getBitmap();
    // Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
    //
    // int w = getWidth(), h = getHeight();
    //
    // Bitmap roundBitmap = BitmapHelper.getRoundedCornerBitmap(getContext(),
    // bitmap, 10, w, h, true, false, true, false);
    // canvas.drawBitmap(roundBitmap, 0, 0, null);
    // }

    public RoundedCornerImageView(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedCornerImageView(final Context context,
            final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final float radius = 5.0f;

        final Path clipPath = new Path();
        final RectF rect = new RectF(0, 0, getWidth(), getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        super.onDraw(canvas);
    }

}
