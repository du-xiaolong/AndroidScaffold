package com.dxl.scaffold.ui.wheel.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.dxl.scaffold.ui.wheel.view.WheelView;


/**
 * 手势监听
 */
public final class LoopViewGestureListener extends GestureDetector.SimpleOnGestureListener {

    private final WheelView wheelView;


    public LoopViewGestureListener(WheelView wheelView) {
        this.wheelView = wheelView;
    }

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        wheelView.scrollBy(velocityY);
        return true;
    }
}
