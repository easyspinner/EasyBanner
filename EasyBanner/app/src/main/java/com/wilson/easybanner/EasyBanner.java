package com.wilson.easybanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.List;


public class EasyBanner extends RelativeLayout implements ViewPager.OnPageChangeListener {


    public static final int PAGE_SIZE = 5;

    public static final int MSG_AUTO_SCROLL = 0x10;

    //indicator position 指示点的位置
    public static final int DOT_MIDDLE = 0;
    public static final int DOT_LEFT = 1;
    public static final int DOT_RIGHT = 2;

    private int mIndicatorGravity = DOT_MIDDLE;

    private boolean mIsUserCanScroll = true;

    private boolean mAutoScroll = true;

    private int mPageDuration = 3000; //单页驻留时间

    private int mPageChangeInterval = 800; //翻页速度

    private int mDotDrawable = R.drawable.banner_dot_selector;

    private EasyViewPager mViewPager;

    private SparseArray<ImageView> mCachedViews = new SparseArray<>();

    private EasyAdapter mAdapterListener;

    private LinearLayout mIndicatorLayout;

    private AutoScrollHandler mHandler = new AutoScrollHandler(this);

    public EasyBanner(Context context) {
        this(context,null);
    }

    public EasyBanner(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public EasyBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context,attrs);
    }


    private void initAttrs(Context context,AttributeSet attributeSet){

        TypedArray ta = context.obtainStyledAttributes(attributeSet,R.styleable.EasyBanner);

        mDotDrawable = ta.getResourceId(R.styleable.EasyBanner_dotDrawable,mDotDrawable);
        mAutoScroll = ta.getBoolean(R.styleable.EasyBanner_autoPlay, mAutoScroll);
        mPageDuration = ta.getInt(R.styleable.EasyBanner_duration, mPageDuration);
        mPageChangeInterval = ta.getInt(R.styleable.EasyBanner_interval,mPageChangeInterval);
        mIsUserCanScroll = ta.getBoolean(R.styleable.EasyBanner_userScroll,mIsUserCanScroll);
        mIndicatorGravity = ta.getInt(R.styleable.EasyBanner_dotPosition,mIndicatorGravity);

        ta.recycle();

    }

    private void initIndicators(){

        if(mCachedViews.size()<2) return;

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams rlParams = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        switch (mIndicatorGravity){

            case DOT_LEFT:
                rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case DOT_RIGHT:
                rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            default:
                rlParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;

        }
        rlParams.setMargins(20,0,20,20);
        addView(relativeLayout,rlParams);

        mIndicatorLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        mIndicatorLayout.setLayoutParams(params);
        relativeLayout.addView(mIndicatorLayout);

        ImageView dotView;
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dotParams.setMargins(10,10,0,0);
        for(int i = 0 ;i<mCachedViews.size();i++)
        {
            dotView = new ImageView(getContext());
            dotView.setLayoutParams(dotParams);
            dotView.setBackgroundResource(R.drawable.banner_dot_selector);
            mIndicatorLayout.addView(dotView);
        }
        changeIndicatorPosition(0);

    }

    private void initViewPagers()
    {
        mViewPager = new EasyViewPager(getContext());
        mViewPager.setAdapter(new EasyPagerAdapter());
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAllowUserScrollable(mIsUserCanScroll);
        mViewPager.setPageScrollVelocity(mPageChangeInterval);
        addView(mViewPager,0,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if(mAutoScroll){
            int startItem = Integer.MAX_VALUE - Integer.MAX_VALUE%mCachedViews.size();
//            mViewPager.setCurrentItem(0,true);
            startAutoScroll();
        }else
            changeIndicatorPosition(0);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScroll();
    }

    @Deprecated
    public void setDatas(List<? extends Object> dataSource)
    {
        ImageView imageView;
        LinearLayout.LayoutParams lyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        for(int i = 0;i<dataSource.size();i++)
        {
            imageView = new ImageView(getContext());
            imageView.setLayoutParams(lyParams);
            mCachedViews.append(i,imageView);
        }
        if(mCachedViews.size()<2) mAutoScroll = false;
        initViewPagers();
        initIndicators();
    }

    public void setPageSize(int pages){
        ImageView imageView;
        LinearLayout.LayoutParams lyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        for(int i = 0;i<pages;i++)
        {
            imageView = new ImageView(getContext());
            imageView.setLayoutParams(lyParams);
            mCachedViews.append(i,imageView);
        }
        if(mCachedViews.size()<2) mAutoScroll = false;
        initViewPagers();
        initIndicators();


    }


    private void changeIndicatorPosition(int position)
    {
        if(mIndicatorLayout!=null && mCachedViews!=null){
            for(int i = 0;i<mIndicatorLayout.getChildCount();i++)
            {
                mIndicatorLayout.getChildAt(i).setEnabled(false);
            }
            mIndicatorLayout.getChildAt(position).setEnabled(true);
        }
    }

    /**
     * 切换到下一页
     */
    private void switchToNextPage() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    public void startAutoScroll(){

        if(mCachedViews.size()>1)
         mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mPageDuration);

    }

    public void stopAutoScroll(){
        if(mAutoScroll){
            mHandler.removeMessages(MSG_AUTO_SCROLL);
        }
    }

    public void setAdapterListener(EasyAdapter adapter){
        this.mAdapterListener = adapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        changeIndicatorPosition(position%mCachedViews.size());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class EasyPagerAdapter extends PagerAdapter{

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            final int realPosition = position%mCachedViews.size();
            View view = mCachedViews.get(realPosition);
            if (view!=null&&container.equals(view.getParent())) {
                container.removeView(view);
            }
            if(mAdapterListener!=null)
                mAdapterListener.fillBannerItem(EasyBanner.this,view,realPosition);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mAutoScroll?Integer.MAX_VALUE:mCachedViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    public interface EasyAdapter {
        void fillBannerItem(EasyBanner banner, View view, int position);
    }

    public interface OnInterceptScrollEvent{

        void onUserScroll();

    }

    private static class AutoScrollHandler extends Handler{

        final WeakReference<EasyBanner> easyBanners;

        AutoScrollHandler(EasyBanner banner){

            easyBanners = new WeakReference<EasyBanner>(banner);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            EasyBanner banner = easyBanners.get();
            switch (msg.what){
                case MSG_AUTO_SCROLL:
                    if(banner!=null){
                        banner.switchToNextPage();
                        sendEmptyMessageDelayed(MSG_AUTO_SCROLL,banner.mPageDuration);
                    }
                break;


            }
        }
    }
}
