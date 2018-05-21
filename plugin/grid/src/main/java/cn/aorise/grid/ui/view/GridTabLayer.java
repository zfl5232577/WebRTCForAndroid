package cn.aorise.grid.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.aorise.grid.R;
import cn.aorise.grid.ui.adapter.TabPagerAdapter;


/**
 * Created by tangjy on 2017/8/25.
 */
public class GridTabLayer extends LinearLayout implements ITabLayerCycle {
    private static final String TAG = "GridTabLayer";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TabPagerAdapter mAdapter;

    public GridTabLayer(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.grid_view_tablayer, this);
    }

    public GridTabLayer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.grid_view_tablayer, this);
    }

    public GridTabLayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.grid_view_tablayer, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void setup(FragmentManager fm, ArrayList<Fragment> fragments, ArrayList<String> titles) {
        mAdapter = new TabPagerAdapter(fm, fragments, titles);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void setTabName(int index, String tabName) {
        mTabLayout.getTabAt(index).setText(tabName);
    }

    @Override
    public void update(Object object) {

    }
}
