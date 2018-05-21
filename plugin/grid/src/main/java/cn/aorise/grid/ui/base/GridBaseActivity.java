package cn.aorise.grid.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.ui.base.BaseActivity;
import cn.aorise.common.core.util.AoriseLog;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.grid.R;
import cn.aorise.grid.common.DialogUtil;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.module.network.entity.response.Session;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.activity.LoginActivity;
import cn.aorise.webrtc.chat.ChatClient;


/**
 * Created by tangjy on 2016/3/14 0014.
 */
public abstract class GridBaseActivity extends BaseActivity {
    private static final String TAG = GridBaseActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void showLoadingDialog() {
        super.showLoadingDialog();
        AoriseLog.i(TAG, "showLoadingDialog");
    }

    @Override
    public void dismissLoadingDialog() {
        super.dismissLoadingDialog();
        AoriseLog.i(TAG, "dismissLoadingDialog");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loginConflict(String data) {
        if (Constant.EventMessage.EVENTBUS_LOGIN_CONFLICT.equals(data)) {
            DialogUtil.showDialog(GridBaseActivity.this, getString(R.string.grid_dialog_conflict_login_title), getString(R.string.grid_dialog_conflict_login_content), getString(R.string.grid_dialog_conflict_login_exit), getString(R.string.grid_dialog_conflict_login_again), R.color.grid_txt_default, R.color.grid_txt_dialog_right, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.tv_dialog_left) {
                        if (!TextUtils.isEmpty(SPUtils.getInstance().getString(Constant.SPCache.USER, ""))) {
                            Session session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
                            User user = session.getUser();
                            if (user != null) {
                                ChatClient.getInstance().logout();
                            }
                        }
                        SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
                        DialogUtil.dismissDialog();
                        GridBaseActivity.this.finish();
                        ActivityManager.getInstance().appExit(getApplicationContext());
                    } else if (v.getId() == R.id.tv_dialog_right) {
                        if (!TextUtils.isEmpty(SPUtils.getInstance().getString(Constant.SPCache.USER, ""))) {
                            Session session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
                            User user = session.getUser();
                            if (user != null) {
                                ChatClient.getInstance().logout();
                            }
                        }
                        SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
                        DialogUtil.dismissDialog();
                        ActivityManager.getInstance().finishAllActivity();
                        openActivity(LoginActivity.class);
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
