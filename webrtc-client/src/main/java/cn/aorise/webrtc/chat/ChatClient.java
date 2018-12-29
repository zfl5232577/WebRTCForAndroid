package cn.aorise.webrtc.chat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.commonsdk.UMConfigure;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.aorise.webrtc.api.API;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.common.Mlog;
import cn.aorise.webrtc.common.Utils;
import cn.aorise.webrtc.ui.ChatService;
import cn.aorise.webrtc.ui.DefaultCallActivity;
import cn.aorise.webrtc.ui.KeepLiveService;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/20
 *     desc   : TODO
 *     version: 1.0
 * </pre>
 */
public class ChatClient {
    private static final String TAG = ChatClient.class.getSimpleName();
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private static ChatClient instance = null;
    private Network currentNetworkType;
    private Context mContext;
    private BroadcastReceiver connectivityBroadcastReceiver;
    private OnDuplicateConnectionCallback onDuplicateConnectionCallback;
    private OnPushCallback mOnPushCallback;
    private ConnectivityManager connManager;
    private SignalManager mSignalManager;
    private Class<? extends Activity> callActivity = DefaultCallActivity.class;
    private static final int JOB_ID = 11;
    private static final int JOB_INTERVAL = 60000;

    public static ChatClient getInstance() {
        if (instance == null) {
            synchronized (ChatClient.class) {
                if (instance == null) {
                    instance = new ChatClient();
                }
            }
        }
        return instance;
    }

    private ChatClient() {
        currentNetworkType = Network.NETWORK_NONE;
        connectivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                switch (action) {
                    case "android.net.conn.CONNECTIVITY_CHANGE":
                        Log.e(TAG, "onReceive: " + "android.net.conn.CONNECTIVITY_CHANGE");
                        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                        Log.e(TAG, "Active Network info: " + (networkInfo == null ? "null" : networkInfo.toString()));
                        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                            int type = networkInfo.getType();
                            if (type == ConnectivityManager.TYPE_WIFI) {
                                currentNetworkType = Network.NETWORK_WIFI;
                            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                                currentNetworkType = Network.NETWORK_MOBILE;
                            } else if (type == ConnectivityManager.TYPE_ETHERNET) {
                                currentNetworkType = Network.NETWORK_CABLE;
                            }
                        }

                        boolean hasNetwork = currentNetworkType != Network.NETWORK_NONE;
                        Constant.hasNetWork = hasNetwork;
                        if (hasNetwork) {
                            if (mSignalManager != null) {
                                mSignalManager.onNetworkChanged();
                            }
                        }
                        break;
                }
            }
        };
    }

    /**
     * 全局初始化，在工程Application的onCreate方法对用
     *
     * @param context       上下文
     * @param chatApiConfig 配置参数对象
     */
    public void init(Context context, ChatAPIConfig chatApiConfig) {
        cn.aorise.common.core.util.Utils.init((Application) context.getApplicationContext());
        setAPI(chatApiConfig);
        /*
        * 友盟推送子子进程也需要初始化，所以提到前面来
        **/
        UMConfigure.setLogEnabled(true);
        PushHelper.getInstance().init(context, API.UMENG_APPKEY, "Umeng", API.UMENG_SECRET);
        PushHelper.getInstance().register(context);
        PushHelper.getInstance().registerHWPush(context);
        PushHelper.getInstance().registerXMPush(context, API.XIAOMI_ID, API.XIAOMI_KEY);
        PushHelper.getInstance().registerMeizuPush(context, API.MEIZUAPP_ID, API.MEIZUAPP_KEY);

        if (!context.getPackageName().equals(getProcessName(context))) {
            Mlog.e(TAG, " not in main process, return");
            return;
        }
        this.mContext = context.getApplicationContext();
        connManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
    }

    public void login(String userName, String nickName, String userIcon) {
        if (mContext == null) {
            return;
        }
        this.mContext.registerReceiver(this.connectivityBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        Constant.LoginInfo.isLogin = true;
        User user = new User();
        user.setUserName(userName);
        user.setNickName(nickName);
        user.setUserIcon(userIcon);
        Constant.LoginInfo.user = user;
        PushHelper.getInstance().setAlias(mContext, user.getUserName(), Constant.ALIAS_TYPE.USER);
        mSignalManager = new SignalManager(mContext);
        mSignalManager.init();
        doStartService();
        scheduleJob();
    }

    public void reconnet() {
        if (mSignalManager != null) {
            mSignalManager.reconnect(true);
        }
    }

    /**
     * 获取视频页面的实际页面
     *
     * @return
     */
    public Class<? extends Activity> getCallActivity() {
        return callActivity;
    }

    /**
     * 开发者定义自己的视频呼叫页面后调用该函数指定
     *
     * @param callActivity
     */
    public void setCallActivity(Class<? extends Activity> callActivity) {
        this.callActivity = callActivity;
    }

    /**
     * 设置日志开关
     *
     * @param enabled
     */
    public void setLogEnabled(boolean enabled) {
        PushHelper.getInstance().setLogEnabled(enabled);
        Mlog.setLogEnabled(enabled);
    }


    public SignalManager getSignalManager() {
        return mSignalManager;
    }

    public boolean isConnected() {
        return mSignalManager.isConnected();
    }

    public void logout() {
        if (mContext == null) {
            return;
        }
        if (connectivityBroadcastReceiver != null) {
            mContext.unregisterReceiver(connectivityBroadcastReceiver);
        }
        PushHelper.getInstance().onDestroy(mContext, Constant.LoginInfo.user.getUserName());
        Constant.LoginInfo.isLogin = false;
        mSignalManager.disconnect();
        doStopService();
        cancelJob();
    }

    void doStartService() {
        if (!Utils.isServiceWork(mContext, ChatService.SERVICE_NAME)) {
            Intent service = new Intent(mContext, ChatService.class);
            mContext.startService(service);
        }
    }

    void doStopService() {
        if (!Utils.isServiceWork(mContext, ChatService.SERVICE_NAME)) {
            Intent service = new Intent(mContext, ChatService.class);
            mContext.stopService(service);
        }
    }

    @TargetApi(21)
    private void scheduleJob() {
        if (Build.VERSION.SDK_INT >= 21) {
            if ((Build.MANUFACTURER.toUpperCase().equals("OPPO") || Build.MANUFACTURER.toUpperCase().equals("VIVO")) && Build.VERSION.SDK_INT <= 22) {
                return;
            }

            try {
                Intent intent = new Intent(mContext, KeepLiveService.class);
                this.mContext.startService(intent);
                JobInfo.Builder builder = new JobInfo.Builder(11, new ComponentName(mContext, KeepLiveService.class));
                builder.setPeriodic(JOB_INTERVAL);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                builder.setPersisted(true);
                JobScheduler jobScheduler = (JobScheduler) this.mContext.getSystemService(Service.JOB_SCHEDULER_SERVICE);
                jobScheduler.schedule(builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @TargetApi(21)
    private void cancelJob() {
        if (Build.VERSION.SDK_INT >= 21) {
            if ((Build.MANUFACTURER.toUpperCase().equals("OPPO") || Build.MANUFACTURER.toUpperCase().equals("VIVO")) && Build.VERSION.SDK_INT <= 22) {
                return;
            }

            try {
                JobScheduler jobScheduler = (JobScheduler) this.mContext.getSystemService(Service.JOB_SCHEDULER_SERVICE);
                jobScheduler.cancel(JOB_ID);
                this.mContext.stopService(new Intent(this.mContext, KeepLiveService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public OnDuplicateConnectionCallback getOnDuplicateConnectionCallback() {
        return onDuplicateConnectionCallback;
    }

    /**
     * 设置登陆冲突后弹窗的回调函数
     *
     * @param onDuplicateConnectionCallback
     */
    public void setOnDuplicateConnectionCallback(OnDuplicateConnectionCallback onDuplicateConnectionCallback) {
        this.onDuplicateConnectionCallback = onDuplicateConnectionCallback;
    }

    public OnPushCallback getOnPushCallback() {
        return mOnPushCallback;
    }

    /**
     * 设置点击PUSH消息的回调函数
     *
     * @param
     */
    public void setOnPushCallback(OnPushCallback onPushCallback) {
        this.mOnPushCallback = onPushCallback;
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

    private void setAPI(ChatAPIConfig config) {
        API.SIGNAL_URL = config.getConfigParams().getSIGNAL_URL();
        API.TOKEN = config.getConfigParams().getTOKEN();
        API.TURN_URL = config.getConfigParams().getTURN_URL();
        API.STUN_URL = config.getConfigParams().getSTUN_URL();
        API.TURN_ACCOUNT = config.getConfigParams().getTURN_ACCOUNT();
        API.TURN_PASSWORD = config.getConfigParams().getTURN_PASSWORD();
        API.UMENG_APPKEY = config.getConfigParams().getUMENG_APPKEY();
        API.UMENG_SECRET = config.getConfigParams().getUMENG_SECRET();
        API.XIAOMI_ID = config.getConfigParams().getXIAOMI_ID();
        API.XIAOMI_KEY = config.getConfigParams().getXIAOMI_KEY();
        API.MEIZUAPP_ID = config.getConfigParams().getMEIZUAPP_ID();
        API.MEIZUAPP_KEY = config.getConfigParams().getMEIZUAPP_KEY();
        if (!TextUtils.isEmpty(API.STUN_URL)) {
            iceServers.add(PeerConnection.IceServer.builder(API.STUN_URL).createIceServer());
        }
        if (!TextUtils.isEmpty(API.TURN_URL) && !TextUtils.isEmpty(API.TURN_ACCOUNT) && !TextUtils.isEmpty(API.TURN_PASSWORD)) {
            iceServers.add(PeerConnection.IceServer.builder(API.TURN_URL)
                    .setUsername(API.TURN_ACCOUNT)
                    .setPassword(API.TURN_PASSWORD).createIceServer());
        }
    }

    public void addStunUrl(String stunUrl) {
        iceServers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer());
    }

    public void addTurnUrl(String turnUrl, String turnAccount, String passWord) {
        iceServers.add(PeerConnection.IceServer.builder(turnUrl)
                .setUsername(turnAccount)
                .setPassword(passWord).createIceServer());
    }

    public LinkedList<PeerConnection.IceServer> getIceServers() {
        return iceServers;
    }
}
