package cn.aorise.webrtc.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.ui.base.BaseActivity;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.webrtc.R;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.chat.PushHelper;
import cn.aorise.webrtc.chat.SignalManager;
import cn.aorise.webrtc.common.DialogUtil;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DuplicateActivity extends BaseActivity {

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        DialogUtil.showDialog(this, getString(getApplicationContext().getApplicationInfo().labelRes)+getString(R.string.grid_dialog_conflict_login_title),
                getString(R.string.grid_dialog_conflict_login_content),getString(R.string.grid_dialog_conflict_login_exit),
                getString(R.string.grid_dialog_conflict_login_again), R.color.grid_txt_default, R.color.grid_txt_dialog_right,
                v -> {
                    if (v.getId() == R.id.tv_dialog_left) {
                        if (ChatClient.getInstance().getOnDuplicateConnectionCallback() == null) {
                            ChatClient.getInstance().logout();
                            SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
                            ActivityManager.getInstance().appExit(this);
                        } else {
                            ChatClient.getInstance().getOnDuplicateConnectionCallback().onExit();
                        }
                    } else if (v.getId() == R.id.tv_dialog_right) {
                        if (ChatClient.getInstance().getOnDuplicateConnectionCallback() == null) {
                            PushHelper.getInstance().setAlias(this, Constant.LoginInfo.user.getUserName(), Constant.ALIAS_TYPE.USER);
                            ChatClient.getInstance().getSignalManager().reconnect(true);
                        } else {
                            ChatClient.getInstance().getOnDuplicateConnectionCallback().onReLogin();
                        }
                    }
                    DialogUtil.dismissDialog();
                    finish();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        DialogUtil.dismissDialog();
        finish();
    }
}
