package org.outlander.utils.img;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class NinePatchDrawable extends Drawable {

    private final NinePatch mNinePatch;
    private final Paint     mPaint = new Paint();
    @SuppressWarnings("unused")
    private Context         mContext;

    public NinePatchDrawable(final NinePatch ninePatch) {
        mNinePatch = ninePatch;
        mPaint.setAlpha(0xff);
    }

    @Override
    public void draw(final Canvas canvas) {
        mNinePatch.draw(canvas, getBounds(), mPaint);
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void setAlpha(final int alpha) {
        mPaint.setAlpha(alpha);
    }

    /*
     * (non-Javadoc)
     * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.
     * ColorFilter)
     */
    @Override
    public void setColorFilter(final ColorFilter cf) {
    }

    /* non-Android APIs */

    public void __setContext(final Context context) {
        mContext = context;
    }

    public NinePatch __getNinePatch() {
        return mNinePatch;
    }
}