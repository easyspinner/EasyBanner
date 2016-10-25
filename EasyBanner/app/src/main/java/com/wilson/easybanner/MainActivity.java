package com.wilson.easybanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EasyBanner.EasyAdapter{

    private EasyBanner mBanner;

    private List<Integer> mModels = new ArrayList<>();

    private String[] imgs = {"http://img2.3lian.com/2014/c7/12/d/77.jpg",
            "http://pic3.bbzhi.com/fengjingbizhi/gaoqingkuanpingfengguangsheyingps/show_fengjingta_281299_11.jpg",
            "http://e.hiphotos.baidu.com/image/h%3D200/sign=1e8feb8facd3fd1f2909a53a004f25ce/c995d143ad4bd113eff4cf935eafa40f4bfb0551.jpg",
            "http://www.bz55.com/uploads1/allimg/120312/1_120312100435_8.jpg",
            "http://img3.iqilu.com/data/attachment/forum/201308/21/192654ai88zf6zaa60zddo.jpg"};

    private int[] imgs2 = {R.drawable.pic1,R.drawable.pic2,
                          R.drawable.pic3,R.drawable.pic4,
            R.drawable.pic5};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBanner = (EasyBanner)findViewById(R.id.easy_banner);
        loadDatas();

    }

    private void loadDatas()
    {
        for(int res:imgs2)
        {
            mModels.add(res);
        }
        mBanner.setAdapterListener(this);
//        mBanner.setDatas(mModels);
        mBanner.setDatas(Arrays.asList(imgs));
    }

    @Override
    public void fillBannerItem(EasyBanner banner, View view, Object model, int position) {
        Log.d("EasyBanner","fillBannerItem:"+(String) model);
        Glide.with(MainActivity.this)
                .load(imgs2[position])
                .centerCrop()
                .placeholder(R.drawable.pic1)
                .into((ImageView)view);
    }
}
