package cn.aorise.platform.login;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.DrawableRes;

import cn.aorise.common.R;
import cn.aorise.common.core.ui.base.BaseActivity;
import cn.aorise.common.core.util.HandlerUtils;
import cn.aorise.common.databinding.AoriseActivityComponentSplashBinding;

/**
 * 公共启动页面
 * Created by tangjy on 2017/3/17.
 */
public class CustomSplashActivity extends BaseActivity {
    private static final String TAG = CustomSplashActivity.class.getSimpleName();
    private static final int DELAY_MILLIS = 1500;
    // private ActivityPaltformSplashBinding mBinding;
    private AoriseActivityComponentSplashBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 防止用户点击状态栏重新激活app
//        boolean isAppLive = ActivityManager.getInstance().resumeApp(this);
//        super.onCreate(savedInstanceState);
//        if (isAppLive) {
//            finish();
//            return;
//        }

        super.onCreate(savedInstanceState);
        HandlerUtils.runOnUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                openActivity(CustomLoginActivity.class);
                finish();
            }
        }, DELAY_MILLIS);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.aorise_activity_component_splash);
        mBinding.ivSplash.setBackgroundResource(getBackgroundResource());
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 设置背景图片
     *
     * @return
     */
    protected
    @DrawableRes
    int getBackgroundResource() {
        return R.drawable.aorise_bg_splash;
    }
}
