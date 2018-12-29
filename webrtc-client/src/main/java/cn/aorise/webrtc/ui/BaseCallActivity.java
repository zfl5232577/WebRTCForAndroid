package cn.aorise.webrtc.ui;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import cn.aorise.common.core.manager.ActivityManager;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.common.core.util.ScreenUtils;
import cn.aorise.webrtc.R;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.common.DialogUtil;
import cn.aorise.webrtc.common.GridBaseActivity;
import cn.aorise.webrtc.common.Utils;
import cn.aorise.webrtc.webrtc.WebRtcClient;
import cn.aorise.webrtc.signal.SignalCallBack;
import cn.aorise.webrtc.signal.SignalMessage;
import cn.aorise.webrtc.stomp.LifecycleEvent;
import cn.aorise.webrtc.stomp.StompMessage;
import cn.aorise.webrtc.webrtc.PercentFrameLayout;
import cn.aorise.webrtc.webrtc.RTCAudioManger;

public abstract class BaseCallActivity extends GridBaseActivity implements WebRtcClient.PeerConnectionEvents {
    private static final String TAG = BaseCallActivity.class.getSimpleName();
    public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
            "org.appspot.apprtc.SAVE_INPUT_AUDIO_TO_FILE";
    public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static final String EXTRA_ID = "org.appspot.apprtc.ID";
    public static final String EXTRA_ENABLE_RTCEVENTLOG = "org.appspot.apprtc.ENABLE_RTCEVENTLOG";
    public static final String EXTRA_USE_LEGACY_AUDIO_DEVICE =
            "org.appspot.apprtc.USE_LEGACY_AUDIO_DEVICE";

    private static final int CALL_TIME_OUT = 60000;//主叫超时
    private static final int CALLED_TIME_OUT = 55000;//被叫超时
    private static final int CHECKING_TIME_OUT = 55000;//连接中超时

    public static final int TYPE_INVITING = 1;
    public static final int TYPE_CALLING = 2;
    public static final int TYPE_CALLED = 3;
    public static final int TYPE_CHECKING = 4;


    private static final int SURFACE_LOCAL_X_CONNECTING = 0;
    private static final int SURFACE_LOCAL_Y_CONNECTING = 0;
    private static final int SURFACE_LOCAL_WIDTH_CONNECTING = 100;
    private static final int SURFACE_LOCAL_HEIGHT_CONNECTING = 100;

    private static final int SURFACE_LOCAL_X_CONNECTED = 72;
    private static final int SURFACE_LOCAL_Y_CONNECTED = 5;
    private static final int SURFACE_LOCAL_WIDTH_CONNECTED = 25;
    private static final int SURFACE_LOCAL_HEIGHT_CONNECTED = 25;

    // Remote video screen position
    private static final int SURFACE_REMOTE_X = 0;
    private static final int SURFACE_REMOTE_Y = 0;
    private static final int SURFACE_REMOTE_WIDTH = 100;
    private static final int SURFACE_REMOTE_HEIGHT = 100;

    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA"
    };
    private boolean permission_granted = true;

    private static WebRtcClient mWebRtcClient;
    private WebRtcClient.PeerConnectionParameters mPeerConnectionParameters;
    private MediaStream mLocalStream;
    private MediaPlayer mMediaPlayer;
    private Vibrator vibrator;
    protected RTCAudioManger mRTCAudioManger;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    protected boolean isCalling = false;
    protected boolean isInitiator;
    protected String userName;
    protected String imgUrl;
    protected String name;
    protected String extras;//扩展信息
    protected int limit;//ADD时，房间人数限制
    protected int type;
    protected long createtime;
    private String data;

    private boolean iceConnected = false;
    private RendererCommon.ScalingType scalingType;
    private EglBase rootEglBase;
    private SurfaceViewRenderer surfaceLocal;
    private SurfaceViewRenderer surfaceRemote;
    private PercentFrameLayout layoutLocalVideo;
    private PercentFrameLayout layoutRemoteVideo;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private SignalCallBack signalCallBack;
    private PhoneBroadcastReceive mPhoneBroadcastReceive;
    private boolean isInited = false;
    private CameraVideoCapturer videoCapturer;
    private boolean isNormal = true;
    protected boolean isVideoCall = true;//视频通话或者语音通话
    protected boolean isAdd;//是否是加入聊天室。false表示自己是房主
    protected String signalMessageType;//


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        surfaceLocal = setSurfaceLocal();
        surfaceRemote = setSurfaceRemote();
        layoutLocalVideo = setLayoutLocalVideo();
        layoutRemoteVideo = setLayoutRemoteVideo();
        remoteSinks.add(surfaceRemote);
        rootEglBase = EglBase.create();
        if (isVideoCall) {
            scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
            if (surfaceLocal != null) {
                surfaceLocal.init(rootEglBase.getEglBaseContext(), null);
                surfaceLocal.setZOrderMediaOverlay(true);
            }
            if (surfaceRemote != null) {
                surfaceRemote.init(rootEglBase.getEglBaseContext(), null);
            }
        } else {
            if (layoutLocalVideo != null) {
                layoutLocalVideo.setVisibility(View.GONE);
            }
            if (layoutRemoteVideo != null) {
                layoutRemoteVideo.setVisibility(View.GONE);
            }
        }
        updateVideoView();
        mRTCAudioManger = RTCAudioManger.getManager();
        mRTCAudioManger.init(this.getApplicationContext());
        if (ChatClient.getInstance().isConnected()) {
            init();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebRtcClient != null && isVideoCall) {
            mWebRtcClient.startVideoSource();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebRtcClient != null && isVideoCall) {
            mWebRtcClient.stopVideoSource();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatClient.getInstance().getSignalManager().removeConnectionListener(signalCallBack);
        if (mPhoneBroadcastReceive != null) {
            unregisterReceiver(mPhoneBroadcastReceive);
            mPhoneBroadcastReceive = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    /**
     * @param context  上下文
     * @param cls      呼叫页面的Activity类
     * @param userName 用户名
     * @param imgUrl   用户头像可为空
     * @param name     用户昵称
     * @param type     主动呼叫还是被叫（1主动呼叫，3被动接听）
     * @return
     */
    public static Intent getIntent(Context context, Class<? extends Activity> cls, String userName, String imgUrl, String name, int type) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("userName", userName);
        intent.putExtra("imgUrl", imgUrl);
        intent.putExtra("type", type);
        intent.putExtra("name", name);
        return intent;
    }

    public static Intent getIntent(Context context, Class<? extends Activity> cls, String userName, String imgUrl, String name, int type,
                                   boolean isVideoCall, boolean isAdd, long createtime) {
        Intent intent = getIntent(context, cls, userName, imgUrl, name, type);
        intent.putExtra("isVideoCall", isVideoCall);
        intent.putExtra("isAdd", isAdd);
        intent.putExtra("createtime", createtime);
        return intent;
    }

    public static Intent getIntent(Context context, Class<? extends Activity> cls, String userName, String imgUrl, String name, int type, boolean isVideoCall) {
        Intent intent = getIntent(context, cls, userName, imgUrl, name, type);
        intent.putExtra("isVideoCall", isVideoCall);
        return intent;
    }

    public static Intent getIntent(Context context, Class<? extends Activity> cls, String userName, String imgUrl, String name, int type, boolean isVideoCall, boolean isAdd, long createtime, String extras) {
        Intent intent = getIntent(context, cls, userName, imgUrl, name, type, isVideoCall, isAdd, createtime);
        intent.putExtra("extras", extras);
        return intent;
    }

    /**
     * @param context     上下文
     * @param cls         呼叫页面的Activity类
     * @param userName    用户名
     * @param imgUrl      用户头像可为空
     * @param name        用户昵称
     * @param type        主动呼叫还是被叫（1主动呼叫，3被动接听）
     * @param isVideoCall 是否是视频，可以是语音
     * @param isAdd       是否加入聊天室，假如多人室群聊，这里true,一对一传false
     * @param createtime  创建的时间戳
     * @param extras      自定义的Json数据串
     * @param limit       聊天室默认人数限制，不传默认为9
     * @return
     */
    public static Intent getIntent(Context context, Class<? extends Activity> cls, String userName, String imgUrl, String name, int type, boolean isVideoCall, boolean isAdd, long createtime, String extras, int limit) {
        Intent intent = getIntent(context, cls, userName, imgUrl, name, type, isVideoCall, isAdd, createtime);
        intent.putExtra("extras", extras);
        intent.putExtra("limit", limit);
        return intent;
    }

    protected void initData() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        imgUrl = intent.getStringExtra("imgUrl");
        type = intent.getIntExtra("type", TYPE_INVITING);
        name = intent.getStringExtra("name");
        extras = intent.getStringExtra("extras");
        isAdd = intent.getBooleanExtra("isAdd", false);
        isVideoCall = intent.getBooleanExtra("isVideoCall", true);
        createtime = intent.getLongExtra("createtime", 0);
        limit = intent.getIntExtra("limit", 1);
        if (BaseCallActivity.TYPE_INVITING == type) {
            isInitiator = true;
            mRunnable = () -> {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REMOVE, "", Constant.SignalType.SIGNAL_REMOVED);
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                disConnect();
            };
        } else {
            isInitiator = false;
            data = intent.getStringExtra("data");
            mRunnable = () -> {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REFUSE, "0", Constant.SignalType.SIGNAL_ADD_MEMBER_REFUSED);
                disConnect();
            };
            mHandler.postDelayed(mRunnable, CALLED_TIME_OUT);
        }
        registerPhoneReceive();
    }

    protected abstract SurfaceViewRenderer setSurfaceLocal();

    protected abstract SurfaceViewRenderer setSurfaceRemote();

    protected abstract PercentFrameLayout setLayoutLocalVideo();

    protected abstract PercentFrameLayout setLayoutRemoteVideo();

    /**
     * 初始化和连接成功
     * 根据通话状态更新布局
     */
    protected abstract void updateCallView();

    private void init() {
        if (isInited) {
            return;
        }
        isInited = true;
        boolean loopback = false;
        mPeerConnectionParameters = new WebRtcClient.PeerConnectionParameters(isVideoCall, loopback, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenHeight(), 30);
        mWebRtcClient = new WebRtcClient(this.getApplicationContext(), rootEglBase, mPeerConnectionParameters, this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }
        mWebRtcClient.createPeerConnectionFactory(options);
        setSignalCallBack();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> unAuthPermissions = Utils.findUnAuthPermissions(this, MANDATORY_PERMISSIONS);
            if (unAuthPermissions.size() > 0) {
                requestPermissions(unAuthPermissions.toArray(new String[unAuthPermissions.size()]), 0);
            } else {
                if (isInitiator) {
                    setmLocalStream();
                } else {
                    playCallRing();
                }
            }
        } else {
            if (isInitiator) {
                setmLocalStream();
            } else {
                playCallRing();
            }
        }
    }

    private void setSignalCallBack() {
        signalCallBack = new SignalCallBack() {
            @Override
            public void onOpen(LifecycleEvent lifecycleEvent) {

            }

            @Override
            public void onMessage(StompMessage stompMessage) {
                Log.e(TAG, "onMessage: " + stompMessage.getPayload());
                SignalMessage signalMessage = GsonUtils.fromJson(stompMessage.getPayload(), SignalMessage.class);
                signalMessageType = signalMessage.getType();

                if (Constant.SignalType.SIGNAL_MEMBER_REFUSED.equals(signalMessageType)) {
                    showLongToast(signalMessage.getData());
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_REFUSED.equals(signalMessageType)) {
                    showLongToast(signalMessage.getData());
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_ANSWERED.equals(signalMessageType)) {
                    mHandler.removeCallbacksAndMessages(null);
                    mWebRtcClient.setRemoteDescription(parseSdp(signalMessage.getData()));
                } else if (Constant.SignalType.SIGNAL_CHANGE.equals(signalMessageType)) {
                    if (isVideoCall) {
                        showLongToast("对方已切换至语音通话");
                        convertToSpeech();
                        updateCallView();
                    }
                } else if (Constant.SignalType.SIGNAL_ROOM_FULL.equals(signalMessageType)) {
                    showLongToast(signalMessage.getData());
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_ADD_MEMBER_REFUSED.equals(signalMessageType)) {
                    showLongToast(signalMessage.getData());
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_REMOVED.equals(signalMessageType)) {
                    String data = signalMessage.getData();
                    showLongToast(data);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_ROOM_DISSOLVED.equals(signalMessageType)) {
                    String data = signalMessage.getData();
                    showLongToast(data);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_MEMBER_LEAVED.equals(signalMessageType)) {
                    String data = signalMessage.getData();
                    showLongToast(data);
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_CANDIDATE.equals(signalMessageType)) {
                    Log.e(TAG, "onMessage: " + Thread.currentThread().getName() + signalMessage.getData());
                    mWebRtcClient.addRemoteIceCandidate(parseIceCandidate(signalMessage.getData()));
                } else if (Constant.SignalType.SIGNAL_PUSH.equals(signalMessage.getType())) {
                    if ("1".equals(signalMessage.getData())) {
                        //推送成功
                        showLongToast("对方不在线，已通知对方！");
                        disConnect();
                    } else {
                        //推送失败
                        showLongToast("对方不在线，请稍后再试！");
                        disConnect();
                    }
                } else if (Constant.SignalType.SIGNAL_DUPLICATE_CONNECTION.equals(signalMessageType)) {
                    DialogUtil.showDialog(BaseCallActivity.this, getString(R.string.grid_dialog_conflict_login_title),
                            getString(R.string.grid_dialog_conflict_login_content), getString(R.string.grid_dialog_conflict_login_exit),
                            getString(R.string.grid_dialog_conflict_login_again), R.color.grid_txt_default, R.color.grid_txt_dialog_right,
                            v -> {
                                if (v.getId() == R.id.tv_dialog_left) {
                                    ChatClient.getInstance().logout();
                                    SPUtils.getInstance().remove(Constant.SPCache.LOGIN);
                                    DialogUtil.dismissDialog();
                                    ActivityManager.getInstance().appExit(BaseCallActivity.this);
                                } else if (v.getId() == R.id.tv_dialog_right) {
                                    ChatClient.getInstance().getSignalManager().reconnect(true);
                                    DialogUtil.dismissDialog();
                                    finish();
                                }
                            });
                }
            }

            @Override
            public void onPingMessage() {

            }

            @Override
            public void onConnected() {
                init();
            }

            @Override
            public void onClosed(LifecycleEvent lifecycleEvent) {

            }

            @Override
            public void onError(LifecycleEvent lifecycleEvent) {

            }
        };
        ChatClient.getInstance().getSignalManager().addConnectionListener(signalCallBack);
    }

    private void updateVideoView() {
        if (!isVideoCall) {//语音通话直接返回
            return;
        }
        if (layoutRemoteVideo != null && surfaceRemote != null) {//设置远程画面窗口
            if (iceConnected) {
                layoutRemoteVideo.setVisibility(View.VISIBLE);
                layoutRemoteVideo.setPosition(SURFACE_REMOTE_X, SURFACE_REMOTE_Y, SURFACE_REMOTE_WIDTH, SURFACE_REMOTE_HEIGHT);
                surfaceRemote.setScalingType(scalingType);
                surfaceRemote.setMirror(false);
                surfaceRemote.requestLayout();
            } else {//连接之前先隐藏
                layoutRemoteVideo.setVisibility(View.GONE);
            }
        }
        if (layoutLocalVideo != null && surfaceLocal != null) {
            if (iceConnected) {
                layoutLocalVideo.setVisibility(View.VISIBLE);
                if (layoutRemoteVideo != null && surfaceRemote != null) {//同时存在远程画面，缩小本地画面
                    layoutLocalVideo.setVisibility(View.VISIBLE);
                    layoutLocalVideo.setPosition(
                            SURFACE_LOCAL_X_CONNECTED, SURFACE_LOCAL_Y_CONNECTED, SURFACE_LOCAL_WIDTH_CONNECTED, SURFACE_LOCAL_HEIGHT_CONNECTED);
                    surfaceLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                    surfaceLocal.setMirror(true);
                    surfaceLocal.requestLayout();
                }
            } else {//连接之前，主叫的话显示本地画面
                layoutLocalVideo.setPosition(
                        SURFACE_LOCAL_X_CONNECTING, SURFACE_LOCAL_Y_CONNECTING, SURFACE_LOCAL_WIDTH_CONNECTING, SURFACE_LOCAL_HEIGHT_CONNECTING);
                surfaceLocal.setScalingType(scalingType);
                surfaceLocal.setMirror(true);
                surfaceLocal.requestLayout();
                if (!isInitiator) {//连接之前，被叫的话就隐藏
                    layoutLocalVideo.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permission_granted = false;
                    DialogUtil.showDialog(this, getString(R.string.grid_dialog_permission_title), getString(R.string.grid_dialog_permission_content), getString(R.string.grid_dialog_permission_exit), getString(R.string.grid_dialog_permission_set), R.color.grid_txt_default, R.color.grid_txt_dialog_right, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == R.id.tv_dialog_left) {
                                DialogUtil.dismissDialog();
                                if (!isInitiator) {
                                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_REFUSE, "", Constant.SignalType.SIGNAL_MEMBER_REFUSED);
                                }
                                disConnect();
                            } else if (v.getId() == R.id.tv_dialog_right) {
                                DialogUtil.dismissDialog();
                                if (!isInitiator) {
                                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_REFUSE, "", Constant.SignalType.SIGNAL_MEMBER_REFUSED);
                                }
                                Utils.pemissionSet(BaseCallActivity.this);
                                disConnect();
                            }
                        }
                    });
                } else {
                    if (i == grantResults.length - 1) {
                        if (permission_granted) {
                            if (isInitiator) {
                                setmLocalStream();
                            } else {
                                playCallRing();
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 切换摄像头
     */
    protected void switchCamera() {
        videoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(final boolean b) {

            }

            @Override
            public void onCameraSwitchError(String s) {

            }
        });
    }

    /**
     * 接听视频
     */
    protected void answer() {
        type = TYPE_CHECKING;
        releaseMediaPlayer();
        setmLocalStream();
        mWebRtcClient.setRemoteDescription(parseSdp(data));
        mWebRtcClient.createAnswer();
        mHandler.removeCallbacksAndMessages(null);
        //假如一直在连接中，15秒后自动断开
        mRunnable = () -> {
            if (!DialogUtil.isShow()) {
                showLongToast(getString(R.string.grid_call_disconnect));
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "网络波动断开连接", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                disConnect();
            }
        };
        mHandler.postDelayed(mRunnable, CHECKING_TIME_OUT);
    }

    protected void answer(boolean isVideoCall) {
        this.isVideoCall = isVideoCall;
        answer();
        if (!isVideoCall) {
            if (layoutLocalVideo != null) {
                layoutLocalVideo.setVisibility(View.GONE);
            }
            if (layoutRemoteVideo != null) {
                layoutRemoteVideo.setVisibility(View.GONE);
            }
            sendSignal(Constant.SignalDestination.SIGNAL_Destination_CHANGE, "", Constant.SignalType.SIGNAL_CHANGE);
        }
    }

    /**
     * 挂断或者拒绝视频
     */
    protected void hangupOrRefuse() {
        if (isInitiator) {
            if (isAdd) {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
            } else {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REMOVE, "", Constant.SignalType.SIGNAL_REMOVED);
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
            }
        } else {
            if (type == TYPE_CALLED) {
                if (isAdd) {
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_ADDREFUSE, "1", Constant.SignalType.SIGNAL_ADD_MEMBER_REFUSED);
                } else {
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_REFUSE, "1", Constant.SignalType.SIGNAL_MEMBER_REFUSED);
                }
            } else if (type == TYPE_CALLING) {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
            } else if (type == TYPE_CHECKING) {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
            }
        }
        disConnect();
    }

    /**
     * 切换声音模式
     */
    protected void switchAudioMode() {
        switch (mRTCAudioManger.getCurrentMode()) {
            case RTCAudioManger.MODE_EARPIECE:
                mRTCAudioManger.changeToSpeakerMode();
                updateCallView();
                break;
            case RTCAudioManger.MODE_SPEAKER:
                mRTCAudioManger.changeToEarpieceMode();
                updateCallView();
                break;
            case RTCAudioManger.MODE_HEADSET:
                showLongToast(getString(R.string.grid_call_audio_mode_headset_insert));
                break;
        }
    }


    /**
     * 切换视频画面
     */
    protected void switchVideo() {
        if (!isVideoCall || layoutRemoteVideo == null
                || layoutLocalVideo == null
                || surfaceRemote == null
                || surfaceLocal == null) {
            return;
        }
        if (isNormal) {
            isNormal = false;
            layoutRemoteVideo.setPosition(
                    SURFACE_LOCAL_X_CONNECTED, SURFACE_LOCAL_Y_CONNECTED, SURFACE_LOCAL_WIDTH_CONNECTED, SURFACE_LOCAL_HEIGHT_CONNECTED);
            surfaceRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            layoutLocalVideo.setPosition(SURFACE_REMOTE_X, SURFACE_REMOTE_Y, SURFACE_REMOTE_WIDTH, SURFACE_REMOTE_HEIGHT);
            surfaceLocal.setScalingType(scalingType);
            surfaceRemote.setZOrderMediaOverlay(true);
            surfaceLocal.setZOrderMediaOverlay(false);
            surfaceLocal.requestLayout();
            surfaceRemote.requestLayout();
        } else {
            isNormal = true;
            layoutRemoteVideo.setPosition(SURFACE_REMOTE_X, SURFACE_REMOTE_Y, SURFACE_REMOTE_WIDTH, SURFACE_REMOTE_HEIGHT);
            surfaceRemote.setScalingType(scalingType);
            layoutLocalVideo.setPosition(
                    SURFACE_LOCAL_X_CONNECTED, SURFACE_LOCAL_Y_CONNECTED, SURFACE_LOCAL_WIDTH_CONNECTED, SURFACE_LOCAL_HEIGHT_CONNECTED);
            surfaceLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            surfaceRemote.setZOrderMediaOverlay(false);
            surfaceLocal.setZOrderMediaOverlay(true);
            surfaceLocal.requestLayout();
            surfaceRemote.requestLayout();
        }
    }

    /**
     * 语音/视频互相转换
     */
    protected void convertToSpeech() {
        if (isVideoCall) {
            isVideoCall = false;
            mWebRtcClient.stopVideoSource();
            if (layoutLocalVideo != null) {
                layoutLocalVideo.setVisibility(View.GONE);
            }
            if (layoutRemoteVideo != null) {
                layoutRemoteVideo.setVisibility(View.GONE);
            }
            sendSignal(Constant.SignalDestination.SIGNAL_Destination_CHANGE, "", Constant.SignalType.SIGNAL_CHANGE);
        }
    }

    private void invite() {
        mWebRtcClient.createOffer();
    }

    private void playCallRing() {
        if (mRTCAudioManger != null) {
            mRTCAudioManger.changeToRingtoneMode();
        }
        mMediaPlayer = MediaPlayer.create(this, Utils.getSystemDefultRingtoneUri(this));
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        }
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {800, 150, 400, 130}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onCreateSuccess(SessionDescription sessionDescription) {
//        Mlog.e(TAG, "onCreateSuccess");
//        //set成功回调下面的onSetSuccess方法
//        mWebRtcClient.setLocalDescription(sessionDescription);
//    }
//
//    @Override
//    public void onSetSuccess() {
//        Mlog.e(TAG, "onSetSuccess: ");
//        SessionDescription localSdp = mWebRtcClient.getmPeerConnection().getLocalDescription();
//        if (localSdp == null) {
//            return;
//        }
//        if (isInitiator) {
//            if (mWebRtcClient.getmPeerConnection().getRemoteDescription() != null) {
//                getLocalCandidate();
//                mWebRtcClient.drainCandidate();
//            } else {
//                if (isAdd) {
//                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_ADD, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ADD);
//                } else {
//                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_OFFERE, Sdp2data(localSdp), Constant.SignalType.SIGNAL_OFFERED);
//                }
//                mHandler.postDelayed(mRunnable, CALL_TIME_OUT);
//            }
//        } else {
//            if (isAdd) {
//                sendSignal(Constant.SignalDestination.SIGNAL_Destination_ADDANSWER, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ANSWERED);
//            } else {
//                sendSignal(Constant.SignalDestination.SIGNAL_Destination_ANSWER, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ANSWERED);
//            }
//            mWebRtcClient.drainCandidate();
//        }
//
//    }
//
//    @Override
//    public void onCreateFailure(String s) {
//        Mlog.e(TAG, "onCreateFailure: ");
//    }
//
//    @Override
//    public void onSetFailure(String s) {
//        Mlog.e(TAG, "onSetFailure: ");
//    }
//
//    @Override
//    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
//        Mlog.e(TAG, "onSignalingChange: ");
//    }
//
//    @Override
//    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//        Mlog.e(TAG, "onIceConnectionChange: " + iceConnectionState);
//        if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
//            mHandler.removeCallbacksAndMessages(null);
//            isCalling = true;
//            type = TYPE_CALLING;
//            iceConnected = true;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    updateCallView();
//                    updateVideoView();
//                }
//            });
//        } else if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED || iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
//            iceConnected = false;
//            mHandler.removeCallbacksAndMessages(null);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (!DialogUtil.isShow()) {
//                        showLongToast(getString(R.string.grid_call_disconnect));
//                        if (isInitiator && !isAdd) {
//                            sendSignal(Constant.SignalDestination.SIGNAL_Destination_REMOVE, "", Constant.SignalType.SIGNAL_REMOVED);
//                        }
//                        sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "网络波动断开连接", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
//                        disConnect();
//                    }
//                }
//            });
//
//        }
//    }
//
//    @Override
//    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
//        Mlog.e(TAG, "onIceGatheringChange: ");
//    }
//
//    @Override
//    public void onIceCandidate(IceCandidate iceCandidate) {
//        Mlog.e(TAG, "onIceCandidate: ");
//        if (isInitiator) {
//            if (mWebRtcClient.getmPeerConnection().getRemoteDescription() != null) {
//                sendLocalCandidate(iceCandidate);
//            } else {
//                mWebRtcClient.addLocalIceCandidate(iceCandidate);
//            }
//        } else {
//            sendLocalCandidate(iceCandidate);
//        }
//    }
//
//    @Override
//    public void onAddStream(MediaStream mediaStream) {
//        if (surfaceRemote == null) {
//            return;
//        }
//        Mlog.e(TAG, "onAddStream: ");
//        if (mWebRtcClient.getmPeerConnection() == null) {
//            return;
//        }
//        if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
//            return;
//        }
//        if (mediaStream.videoTracks.size() == 1) {
//            remoteVideoTrack = mediaStream.videoTracks.get(0);
//            remoteVideoTrack.setEnabled(true);
//            remoteVideoTrack.addSink(remoteProxyRenderer);
//        }
//    }
//
//    @Override
//    public void onRemoveStream(MediaStream mediaStream) {
//        Mlog.e(TAG, "onRemoveStream: ");
//        remoteVideoTrack = null;
//    }
//
//    @Override
//    public void onDataChannel(DataChannel dataChannel) {
//        Mlog.e(TAG, "onDataChannel: ");
//    }
//
//    @Override
//    public void onRenegotiationNeeded() {
//        Mlog.e(TAG, "onRenegotiationNeeded: ");
//    }

    private void setmLocalStream() {
        mRTCAudioManger.changeToCallMode();
        if (mWebRtcClient != null) {
            VideoCapturer videoCapturer = null;
            if (mPeerConnectionParameters.videoCallEnabled) {
                videoCapturer = getVideoCapturer();
            }
            mWebRtcClient.createPeerConnection(
                    surfaceLocal, remoteSinks, videoCapturer);
            if (BaseCallActivity.TYPE_INVITING == type) {
                invite();
            }
        }
    }

    private CameraVideoCapturer getVideoCapturer() {
        CameraEnumerator enumerator;
        if (Camera2Enumerator.isSupported(this)) {
            enumerator = new Camera2Enumerator(this);
        } else {
            enumerator = new Camera1Enumerator(false);
        }
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private void sendLocalCandidate(IceCandidate iceCandidate) {
        sendSignal(Constant.SignalDestination.SIGNAL_Destination_CANDIDATE,
                IceCandidate2Data(iceCandidate), Constant.SignalType.SIGNAL_CANDIDATE);
    }


    private void sendSignal(String destination, String data, String type) {
        if (ChatClient.getInstance().isConnected() && mWebRtcClient != null) {
            if (Constant.LoginInfo.isLogin && Constant.LoginInfo.user != null) {
                SignalMessage signalMessage = new SignalMessage(Constant.LoginInfo.user.getUserName(), userName, data, type, Constant.LoginInfo.user.getNickName(), Constant.LoginInfo.user.getUserIcon(), !isVideoCall);
                signalMessage.setLimit(limit);
                if (!TextUtils.isEmpty(extras)) {
                    signalMessage.setExtras(extras);
                }
                ChatClient.getInstance().getSignalManager().send(destination, GsonUtils.toJson(signalMessage));
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        try {
            if (null != vibrator) {
                vibrator.cancel();
                vibrator = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disConnect() {
        Log.e(TAG, "disConnect: ========================================");
        releaseMediaPlayer();
        if (signalCallBack != null) {
            ChatClient.getInstance().getSignalManager().removeConnectionListener(signalCallBack);
            signalCallBack = null;
        }
        if (mRTCAudioManger != null) {
            mRTCAudioManger.close();
            mRTCAudioManger = null;
        }

        if (mWebRtcClient != null) {
            mWebRtcClient.close();
            mWebRtcClient = null;
        }
        if (surfaceLocal != null) {
            surfaceLocal.release();
        }
        if (surfaceRemote != null) {
            surfaceRemote.release();
        }
        finish();
    }

    private void registerPhoneReceive() {
        if (mPhoneBroadcastReceive != null) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        mPhoneBroadcastReceive = new PhoneBroadcastReceive();
        registerReceiver(mPhoneBroadcastReceive, intentFilter);
    }

    private String Sdp2data(SessionDescription sessionDescription) {
        String data = "";
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", sessionDescription.type.canonicalForm());
            payload.put("sdp", sessionDescription.description);
            data = payload.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private SessionDescription parseSdp(String data) {
        SessionDescription sessionDescription = null;
        try {
            JSONObject payload = new JSONObject(data);
            sessionDescription = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sessionDescription;
    }

    private String IceCandidate2Data(IceCandidate iceCandidate) {
        String data = "";
        try {
            JSONObject payload = new JSONObject();
            payload.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            payload.put("candidate", iceCandidate.sdp);
            payload.put("sdpMid", iceCandidate.sdpMid);
            data = payload.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private IceCandidate parseIceCandidate(String data) {
        IceCandidate iceCandidate = null;
        try {
            JSONObject payload = new JSONObject(data);
            iceCandidate = new IceCandidate(
                    payload.getString("sdpMid"),
                    payload.getInt("sdpMLineIndex"),
                    payload.getString("candidate")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return iceCandidate;
    }

    @Override
    public void onLocalDescription(SessionDescription localSdp) {
        if (localSdp == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInitiator) {
                    if (isAdd) {
                        sendSignal(Constant.SignalDestination.SIGNAL_Destination_ADD, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ADD);
                    } else {
                        sendSignal(Constant.SignalDestination.SIGNAL_Destination_OFFERE, Sdp2data(localSdp), Constant.SignalType.SIGNAL_OFFERED);
                    }
                    mHandler.postDelayed(mRunnable, CALL_TIME_OUT);
                } else {
                    if (isAdd) {
                        sendSignal(Constant.SignalDestination.SIGNAL_Destination_ADDANSWER, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ANSWERED);
                    } else {
                        sendSignal(Constant.SignalDestination.SIGNAL_Destination_ANSWER, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ANSWERED);
                    }
                }
                if (mPeerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + mPeerConnectionParameters.videoMaxBitrate);
                    mWebRtcClient.setVideoMaxBitrate(mPeerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Log.e(TAG, "onIceCandidate: " + IceCandidate2Data(candidate));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendLocalCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.e(TAG, "onIceCandidatesRemoved: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mWebRtcClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                mWebRtcClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacksAndMessages(null);
                isCalling = true;
                type = TYPE_CALLING;
                iceConnected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCallView();
                        updateVideoView();
                    }
                });
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = false;
                mHandler.removeCallbacksAndMessages(null);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!DialogUtil.isShow()) {
                            showLongToast(getString(R.string.grid_call_disconnect));
                            if (isInitiator && !isAdd) {
                                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REMOVE, "", Constant.SignalType.SIGNAL_REMOVED);
                            }
                            sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "网络波动断开连接", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                            disConnect();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    public class PhoneBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent == null ? "" : intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                //拨打电话
            } else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    //耳机已插入
                    if (mRTCAudioManger != null) {
                        mRTCAudioManger.changeToHeadsetMode();
                    }
                } else if (state == 0) {
                    //耳机已拔出
                    if (mRTCAudioManger != null) {
                        mRTCAudioManger.changeToEarpieceMode();
                    }
                    updateCallView();
                } else {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                    telephonyManager.listen(new PhoneStateListener() {
                        @Override
                        public void onCallStateChanged(int state, String incomingNumber) {
                            super.onCallStateChanged(state, incomingNumber);
                            switch (state) {
                                case TelephonyManager.CALL_STATE_IDLE:

                                    break;
                                case TelephonyManager.CALL_STATE_OFFHOOK:
                                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", Constant.SignalType.SIGNAL_MEMBER_LEAVED);
                                    disConnect();
                                    break;
                                case TelephonyManager.CALL_STATE_RINGING:

                                    break;
                            }
                        }
                    }, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    }
}
