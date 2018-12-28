package cn.aorise.webrtc.webrtc;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

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
    private int defultMode;

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
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        defultMode = audioManager.getMode();
        audioManager.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        changeToSpeakerMode();            //默认为扬声器播放
    }

    public void changeToRingtoneMode(){
        audioManager.setMode(AudioManager.MODE_RINGTONE);
    }

    /**
     * 初始化音频管理器
     */
    public void changeToCallMode() {
        if (audioManager==null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }




    public void close() {
        audioManager.setMode(defultMode);
        audioManager.abandonAudioFocus(null);
        changeToSpeakerMode();
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
        if (audioManager==null){
            return;
        }
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode() {
        if (audioManager==null){
            return;
        }
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode() {
        if (audioManager==null){
            return;
        }
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }


    /**
     * 调大音量
     */
    public void raiseVolume() {
        if (audioManager==null){
            return;
        }
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 调小音量
     */
    public void lowerVolume() {
        if (audioManager==null){
            return;
        }
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    public void raiseVolume(Context context) {
        setVolume(context, true);

    }

    public void lowerVolume(Context context) {
        setVolume(context, false);
    }

    private void setVolume(Context context, boolean upVolume) {
        if (audioManager==null){
            return;
        }
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (upVolume) {
            volume += 1;
        } else {
            volume -= 1;
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (volume >= 0 && volume <= maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
        }
    }

}
