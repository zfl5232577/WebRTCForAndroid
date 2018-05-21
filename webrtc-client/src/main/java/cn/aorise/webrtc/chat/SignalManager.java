package cn.aorise.webrtc.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.NetworkUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.webrtc.R;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.common.DialogUtil;
import cn.aorise.webrtc.common.Mlog;
import cn.aorise.webrtc.common.Utils;
import cn.aorise.webrtc.provider.LifecycleEvent;
import cn.aorise.webrtc.signal.SignalCallBack;
import cn.aorise.webrtc.signal.SignalMessage;
import cn.aorise.webrtc.stomp.StompClient;
import cn.aorise.webrtc.stomp.StompMessage;
import cn.aorise.webrtc.ui.BaseCallActivity;
import cn.aorise.webrtc.ui.DaemonActivity;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/21
 *     desc   : 信令管理器
 *     version: 1.0
 * </pre>
 */
public class SignalManager {
    private static final String TAG = SignalManager.class.getSimpleName();
    public static final String SIGNAL_DUPLICATE_CONNECTION = "cn.aorise.grid.webrtc.DUPLICATE_CONNECTION";
    private List<SignalCallBack> signalCallBacks;
    private Context mContext;
    private long mDelayMillis = 0;
    private int reconnectCount = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = this::reconnect;
    private LocalBroadcastManager mLocalBroadcastManager;
    private PushCallBack pushCallBack;
    private SignalCallBack signalCallBack;
    private PowerManager.WakeLock mWakeLock;
    private HeartBeatThread heartBeatThread;

    public SignalManager(Context context) {
        signalCallBacks = new CopyOnWriteArrayList<>();
        mContext = context;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    public void init() {
        signalCallBack = new SignalCallBack() {
            @Override
            public void onOpen(LifecycleEvent lifecycleEvent) {
                Mlog.i(TAG, "onOpen: websocket----------------Open");
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onOpen(lifecycleEvent);
                }
            }

            @Override
            public void onMessage(StompMessage stompMessage) {
                Mlog.i(TAG, "onMessage: websocket----------------" + stompMessage.getPayload());
                if (stompMessage.getPayload() != null) {
                    SignalMessage signalMessage = GsonUtils.fromJson(stompMessage.getPayload(), SignalMessage.class);
                    if (Constant.SignalType.SIGNAL_DUPLICATE_CONNECTION.equals(signalMessage.getType())) {
                        Constant.LoginInfo.isLogin = false;
                        Activity currentActivity = ActivityManager.getInstance().currentActivity();
                        if (!(currentActivity instanceof BaseCallActivity)) {
                            //一像素界面开屏即finish。所以不能弹窗
                            if (currentActivity instanceof DaemonActivity) {
                                ActivityManager.getInstance().finishActivity();
                                currentActivity = ActivityManager.getInstance().currentActivity();
                            }
                            DialogUtil.showDialog(currentActivity, mContext.getString(R.string.grid_dialog_conflict_login_title),
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
                                                Constant.LoginInfo.isLogin = true;
                                                reconnect();
                                            } else {
                                                ChatClient.getInstance().getOnDuplicateConnectionCallback().onReLogin();
                                            }
                                        }
                                    });
                        }
                    } else if (Constant.SignalType.SIGNAL_PUSH.equals(signalMessage.getType())) {
                        if (pushCallBack != null) {
                            if ("1".equals(signalMessage.getData())) {
                                //推送成功
                                pushCallBack.onSuccess();
                            } else {
                                //推送失败
                                pushCallBack.onFailure();
                            }
                        }
                    } else {
                        if (Constant.SignalType.SIGNAL_OFFERED.equals(signalMessage.getType())) {
                            if (!Utils.isCalling(mContext)) {
                                Intent intent = BaseCallActivity.getIntent(mContext, ChatClient.getInstance().getCallActivity(), signalMessage.getSender(), signalMessage.getSenderImgurl(), signalMessage.getSenderName(), BaseCallActivity.TYPE_CALLED);
                                intent.putExtra("data", signalMessage.getData());
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
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onPingMessage();
                }
            }

            @Override
            public void onConnected() {
                Mlog.i(TAG, "onMessage: websocket----------------onConnected");
                mDelayMillis = 0;
                reconnectCount = 0;
                if (heartBeatThread == null) {
                    heartBeatThread = new HeartBeatThread();
                    heartBeatThread.start();
                }
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onConnected();
                }
            }

            @Override
            public void onClosed(LifecycleEvent lifecycleEvent) {
                Mlog.i(TAG, "onMessage: websocket----------------onClosed");
                mHandler.postDelayed(mRunnable, mDelayMillis);
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onClosed(lifecycleEvent);
                }
            }

            @Override
            public void onError(LifecycleEvent lifecycleEvent) {
                Mlog.i(TAG, "onMessage: websocket----------------onError" + lifecycleEvent.getException());
                mHandler.postDelayed(mRunnable, mDelayMillis);
                for (SignalCallBack signalCallBack : signalCallBacks) {
                    signalCallBack.onError(lifecycleEvent);
                }
            }
        };
        connect();
    }

    /**
     * 发生指令空包
     *
     * @param destination
     */
    public void send(String destination) {
        StompClient.getInstance().send(destination).subscribe();
    }

    /**
     * 发字符串消息
     *
     * @param destination
     * @param data
     */
    public void send(String destination, String data) {
        StompClient.getInstance().send(destination, data).subscribe();
    }

    /**
     * 发送消息体
     *
     * @param stompMessage
     */
    public void send(StompMessage stompMessage) {
        StompClient.getInstance().send(stompMessage).subscribe();
    }

    /**
     * 对方处于离线或者忙碌状态，可以推送离线消息
     * 推送离线消息
     */
    public void push(SignalMessage signalMessage, PushCallBack pushCallBack) {
        this.pushCallBack = pushCallBack;
        send(Constant.SignalDestination.SIGNAL_Destination_PUSH, GsonUtils.toJson(signalMessage));
    }

    /**
     * 呼叫视频，直接调起CallActivity，此时离线就不做处理
     *
     * @param context
     * @param user    被呼叫用户信息
     */
    public void call(Context context, User user) {
        call(context, user, null);
    }

    /**
     * 呼叫视频，直接调起CallActivity,对方离线推送离线消息
     *
     * @param context
     * @param user         被呼叫用户信息
     * @param pushCallBack 推送的回调
     */
    public void call(Context context, User user, PushCallBack pushCallBack) {
        if (NetworkUtils.isConnected()) {
            if (!Utils.isCalling(mContext)) {
                if (user.status.equals(Constant.Status.ON_LINE)) {
                    if (!user.getUserName().equals(Constant.LoginInfo.user.getUserName())) {
                        Intent intent = BaseCallActivity.getIntent(mContext, ChatClient.getInstance().getCallActivity(), user.getUserName(), user.getUserIcon(), user.getNickName(), BaseCallActivity.TYPE_INVITING);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(context, context.getString(R.string.grid_call_self), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (pushCallBack != null) {
                        SignalMessage signalMessage = new SignalMessage(Constant.LoginInfo.user.getUserName(), user.getUserName(), "", "PUSH", Constant.LoginInfo.user.getNickName(), Constant.LoginInfo.user.getUserIcon());
                        push(signalMessage, pushCallBack);
                    }
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

    private void connect() {
        StompClient.getInstance().connect(signalCallBack);
    }

    public void onNetworkChanged() {
        reconnect();
    }

    public void reconnect() {
        Mlog.e(TAG, "reconnect: ");
        if (!Constant.LoginInfo.isLogin || isConnected()) {
            return;
        }
        if (!NetworkUtils.isConnected()) {
            mDelayMillis = 0;
            reconnectCount = 0;
            return;
        }
        reconnectCount++;
        if (reconnectCount > 3) {
            mDelayMillis = 20000;
        }
        //目前必须先断开连接然后在重新发起连接，不断开连接。只能发送数据不能接受数据（原因未知）
        disconnect();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
        connect();
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public void disconnect() {
        Log.e(TAG, "disconnect: ");
        mHandler.removeCallbacksAndMessages(null);
        StompClient.getInstance().disconnect();
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public boolean isConnected() {
        return StompClient.getInstance().isConnected();
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

    public static class HeartBeatThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (StompClient.getInstance().isConnected()) {
                    StompClient.getInstance().send(Constant.SignalDestination.SIGNAL_Destination_PONG).subscribe();
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
