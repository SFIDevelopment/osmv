package at.the.gogo.parkoid.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * An small bar indicating the title of the previous, current and next page to
 * be shown in a ViewPager. Made to resemble the indicator in the Google+
 * application in function.
 * 
 * @author Mark Gjøl @ Zylinc
 */
public class ViewPagerIndicator extends RelativeLayout implements
        OnPageChangeListener {
    private static final int PADDING = 5;

    TextView                 mPrevious;
    TextView                 mCurrent;
    TextView                 mNext;
    int                      mCurItem;

    LinearLayout             mPreviousGroup;
    LinearLayout             mNextGroup;

    int                      mArrowPadding;
    int                      mSize;

    ImageView                mCurrentIndicator;
    ImageView                mPrevArrow;
    ImageView                mNextArrow;

    int[]                    mFocusedTextColor;
    int[]                    mUnfocusedTextColor;

    OnClickListener          mOnClickHandler;

    public interface PageInfoProvider {
        String getTitle(int pos);
    }

    public interface OnClickListener {
        void onNextClicked(View v);

        void onPreviousClicked(View v);

        void onCurrentClicked(View v);
    }

    public void setOnClickListener(final OnClickListener handler) {
        mOnClickHandler = handler;
        mPreviousGroup.setOnClickListener(new OnPreviousClickedListener());
        mCurrent.setOnClickListener(new OnCurrentClickedListener());
        mNextGroup.setOnClickListener(new OnNextClickedListener());
    }

    public int getCurrentPosition() {
        return mCurItem;
    }

    PageInfoProvider mPageInfoProvider;

    public void setPageInfoProvider(final PageInfoProvider pageInfoProvider) {
        mPageInfoProvider = pageInfoProvider;
    }

    public void setFocusedTextColor(final int[] col) {
        System.arraycopy(col, 0, mFocusedTextColor, 0, 3);
        updateColor(0);
    }

    public void setUnfocusedTextColor(final int[] col) {
        System.arraycopy(col, 0, mUnfocusedTextColor, 0, 3);
        mNext.setTextColor(Color.argb(255, col[0], col[1], col[2]));
        mPrevious.setTextColor(Color.argb(255, col[0], col[1], col[2]));
        updateColor(0);
    }

    /**
     * Initialization
     * 
     * @param startPos
     *            The initially selected element in the ViewPager
     * @param size
     *            Total amount of elements in the ViewPager
     * @param pageInfoProvider
     *            Interface that returns page titles
     */
    public void init(final int startPos, final int size,
            final PageInfoProvider pageInfoProvider) {
        setPageInfoProvider(pageInfoProvider);
        mSize = size;
        setText(startPos - 1);
        mCurItem = startPos;

    }

    public ViewPagerIndicator(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        addContent();
    }

    public ViewPagerIndicator(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        addContent();
    }

    public ViewPagerIndicator(final Context context) {
        super(context);
        addContent();
    }

    /**
     * Add drawables for arrows
     * 
     * @param prev
     *            Left pointing arrow
     * @param next
     *            Right pointing arrow
     */
    public void setArrows(final Drawable prev, final Drawable next) {
        mPrevArrow = new ImageView(getContext());
        mPrevArrow.setImageDrawable(prev);

        mNextArrow = new ImageView(getContext());
        mNextArrow.setImageDrawable(next);

        final LinearLayout.LayoutParams arrowLayoutParams = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        arrowLayoutParams.gravity = Gravity.CENTER;

        mPreviousGroup.removeAllViews();
        mPreviousGroup.addView(mPrevArrow, arrowLayoutParams);
        mPreviousGroup.addView(mPrevious, arrowLayoutParams);

        mPrevious.setPadding(ViewPagerIndicator.PADDING, 0, 0, 0);
        mNext.setPadding(0, 0, ViewPagerIndicator.PADDING, 0);

        mArrowPadding = ViewPagerIndicator.PADDING + prev.getIntrinsicWidth();

        mNextGroup.addView(mNextArrow, arrowLayoutParams);
        updateArrows(mCurItem);
    }

    /**
     * Create all views, build the layout
     */
    private void addContent() {
        mFocusedTextColor = new int[] { 0, 0, 0 };
        mUnfocusedTextColor = new int[] { 190, 190, 190 };

        // Text views
        mPrevious = new TextView(getContext());
        mCurrent = new TextView(getContext());
        mNext = new TextView(getContext());

        final RelativeLayout.LayoutParams previousParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        previousParams.addRule(RelativeLayout.ALIGN_LEFT);

        final RelativeLayout.LayoutParams currentParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        currentParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        final RelativeLayout.LayoutParams nextParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        // Groups holding text and arrows
        mPreviousGroup = new LinearLayout(getContext());
        mPreviousGroup.setOrientation(LinearLayout.HORIZONTAL);
        mNextGroup = new LinearLayout(getContext());
        mNextGroup.setOrientation(LinearLayout.HORIZONTAL);

        mPreviousGroup.addView(mPrevious, new LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        mNextGroup.addView(mNext, new LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

        addView(mPreviousGroup, previousParams);
        addView(mCurrent, currentParams);
        addView(mNextGroup, nextParams);

        mPrevious.setSingleLine();
        mCurrent.setSingleLine();
        mNext.setSingleLine();

        mPrevious.setText("previous");
        mCurrent.setText("current");
        mNext.setText("next");

        mPrevious.setClickable(false);
        mNext.setClickable(false);
        mCurrent.setClickable(true);
        mPreviousGroup.setClickable(true);
        mNextGroup.setClickable(true);

        // Set colors
        mNext.setTextColor(Color.argb(255, mUnfocusedTextColor[0],
                mUnfocusedTextColor[1], mUnfocusedTextColor[2]));
        mPrevious.setTextColor(Color.argb(255, mUnfocusedTextColor[0],
                mUnfocusedTextColor[1], mUnfocusedTextColor[2]));
        updateColor(0);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

    @Override
    public void onPageScrolled(int position, final float positionOffset,
            int positionOffsetPixels) {
        positionOffsetPixels = adjustOffset(positionOffsetPixels);
        position = updatePosition(position, positionOffsetPixels);
        setText(position - 1);
        updateColor(positionOffsetPixels);
        updateArrows(position);
        updatePositions(positionOffsetPixels);
    }

    void updatePositions(final int positionOffsetPixels) {
        final int textWidth = mCurrent.getWidth() - mCurrent.getPaddingLeft()
                - mCurrent.getPaddingRight();
        int maxOffset = (this.getWidth() / 2) - (textWidth / 2) - mArrowPadding;
        if (positionOffsetPixels > 0) {
            maxOffset -= this.getPaddingLeft();
            final int offset = Math.min(positionOffsetPixels, maxOffset - 1);
            mCurrent.setPadding(0, 0, 2 * offset, 0);

            // Move previous text out of the way. Slightly buggy.
            /*
             * int overlapLeft = mPreviousGroup.getRight() - mCurrent.getLeft()
             * + mArrowPadding; mPreviousGroup.setPadding(0, 0, Math.max(0,
             * overlapLeft), 0); mNextGroup.setPadding(0, 0, 0, 0);
             */
        } else {
            maxOffset -= this.getPaddingRight();
            final int offset = Math.max(positionOffsetPixels, -maxOffset);
            mCurrent.setPadding(-2 * offset, 0, 0, 0);

            // Move next text out of the way. Slightly buggy.
            /*
             * int overlapRight = mCurrent.getRight() - mNextGroup.getLeft() +
             * mArrowPadding; mNextGroup.setPadding(Math.max(0, overlapRight),
             * 0, 0, 0); mPreviousGroup.setPadding(0, 0, 0, 0);
             */
        }
    }

    /**
     * Hide arrows if we can't scroll further
     * 
     * @param position
     */
    void updateArrows(final int position) {
        if (mPrevArrow != null) {
            mPrevArrow.setVisibility(position == 0 ? View.INVISIBLE
                    : View.VISIBLE);
            mNextArrow.setVisibility(position == (mSize - 1) ? View.INVISIBLE
                    : View.VISIBLE);
        }
    }

    /**
     * Adjust position to be the view that is showing the most.
     * 
     * @param givenPosition
     * @param offset
     * @return
     */
    int updatePosition(final int givenPosition, final int offset) {
        int pos;
        if (offset < 0) {
            pos = givenPosition + 1;
        } else {
            pos = givenPosition;
        }
        return pos;
    }

    /**
     * Fade "currently showing" color depending on it's position
     * 
     * @param offset
     */
    void updateColor(int offset) {
        offset = Math.abs(offset);
        // Initial condition: offset is always 0, this.getWidth is also 0! 0/0 =
        // NaN
        final int width = this.getWidth();
        float fraction = width == 0 ? 0 : offset / (width / 4.0f);
        fraction = Math.min(1, fraction);
        final int r = (int) ((mUnfocusedTextColor[0] * fraction) + (mFocusedTextColor[0] * (1 - fraction)));
        final int g = (int) ((mUnfocusedTextColor[1] * fraction) + (mFocusedTextColor[1] * (1 - fraction)));
        final int b = (int) ((mUnfocusedTextColor[2] * fraction) + (mFocusedTextColor[2] * (1 - fraction)));
        mCurrent.setTextColor(Color.argb(255, r, g, b));
    }

    /**
     * Update text depending on it's position
     * 
     * @param prevPos
     */
    void setText(final int prevPos) {
        if (prevPos < 0) {
            mPrevious.setText("");
        } else {
            mPrevious.setText(mPageInfoProvider.getTitle(prevPos));
        }
        mCurrent.setText(mPageInfoProvider.getTitle(prevPos + 1));
        if ((prevPos + 2) == mSize) {
            mNext.setText("");
        } else {
            mNext.setText(mPageInfoProvider.getTitle(prevPos + 2));
        }
    }

    // Original:
    // 244, 245, 0, 1, 2
    // New:
    // -2, -1, 0, 1, 2
    int adjustOffset(int positionOffsetPixels) {
        // Move offset half width
        positionOffsetPixels += this.getWidth() / 2;
        // Clamp to width
        positionOffsetPixels %= this.getWidth();
        // Center around zero
        positionOffsetPixels -= this.getWidth() / 2;
        return positionOffsetPixels;
    }

    @Override
    public void onPageSelected(final int position) {
        // Reset padding when the page is finally selected (May not be
        // necessary)
        mCurrent.setPadding(0, 0, 0, 0);
        mCurItem = position;
    }

    class OnPreviousClickedListener implements
            android.view.View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (mOnClickHandler != null) {
                mOnClickHandler.onPreviousClicked(ViewPagerIndicator.this);
            }
        }
    }

    class OnCurrentClickedListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (mOnClickHandler != null) {
                mOnClickHandler.onCurrentClicked(ViewPagerIndicator.this);
            }
        }
    }

    class OnNextClickedListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (mOnClickHandler != null) {
                mOnClickHandler.onNextClicked(ViewPagerIndicator.this);
            }
        }
    }
}
