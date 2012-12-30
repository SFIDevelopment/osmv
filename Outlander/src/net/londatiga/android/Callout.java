package net.londatiga.android;

import org.outlander.R;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.cyrilmottier.polaris.MapCalloutView;

public class Callout extends PopupWindows {

    MapCalloutView calloutView;

    public Callout(Context context) {
        super(context);

        calloutView = new MapCalloutView(context);

        setContentView(calloutView);

    }

    public void setTitle(CharSequence title) {
        calloutView.setTitle(title);
    }

    public void setSubtitle(CharSequence subTitle) {
        calloutView.setSubtitle(subTitle);
    }

    public void show(final View anchor, final int screenX, final int screenY) {

        final int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        final int x = screenX + location[0];
        final int y = screenY + location[1];

        final Rect anchorRect = new Rect(x - 1, y - 1, x + 1, y + 1);
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
    
        final int xPos = (anchorRect.left + (anchorRect.width() >> 1));
        final int yPos = anchorRect.top;

        final int popupXPos = xPos;

        // if (popupXPos + rootWidth > screenWidth) {
        // popupXPos = screenWidth - rootWidth;
        // }
        // TODO: left border

        boolean onTop = false;
        int popupYPos = yPos;
        if ((popupYPos + rootHeight) > screenHeight) {
            popupYPos = yPos - rootHeight;
            onTop = true;
        }

        
        setAnimationStyle(ANIM_GROW_FROM_LEFT,screenWidth, anchorRect.centerX(), onTop);
        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, popupXPos, popupYPos);
    }

}
