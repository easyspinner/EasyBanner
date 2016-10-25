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
    private boolean mIsUserCanScroll = true;
    private boolean mAutoScroll = true;
    private int mPageDuration = 3000; //单页驻留时间
    private int mPageChangeInterval = 800; //

    private int mDotDrawable = R.drawable.banner_dot_selector;

    private EasyViewPager mViewPager;

    private SparseArray<ImageView> mCachedViews = new SparseArray<>();

    private EasyAdapter mAdapterListener;

    private List<? extends Object> mModels;

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

        ta.recycle();

    }

    private void initIndicators(){

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams rlParams = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
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
        for(int i = 0 ;i<mModels.size();i++)
        {
            dotView = new ImageView(getContext());
            dotView.setLayoutParams(dotParams);
            dotView.setBackgroundResource(R.drawable.banner_dot_selector);
            mIndicatorLayout.addView(dotView);
        }
        changeDot(0);

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
            mViewPager.setCurrentItem(0,true);
            startAutoScroll();
        }else
            changeDot(0);

    }


    public void setDatas(List<? extends Object> dataSource)
    {
        ImageView imageView;
        LinearLayout.LayoutParams lyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mModels = dataSource;
        for(int i = 0;i<dataSource.size();i++)
        {
            imageView = new ImageView(getContext());
            imageView.setLayoutParams(lyParams);
            mCachedViews.append(i,imageView);
        }
        initViewPagers();
        initIndicators();
    }


    private void changeDot(int position)
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

        mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mPageDuration);

    }

    public void setAdapterListener(EasyAdapter adapter){
        this.mAdapterListener = adapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        changeDot(position%mCachedViews.size());
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
                mAdapterListener.fillBannerItem(EasyBanner.this,view,mModels == null?null:mModels.get(realPosition),realPosition);
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
        void fillBannerItem(EasyBanner banner, View view, Object model, int position);
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
