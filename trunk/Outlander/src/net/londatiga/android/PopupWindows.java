package net.londatiga.android;

import org.outlander.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Custom popup window.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 */
public class PopupWindows {

    protected static final int ANIM_GROW_FROM_LEFT   = 1;
    protected static final int ANIM_GROW_FROM_RIGHT  = 2;
    protected static final int ANIM_GROW_FROM_CENTER = 3;
    protected static final int ANIM_REFLECT          = 4;
    protected static final int ANIM_AUTO             = 5;

    protected Context          mContext;
    protected PopupWindow      mWindow;
    protected View             mRootView;
    protected Drawable         mBackground           = null;
    protected WindowManager    mWindowManager;

    /**
     * Constructor.
     * 
     * @param context
     *            Context
     */
    public PopupWindows(final Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);

        mWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mWindow.dismiss();

                    return true;
                }

                return false;
            }
        });

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * On dismiss
     */
    protected void onDismiss() {
    }

    /**
     * On show
     */
    protected void onShow() {
    }

    /**
     * On pre show
     */
    protected void preShow() {
        if (mRootView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }

        onShow();

        if (mBackground == null) {
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        else {
            mWindow.setBackgroundDrawable(mBackground);
        }

        mWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);

        mWindow.setContentView(mRootView);
    }

    /**
     * Set background drawable.
     * 
     * @param background
     *            Background drawable
     */
    public void setBackgroundDrawable(final Drawable background) {
        mBackground = background;
    }

    /**
     * Set content view.
     * 
     * @param root
     *            Root view
     */
    public void setContentView(final View root) {
        mRootView = root;

        mWindow.setContentView(root);
    }

    /**
     * Set content view.
     * 
     * @param layoutResID
     *            Resource id
     */
    public void setContentView(final int layoutResID) {
        final LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(layoutResID, null));
    }

    /**
     * Set listener on window dismissed.
     * 
     * @param listener
     */
    public void setOnDismissListener(final PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    /**
     * Dismiss the popup window.
     */
    public void dismiss() {
        mWindow.dismiss();
    }

    /**
     * Set animation style
     * 
     * @param screenWidth
     *            screen width
     * @param requestedX
     *            distance from left edge
     * @param onTop
     *            flag to indicate where the popup should be displayed. Set TRUE
     *            if displayed on top of anchor view and vice versa
     */
    public void setAnimationStyle(final int animStyle, final int screenWidth, final int requestedX, final boolean onTop) {
        // final int arrowPos = requestedX - (mArrowUp.getMeasuredWidth() / 2);

        switch (animStyle) {
            case ANIM_GROW_FROM_LEFT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
                break;

            case ANIM_REFLECT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
                break;

            case ANIM_AUTO:
                // if (arrowPos <= (screenWidth / 4)) {
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                // }
                // else if ((arrowPos > (screenWidth / 4)) && (arrowPos < (3 *
                // (screenWidth / 4)))) {
                // mWindow.setAnimationStyle((onTop) ?
                // R.style.Animations_PopUpMenu_Center :
                // R.style.Animations_PopDownMenu_Center);
                // }
                // else {
                // mWindow.setAnimationStyle((onTop) ?
                // R.style.Animations_PopUpMenu_Right :
                // R.style.Animations_PopDownMenu_Right);
                // }

                break;
        }
    }

}