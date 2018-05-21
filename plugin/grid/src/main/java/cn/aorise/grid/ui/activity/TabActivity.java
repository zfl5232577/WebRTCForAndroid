package cn.aorise.grid.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import cn.aorise.grid.R;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.databinding.GridActivityMainBinding;
import cn.aorise.grid.module.network.entity.response.Region;
import cn.aorise.grid.ui.base.GridBaseActivity;
import cn.aorise.grid.ui.fragment.TabFragment;


public class TabActivity extends GridBaseActivity implements View.OnClickListener {
    private GridActivityMainBinding mBinding;
    private int mNumber = 0;
    private TabFragment mTabFragment;
    private Region mRegion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_activity_tab);
        Toolbar toolBar = getToolBar();
        toolBar.setBackgroundResource(R.drawable.grid_bg_toolbar);
        Intent intent = getIntent();
        mRegion = intent.getParcelableExtra(Constant.TransportKey.REGION_RID_KEY);
        setToolBarTitle(mRegion.name);
        toolBar.setNavigationOnClickListener(this);
        setFragment();
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {

    }

    private void setFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mTabFragment = TabFragment.newInstance(this);
        fragmentManager.beginTransaction()
                .replace(R.id.tab_container,
                        mTabFragment, Integer.toString(++mNumber))
                .addToBackStack(null)
                .commit();
        mTabFragment.update(mRegion.id);
    }

    @Override
    protected void initEvent() {
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(Constant.EventMessage.EVENTBUS_ISREFRESH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTabFragment.stopRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTabFragment.startRefresh();
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
