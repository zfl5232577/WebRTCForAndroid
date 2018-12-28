package cn.aorise.webrtc.ui;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import cn.aorise.webrtc.common.Utils;


/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/17
 *     desc   : 保活服务，利用JobService拉活进程
 *     version: 1.0
 * </pre>
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class KeepLiveService extends JobService {
    private static final String TAG = "KeepLiveService";

    public KeepLiveService() {
    }

    public boolean onStartJob(JobParameters var1) {
        Log.e(TAG, "onStartJob: 拉活应用" );
        if (!Utils.isServiceWork(this , ChatService.SERVICE_NAME)) {
            Intent service = new Intent(this, ChatService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
        }
        return false;
    }

    public int onStartCommand(Intent var1, int var2, int var3) {
        return START_STICKY;
    }

    public boolean onStopJob(JobParameters var1) {
        return false;
    }
}
