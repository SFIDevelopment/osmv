package net.londatiga.android;

import org.outlander.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Popup window, shows action list as icon and text like the one in Gallery3D
 * app.
 * 
 * @author Lorensius. W. T
 */
public class QuickAction extends PopupWindows {

    private View                      mRootView;
    private ImageView                 mArrowUp;
    private ImageView                 mArrowDown;
    private final LayoutInflater      inflater;
    private ViewGroup                 mTrack;
    // private ScrollView mScroller;
    private OnActionItemClickListener mListener;

    private int                       mChildPos;
    private int                       animStyle;

    /**
     * Constructor.
     * 
     * @param context
     *            Context
     */
    public QuickAction(final Context context) {
        super(context);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setRootViewId(R.layout.popup);

        animStyle = QuickAction.ANIM_AUTO;
        mChildPos = 0;
    }

    /**
     * Set root view.
     * 
     * @param id
     *            Layout resource id
     */
    public void setRootViewId(final int id) {
        mRootView = inflater.inflate(id, null);
        mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);

        mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);
        mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

        // mScroller = (ScrollView) mRootView.findViewById(R.id.scroller);

        setContentView(mRootView);
    }

    /**
     * Set animation style
     * 
     * @param animStyle
     *            animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(final int animStyle) {
        this.animStyle = animStyle;
    }

    /**
     * Set listener for action item clicked.
     * 
     * @param listener
     *            Listener
     */
    public void setOnActionItemClickListener(final OnActionItemClickListener listener) {
        mListener = listener;
    }

    /**
     * Add action item
     * 
     * @param action
     *            {@link ActionItem}
     */
    public void addActionItem(final ActionItem action) {

        final String title = action.getTitle();
        final Drawable icon = action.getIcon();

        final View container = inflater.inflate(R.layout.action_item, null);

        final ImageView img = (ImageView) container.findViewById(R.id.iv_icon);
        final TextView text = (TextView) container.findViewById(R.id.tv_title);

        if (icon != null) {
            img.setImageDrawable(icon);
        }
        else {
            img.setVisibility(View.GONE);
        }

        if (title != null) {
            text.setText(title);
        }
        else {
            text.setVisibility(View.GONE);
        }

        final int pos = mChildPos;

        if (action.isClickable()) {
            container.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (mListener != null) {
                        mListener.onItemClick(pos);
                    }

                    dismiss();
                }
            });
        }
        container.setFocusable(true);
        container.setClickable(true);

        mTrack.addView(container, mChildPos);

        mChildPos++;
    }

    /**
     * Show popup window. Popup is automatically positioned, on top or bottom of
     * anchor view.
     */
    public void show(final View anchor) {

        final int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        final Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

        show(anchor, anchorRect);
    }

    private void show(final View anchor, final Rect anchorRect) {

        preShow();

        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final int rootHeight = mRootView.getMeasuredHeight();
        // final int rootWidth = mRootView.getMeasuredWidth();

        final int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        final int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

        final int arrowWidth = mArrowUp.getMeasuredWidth();
        final int arrowHeight = mArrowUp.getMeasuredHeight();

        final int xPos = (anchorRect.left + (anchorRect.width() >> 1)) - (arrowWidth >> 1);
        final int yPos = anchorRect.top;

        final int popupXPos = xPos - arrowWidth;

        // if (popupXPos + rootWidth > screenWidth) {
        // popupXPos = screenWidth - rootWidth;
        // }
        // TODO: left border

        boolean onTop = false;
        int popupYPos = yPos + arrowHeight;
        if ((popupYPos + rootHeight) > screenHeight) {
            popupYPos = yPos - arrowHeight - rootHeight;
            onTop = true;
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), xPos);
        setAnimationStyle(animStyle, screenWidth, anchorRect.centerX(), onTop);
        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, popupXPos, popupYPos);
    }

    public void show(final View anchor, final int screenX, final int screenY) {

        final int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        final int x = screenX + location[0];
        final int y = screenY + location[1];

        final Rect anchorRect = new Rect(x - 1, y - 1, x + 1, y + 1);
        show(anchor, anchorRect);
    }

    /**
     * Show arrow
     * 
     * @param whichArrow
     *            arrow type resource id
     * @param requestedX
     *            distance from left screen
     */
    private void showArrow(final int whichArrow, final int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        final ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();

        param.leftMargin = arrowWidth / 2;

        hideArrow.setVisibility(View.INVISIBLE);
    }

    /**
     * Listener for item click
     */
    public interface OnActionItemClickListener {

        public abstract void onItemClick(int pos);
    }
}