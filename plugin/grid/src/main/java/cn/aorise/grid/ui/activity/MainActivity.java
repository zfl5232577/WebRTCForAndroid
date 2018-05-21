package cn.aorise.grid.ui.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.module.network.APICallback;
import cn.aorise.common.core.module.network.APIObserver;
import cn.aorise.common.core.module.network.RxSchedulers;
import cn.aorise.common.core.util.AoriseLog;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.grid.R;
import cn.aorise.grid.common.GlideCircleTransform;
import cn.aorise.grid.common.Utils;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.databinding.GridActivityMainBinding;
import cn.aorise.grid.module.cache.UserInfoCache;
import cn.aorise.grid.module.network.API;
import cn.aorise.grid.module.network.GridApiService;
import cn.aorise.grid.module.network.entity.response.Region;
import cn.aorise.grid.module.network.entity.response.Session;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.base.GridBaseActivity;
import cn.aorise.grid.ui.fragment.TabFragment;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.signal.SignalMessage;
import okhttp3.ResponseBody;
import retrofit2.Response;


public class MainActivity extends GridBaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // 时间间隔
    private static final long EXIT_INTERVAL = 2000L;
    // 需要监听几次点击事件数组的长度就为几
    // 如果要监听双击事件则数组长度为2，如果要监听3次连续点击事件则数组长度为3...
    private long[] mHints = new long[2];

    private GridActivityMainBinding mBinding;

    private int mNumber = 0;
    private long mExitTime;

    private ImageView mIndicatorCloseView;
    private ImageView mIndicatorExitView;
    private View mHeaderView;
    private TextView mStatusView;
    private TextView mNameView;
    private ImageView mAvatarView;

    private int mRegionRid;
    private User user;
    private Session session;
    private TabFragment mTabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        getInitData();
    }

    @Override
    protected void initView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.grid_activity_main);
        setTitleCenter(Gravity.CENTER);
        getToolBar().setNavigationIcon(null);
        getToolBar().setBackgroundResource(R.drawable.grid_bg_toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, getToolBar(), R.string.grid_navigation_drawer_open, R.string.grid_navigation_drawer_close);
        mBinding.drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        gotoTabFragment(null);

        mIndicatorCloseView = (ImageView) MenuItemCompat.getActionView(mBinding.navView.getMenu().
                findItem(R.id.nav_close));
        mIndicatorExitView = (ImageView) MenuItemCompat.getActionView(mBinding.navView.getMenu().
                findItem(R.id.nav_exit));
        mIndicatorCloseView.setImageResource(R.drawable.grid_ic_unfold);
        mIndicatorExitView.setImageResource(R.drawable.grid_ic_unfold);

        mHeaderView = mBinding.navView.inflateHeaderView(R.layout.grid_nav_header_main);
        mNameView = (TextView) mHeaderView.findViewById(R.id.tv_name);
        mStatusView = (TextView) mHeaderView.findViewById(R.id.tv_status);
        mAvatarView = (ImageView) mHeaderView.findViewById(R.id.iv_avatar);

        mBinding.navView.setItemIconTintList(null);
        mBinding.navView.setItemTextColor(null);

        session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
        if (session != null) {
            user = session.getUser();
            setUserInfo();
        }
    }

    private void setUserInfo() {
        mBinding.navView.getMenu().findItem(R.id.nav_name).setTitle(user.name);
        mBinding.navView.getMenu().findItem(R.id.nav_phone).setTitle(user.phone);
        mBinding.navView.getMenu().findItem(R.id.nav_email).setTitle(user.mail);
        mBinding.navView.getMenu().findItem(R.id.nav_identity).setTitle(session.role);
        mNameView.setText(user.username);

    }

    @Override
    protected void initEvent() {
        mBinding.navView.setNavigationItemSelectedListener(this);
        Utils.WifiNeverDormancy(this);
    }


    private void getInitData() {
        GridApiService.Factory.create().getCurrentPopedom()
                .compose(RxSchedulers.compose(this, this.<Region>bindToLifecycle()))
                .subscribe(new APIObserver<Region>(this, new APICallback<Region>() {
                    @Override
                    public void onError(Throwable throwable) {
                        AoriseLog.i(TAG, "===========> onError");
                        handleError();
                    }

                    @Override
                    public void onNext(Region regionResponse) {
                        handleSuccess(regionResponse);
                    }
                }));
    }

    private void handleError() {
        if (!Constant.hasNetWork) {
            showToast(R.string.grid_network_setting);
        } else {
            showToast(R.string.grid_get_information_fail);
        }
    }

    //处理成功逻辑
    private void handleSuccess(Region regionResponse) {
        mBinding.navView.getMenu().findItem(R.id.nav_location).setTitle(regionResponse.name);
        if (user.isAdmin == Constant.IS_ADMIN) {
            setToolBarTitle(getString(R.string.grid_person_admin_name));
        } else {
            setToolBarTitle(regionResponse.name);
        }
        mRegionRid = regionResponse.id;
        mTabFragment.update(mRegionRid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshNetWork(Boolean hasNetWork) {
        if (hasNetWork) {
            getInitData();
        }
    }


    private void logout() {
        GridApiService.Factory.create().logout()
                .compose(RxSchedulers.compose(this, this.<Response<ResponseBody>>bindToLifecycle()))
                .subscribe(new APIObserver<Response<ResponseBody>>(this, new APICallback<Response<ResponseBody>>() {
                    @Override
                    public void onError(Throwable throwable) {
                        AoriseLog.e(TAG, "===========> onError");
                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        AoriseLog.i(TAG, "===========> onNext");
                        SPUtils.getInstance().put(Constant.SPCache.LOGIN, false);
                        UserInfoCache.removeCookie();
                        ChatClient.getInstance().logout();
                        ActivityManager.getInstance().finishAllActivity();
                        openActivity(LoginActivity.class);
                    }
                }));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshStatus(String status) {
        if (Constant.Status.ON_LINE.equals(status)) {
            mStatusView.setText(getResources().getString(R.string.grid_person_status1));
            mStatusView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.grid_ic_online_status), null, null, null);
            mStatusView.setCompoundDrawablePadding(7);
            Glide.with(MainActivity.this).load(API.BASE_URL + user.imgurl)
                    .bitmapTransform(new GlideCircleTransform(MainActivity.this))
                    .error(R.drawable.grid_def_online_favicon)
                    .placeholder(R.drawable.grid_def_online_favicon)
                    .crossFade(500)
                    .into(mAvatarView);
        } else if (Constant.Status.OFF_Line.equals(status)) {
            mStatusView.setText(getResources().getString(R.string.grid_person_status2));
            mStatusView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.grid_ic_offline_status), null, null, null);
            mStatusView.setCompoundDrawablePadding(7);
            Glide.with(MainActivity.this).load(API.BASE_URL + user.imgurl)
                    .bitmapTransform(new GlideCircleTransform(MainActivity.this))
                    .error(R.drawable.grid_def_offline_favicon)
                    .placeholder(R.drawable.grid_def_offline_favicon)
                    .crossFade(500)
                    .into(mAvatarView);
        } else if (Constant.Status.BUSY.equals(status)) {
            mStatusView.setText(getResources().getString(R.string.grid_person_status3));
            mStatusView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.grid_ic_busy_status), null, null, null);
            mStatusView.setCompoundDrawablePadding(7);
            Glide.with(MainActivity.this).load(API.BASE_URL + user.imgurl)
                    .bitmapTransform(new GlideCircleTransform(MainActivity.this))
                    .error(R.drawable.grid_def_online_favicon)
                    .placeholder(R.drawable.grid_def_online_favicon)
                    .crossFade(500)
                    .into(mAvatarView);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPushCall(SignalMessage signalMessage) {
        if (!Constant.SignalType.SIGNAL_PUSH.equals(signalMessage.getType())) {
            return;
        }
        if ("1".equals(signalMessage.getData())) {
            showToast("推送成功");
        } else {
            showToast("推送失败");
        }
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(false);
//            int count = getSupportFragmentManager().getBackStackEntryCount();
//            AoriseLog.i(TAG, "onBackPressed count = " + count);
//            if (2 == count) {
//                // 将mHints数组内的所有元素左移一个位置
//                System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
//                // 获得当前系统已经启动的时间
//                mHints[mHints.length - 1] = SystemClock.uptimeMillis();
//                if ((SystemClock.uptimeMillis() - mHints[0]) > EXIT_INTERVAL) {
//                    showToast(getString(R.string.aorise_label_double_exit));
//                } else {
//                    exit();
//                }
//            } else {
//                super.onBackPressed();
//            }
            /*if ((System.currentTimeMillis() - mExitTime) > EXIT_INTERVAL) {
                showToast(getString(R.string.aorise_label_double_exit));
                mExitTime = System.currentTimeMillis();
            } else {
                exit();
                super.onBackPressed();
            }*/
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_close) {
            closeDrawer();
            exit();
        } else if (id == R.id.nav_exit) {
            logout();
        }

        return true;
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void exit() {
        finish();
        //ActivityManager.getInstance().appExit(getApplicationContext());
    }


    public void gotoTabFragment(Object object) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mTabFragment = TabFragment.newInstance(this);
        fragmentManager.beginTransaction()
                .replace(R.id.tab_container,
                        mTabFragment, Integer.toString(mNumber))
                .addToBackStack(null)
                .commit();
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
