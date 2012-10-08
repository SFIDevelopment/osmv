package org.outlander.views;

import org.outlander.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class PopupWindowWrapper {

    // v.getMeasuredHeight()

    public PopupWindow createPopupAndShow(final Context context, final View parentView, final View contentView, final int width, final int height,
            final int gravity) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View baseView = inflater.inflate(R.layout.popupmainlayout, null, false);

        final LinearLayout contentframe = (LinearLayout) baseView.findViewById(R.id.popupcontent);

        // add content
        contentframe.addView(contentView);

        final Button closeButton = (Button) baseView.findViewById(R.id.closebtn);

        final PopupWindow popupwindow = new PopupWindow(baseView, width, height);

        closeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                popupwindow.dismiss();
            }
        });

        // popupwindow.setBackgroundDrawable(new BitmapDrawable());
        // popupwindow.setOutsideTouchable(true);
        // popupwindow.showAsDropDown(btnSelectWeight);
        //

        popupwindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {

                return false;
            }

        });

        popupwindow.setAnimationStyle(R.style.Animations_PopDownMenu_Center);

        popupwindow.showAtLocation(parentView, gravity, 10, 10);

        return popupwindow;
    }

    public PopupWindow createPopup(final Context context, final View parentView, final View contentView, final int percentWidth, final int percentHeight,
            final int gravity) {

        final int width = (parentView.getMeasuredWidth() / 100) * percentWidth;
        final int height = (parentView.getMeasuredHeight() / 100) * percentHeight;

        return createPopup(context, parentView, contentView, width, height, gravity);

    }

}
