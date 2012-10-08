package org.outlander.views;

import org.outlander.views.SectionedListViewAdapter.HasMorePagesListener;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A ListView that maintains a header pinned at the top of the list. The pinned
 * header can be pushed up and dissolved as needed. It also supports pagination
 * by setting a custom view as the loading indicator.
 */
public class SectionedListView extends ListView implements HasMorePagesListener {

    public static final String       TAG                = SectionedListView.class.getSimpleName();

    View                             listFooter;
    boolean                          footerViewAttached = false;

    private View                     mHeaderView;
    private boolean                  mHeaderViewVisible;

    private int                      mHeaderViewWidth;
    private int                      mHeaderViewHeight;

    private SectionedListViewAdapter adapter;

    public void setPinnedHeaderView(final View view) {
        mHeaderView = view;

        // Disable vertical fading when the pinned header is present
        // TODO change ListView to allow separate measures for top and bottom
        // fading edge;
        // in this particular case we would like to disable the top, but not the
        // bottom edge.
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
        }
    }

    public void configureHeaderView(final int position) {
        if (mHeaderView == null) {
            return;
        }

        final int state = adapter.getPinnedHeaderState(position);
        switch (state) {
            case SectionedListViewAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case SectionedListViewAdapter.PINNED_HEADER_VISIBLE: {
                adapter.configurePinnedHeader(mHeaderView, position, 255);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            }

            case SectionedListViewAdapter.PINNED_HEADER_PUSHED_UP: {
                final View firstView = getChildAt(0);
                if (firstView != null) {
                    final int bottom = firstView.getBottom();
                    final int headerHeight = mHeaderView.getHeight();
                    int y;
                    int alpha;
                    if (bottom < headerHeight) {
                        y = (bottom - headerHeight);
                        alpha = (255 * (headerHeight + y)) / headerHeight;
                    }
                    else {
                        y = 0;
                        alpha = 255;
                    }
                    adapter.configurePinnedHeader(mHeaderView, position, alpha);
                    if (mHeaderView.getTop() != y) {
                        mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
                    }
                    mHeaderViewVisible = true;
                }
                break;
            }
        }
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    public SectionedListView(final Context context) {
        super(context);
    }

    public SectionedListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SectionedListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLoadingView(final View listFooter) {
        this.listFooter = listFooter;
    }

    public View getLoadingView() {
        return listFooter;
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        if (!(adapter instanceof SectionedListViewAdapter)) {
            throw new IllegalArgumentException(SectionedListView.class.getSimpleName() + " must use adapter of type "
                    + SectionedListViewAdapter.class.getSimpleName());
        }

        // previous adapter
        if (this.adapter != null) {
            this.adapter.setHasMorePagesListener(null);
            this.setOnScrollListener(null);
        }

        this.adapter = (SectionedListViewAdapter) adapter;
        ((SectionedListViewAdapter) adapter).setHasMorePagesListener(this);
        this.setOnScrollListener((SectionedListViewAdapter) adapter);

        final View dummy = new View(getContext());
        super.addFooterView(dummy);
        super.setAdapter(adapter);
        super.removeFooterView(dummy);
    }

    @Override
    public SectionedListViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void noMorePages() {
        if (listFooter != null) {
            this.removeFooterView(listFooter);
        }
        footerViewAttached = false;
    }

    @Override
    public void mayHaveMorePages() {
        if (!footerViewAttached && (listFooter != null)) {
            this.addFooterView(listFooter);
            footerViewAttached = true;
        }
    }

    public boolean isLoadingViewVisible() {
        return footerViewAttached;
    }
}
