package cn.aorise.grid.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.aorise.common.core.module.network.APICallback;
import cn.aorise.common.core.module.network.APIObserver;
import cn.aorise.common.core.module.network.RxSchedulers;
import cn.aorise.grid.R;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.databinding.GridFragmentTabBinding;
import cn.aorise.grid.module.network.GridApiService;
import cn.aorise.grid.module.network.entity.response.Region;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.base.GridBaseFragment;


/**
 * Created by tangjy on 2017/8/25.
 */
public class TabFragment extends GridBaseFragment {
    private static final String TAG = "TabFragment";
    private static Context mContext;
    private GridFragmentTabBinding mBinding;
    private ArrayList<Fragment> mFragments;
    private ArrayList<String> mTitles;
    private int mRegionRid;
    private List<Region> mRegionList;
    private List<User> mUserList;
    private PopedomFragment mPopedomFragment;
    private PersonalFragment mPersonalFragment;
    public boolean refreshRunning;
    private int refreshTime = 5000;
    private Handler handler = new Handler();

    @Subscribe
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mContext = getContext();
        mUserList = new ArrayList<>();
        mPopedomFragment = PopedomFragment.newInstance();
        mPersonalFragment = PersonalFragment.newInstance();
        mFragments.add(mPersonalFragment);
        mFragments.add(mPopedomFragment);
        initEvent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.grid_fragment_tab, container, false);
        mBinding.gridTabLayer.setup(getChildFragmentManager(), mFragments, mTitles);
        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void initUserData() {
        GridApiService.Factory.create().getUser(mRegionRid)
                .compose(RxSchedulers.compose(this.<List<User>>bindToLifecycle()))
                .subscribe(new APIObserver<List<User>>(getBaseActivity(), new APICallback<List<User>>() {
                    @Override
                    public void onError(Throwable throwable) {
                    }

                    @Override
                    public void onNext(List<User> userList) {
                        handleUserData(userList);
						startRefresh();
                    }
                }));
    }

    private void handleError() {
        if (!Constant.hasNetWork) {
            Toast.makeText(getContext(), getString(R.string.grid_network_setting), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.grid_get_information_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUserData(List<User> userList) {
        mUserList = userList;
        setTabName(0, getString(R.string.grid_tab_personal) + "(" + mUserList.size() + ")");
        mPersonalFragment.update(userList);
    }

    private void initPopedonData() {
        GridApiService.Factory.create().getChildPopedom(mRegionRid)
                .compose(RxSchedulers.compose(getBaseActivity(), this.<List<Region>>bindToLifecycle()))
                .subscribe(new APIObserver<List<Region>>(getBaseActivity(), new APICallback<List<Region>>() {
                    @Override
                    public void onError(Throwable throwable) {
                    }

                    @Override
                    public void onNext(List<Region> regions) {
                        handlePopedomData(regions);
                    }
                }));
    }

    private void handlePopedomData(List<Region> regions) {
        mRegionList = regions;
        setTabName(1, getString(R.string.grid_tab_popedom) + "(" + mRegionList.size() + ")");
        mPopedomFragment.update(regions);
    }

    // 动态设置tablayout的TabName
    private void setTabName(int index, String name) {
        mBinding.gridTabLayer.setTabName(index, name);
    }

    private void initEvent() {
        if (mUserList == null) {
            mTitles.add(getString(R.string.grid_tab_personal));
        } else {
            mTitles.add(getString(R.string.grid_tab_personal) + "(" + mUserList.size() + ")");
        }
        if (mRegionList == null) {
            mTitles.add(getString(R.string.grid_tab_popedom));
        } else {
            mTitles.add(getString(R.string.grid_tab_popedom) + "(" + mRegionList.size() + ")");
        }
    }

    public TabFragment() {
        mFragments = new ArrayList<>();
        mTitles = new ArrayList<>();
    }


    public static TabFragment newInstance(Context context) {
        TabFragment fragment = new TabFragment();
        mContext = context;
        return fragment;
    }

    public void update(int regionsRid) {
        mRegionRid = regionsRid;
        initUserData();
        initPopedonData();
    }


    /**
     * 监听网络实时变化
     *
     * @param hasNetWork
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshNetWork(Boolean hasNetWork) {
        if (hasNetWork) {
            mBinding.rvHint.setVisibility(View.GONE);
            //有网络才自动刷新,否则不刷新
            //断开重连,立即刷新并重新开启自动刷新
            update(mRegionRid);
            refreshRunning = false;
            startRefresh();
        } else {
            stopRefresh();
            mBinding.rvHint.setVisibility(View.VISIBLE);
        }
    }

    // 关闭上一层页面 下一层页面的fragment开启定时刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void isRefresh(String refresh) {
        if (refresh.equals(Constant.EventMessage.EVENTBUS_ISREFRESH) && Constant.hasNetWork) {
            refreshRunning = false;
            startRefresh();
        }
    }

    private Runnable refreshRunable = new Runnable() {
        @Override
        public void run() {
            initUserData();
            handler.postDelayed(refreshRunable, refreshTime);
        }
    };


    // 开启自动刷新
    public void startRefresh() {
        synchronized (TabFragment.class) {
            if (!refreshRunning) {
                refreshRunning = true;
                handler.postDelayed(refreshRunable, refreshTime);
            }
        }
    }

    // 关闭自动刷新
    public void stopRefresh() {
        synchronized (TabFragment.class) {
            if (refreshRunning) {
                refreshRunning = false;
                handler.removeCallbacksAndMessages(null);
            }
        }
    }
}
