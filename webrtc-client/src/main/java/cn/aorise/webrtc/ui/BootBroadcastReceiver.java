package cn.aorise.webrtc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.aorise.common.core.util.SPUtils;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.common.Utils;

/**
 * Created by 54926 on 2017/9/7.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
    private static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED) || intent.getAction().equals(ACTION_USER_PRESENT)) {

            if (Constant.LoginInfo.isLogin) {
                if (!Utils.isServiceWork(context, ChatService.SERVICE_NAME)) {
                    Intent service = new Intent(context, ChatService.class);
                    context.startService(service);
                }
            }
        }else if (ACTION_CONNECTIVITY_CHANGE.equals(action)) {
            if (Constant.LoginInfo.isLogin) {
                if (!Utils.isServiceWork(context, ChatService.SERVICE_NAME)) {
                    Intent service = new Intent(context, ChatService.class);
                    context.startService(service);
                }
            }
        }
    }
}
