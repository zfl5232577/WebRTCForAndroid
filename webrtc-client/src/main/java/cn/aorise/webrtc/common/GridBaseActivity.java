package cn.aorise.webrtc.common;

import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.aorise.common.core.ui.base.BaseActivity;
import cn.aorise.common.core.util.AoriseLog;


/**
 * Created by tangjy on 2016/3/14 0014.
 */
public abstract class GridBaseActivity extends BaseActivity {
    private static final String TAG = GridBaseActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void loginConflict(String data) {
//        if (Constant.EventMessage.EVENTBUS_LOGIN_CONFLICT.equals(data)) {
//            DialogUtil.showDialog(GridBaseActivity.this, getString(R.string.grid_dialog_conflict_login_title), getString(R.string.grid_dialog_conflict_login_content), getString(R.string.grid_dialog_conflict_login_exit), getString(R.string.grid_dialog_conflict_login_again), R.color.grid_txt_default, R.color.grid_txt_dialog_right, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (v.getId() == R.id.tv_dialog_left) {
//                        ChatService.stopService(GridBaseActivity.this);
//                        SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
//                        DialogUtil.dismissDialog();
//                        GridBaseActivity.this.finish();
//                        ActivityManager.getInstance().appExit(getApplicationContext());
//                    } else if (v.getId() == R.id.tv_dialog_right) {
//                        ChatService.stopService(GridBaseActivity.this);
//                        SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
//                        DialogUtil.dismissDialog();
//                        ActivityManager.getInstance().finishAllActivity();
//                        openActivity(LoginActivity.class);
//                    }
//                }
//            });
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
