package cn.aorise.webrtc.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.NetworkUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.webrtc.R;
import cn.aorise.webrtc.api.API;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.common.DialogUtil;
import cn.aorise.webrtc.common.Mlog;
import cn.aorise.webrtc.common.Utils;
import cn.aorise.webrtc.signal.SignalCallBack;
import cn.aorise.webrtc.signal.SignalMessage;
import cn.aorise.webrtc.stomp.LifecycleEvent;
import cn.aorise.webrtc.stomp.Stomp;
import cn.aorise.webrtc.stomp.StompClient;
import cn.aorise.webrtc.stomp.StompMessage;
import cn.aorise.webrtc.ui.BaseCallActivity;
import cn.aorise.webrtc.ui.DaemonActivity;
import cn.aorise.webrtc.ui.DuplicateActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/21
 *     desc   : 信令管理器
 *     version: 2.0
 * </pre>
 */
public class SignalManager {
    private static final String TAG = SignalManager.class.getSimpleName();
    private Context mContext;
    private StompClient mStompClient;
    private List<SignalCallBack> signalCallBacks;
    private long mDelayMillis = 0;
    private int reconnectCount = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = () -> reconnect(false);
    private LocalBroadcastManager mLocalBroadcastManager;
    private SignalCallBack signalCallBack;
    private PowerManager.WakeLock mWakeLock;
    private Disposable disposable;
    private boolean isConnected;
    private boolean isConnecting;

    public SignalManager(Context context) {
        mContext = context;
        signalCallBacks = new CopyOnWriteArrayList<>();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }

    }

    public void init() {
        signalCallBack = new SignalCallBack() {
            @Override
            public void onOpen(LifecycleEvent lifecycleEvent) {
                Mlog.i(TAG, "onOpen: websocket----------------Open");
                mDelayMillis = 0;
                reconnectCount = 0;
                isConnecting = false;
                isConnected = true;
                mHandler.removeCallbacksAndMessages(null);
                if (disposable == null || disposable.isDisposed()) {
                    disposable = Observable.interval(8, TimeUnit.SECONDS)
                            .observeOn(Schedulers.io())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    if (mStompClient.isConnected()) {
                                        mStompClient.send(Constant.SignalDestination.SIGNAL_Destination_PONG).subscribe();
                                    }
                                }
                            });
                }
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onOpen(lifecycleEvent);
                }
            }

            @Override
            public void onMessage(StompMessage stompMessage) {
                Log.e(TAG, "onMessage: " + Thread.currentThread().getName() + "====================");
                Mlog.i(TAG, "onMessage: websocket----------------" + stompMessage.getPayload());
                if (stompMessage.getPayload() != null) {
                    SignalMessage signalMessage = GsonUtils.fromJson(stompMessage.getPayload(), SignalMessage.class);
                    if (Constant.SignalType.SIGNAL_DUPLICATE_CONNECTION.equals(signalMessage.getType())) {
                        Constant.LoginInfo.isLogin = false;
                        isConnected = false;
                        PushHelper.getInstance().deleteAlias(mContext, Constant.LoginInfo.user.getUserName(), Constant.ALIAS_TYPE.USER);
                        Activity currentActivity = ActivityManager.getInstance().currentActivity();
                        if (currentActivity == null || currentActivity instanceof DaemonActivity) {
                            Intent intent = new Intent(mContext, DuplicateActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            return;
                        }
                        if (!(currentActivity instanceof BaseCallActivity)) {
                            DialogUtil.showDialog(currentActivity, mContext.getString(mContext.getApplicationContext().getApplicationInfo().labelRes) + mContext.getString(R.string.grid_dialog_conflict_login_title),
                                    mContext.getString(R.string.grid_dialog_conflict_login_content), mContext.getString(R.string.grid_dialog_conflict_login_exit),
                                    mContext.getString(R.string.grid_dialog_conflict_login_again), R.color.grid_txt_default, R.color.grid_txt_dialog_right,
                                    v -> {
                                        if (v.getId() == R.id.tv_dialog_left) {
                                            DialogUtil.dismissDialog();
                                            if (ChatClient.getInstance().getOnDuplicateConnectionCallback() == null) {
                                                ChatClient.getInstance().logout();
                                                SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
                                                ActivityManager.getInstance().appExit(mContext);
                                            } else {
                                                ChatClient.getInstance().getOnDuplicateConnectionCallback().onExit();
                                            }
                                        } else if (v.getId() == R.id.tv_dialog_right) {
                                            DialogUtil.dismissDialog();
                                            if (ChatClient.getInstance().getOnDuplicateConnectionCallback() == null) {
                                                PushHelper.getInstance().setAlias(mContext, Constant.LoginInfo.user.getUserName(), Constant.ALIAS_TYPE.USER);
                                                reconnect(true);
                                            } else {
                                                ChatClient.getInstance().getOnDuplicateConnectionCallback().onReLogin();
                                            }
                                        }
                                    });
                        }
                    } else {
                        if (Constant.SignalType.SIGNAL_OFFERED.equals(signalMessage.getType())) {
                            if (!Utils.isCalling(mContext)) {
                                Intent intent = BaseCallActivity.getIntent(mContext, ChatClient.getInstance().getCallActivity(), signalMessage.getSender(), signalMessage.getSenderImgurl(), signalMessage.getSenderName(), BaseCallActivity.TYPE_CALLED, !signalMessage.isAudioFlag());
                                intent.putExtra("data", signalMessage.getData());
                                intent.putExtra("extras", signalMessage.getExtras());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        } else if (Constant.SignalType.SIGNAL_ADD.equals(signalMessage.getType())) {
                            if (!Utils.isCalling(mContext)) {
                                Intent intent = BaseCallActivity.getIntent(mContext, ChatClient.getInstance().getCallActivity(), signalMessage.getSender(), signalMessage.getSenderImgurl(), signalMessage.getSenderName(), BaseCallActivity.TYPE_CALLED, !signalMessage.isAudioFlag(), true, 0);
                                intent.putExtra("data", signalMessage.getData());
                                intent.putExtra("extras", signalMessage.getExtras());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        }
                    }
                }
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onMessage(stompMessage);
                }
            }

            @Override
            public void onPingMessage() {
                Mlog.i(TAG, "onMessage: websocket----------------onPingMessage");
                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(mRunnable, 30000);
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onPingMessage();
                }
            }

            @Override
            public void onConnected() {
                Mlog.i(TAG, "onMessage: websocket----------------onConnected");
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onConnected();
                }
            }

            @Override
            public void onClosed(LifecycleEvent lifecycleEvent) {
                Mlog.i(TAG, "onMessage: websocket----------------onClosed");
                isConnecting = false;
                isConnected = false;
                mHandler.postDelayed(mRunnable, mDelayMillis);
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onClosed(lifecycleEvent);
                }
            }

            @Override
            public void onError(LifecycleEvent lifecycleEvent) {
                Mlog.i(TAG, "onMessage: websocket----------------onError" + lifecycleEvent.getException());
            }
        };
        connect();
    }

    private void connect() {
        isConnecting = true;
        HashMap<String, String> connectHttpHeaders = new HashMap<>();
        connectHttpHeaders.put("token", API.TOKEN);
        connectHttpHeaders.put("username", Constant.LoginInfo.user.getUserName());
        connectHttpHeaders.put("device-type", Constant.DeviceType.MOBILE);
        try {
            connectHttpHeaders.put("name", URLEncoder.encode(Constant.LoginInfo.user.getNickName(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // default http OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .build();
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, API.SIGNAL_URL, connectHttpHeaders, okHttpClient);
        mStompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            if (signalCallBack != null) {
                                signalCallBack.onOpen(lifecycleEvent);
                            }
                            break;
                        case ERROR:
                            if (signalCallBack != null) {
                                signalCallBack.onError(lifecycleEvent);
                            }
                            break;
                        case CLOSED:
                            if (signalCallBack != null) {
                                signalCallBack.onClosed(lifecycleEvent);
                            }
                            break;
                    }
                });
        topicMessage();
        mStompClient.connect();
//        //如果连接超过30000ms没反应，重新连接
//        mHandler.postDelayed(mRunnable,30000);
    }

    private void topicMessage() {
        mStompClient.topic("/user/topic/message")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    if (signalCallBack != null) {
                        signalCallBack.onMessage(topicMessage);
                    }
                });

        mStompClient.topic("/user/topic/error")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    if (signalCallBack != null) {
                        signalCallBack.onMessage(topicMessage);
                    }
                });

        mStompClient.topic("/user/topic/ping")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    if (signalCallBack != null) {
                        signalCallBack.onPingMessage();
                    }
                });
    }

    /**
     * 发生指令空包
     *
     * @param destination
     */
    public void send(String destination) {
        mStompClient.send(destination).subscribe();
    }

    /**
     * 发字符串消息
     *
     * @param destination
     * @param data
     */
    public void send(String destination, String data) {
        mStompClient.send(destination, data).subscribe();
    }

    /**
     * 发送消息体
     *
     * @param stompMessage
     */
    public void send(StompMessage stompMessage) {
        mStompClient.send(stompMessage).subscribe();
    }

    /**
     * 呼叫视频，直接调起CallActivity，此时离线就不做处理
     *
     * @param context
     * @param user    被呼叫用户信息
     * @param isVideo 是否视频，false为语音通话
     * @param isAdd   是否加入聊天室，假如多人室群聊，这里true,一对一传false
     */
    public void call(Context context, User user, boolean isVideo, boolean isAdd) {
        call(context, user, isVideo, isAdd, null);
    }

    /**
     * 呼叫视频，直接调起CallActivity,对方离线推送离线消息
     *
     * @param context
     * @param user    被呼叫用户信息
     * @param isVideo 是否视频，false为语音通话
     * @param isAdd   是否加入聊天室，假如多人室群聊，这里true,一对一传false
     * @param extras  自定义的Json数据串
     * @param limit   聊天室默认人数限制，不传默认为9
     */
    public void call(Context context, User user, boolean isVideo, boolean isAdd, String extras, int limit) {
        if (NetworkUtils.isConnected()) {
            if (!Utils.isCalling(mContext)) {
                if (!user.getUserName().equals(Constant.LoginInfo.user.getUserName())) {
                    Intent intent = BaseCallActivity.getIntent(mContext, ChatClient.getInstance().getCallActivity(), user.getUserName(), user.getUserIcon(), user.getNickName(), BaseCallActivity.TYPE_INVITING, isVideo, isAdd, 0, extras, limit);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.grid_call_self), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.grid_call_busy),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context,
                    context.getResources().getString(R.string.grid_network_setting),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 呼叫视频，直接调起CallActivity,对方离线推送离线消息
     *
     * @param context
     * @param user         被呼叫用户信息
     * @param pushCallBack 推送的回调
     */
    public void call(Context context, User user, boolean isVideo, boolean isAdd, PushCallBack pushCallBack) {
        if (NetworkUtils.isConnected()) {
            if (!Utils.isCalling(mContext)) {
                if (!user.getUserName().equals(Constant.LoginInfo.user.getUserName())) {
                    Intent intent = BaseCallActivity.getIntent(mContext, ChatClient.getInstance().getCallActivity(), user.getUserName(), user.getUserIcon(), user.getNickName(), BaseCallActivity.TYPE_INVITING, isVideo, isAdd, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.grid_call_self), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.grid_call_busy),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context,
                    context.getResources().getString(R.string.grid_network_setting),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onNetworkChanged() {
        reconnectCount = 0;
        mDelayMillis = 0;
        reconnect(false);
    }

    public void reconnect(boolean isActive) {
        if (isActive) {
            Constant.LoginInfo.isLogin = true;
        }
        if (!Constant.LoginInfo.isLogin || isConnected() || isConnecting()) {
            Log.e(TAG, "reconnect: " + !Constant.LoginInfo.isLogin + isConnected() + mStompClient.isConnecting());
            return;
        }
        Log.e(TAG, "reconnect: " + Thread.currentThread().getName());
        reconnectCount++;
        if (reconnectCount > 3) {
            mDelayMillis = 20000;
        }
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
        disconnect();
        connect();
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public void disconnect() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        mStompClient.disconnect();
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public boolean isConnected() {
        return isConnected && mStompClient.isConnected();
    }

    public boolean isConnecting() {
        return isConnecting && mStompClient.isConnecting();
    }

    public void addConnectionListener(SignalCallBack signalCallBack) {
        if (signalCallBack == null || signalCallBacks.contains(signalCallBack)) {
            return;
        }
        signalCallBacks.add(signalCallBack);
    }


    public void removeConnectionListener(SignalCallBack signalCallBack) {
        if (signalCallBack == null || !signalCallBacks.contains(signalCallBack)) {
            return;
        }
        signalCallBacks.remove(signalCallBack);
    }

    public interface PushCallBack {
        void onSuccess();

        void onFailure();
    }
}
