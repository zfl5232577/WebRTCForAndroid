package cn.aorise.webrtc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.aorise.webrtc.chat.SignalManager;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/23
 *     desc   : APP需要继承该类，来处理登陆冲突的逻辑
 *     version: 1.0
 * </pre>
 */
public abstract class ChatReceive extends BroadcastReceiver{
    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent == null ? "" : intent.getAction();
        if (action == null) {
            return;
        }
        switch (action){
            case SignalManager.SIGNAL_DUPLICATE_CONNECTION:
                duplicateConnection();
                break;
        }
    }


    /**
     * 重复登陆后的逻辑方法，可以弹窗退出登陆或者重复登录
     */
    protected abstract void duplicateConnection();
}
