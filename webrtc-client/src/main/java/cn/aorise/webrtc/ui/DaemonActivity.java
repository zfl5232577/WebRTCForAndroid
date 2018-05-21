package cn.aorise.webrtc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.ui.base.BaseActivity;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/13
 *     desc   : 守护进程的一像素页面
 *     version: 1.0
 * </pre>
 */
public class DaemonActivity extends BaseActivity {

    public static final String CLOSE_ACTION = "close";
    private static final String TAG = "DaemonActivity";

    private static Intent newIntent(Context context) {
        Intent intent = new Intent(context, DaemonActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    public static void startActivity(Context context) {
        context.startActivity(newIntent(context));
    }

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
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        View view = new View(getApplicationContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));

        setContentView(view);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " );
    }
}