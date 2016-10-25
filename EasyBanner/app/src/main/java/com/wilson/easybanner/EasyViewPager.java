package com.wilson.easybanner;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @ClassName EasyViewPager
 * @date 2016/10/24 13:49
 * @author wilson
 * @Description 自定义viewPager
 * @modifier
 * @modify_time
 */

public class EasyViewPager extends ViewPager {

    private boolean isCanScroll = true;

    public EasyViewPager(Context context) {
        this(context,null);
    }

    public EasyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置是否允许用户手指滑动
     *
     * @param allowUserScrollable true表示允许跟随用户触摸滑动，false反之
     */
    public void setAllowUserScrollable(boolean allowUserScrollable) {
        isCanScroll = allowUserScrollable;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isCanScroll?super.onTouchEvent(ev):isCanScroll;
    }


    /**
     * @Description 
     * @param millSeconds 设置viewpager切换速率
     * @return 
     * @throws 
     */
    
    public void setPageScrollVelocity(int millSeconds){

        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            scrollerField.set(this,new EasyScroller(getContext(),millSeconds));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * @Description
     * @param position 设置viewpager到指定页面
     * @return
     * @throws
     */

    public void setPageScrollToPosition(int position){
        Class viewpagerClass = ViewPager.class;
        try {
            Method setCurrentItemInternalMethod = viewpagerClass.getDeclaredMethod("setCurrentItemInternal", int.class, boolean.class, boolean.class);
            setCurrentItemInternalMethod.setAccessible(true);
            setCurrentItemInternalMethod.invoke(this, position, true, true);
            ViewCompat.postInvalidateOnAnimation(this);
        } catch (Exception e) {
        }

    }
}
