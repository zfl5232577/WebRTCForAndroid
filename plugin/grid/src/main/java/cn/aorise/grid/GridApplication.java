package cn.aorise.grid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.aorise.common.core.interfaces.IAppCycle;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.grid.common.DialogUtil;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.module.network.entity.response.Session;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.activity.LoginActivity;
import cn.aorise.webrtc.chat.ChatAPIConfig;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.chat.OnDuplicateConnectionCallback;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class GridApplication implements IAppCycle {
    public static Context mContext;
    private static final String TAG = GridApplication.class.getSimpleName();

    @Override
    public void onCreate(Application context) {
        Log.i(TAG, "init");
        mContext = context;
        watchNetWork();
        String processName = getProcessName(mContext);
        Log.e(TAG, "onCreate:processName: "+processName);
        Log.e(TAG, "onCreate:packgeName: "+mContext.getPackageName());

        //wss://10.116.64.39:8443/ws/signaling
        //http://10.116.64.39:8081/ws/signaling
        //https://10.16.2.88:8443
        ChatAPIConfig config = new ChatAPIConfig("wss://10.16.2.88:8443/ws/signaling",
                "dde675aad8a64049894e10f9f65fe291",
                "stun:10.16.2.88:3478",
                "turn:10.16.2.88:3478",
                "turn",
                "05b4c082-5212-4df1-8721-476bab74754a",
                "5ad462c3f43e485022000029",
                "cea9816851ca827dd4153f9f4fa775c7",
                "2882303761517770616",
                "5371777025616");
        ChatClient.getInstance().init(mContext,config);
//        ChatClient.getInstance().setCallActivity(NewCallActivity.class);
//        ChatClient.getInstance().setOnDuplicateConnectionCallback(new OnDuplicateConnectionCallback() {
//            @Override
//            public void onExit() {
//                if (!TextUtils.isEmpty(SPUtils.getInstance().getString(Constant.SPCache.USER, ""))) {
//                    Session session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
//                    User user = session.getUser();
//                    if (user != null) {
//                        ChatClient.getInstance().logout();
//                    }
//                }
//                SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
//                cn.aorise.common.core.manager.ActivityManager.getInstance().appExit(mContext);
//            }
//
//            @Override
//            public void onReLogin() {
//                if (!TextUtils.isEmpty(SPUtils.getInstance().getString(Constant.SPCache.USER, ""))) {
//                    Session session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
//                    User user = session.getUser();
//                    if (user != null) {
//                        ChatClient.getInstance().logout();
//                    }
//                }
//                SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
//                DialogUtil.dismissDialog();
//                Activity activity = cn.aorise.common.core.manager.ActivityManager.getInstance().currentActivity();
//                cn.aorise.common.core.manager.ActivityManager.getInstance().finishAllActivity();
//                activity.startActivity(new Intent(activity,LoginActivity.class));
//            }
//        });
        if (SPUtils.getInstance().getBoolean(Constant.SPCache.LOGIN, false)) {
            if (!TextUtils.isEmpty(SPUtils.getInstance().getString(Constant.SPCache.USER, ""))) {
                Session session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
                User user = session.getUser();
                if (user != null) {
                    ChatClient.getInstance().login(user.username,
                            user.name,user.imgurl);
                }
            }
        }
    }

    private void watchNetWork() {
        ReactiveNetwork.observeNetworkConnectivity(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    if (connectivity.getState().equals(NetworkInfo.State.CONNECTED)) {
                        Constant.hasNetWork = true;
                    } else if (connectivity.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        Constant.hasNetWork = false;
                    }
                    EventBus.getDefault().post(Constant.hasNetWork);
                });
    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    /**
     * @return 获取全局上下文
     */
    public static synchronized Context getContext() {
        if (mContext != null) {
            return mContext;
        }
        return null;
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int level) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }
}
