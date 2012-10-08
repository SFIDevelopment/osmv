package org.outlander.utils.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class NinePatch {

    protected Bitmap mBitmap;

    protected byte[] mChunk;

    protected int    mAlpha = 0xff;

    protected Rect   src    = new Rect();

    protected Rect   dst    = new Rect();

    protected Paint  p      = new Paint();

    protected Rect   inner  = new Rect();

    protected int    mWidth;

    protected int    mHeight;

    public NinePatch(final Bitmap bitmap, final byte[] chunk, final String srcName) {
        mBitmap = bitmap;
        mChunk = chunk;

        // substract marker-lines from 9patch bitmap's dimensions
        mWidth = bitmap.getWidth() - 2;
        mHeight = bitmap.getHeight() - 2;

        extractRectFromChunk(0, src);
    }

    public void draw(final Canvas canvas, final Rect location) {
        doDraw(canvas, location.left, location.top, location.right, location.bottom, mAlpha);
    }

    public void draw(final Canvas canvas, final Rect location, final Paint paint) {
        doDraw(canvas, location.left, location.top, location.right, location.bottom, Color.alpha(paint.getColor()));
    }

    public void draw(final Canvas canvas, final RectF location) {
        doDraw(canvas, location.left, location.top, location.right, location.bottom, mAlpha);
    }

    private void doDraw(final Canvas c, final float left, final float top, final float right, final float bottom, final int alpha)

    {
        doDraw(c, (int) left, (int) top, (int) right, (int) bottom, alpha);
    }

    private void doDraw(final Canvas c, final int left, final int top, final int right, final int bottom, final int alpha) {
        p.setAlpha(alpha);

        final int pos = 0;

        // 9-patch rects-encoding Drine (not Android) uses inside mChunk
        // quad-byte groups:
        //
        // --------------------------------------------------
        // 1st row: | top-left | top-middle | top-right |
        // |------------------------------------------------|
        // 2nd row: | left-middle | center | right-middle |
        // |------------------------------------------------|
        // 3rd row: | bottom-left | bottom-moddle | bottom-right |
        // --------------------------------------------------

        // ////////// 1st row

        extractRectFromChunk(pos, src);

        inner.left = mChunk[0];
        inner.top = mChunk[1];
        inner.right = mChunk[2];
        inner.bottom = mChunk[3];

        // precalculate some things

        final int rightColWidth = mWidth - inner.right;
        final int leftColWidth = inner.left;

        final int bottomRowHeight = mHeight - inner.bottom;
        final int topRowHeight = inner.top;

        // top-left
        src.left = 0 + 1;
        src.top = 0 + 1;
        src.right = inner.left + 1;
        src.bottom = inner.top + 1;
        dst.set(left, top, left + leftColWidth, top + topRowHeight);
        c.drawBitmap(mBitmap, src, dst, p);

        // top-middle
        src.left = inner.left + 1;
        src.top = 0 + 1;
        src.right = inner.right + 1;
        src.bottom = inner.top + 1;
        dst.set(left + leftColWidth, top, right - rightColWidth, top + topRowHeight);
        c.drawBitmap(mBitmap, src, dst, p);

        // top-right
        src.left = inner.right + 1;
        src.top = 0 + 1;
        src.right = mWidth + 1;
        src.bottom = inner.top + 1;
        dst.set(right - rightColWidth, top, right, top + topRowHeight);
        c.drawBitmap(mBitmap, src, dst, p);

        // /////////// 2nd row

        // if (!true)
        // return;

        // left-middle
        src.left = 0 + 1;
        src.top = inner.top + 1;
        src.right = inner.left + 1;
        src.bottom = inner.bottom + 1;
        dst.set(left, top + topRowHeight, left + leftColWidth, bottom - bottomRowHeight);
        c.drawBitmap(mBitmap, src, dst, p);

        // center
        src.left = inner.left + 1;
        src.top = inner.top + 1;
        src.right = inner.right + 1;
        src.bottom = inner.bottom + 1;
        dst.set(left + leftColWidth, top + topRowHeight, right - rightColWidth, bottom - bottomRowHeight);
        c.drawBitmap(mBitmap, src, dst, p);

        // right-middle
        src.left = inner.right + 1;
        src.top = inner.top + 1;
        src.right = mWidth + 1;
        src.bottom = inner.bottom + 1;
        dst.set(right - rightColWidth, top + topRowHeight, right, bottom - bottomRowHeight);
        c.drawBitmap(mBitmap, src, dst, p);

        // /////////// 3rd row

        // bottom-left
        src.left = 0 + 1;
        src.top = inner.bottom + 1;
        src.right = inner.left + 1;
        src.bottom = mHeight + 1;
        dst.set(left, bottom - bottomRowHeight, left + leftColWidth, bottom);
        c.drawBitmap(mBitmap, src, dst, p);

        // bottom-middle
        src.left = inner.left + 1;
        src.top = inner.bottom + 1;
        src.right = inner.right + 1;
        src.bottom = mHeight + 1;
        dst.set(left + leftColWidth, bottom - bottomRowHeight, right - rightColWidth, bottom);
        c.drawBitmap(mBitmap, src, dst, p);

        // bottom-right
        src.left = inner.right + 1;
        src.top = inner.bottom + 1;
        src.right = mWidth + 1;
        src.bottom = mHeight + 1;
        dst.set(right - rightColWidth, bottom - bottomRowHeight, right, bottom);
        c.drawBitmap(mBitmap, src, dst, p);

        // support for proportional streching and padding...
    }

    public int getHeight() {
        return mBitmap.getHeight();
    }

    public int getWidth() {
        return mBitmap.getWidth();
    }

    public final boolean hasAlpha() {
        return mBitmap.hasAlpha();
    }

    public static boolean isNinePatchChunk(final byte[] chunk) {
        return true;
    }

    public void setPaint(final Paint p) {
        mAlpha = Color.alpha(p.getColor());
    }

    protected final void extractRectFromChunk(final int pos, final Rect toRect) {
        toRect.left = mChunk[pos + 0];
        toRect.top = mChunk[pos + 1];
        toRect.right = mChunk[pos + 2];
        toRect.bottom = mChunk[pos + 3];
    }

    public Rect __getInnerRect() {
        return src;
    }

    public Bitmap __getSourceBitmap() {
        return mBitmap;
    }
}