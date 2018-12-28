package com.mark.webrtc.simple;

import android.content.Context;
import android.support.multidex.MultiDexApplication;


import cn.aorise.webrtc.chat.ChatAPIConfig;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.chat.OnPushCallback;
import cn.aorise.webrtc.ui.PushActivity;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/12/24
 *     desc   : TODO
 *     version: 1.0
 * </pre>
 */
public class APP extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        initRTC(this);
    }

    private void initRTC(Context context) {
        ChatAPIConfig config = new ChatAPIConfig.Builder().signalUrl("ws://apidev.aorise.org/visual-video/ws/signaling")
                .token("dde675aad8a64049894e10f9f65fe291")
                .stunUrl("stun:222.244.147.121:3479")
                .turnUrl("turn:222.244.147.121:3479")
                .turnAccountAndPassword("test", "test")
                .meizuPush("1002048", "0cb789ed6dda4385936fbeeae4016d30")
                .umengPush("5ba9fcd3b465f5ae5e000147", "2a6c8157ef9a954c7608604eb3e3aa49")
                .xiaomiPush("2882303761517914834", "5971791479834")
                .build();
        ChatClient.getInstance().init(context, config);
        ChatClient.getInstance().setLogEnabled(BuildConfig.DEBUG);
        //设置自定义集成BaseCallActivity界面
//        ChatClient.getInstance().setCallActivity(VisualCallActivity.class);
        //点击离线未接来电处理记过，不添加这个监听器，默认直接拨号
        ChatClient.getInstance().setOnPushCallback(new OnPushCallback() {
            @Override
            public void handPushMessage(PushActivity.PushMessageBean pushMessageBean) {

            }
        });
        //假如做了一次登陆后免等。已经登陆，
//        if (UserManasger.getInstance().isLogin()) {
//            ChatClient.getInstance().login(UserManasger.getInstance().getUserId(), UserManasger.getInstance().getUserName(), "");
//        }
    }
}
