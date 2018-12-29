package cn.aorise.webrtc.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.webrtc.SurfaceViewRenderer;

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

    private TextView tvName;
    private TextView tvAnswer;
    private TextView tvRefuse;
    private TextView tvCalling;
    private TextView tvConnect;
    private ImageView ivAvatar;
    private ImageView ivAnswer;
    private LinearLayout layoutUserInfo;
    private LinearLayout layoutCallInvite;
    private LinearLayout layoutCallRefuse;
    private LinearLayout layoutCallAnswer;

    @Override
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
        tvName.setText(name);
        Glide.with(this).load(imgUrl)
                .bitmapTransform(new GlideCircleTransform(this))
                .error(R.drawable.grid_def_facetime_favicon)
                .placeholder(R.drawable.grid_def_facetime_favicon)
                .crossFade(500)
                .into(ivAvatar);
        updateCallView();
    }

    @Override
    protected void initEvent() {
        layoutCallInvite.setOnClickListener(this);
        layoutCallRefuse.setOnClickListener(this);
        layoutCallAnswer.setOnClickListener(this);
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
                tvAnswer.setText(R.string.grid_call_hands_free);
                tvRefuse.setText(R.string.grid_call_hang_up);
                if (mRTCAudioManger != null) {
                    switch (mRTCAudioManger.getCurrentMode()) {
                        case RTCAudioManger.MODE_SPEAKER:
                            ivAnswer.setImageResource(R.drawable.grid_ic_hands_free);
                            break;
                        case RTCAudioManger.MODE_EARPIECE:
                            ivAnswer.setImageResource(R.drawable.grid_ic_speak);
                            break;
                    }
                    break;
                }
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.layout_call_answer) {
            if (isCalling) {
//                switchAudioMode();
                convertToSpeech();
            } else {
                //接受邀请
                layoutCallAnswer.setClickable(false);
                layoutCallRefuse.setClickable(false);
                tvConnect.setText(R.string.grid_call_connecting);
                answer();
            }

        } else if (id == R.id.layout_call_invite) {
            hangupOrRefuse();
        } else if (id == R.id.layout_call_refuse) {
            hangupOrRefuse();
        }
    }
}
