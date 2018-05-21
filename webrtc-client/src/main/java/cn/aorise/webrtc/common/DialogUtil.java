package cn.aorise.webrtc.common;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import cn.aorise.webrtc.R;


/**
 * Created by 54926 on 2017/9/1.
 */

public class DialogUtil {
    private static AlertDialog mDialog;

    public static void showDialog(final Context context, String title, String content, String left, String right, int leftColorId, int rightColorId, View.OnClickListener onClickListener) {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        mDialog = builder.create();
        View view = View.inflate(context, R.layout.grid_dialog_view, null);
        mDialog.setView(view);
        mDialog.show();
        TextView mTitleView = (TextView) view.findViewById(R.id.tv_dialog_title);
        TextView mContentView = (TextView) view.findViewById(R.id.tv_dialog_content);
        TextView mLeftView = (TextView) view.findViewById(R.id.tv_dialog_left);
        TextView mRightView = (TextView) view.findViewById(R.id.tv_dialog_right);

        mTitleView.setText(title);
        mContentView.setText(content);
        mLeftView.setText(left);
        mRightView.setText(right);

        mLeftView.setTextColor(leftColorId);
        mRightView.setTextColor(rightColorId);

        mLeftView.setOnClickListener(onClickListener);
        mRightView.setOnClickListener(onClickListener);
    }

    public static boolean isShow(){
        return mDialog!=null&&mDialog.isShowing();
    }

    public static void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
            mDialog = null;
        }
    }

}
