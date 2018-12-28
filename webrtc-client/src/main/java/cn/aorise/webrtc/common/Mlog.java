package cn.aorise.webrtc.common;

import android.util.Log;

import cn.aorise.webrtc.BuildConfig;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/21
 *     desc   : 日志打印类
 *     version: 1.0
 * </pre>
 */
public class Mlog {
    public static boolean enabled = BuildConfig.DEBUG;

    public static void setLogEnabled(boolean enabled){
        Mlog.enabled = enabled;
    }
    public static void e(String tag, String msg) {
        if (enabled) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (enabled) {
            Log.i(tag, msg);
        }
    }
}
