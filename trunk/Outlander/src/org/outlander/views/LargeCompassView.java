package org.outlander.views;

import org.outlander.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;

public class LargeCompassView extends BaseCompassView {

    // private changeThread watchdog = null;
    private boolean              wantStop              = false;
    private boolean              lock                  = false;
    private boolean              drawing               = false;

    private Bitmap               compassUnderlay       = null;
    private Bitmap               compassRose           = null;
    private Bitmap               compassArrow          = null;
    private Bitmap               compassOverlay        = null;

    private Double               heading               = Double.valueOf(0);
    private Double               targetHeading         = Double.valueOf(0);
    private Double               northHeading          = Double.valueOf(0);
    private PaintFlagsDrawFilter setfil                = null;
    private PaintFlagsDrawFilter remfil                = null;
    private int                  compassUnderlayWidth  = 0;
    private int                  compassUnderlayHeight = 0;
    private int                  compassRoseWidth      = 0;
    private int                  compassRoseHeight     = 0;
    private int                  compassArrowWidth     = 0;
    private int                  compassArrowHeight    = 0;
    private int                  compassOverlayWidth   = 0;
    private int                  compassOverlayHeight  = 0;
    private boolean              bigOrSmall            = true;

    public LargeCompassView(final Context contextIn, final boolean big) {
        super(contextIn);

        bigOrSmall = big;

    }

    @Override
    public void onAttachedToWindow() {
        if (compassUnderlay == null) {
            compassUnderlay = BitmapFactory.decodeResource(mCtx.getResources(),
                    bigOrSmall ? R.drawable.compass_underlay
                            : R.drawable.compass_underlay_small);
        }
        if (compassRose == null) {
            compassRose = BitmapFactory.decodeResource(mCtx.getResources(),
                    bigOrSmall ? R.drawable.compass_rose
                            : R.drawable.compass_rose_small);
        }
        if (compassArrow == null) {
            compassArrow = BitmapFactory.decodeResource(mCtx.getResources(),
                    bigOrSmall ? R.drawable.compass_arrow
                            : R.drawable.compass_arrow_small);
        }
        if (compassOverlay == null) {
            compassOverlay = BitmapFactory.decodeResource(mCtx.getResources(),
                    bigOrSmall ? R.drawable.compass_overlay
                            : R.drawable.compass_overlay_small);
        }

        compassUnderlayWidth = compassUnderlay.getWidth();
        compassUnderlayHeight = compassUnderlayWidth;
        compassRoseWidth = compassRose.getWidth();
        compassRoseHeight = compassRoseWidth;
        compassArrowWidth = compassArrow.getWidth();
        compassArrowHeight = compassArrowWidth;
        compassOverlayWidth = compassOverlay.getWidth();
        compassOverlayHeight = compassOverlayWidth;

        setfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
        remfil = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0);

        wantStop = false;

        // watchdog = new changeThread(changeHandler);
        // watchdog.start();
    }

    @Override
    public void onDetachedFromWindow() {
        wantStop = true;

        if (compassUnderlay != null) {
            compassUnderlay.recycle();
        }

        if (compassRose != null) {
            compassRose.recycle();
        }

        if (compassArrow != null) {
            compassArrow.recycle();
        }

        if (compassOverlay != null) {
            compassOverlay.recycle();
        }
    }

    public void updateNorth(final Double northHeadingIn,
            final Double targetHeadingIn) {
        northHeading = northHeadingIn;
        targetHeading = targetHeadingIn;
    }

    private class changeThread extends Thread {

        @SuppressWarnings("unused")
        Handler handler = null;

        //
        // public changeThread(final Handler handlerIn) {
        // handler = handlerIn;
        // }

        @Override
        public void run() {
            while (wantStop == false) {
                try {
                    sleep(50);
                } catch (final Exception e) {
                    // nothing
                }

                if ((Math.abs(mAzimuth - northHeading) < 2)
                        && (Math.abs(heading - targetHeading) < 2)) {
                    continue;
                }

                lock = true;

                Double diff = new Double(0);
                Double diffAbs = new Double(0);
                Double tempAzimuth = new Double(0);
                Double tempHeading = new Double(0);

                final Double actualAzimuth = (double) mAzimuth;
                final Double actualHeading = heading;

                diff = northHeading - actualAzimuth;
                diffAbs = Math.abs(northHeading - actualAzimuth);
                if (diff < 0) {
                    diff = diff + 360;
                } else if (diff >= 360) {
                    diff = diff - 360;
                }

                if ((diff > 0) && (diff <= 180)) {
                    if (diffAbs > 5) {
                        tempAzimuth = actualAzimuth + 2;
                    } else if (diffAbs > 1) {
                        tempAzimuth = actualAzimuth + 1;
                    } else {
                        tempAzimuth = actualAzimuth;
                    }
                } else if ((diff > 180) && (diff < 360)) {
                    if (diffAbs > 5) {
                        tempAzimuth = actualAzimuth - 2;
                    } else if (diffAbs > 1) {
                        tempAzimuth = actualAzimuth - 1;
                    } else {
                        tempAzimuth = actualAzimuth;
                    }
                } else {
                    tempAzimuth = actualAzimuth;
                }

                diff = targetHeading - actualHeading;
                diffAbs = Math.abs(targetHeading - actualHeading);
                if (diff < 0) {
                    diff = diff + 360;
                } else if (diff >= 360) {
                    diff = diff - 360;
                }

                if ((diff > 0) && (diff <= 180)) {
                    if (diffAbs > 5) {
                        tempHeading = actualHeading + 2;
                    } else if (diffAbs > 1) {
                        tempHeading = actualHeading + 1;
                    } else {
                        tempHeading = actualHeading;
                    }
                } else if ((diff > 180) && (diff < 360)) {
                    if (diffAbs > 5) {
                        tempHeading = actualHeading - 2;
                    } else if (diffAbs > 1) {
                        tempHeading = actualHeading - 1;
                    } else {
                        tempHeading = actualHeading;
                    }
                } else {
                    tempHeading = actualHeading;
                }

                if (tempAzimuth >= 360) {
                    tempAzimuth = tempAzimuth - 360;
                } else if (tempAzimuth < 0) {
                    tempAzimuth = tempAzimuth + 360;
                }

                if (tempHeading >= 360) {
                    tempHeading = tempHeading - 360;
                } else if (tempHeading < 0) {
                    tempHeading = tempHeading + 360;
                }

                mAzimuth = tempAzimuth;
                heading = tempHeading;

                lock = false;

                // changeHandler.sendMessage(new Message());
            }
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (lock == true) {
            return;
        }
        if (drawing == true) {
            return;
        }

        final Double azimuthTemp = mAzimuth;
        Double azimuthRelative = azimuthTemp - heading;
        if (azimuthRelative < 0) {
            azimuthRelative = azimuthRelative + 360;
        } else if (azimuthRelative >= 360) {
            azimuthRelative = azimuthRelative - 360;
        }

        // compass margins
        final int canvasCenterX = (compassRoseWidth / 2)
                + ((getWidth() - compassRoseWidth) / 2);
        final int canvasCenterY = (compassRoseHeight / 2)
                + ((getHeight() - compassRoseHeight) / 2);

        int marginLeftTemp = 0;
        int marginTopTemp = 0;

        drawing = true;
        super.onDraw(canvas);

        canvas.save();
        canvas.setDrawFilter(setfil);

        marginLeftTemp = (getWidth() - compassUnderlayWidth) / 2;
        marginTopTemp = (getHeight() - compassUnderlayHeight) / 2;

        canvas.drawBitmap(compassUnderlay, marginLeftTemp, marginTopTemp, null);

        marginLeftTemp = (getWidth() - compassRoseWidth) / 2;
        marginTopTemp = (getHeight() - compassRoseHeight) / 2;

        canvas.rotate(new Float(-(azimuthTemp)), canvasCenterX, canvasCenterY);
        canvas.drawBitmap(compassRose, marginLeftTemp, marginTopTemp, null);
        canvas.rotate(new Float(azimuthTemp), canvasCenterX, canvasCenterY);

        marginLeftTemp = (getWidth() - compassArrowWidth) / 2;
        marginTopTemp = (getHeight() - compassArrowHeight) / 2;

        canvas.rotate(new Float(-(azimuthRelative)), canvasCenterX,
                canvasCenterY);
        canvas.drawBitmap(compassArrow, marginLeftTemp, marginTopTemp, null);
        canvas.rotate(new Float(azimuthRelative), canvasCenterX, canvasCenterY);

        marginLeftTemp = (getWidth() - compassOverlayWidth) / 2;
        marginTopTemp = (getHeight() - compassOverlayHeight) / 2;

        canvas.drawBitmap(compassOverlay, marginLeftTemp, marginTopTemp, null);

        canvas.setDrawFilter(remfil);
        canvas.restore();

        drawing = false;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
            final int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(final int measureSpec) {
        int result = 0;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = compassArrow.getWidth() + getPaddingLeft()
                    + getPaddingRight();

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureHeight(final int measureSpec) {
        int result = 0;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = compassArrow.getHeight() + getPaddingTop()
                    + getPaddingBottom();

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(final Double heading) {
        this.heading = heading;
    }

    public Double getTargetHeading() {
        return targetHeading;
    }

    public void setTargetHeading(final Double targetHeading) {
        this.targetHeading = targetHeading;
    }

    public Double getNorthHeading() {
        return northHeading;
    }

    public void setNorthHeading(final Double northHeading) {
        this.northHeading = northHeading;
    }
}