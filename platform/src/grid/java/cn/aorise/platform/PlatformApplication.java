package cn.aorise.platform;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import cn.aorise.common.BaseApplication;
import cn.aorise.common.core.manager.AppManager;
import cn.aorise.grid.GridApplication;

public class PlatformApplication extends BaseApplication {
    private static final String TAG = PlatformApplication.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        AppManager.getInstance().add(new GridApplication());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
//        if (BuildConfig.DEBUG) {
//            AoriseUtil.enabledStrictMode();
//        }
        LeakCanary.install(this);
    }


    @Override
    public void destroy(Application context, boolean isKillProcess) {
        super.destroy(context, isKillProcess);
        Log.i(TAG, "destroy");
    }
}
