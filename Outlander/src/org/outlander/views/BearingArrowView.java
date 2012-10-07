package org.outlander.views;

import org.outlander.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class BearingArrowView extends View {

    Drawable                arrow;
    Rect                    boundingRect;
    protected final Picture mBackgroundFrame = null; // new Picture();

    private int             mRadius          = 20;

    private float           COMPASS_ROSE_CENTER_X;
    private float           COMPASS_ROSE_CENTER_Y;

    int                     mScale           = 1;

    int                     bearing;

    public BearingArrowView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public BearingArrowView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BearingArrowView(final Context ctx) {
        super(ctx);
        init(ctx);
    }

    private void init(final Context ctx) {
        arrow = ctx.getResources().getDrawable(R.drawable.arrow_b);

        COMPASS_ROSE_CENTER_X = (mBackgroundFrame.getWidth() / 2) - 0.5f;
        COMPASS_ROSE_CENTER_Y = (mBackgroundFrame.getHeight() / 2) - 0.5f;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
            final int heightMeasureSpec) {

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int chosenWidth = chooseDimension(widthMode, widthSize);
        final int chosenHeight = chooseDimension(heightMode, heightSize);

        final int chosenDimension = Math.min(chosenWidth, chosenHeight);

        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(final int mode, final int size) {
        if ((mode == MeasureSpec.AT_MOST) || (mode == MeasureSpec.EXACTLY)) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredSize();
        }
    }

    // in case there is no size specified
    private int getPreferredSize() {
        return 30;
    }

    // @Override
    // protected void onMeasure(final int widthMeasureSpec,
    // final int heightMeasureSpec) {
    // setMeasuredDimension(((int) COMPASS_ROSE_CENTER_X * 2) + 1,
    // ((int) COMPASS_ROSE_CENTER_Y * 2) + 1);
    // }

    @Override
    protected void onDraw(final Canvas c) {

        if (isEnabled()) {

            if (mBackgroundFrame == null) {
                createBackgroundFramePicture();
            }

            mBackgroundFrame.draw(c);

            c.save();
            c.rotate((360 - getBearing()), COMPASS_ROSE_CENTER_X,
                    COMPASS_ROSE_CENTER_Y);

            arrow.draw(c);

        }

    }

    public int getBearing() {
        return bearing;
    }

    public void setBearing(final int bearing) {
        this.bearing = bearing;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private void createBackgroundFramePicture() {
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

        mRadius = (Math.min(getWidth(), getHeight()) / 2) - 4;

        final int picBorderWidthAndHeight = ((mRadius + 2) * 2);
        final int center = picBorderWidthAndHeight / 2;

        final Canvas canvas = mBackgroundFrame.beginRecording(
                picBorderWidthAndHeight, picBorderWidthAndHeight);

        // draw compass inner circle and border
        canvas.drawCircle(center, center, mRadius * mScale, innerPaint);
        canvas.drawCircle(center, center, mRadius * mScale, outerPaint);

        boundingRect = new Rect((center / 3), (center / 3),
                (int) (center + (center / 1.5)),
                (int) (center + (center / 1.5)));

        arrow.setBounds(boundingRect);
        mBackgroundFrame.endRecording();
    }

}
