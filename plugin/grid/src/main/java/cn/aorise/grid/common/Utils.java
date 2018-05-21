package cn.aorise.grid.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

import static cn.aorise.grid.GridApplication.mContext;

/**
 * Created by Administrator on 2015/11/6 0006.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * common
     **********************************************************************************************/

    /**
     * 获取系统默认铃声的Uri
     */
    public static Uri getSystemDefultRingtoneUri(Context context) {
        return RingtoneManager.getActualDefaultRingtoneUri(context,
                RingtoneManager.TYPE_RINGTONE);
    }

    /**
     * private
     **********************************************************************************************/

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager manager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<android.app.ActivityManager.RunningServiceInfo> myList = manager.getRunningServices(Integer.MAX_VALUE);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * 获取未授权的权限
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static List<String> findUnAuthPermissions(Context context, String... permissions) {
        List<String> unAuthPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                unAuthPermissions.add(permission);
            }
        }
        return unAuthPermissions;
    }

    /**
     * 跳转到权限设置界面
     */
    public static void pemissionSet(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(intent);
    }

    public static void exit(Activity activity) {
        activity.finish();
    }

    /**
     * 获取电话状态
     */
    public static int getCallState() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
        return telephonyManager.getCallState();
    }

    /**
     * 获取电话状态
     */
    public static boolean isCalling() {
        int state = getCallState();
        if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 设置手机休眠后WLAN不断开
     */
    public static void WifiNeverDormancy(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        int value = Settings.System.getInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
        if (Settings.System.WIFI_SLEEP_POLICY_NEVER != value) {
            Settings.System.putInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);
        }
    }
}
