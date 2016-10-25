package com.wilson.easybanner;

import android.content.Context;
import android.widget.Scroller;


public class EasyScroller extends Scroller {

    private int mDuration;

    public EasyScroller(Context context, int duration) {
        super(context);
        this.mDuration = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy,mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
