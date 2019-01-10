package cn.aorise.webrtc.ui;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.webrtc.SurfaceViewRenderer;

import cn.aorise.common.core.util.HandlerUtils;
import cn.aorise.webrtc.R;
import cn.aorise.webrtc.common.GlideCircleTransform;
import cn.aorise.webrtc.webrtc.PercentFrameLayout;
import cn.aorise.webrtc.webrtc.RTCAudioManger;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/05/03
 *     desc   : 默认的视频呼叫页面
 *     version: 1.0
 * </pre>
 */
public class DefaultCallActivity extends BaseCallActivity implements View.OnClickListener {


    private TextView tvUserName;
    private ImageView ivUserIcon;
    private TextView tvRefuse;
    private TextView tvAnswer;
    private TextView tvVoiceAnswer;
    private TextView tvCallType;
    private TextView tvHangUp;
    private TextView tvCallTime;
    private long currentSecond;
    private boolean frist = true;

    @Override
    protected void initView() {
        setContentView(R.layout.rtc_activity_call);
        tvUserName = findViewById(R.id.tv_user_name);
        ivUserIcon = findViewById(R.id.iv_user_icon);
        tvRefuse = findViewById(R.id.tv_refuse);
        tvAnswer = findViewById(R.id.tv_answer);
        tvVoiceAnswer = findViewById(R.id.tv_voice_answer);
        tvCallType = findViewById(R.id.tv_call_type);
        tvHangUp = findViewById(R.id.tv_hang_up);
        tvCallTime = findViewById(R.id.tv_call_time);
        updateCallView();
    }

    @Override
    protected void initEvent() {
        tvAnswer.setOnClickListener(this);
        tvHangUp.setOnClickListener(this);
        tvRefuse.setOnClickListener(this);
        tvVoiceAnswer.setOnClickListener(this);
    }

    @Override
    protected SurfaceViewRenderer setSurfaceLocal() {
        return findViewById(R.id.surface_local);
    }

    @Override
    protected SurfaceViewRenderer setSurfaceRemote() {
        return findViewById(R.id.surface_remote);
    }

    @Override
    protected PercentFrameLayout setLayoutLocalVideo() {
        return findViewById(R.id.layout_local_video);
    }

    @Override
    protected PercentFrameLayout setLayoutRemoteVideo() {
        return findViewById(R.id.layout_remote_video);
    }

    @Override
    protected void updateCallView() {
        switch (type) {
            case TYPE_INVITING:
                if (!TextUtils.isEmpty(name)) {
                    tvUserName.setText(name);
                }
                if (!TextUtils.isEmpty(imgUrl)) {
                    Glide.with(this).load(imgUrl)
                            .bitmapTransform(new GlideCircleTransform(this))
                            .error(R.drawable.rtc_def_facetime_favicon)
                            .placeholder(R.drawable.rtc_def_facetime_favicon)
                            .crossFade(500)
                            .into(ivUserIcon);
                }
                tvRefuse.setVisibility(View.GONE);
                tvAnswer.setVisibility(View.GONE);
                tvVoiceAnswer.setVisibility(View.GONE);
                tvCallType.setText(R.string.grid_call_wait_answer);
                break;
            case TYPE_CALLING:
                tvVoiceAnswer.setVisibility(View.GONE);
                tvCallType.setText(R.string.grid_text_calling);
                tvHangUp.setVisibility(View.VISIBLE);
                if (isVideoCall) {
                    tvRefuse.setVisibility(View.VISIBLE);
                    tvAnswer.setVisibility(View.VISIBLE);
                    tvAnswer.setText(R.string.visual_text_switch_voice_call);
                    tvRefuse.setText(R.string.rtc_text_switch_camera);
                    tvAnswer.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.rtc_ic_swap_video_voice), null, null);
                    tvRefuse.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.rtc_ic_swap_camera), null, null);
                } else {
                    if (isAudioEnabled) {
                        tvRefuse.setCompoundDrawablesWithIntrinsicBounds(null,
                                getResources().getDrawable(R.drawable.rtc_ic_mute), null, null);
                    } else {
                        tvRefuse.setCompoundDrawablesWithIntrinsicBounds(null,
                                getResources().getDrawable(R.drawable.rtc_ic_muted), null, null);
                    }
                    switch (mRTCAudioManger.getCurrentMode()) {
                        case RTCAudioManger.MODE_SPEAKER:
                            tvAnswer.setAlpha(1f);
                            tvAnswer.setCompoundDrawablesWithIntrinsicBounds(null,
                                    getResources().getDrawable(R.drawable.rtc_ic_speak), null, null);
                            break;

                        case RTCAudioManger.MODE_EARPIECE:
                            tvAnswer.setAlpha(1f);
                            tvAnswer.setCompoundDrawablesWithIntrinsicBounds(null,
                                    getResources().getDrawable(R.drawable.rtc_ic_hands_free), null, null);
                            break;

                        case RTCAudioManger.MODE_HEADSET:
                            tvAnswer.setAlpha(0.5f);
                            break;
                    }
                    tvAnswer.setText(R.string.grid_call_hands_free);
                    tvRefuse.setText(R.string.grid_call_mute);
                }
                if (frist) {
                    frist = false;
                    //通话计时器
                    HandlerUtils.runOnUiThreadDelay((new Runnable() {
                        @Override
                        public void run() {
                            currentSecond += 1000;
                            tvCallTime.setText(transformTime(currentSecond));
                            HandlerUtils.runOnUiThreadDelay(this, 1000);
                        }
                    }), 1000);
                }
                break;
            case TYPE_CALLED:
                if (!TextUtils.isEmpty(name)) {
                    tvUserName.setText(name);
                }
                if (!TextUtils.isEmpty(imgUrl)) {
                    Glide.with(this).load(imgUrl)
                            .bitmapTransform(new GlideCircleTransform(this))
                            .error(R.drawable.rtc_def_facetime_favicon)
                            .placeholder(R.drawable.rtc_def_facetime_favicon)
                            .crossFade(500)
                            .into(ivUserIcon);
                }
                tvHangUp.setVisibility(View.GONE);
                if (isVideoCall) {
                    tvCallType.setText(R.string.visual_text_invitation_you_video_call);
                } else {
                    tvCallType.setText(R.string.visual_text_invitation_you_voice_call);
                    tvVoiceAnswer.setVisibility(View.GONE);
                    tvAnswer.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.rtc_ic_answer_call), null, null);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_answer) {
            if (BaseCallActivity.TYPE_CALLING == type) {
                if (isVideoCall) {
                    convertToSpeech();
                } else {
                    switchAudioMode();
                }
            } else {
                tvCallTime.setText(cn.aorise.webrtc.R.string.grid_call_connecting);
                answer();
            }
        } else if (id == R.id.tv_hang_up) {
            hangupOrRefuse();
        } else if (id == R.id.tv_refuse) {
            if (BaseCallActivity.TYPE_CALLING == type) {
                if (isVideoCall) {
                    switchCamera();
                } else {
                    swipAudioEnabled();
                }
            } else {
                hangupOrRefuse();
            }
        } else if (id == R.id.tv_voice_answer) {
            tvCallTime.setText(cn.aorise.webrtc.R.string.grid_call_connecting);
            answer(false);
        }
    }

    /**
     * 根据毫秒返回时分秒
     *
     * @param time
     * @return
     */
    @SuppressLint("DefaultLocale")
    private String transformTime(long time) {
        time = time / 1000;//总秒数
        if (time < 3600) {
            int s = (int) (time % 60);//秒
            int m = (int) (time / 60);//分
            return String.format("%02d:%02d", m, s);
        } else {
            int h = (int) (time / 3600);//时
            int m = (int) (time % 3600 / 60);//分
            int s = (int) (time % 3600 % 60);//秒
            return String.format("%02d:%02d:%02d", h, m, s);
        }
    }

    @Override
    public void finish() {
        if (getString(R.string.grid_call_connecting).equals(tvCallTime.getText().toString()) || (signalMessageType != null && (signalMessageType.equals(cn.aorise.webrtc.api.Constant.SignalType.SIGNAL_REMOVED)
                || signalMessageType.equals(cn.aorise.webrtc.api.Constant.SignalType.SIGNAL_REFUSED)
                || signalMessageType.equals(cn.aorise.webrtc.api.Constant.SignalType.SIGNAL_ADD_MEMBER_REFUSED)
                || signalMessageType.equals(cn.aorise.webrtc.api.Constant.SignalType.SIGNAL_ROOM_FULL)))) {
            tvVoiceAnswer.setClickable(false);
            tvRefuse.setClickable(false);
            tvHangUp.setClickable(false);
            tvAnswer.setClickable(false);
            HandlerUtils.runOnUiThreadDelay(new Runnable() {
                @Override
                public void run() {
                    DefaultCallActivity.super.finish();
                }
            }, 1500);
        } else {
            super.finish();
        }
    }
}
