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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.List;

import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.webrtc.R;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.common.DialogUtil;
import cn.aorise.webrtc.common.GlideCircleTransform;
import cn.aorise.webrtc.common.GridBaseActivity;
import cn.aorise.webrtc.common.Mlog;
import cn.aorise.webrtc.common.Utils;
import cn.aorise.webrtc.provider.LifecycleEvent;
import cn.aorise.webrtc.signal.SignalCallBack;
import cn.aorise.webrtc.signal.SignalMessage;
import cn.aorise.webrtc.stomp.StompMessage;
import cn.aorise.webrtc.webrtc.PeerConnectionListener;
import cn.aorise.webrtc.webrtc.PeerConnectionParameters;
import cn.aorise.webrtc.webrtc.PercentFrameLayout;
import cn.aorise.webrtc.webrtc.RTCAudioManger;
import cn.aorise.webrtc.webrtc.WebRtcClient;

public class CallActivity extends GridBaseActivity implements View.OnClickListener, PeerConnectionListener {
    private static final String TAG = CallActivity.class.getSimpleName();
    private static final int CALL_TIME_OUT = 60000;//主叫超市
    private static final int CALLED_TIME_OUT = 55000;//被叫超时

    public static final int TYPE_INVITING = 1;
    public static final int TYPE_CALLING = 2;
    public static final int TYPE_CALLED = 3;

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

    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";

    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;

    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA"
    };
    private boolean permission_granted = true;

    private static WebRtcClient mWebRtcClient;
    private PeerConnectionParameters mPeerConnectionParameters;
    private MediaStream mLocalStream;
    private MediaPlayer mMediaPlayer;
    private Vibrator vibrator;
    private RTCAudioManger mRTCAudioManger;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private boolean isCalling = false;
    private boolean isInitiator;
    private String userName;
    private String imgUrl;
    private String name;
    private int type;
    private String data;

    private boolean iceConnected = false;
    private RendererCommon.ScalingType scalingType;
    private EglBase rootEglBase;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private TextView tvName;
    private TextView tvAnswer;
    private TextView tvRefuse;
    private TextView tvCalling;
    private TextView tvConnect;
    private ImageView ivAvatar;
    private ImageView ivAnswer;
    private SurfaceViewRenderer surfaceLocal;
    private SurfaceViewRenderer surfaceRemote;
    private PercentFrameLayout layoutLocalVideo;
    private PercentFrameLayout layoutRemoteVideo;
    private LinearLayout layoutUserInfo;
    private LinearLayout layoutCallInvite;
    private LinearLayout layoutCallRefuse;
    private LinearLayout layoutCallAnswer;

    private SignalCallBack signalCallBack;
    private PhoneBroadcastReceive mPhoneBroadcastReceive;
    private boolean isInited = false;
    private VideoCapturerAndroid videoCapturer;
    private boolean isNormal = true;


    public static Intent getIntent(Context context, Class<? extends Activity> cls, String userName, String imgUrl, String name, int type) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("userName", userName);
        intent.putExtra("imgUrl", imgUrl);
        intent.putExtra("type", type);
        intent.putExtra("name", name);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    protected void initData() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        imgUrl = intent.getStringExtra("imgUrl");
        type = intent.getIntExtra("type", TYPE_INVITING);
        name = intent.getStringExtra("name");
        if (CallActivity.TYPE_INVITING == type) {
            isInitiator = true;
            mRunnable = () -> {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REMOVE, "", "");
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
                disConnect();
            };
        } else {
            isInitiator = false;
            data = intent.getStringExtra("data");
            mRunnable = () -> {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REFUSE, "无人接听", "");
                disConnect();
            };
            mHandler.postDelayed(mRunnable, CALLED_TIME_OUT);
        }

        registerPhoneReceive();
    }

    protected void initView() {
        setContentView(R.layout.grid_activity_call);
        tvName = findViewById(R.id.tv_name);
        ivAvatar = findViewById(R.id.iv_avatar);
        ivAnswer = findViewById(R.id.iv_answer);
        tvAnswer = findViewById(R.id.tv_answer);
        tvRefuse = findViewById(R.id.tv_refuse);
        tvCalling = findViewById(R.id.tv_calling);
        tvConnect = findViewById(R.id.tv_connect);
        layoutUserInfo = findViewById(R.id.layout_user_info);
        layoutCallInvite = findViewById(R.id.layout_call_invite);
        layoutCallRefuse = findViewById(R.id.layout_call_refuse);
        layoutCallAnswer = findViewById(R.id.layout_call_answer);
        layoutLocalVideo = findViewById(R.id.layout_local_video);
        layoutRemoteVideo = findViewById(R.id.layout_remote_video);
        surfaceLocal = findViewById(R.id.surface_local);
        surfaceRemote = findViewById(R.id.surface_remote);
        tvName.setText(name);
        Glide.with(this).load(imgUrl)
                .bitmapTransform(new GlideCircleTransform(this))
                .error(R.drawable.grid_def_facetime_favicon)
                .placeholder(R.drawable.grid_def_facetime_favicon)
                .crossFade(500)
                .into(ivAvatar);
        updateCallView();
        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
        rootEglBase = EglBase.create();
        surfaceLocal.init(rootEglBase.getEglBaseContext(), null);
        surfaceRemote.init(rootEglBase.getEglBaseContext(), null);
        surfaceLocal.setZOrderMediaOverlay(true);
        updateVideoView();
        if (ChatClient.getInstance().isConnected()) {
            init();
        }
    }

    private void init() {
        if (isInited) {
            return;
        }
        isInited = true;
        mPeerConnectionParameters = new PeerConnectionParameters(
                true, false, HD_VIDEO_WIDTH, HD_VIDEO_HEIGHT, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        mWebRtcClient = new WebRtcClient(this.getApplicationContext(), rootEglBase.getEglBaseContext(), mPeerConnectionParameters, this);
        mRTCAudioManger = RTCAudioManger.getManager();
        mRTCAudioManger.init(this.getApplicationContext());
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

    private void updateVideoView() {
        layoutRemoteVideo.setPosition(SURFACE_REMOTE_X, SURFACE_REMOTE_Y, SURFACE_REMOTE_WIDTH, SURFACE_REMOTE_HEIGHT);
        surfaceRemote.setScalingType(scalingType);
        surfaceRemote.setMirror(false);

        if (iceConnected) {
            layoutLocalVideo.setPosition(
                    SURFACE_LOCAL_X_CONNECTED, SURFACE_LOCAL_Y_CONNECTED, SURFACE_LOCAL_WIDTH_CONNECTED, SURFACE_LOCAL_HEIGHT_CONNECTED);
            surfaceLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        } else {
            layoutLocalVideo.setPosition(
                    SURFACE_LOCAL_X_CONNECTING, SURFACE_LOCAL_Y_CONNECTING, SURFACE_LOCAL_WIDTH_CONNECTING, SURFACE_LOCAL_HEIGHT_CONNECTING);
            surfaceLocal.setScalingType(scalingType);
        }

        surfaceLocal.setMirror(true);
        surfaceLocal.requestLayout();
        surfaceRemote.requestLayout();
    }

    protected void initEvent() {
        layoutCallInvite.setOnClickListener(this);
        layoutCallRefuse.setOnClickListener(this);
        layoutCallAnswer.setOnClickListener(this);
        signalCallBack = new SignalCallBack() {
            @Override
            public void onOpen(LifecycleEvent lifecycleEvent) {

            }

            @Override
            public void onMessage(StompMessage stompMessage) {
                Log.e(TAG, "onMessage: " + stompMessage.getPayload());
                SignalMessage signalMessage = GsonUtils.fromJson(stompMessage.getPayload(), SignalMessage.class);
                String type = signalMessage.getType();
                if (Constant.SignalType.SIGNAL_MEMBER_REFUSED.equals(type)) {
                    showToast(signalMessage.getData());
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_REFUSED.equals(type)) {
                    showToast(signalMessage.getData());
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_ANSWERED.equals(type)) {
                    mHandler.removeCallbacksAndMessages(null);
                    mWebRtcClient.setRemoteDescription(parseSdp(signalMessage.getData()));
                } else if (Constant.SignalType.SIGNAL_REMOVED.equals(type)) {
                    String data = signalMessage.getData();
                    showToast(data);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_ROOM_DISSOLVED.equals(type)) {
                    String data = signalMessage.getData();
                    showToast(data);
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_MEMBER_LEAVED.equals(type)) {
                    String data = signalMessage.getData();
                    showToast(data);
                    sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
                    disConnect();
                } else if (Constant.SignalType.SIGNAL_CANDIDATE.equals(type)) {
                    mWebRtcClient.addRemoteIceCandidate(parseIceCandidate(signalMessage.getData()));
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
                                Utils.pemissionSet(CallActivity.this);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.layout_call_answer) {
            if (isCalling) {
//                //RTCAudioManager切换免提，扬声器，耳机播放声音
                switchAudioMode();
                //切换摄像头
//                videoCapturer.switchCamera(new VideoCapturerAndroid.CameraSwitchHandler() {
//                    @Override
//                    public void onCameraSwitchDone(final boolean b) {
//
//                    }
//
//                    @Override
//                    public void onCameraSwitchError(String s) {
//
//                    }
//                });
            } else {
                //接受邀请
                layoutCallAnswer.setClickable(false);
                layoutCallRefuse.setClickable(false);
                tvConnect.setText(R.string.grid_call_connecting);
                releaseMediaPlayer();
                setmLocalStream();
                mWebRtcClient.setRemoteDescription(parseSdp(data));
                mWebRtcClient.createAnswerSdp();
                mHandler.removeCallbacksAndMessages(null);
            }

        } else if (id == R.id.layout_call_invite) {
            layoutCallInvite.setClickable(false);
            sendSignal(Constant.SignalDestination.SIGNAL_Destination_REMOVE, "", "");
            sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
            disConnect();
        } else if (id == R.id.layout_call_refuse) {
            layoutCallAnswer.setClickable(false);
            layoutCallRefuse.setClickable(false);
            if (type == TYPE_CALLED) {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_REFUSE, "", Constant.SignalType.SIGNAL_MEMBER_REFUSED);
            } else if (type == TYPE_CALLING) {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
            }
            disConnect();
        }
    }

    private void switchAudioMode() {
        switch (mRTCAudioManger.getCurrentMode()) {
            case RTCAudioManger.MODE_EARPIECE:
                mRTCAudioManger.changeToSpeakerMode();
                ivAnswer.setImageResource(R.drawable.grid_ic_hands_free);
                break;
            case RTCAudioManger.MODE_SPEAKER:
                mRTCAudioManger.changeToEarpieceMode();
                ivAnswer.setImageResource(R.drawable.grid_ic_speak);
                break;
            case RTCAudioManger.MODE_HEADSET:
                showToast(getString(R.string.grid_call_audio_mode_headset_insert));
                break;
        }
    }

    /**
     * 切换视频画面
     */
    protected void switchVideo() {
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

    private void updateCallView() {
        switch (type) {
            case TYPE_INVITING:
                layoutUserInfo.setVisibility(View.VISIBLE);
                layoutCallInvite.setVisibility(View.VISIBLE);
                layoutCallRefuse.setVisibility(View.GONE);
                layoutCallAnswer.setVisibility(View.GONE);
                tvCalling.setVisibility(View.GONE);
                tvConnect.setText(R.string.grid_call_wait_answer);
                break;
            case TYPE_CALLING:
                layoutUserInfo.setVisibility(View.GONE);
                layoutCallInvite.setVisibility(View.GONE);
                layoutCallRefuse.setVisibility(View.VISIBLE);
                layoutCallAnswer.setVisibility(View.VISIBLE);
                layoutCallRefuse.setClickable(true);
                layoutCallAnswer.setClickable(true);
                tvCalling.setVisibility(View.VISIBLE);
                tvCalling.setText("和" + name + "通话中");
                ivAnswer.setImageResource(R.drawable.grid_ic_speak);
                tvAnswer.setText(R.string.grid_call_hands_free);
                tvRefuse.setText(R.string.grid_call_hang_up);
                break;
            case TYPE_CALLED:
                layoutUserInfo.setVisibility(View.VISIBLE);
                layoutCallInvite.setVisibility(View.GONE);
                layoutCallRefuse.setVisibility(View.VISIBLE);
                layoutCallAnswer.setVisibility(View.VISIBLE);
                tvCalling.setVisibility(View.GONE);
                ivAnswer.setImageResource(R.drawable.grid_ic_answer);
                tvConnect.setText(R.string.grid_call_receive_video);
                tvAnswer.setText(R.string.grid_call_answer);
                tvRefuse.setText(R.string.grid_call_refuse);
                break;
        }
    }

    private void invite() {
        mWebRtcClient.createOfferSdp();
    }

    private void playCallRing() {
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

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Mlog.e(TAG, "onCreateSuccess");
        //set成功回调下面的onSetSuccess方法
        mWebRtcClient.setLocalDescription(sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        Mlog.e(TAG, "onSetSuccess: ");
        SessionDescription localSdp = mWebRtcClient.getmPeerConnection().getLocalDescription();
        if (localSdp == null) {
            return;
        }
        if (isInitiator) {
            if (mWebRtcClient.getmPeerConnection().getRemoteDescription() != null) {
                getLocalCandidate();
                mWebRtcClient.drainCandidate();
            } else {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_OFFERE, Sdp2data(localSdp), "");
                mHandler.postDelayed(mRunnable, CALL_TIME_OUT);
            }
        } else {
            if (mWebRtcClient.getmPeerConnection().getLocalDescription() != null) {
                sendSignal(Constant.SignalDestination.SIGNAL_Destination_ANSWER, Sdp2data(localSdp), Constant.SignalType.SIGNAL_ANSWERED);
                mWebRtcClient.drainCandidate();
            }
        }

    }

    @Override
    public void onCreateFailure(String s) {
        Mlog.e(TAG, "onCreateFailure: ");
    }

    @Override
    public void onSetFailure(String s) {
        Mlog.e(TAG, "onSetFailure: ");
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Mlog.e(TAG, "onSignalingChange: ");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Mlog.e(TAG, "onIceConnectionChange: ");
        if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
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
        } else if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED || iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
            showToast("网络不稳定，请耐心等待");
            //            iceConnected = false;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showToast(getString(R.string.grid_call_disconnect));
//                    if (isInitiator) {
//                        sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
//                    }
//                    disConnect();
//                }
//            });

        }
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Mlog.e(TAG, "onIceGatheringChange: ");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Mlog.e(TAG, "onIceCandidate: ");
        if (isInitiator) {
            if (mWebRtcClient.getmPeerConnection().getRemoteDescription() != null) {
                sendLocalCandidate(iceCandidate);
            } else {
                mWebRtcClient.addLocalIceCandidate(iceCandidate);
            }
        } else {
            sendLocalCandidate(iceCandidate);
        }
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Mlog.e(TAG, "onAddStream: ");
        if (mWebRtcClient.getmPeerConnection() == null) {
            return;
        }
        if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
            return;
        }
        if (mediaStream.videoTracks.size() == 1) {
            remoteVideoTrack = mediaStream.videoTracks.get(0);
            remoteVideoTrack.setEnabled(true);
            remoteVideoTrack.addRenderer(new VideoRenderer(surfaceRemote));
        }
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Mlog.e(TAG, "onRemoveStream: ");
        remoteVideoTrack = null;
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Mlog.e(TAG, "onDataChannel: ");
    }

    @Override
    public void onRenegotiationNeeded() {
        Mlog.e(TAG, "onRenegotiationNeeded: ");
    }

    private void setmLocalStream() {
        if (mWebRtcClient != null) {
            mLocalStream = mWebRtcClient.createLocalStream();
            if (mLocalStream != null) {
                MediaConstraints audioConstraints = new MediaConstraints();
                MediaConstraints videoConstraints = new MediaConstraints();

                if (mPeerConnectionParameters.videoCodecHwAcceleration) {
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_WIDTH_CONSTRAINT, "640"));
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_WIDTH_CONSTRAINT, "1920"));
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_HEIGHT_CONSTRAINT, "480"));
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_HEIGHT_CONSTRAINT, "1080"));
                }
                videoCapturer = getVideoCapturer();
                localVideoTrack = mWebRtcClient.createVideoTrack(mWebRtcClient.createVideoSource(videoCapturer, videoConstraints));
                localVideoTrack.addRenderer(new VideoRenderer(surfaceLocal));

                mLocalStream.addTrack(localVideoTrack);
                mLocalStream.addTrack(mWebRtcClient.createAudioTrack(audioConstraints));

                mWebRtcClient.setLocalStream(mLocalStream);

                if (CallActivity.TYPE_INVITING == type) {
                    invite();
                }
            }
        }
    }


    private VideoCapturerAndroid getVideoCapturer() {
        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
        String frontCameraDeviceName =
                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
        if (frontCameraDeviceName != null) {
            cameraDeviceName = frontCameraDeviceName;
        }
        return VideoCapturerAndroid.create(cameraDeviceName, null, rootEglBase.getEglBaseContext());
    }

    private void getLocalCandidate() {
        for (IceCandidate iceCandidate : mWebRtcClient.getQueuedLocalCandidates()) {
            sendLocalCandidate(iceCandidate);
        }
    }

    private void sendLocalCandidate(IceCandidate iceCandidate) {
        sendSignal(Constant.SignalDestination.SIGNAL_Destination_CANDIDATE,
                IceCandidate2Data(iceCandidate), Constant.SignalType.SIGNAL_CANDIDATE);
    }


    private void sendSignal(String destination, String data, String type) {
        if (ChatClient.getInstance().isConnected()) {
            if (Constant.LoginInfo.isLogin && Constant.LoginInfo.user != null) {
                SignalMessage signalMessage = new SignalMessage(Constant.LoginInfo.user.getUserName(), userName, data, type, Constant.LoginInfo.user.getNickName(), Constant.LoginInfo.user.getUserIcon());
                mWebRtcClient.sendSignal(destination, GsonUtils.toJson(signalMessage));
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebRtcClient != null) {
            mWebRtcClient.stopVideoSource();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebRtcClient != null) {
            mWebRtcClient.startVideoSource();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        ChatClient.getInstance().getSignalManager().removeConnectionListener(signalCallBack);
        releaseMediaPlayer();
        if (mPhoneBroadcastReceive != null) {
            unregisterReceiver(mPhoneBroadcastReceive);
            mPhoneBroadcastReceive = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (mRTCAudioManger != null) {
            mRTCAudioManger.changeToSpeakerMode();
            mRTCAudioManger.close();
            mRTCAudioManger = null;
        }

        if (mWebRtcClient != null) {
            mWebRtcClient.disposePeerConnection();
            mWebRtcClient = null;
        }
        if (surfaceLocal != null) {
            surfaceLocal.release();
        }
        if (surfaceRemote != null) {
            surfaceRemote.release();
        }
        if (rootEglBase != null) {
            rootEglBase.release();
            rootEglBase = null;
        }
//        try {
//            videoCapturer.stopCapture();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
                    mRTCAudioManger.changeToHeadsetMode();
                } else if (state == 0) {
                    //耳机已拔出
                    mRTCAudioManger.changeToEarpieceMode();
                    ivAnswer.setImageResource(R.drawable.grid_ic_speak);
                }
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
                                sendSignal(Constant.SignalDestination.SIGNAL_Destination_LEAVE, "", "");
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
