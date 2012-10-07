package org.outlander.instruments;

import org.outlander.R;
import org.outlander.utils.Ut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TinyCompassView extends View {
    private Drawable      mCompass;
    private float         mAzimuth = 0;
    // private final boolean mSideBottom;
    private final int     PADDING  = 2;
    private final Context mCtx;

    private int           compassWidth;
    private int           compassHeight;
    private final boolean drawLarge;

    public TinyCompassView(final Context ctx, final boolean sideBottom,
            final boolean drawLarge) {
        super(ctx);

        mCtx = ctx;
        // mSideBottom = sideBottom;
        this.drawLarge = drawLarge;
    }

    private boolean getCompassImg() {
        if (mCompass == null) {
            try {
                // mCompass =
                // mCtx.getResources().getDrawable(R.drawable.arrow_n);
                mCompass = mCtx.getResources().getDrawable(
                        R.drawable.compass_rose_small);

                compassWidth = (drawLarge ? mCompass.getMinimumWidth()
                        : mCompass.getMinimumWidth() / 2);
                compassHeight = (drawLarge ? mCompass.getMinimumHeight()
                        : mCompass.getMinimumHeight() / 2);

            } catch (final OutOfMemoryError e) {
                Ut.w("OutOfMemoryError");
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onDetachedFromWindow() {

        mCompass = null;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (getCompassImg()) {
            canvas.save();
            // if (mSideBottom) {
            // canvas.rotate(360 - mAzimuth,
            // PADDING + mCompass.getMinimumWidth() / 2,
            // PADDING + mCompass.getMinimumHeight() / 2);
            // mCompass.setBounds(PADDING, PADDING,
            // PADDING + mCompass.getMinimumWidth(),
            // PADDING + mCompass.getMinimumHeight());
            // } else {
            // // canvas.rotate(360 - mAzimuth,
            // // PADDING + mCompass.getMinimumWidth() / 2,
            // // getHeight() - mCompass.getMinimumHeight() / 2 - PADDING);
            // // mCompass.setBounds(PADDING,
            // // getHeight() - mCompass.getMinimumHeight() - PADDING,
            // // PADDING + mCompass.getMinimumWidth(),
            // // getHeight() - PADDING);
            canvas.rotate(360 - mAzimuth, PADDING, PADDING);
            mCompass.setBounds(PADDING, PADDING, PADDING + compassWidth,
                    PADDING + compassWidth);
            // }
            mCompass.draw(canvas);
            canvas.restore();
        }

        super.onDraw(canvas);
    }

    public void setAzimuth(final float aAzimuth) {
        mAzimuth = aAzimuth;
    }

}
