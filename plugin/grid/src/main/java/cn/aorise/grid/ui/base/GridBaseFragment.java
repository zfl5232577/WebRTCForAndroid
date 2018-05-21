package cn.aorise.grid.ui.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import cn.aorise.common.core.ui.base.BaseFragment;
import cn.aorise.common.core.util.AoriseLog;
import cn.aorise.grid.R;

public abstract class GridBaseFragment extends BaseFragment {
    private static final String TAG = GridBaseFragment.class.getSimpleName();
    private ProgressDialog mLoadingDialog = null;

    //    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        GridApplication.getRefWatcher(getActivity()).watch(this);
//    }
    public void showLoadingDialog(Context context) {
        AoriseLog.i(TAG, "showLoadingDialog");
        if (null == this.mLoadingDialog) {
            this.mLoadingDialog = new ProgressDialog(context);
            this.mLoadingDialog.setCancelable(true);
            this.mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialogInterface) {
                    dismissLoadingDialog();
                }
            });
            this.mLoadingDialog.setProgressStyle(0);
            this.mLoadingDialog.setMessage(context.getString(R.string.grid_dialog_message));
            this.mLoadingDialog.setCanceledOnTouchOutside(false);
            this.mLoadingDialog.show();
        }

    }

    public void dismissLoadingDialog() {
        AoriseLog.i(TAG, "dismissLoadingDialog");
        if (this.mLoadingDialog != null && this.mLoadingDialog.isShowing()) {
            this.mLoadingDialog.dismiss();
        }
        this.mLoadingDialog = null;
    }

}
