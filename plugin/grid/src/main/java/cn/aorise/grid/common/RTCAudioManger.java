package cn.aorise.grid.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;

import org.greenrobot.eventbus.EventBus;

import cn.aorise.grid.config.Constant;

import static android.media.AudioManager.FLAG_SHOW_UI;

/**
 * Created by 54926 on 2017/9/7.
 */

public class RTCAudioManger {
    public static final int MODE_SPEAKER = 0;//外放模式
    public static final int MODE_HEADSET = 1;//耳机模式
    public static final int MODE_EARPIECE = 2;//听筒模式

    private static RTCAudioManger RTCAudioManger;
    private AudioManager audioManager;
    private Context context;
    private int currentMode = MODE_EARPIECE;
    private BroadcastReceiver mWiredHeadsetReceiver;

    public static RTCAudioManger getManager() {
        if (RTCAudioManger == null) {
            synchronized (RTCAudioManger.class) {
                RTCAudioManger = new RTCAudioManger();
            }
        }
        return RTCAudioManger;
    }

    public void init(Context context) {
        this.context = context;
        initAudioManager();
    }

    /**
     * 初始化音频管理器
     */
    private void initAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(android.media.AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(android.media.AudioManager.MODE_IN_CALL);
        }
        changeToEarpieceMode();            //默认为扬声器播放
        registerWiredHeadSetReceiver();
    }

    private void registerWiredHeadSetReceiver() {
        mWiredHeadsetReceiver = new HeadsetReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(mWiredHeadsetReceiver, filter);
    }

    private void unRegisterWiredHeadSetReceiver() {
        context.unregisterReceiver(mWiredHeadsetReceiver);
        mWiredHeadsetReceiver = null;
    }

    public void close() {
        unRegisterWiredHeadSetReceiver();
    }

    /**
     * 获取当前播放模式
     *
     * @return
     */
    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * 切换到听筒模式
     */
    public void changeToEarpieceMode() {
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(android.media.AudioManager.MODE_IN_COMMUNICATION), android.media.AudioManager.FX_KEY_CLICK);
        } else {
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(android.media.AudioManager.MODE_IN_CALL), android.media.AudioManager.FX_KEY_CLICK);
        }
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode() {
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode() {
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }


    /**
     * 调大音量
     */
    public void raiseVolume() {
        int currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_RAISE, android.media.AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 调小音量
     */
    public void lowerVolume() {
        int currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_LOWER, android.media.AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    public void raiseVolume(Context context) {
        setVolume(context, true);

    }

    public void lowerVolume(Context context) {
        setVolume(context, false);
    }

    private void setVolume(Context context, boolean upVolume) {
        int volume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        if (upVolume) {
            volume += 1;
        } else {
            volume -= 1;
        }
        int maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
        if (volume >= 0 && volume <= maxVolume) {
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
        }
    }

    class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1) {
                        //耳机已插入
                        RTCAudioManger.changeToHeadsetMode();
                    } else if (state == 0) {
                        //耳机已拔出
                        RTCAudioManger.changeToEarpieceMode();
                        EventBus.getDefault().post(Constant.EventMessage.EVENTBUS_SWITCH_AUDIO_STATUS);
                    }
                    break;
            }
        }
    }

}
