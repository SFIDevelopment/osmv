package org.outlander.views;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class SpeedometerView extends View {

    private static final DecimalFormat a = new DecimalFormat("##");
    private final Paint                b;
    private final Paint                c = new Paint();
    private final Paint                d;
    private final Paint                e;
    private final int                  f = -16777216;
    private final int                  g = -13421773;
    private int                        h;
    private int                        i = 8;
    private int                        j = 36;
    private float                      k = 0.0F;
    private float                      l = 20.0F;
    private final boolean              m = true;
    private String                     n;
    private boolean                    o;
    private final PointF               p = new PointF();

    public SpeedometerView(final Context paramContext) {
        this(paramContext, null, null);
    }

    public SpeedometerView(final Context paramContext, final AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, null);
    }

    public SpeedometerView(final Context paramContext, final AttributeSet paramAttributeSet, final Typeface paramTypeface) {
        super(paramContext, paramAttributeSet);
        c.setStyle(Paint.Style.FILL);
        c.setAntiAlias(true);
        c.setColor(-16777216);
        b = new Paint();
        b.setStyle(Paint.Style.FILL);
        b.setAntiAlias(true);
        b.setColor(-1429436280);
        d = new TextPaint();
        d.setTextAlign(Paint.Align.CENTER);
        d.setColor(-1);
        d.setAntiAlias(true);
        e = new TextPaint();
        e.setTextAlign(Paint.Align.CENTER);
        e.setColor(-1);
        e.setAntiAlias(true);
        if (paramTypeface != null) {
            d.setTypeface(paramTypeface);
            e.setTypeface(paramTypeface);
        }
    }

    private void a(final int paramInt, final PointF paramPointF) {
        final float f1 = 0.01745329F * paramInt;
        paramPointF.x = FloatMath.sin(f1);
        paramPointF.y = FloatMath.cos(f1);
    }

    public void aa(final float paramFloat) {
    }

    public void aa(final Object paramObject) {
        if ((paramObject instanceof SpeedometerView)) {
            final SpeedometerView localSpeedometerView = (SpeedometerView) paramObject;
            k = localSpeedometerView.k;
            l = localSpeedometerView.l;
            n = localSpeedometerView.n;
        }
    }

    public void a(final String paramString) {
        n = paramString;
    }

    public void b(final float paramFloat) {
        k = paramFloat;
        if ((m) && (paramFloat > l)) {
            l = (float) (10.0D * Math.ceil(paramFloat / 10.0F));
        }
    }

    @Override
    protected void onDraw(final Canvas paramCanvas) {
        final int i1 = getWidth() / 2;
        final int i2 = getHeight() / 2;
        c.setColor(-16777216);
        paramCanvas.drawCircle(i1, i2, h, c);
        c.setColor(-13421773);
        paramCanvas.drawCircle(i1, i2, h - 2, c);
        final float f1 = (360.0F * k) / l;
        paramCanvas.drawArc(new RectF(2 + (i1 - h), 2 + (i2 - h), (i1 + h) - 2, (i2 + h) - 2), 0.0F, f1, true, b);
        c.setColor(-16777216);
        c.setStrokeWidth(1.0F);
        int i5 = 0;
        for (int i7 = 0;; i7 = i7) {
            int i6 = 0;
            float f2 = 0;
            int i4 = 0;
            if (i5 >= j) {
                c.setStrokeWidth(3.0F);
                a(90, p);
                i6 = (int) (p.x * h);
                i5 = (int) (p.y * h);
                paramCanvas.drawLine(i1, i2, i6 + i1, i5 + i2, c);
                c.setStrokeWidth(1.0F);
                c.setColor(-16777216);
                paramCanvas.drawCircle(i1, i2, h - 2 - i, c);
                f2 = d.getTextSize();
                final int i3 = (int) (i2 + (f2 / 3.0F));
                paramCanvas.drawText(SpeedometerView.a.format(k), i1, i3, d);
                if ((o) && (n != null)) {
                    i4 = (int) (i3 + (f2 / 2.0F));
                    paramCanvas.drawText(n, i1, i4, e);
                }
                return;
            }
            a(i7, p);
            final int i8 = (int) (p.x * h);
            final int i9 = (int) (p.y * h);
            paramCanvas.drawLine(i1, i4, i8 + i1, i9 + i4, c);
            i7 = (i6 + i7);
            f2++;
        }
    }

    @Override
    protected void onMeasure(final int paramInt1, final int paramInt2) {
        final int i4 = View.MeasureSpec.getSize(paramInt1);
        int i3 = View.MeasureSpec.getSize(paramInt2);
        final int i2 = View.MeasureSpec.getMode(paramInt1);
        final int i1 = View.MeasureSpec.getMode(paramInt2);
        if ((i2 != 1073741824) || (i1 != 1073741824)) {
            i3 -= 2;
        }
        setMeasuredDimension(i4, i3);
    }

    @Override
    protected void onSizeChanged(final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4) {
        super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
        final int i2 = paramInt1 / 2;
        final int i1 = paramInt2 / 2;
        int i3 = Math.min(paramInt1, paramInt2);
        h = (i3 / 2);
        i = (int) (h / 3.0F);
        if (i3 <= 200) {
            i3 = 24;
        }
        else {
            i3 = 36;
        }
        j = i3;
        float f2 = -2 + (h - i);
        d.setTextSize(0.8F * f2);
        f2 *= 0.3F;
        e.setTextSize(f2);
        boolean bool;
        if (f2 < 9.0F) {
            bool = false;
        }
        else {
            bool = true;
        }
        o = bool;
        final float f1 = i2;
        final float f3 = i1;
        final int localObject[] = new int[4];
        localObject[0] = -16711936;
        localObject[1] = -256;
        localObject[2] = -16776961;
        localObject[3] = -65536;

        b.setShader(new SweepGradient(f1, f3, localObject, null));
    }
}
