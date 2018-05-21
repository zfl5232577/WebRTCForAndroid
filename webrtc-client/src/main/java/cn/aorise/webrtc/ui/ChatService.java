package cn.aorise.webrtc.ui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import cn.aorise.common.core.manager.ActivityManager;

public class ChatService extends Service {
    private static final String TAG = ChatService.class.getSimpleName();
    public static final String SERVICE_NAME = "cn.aorise.webrtc.ui.service.ChatService";
    private DaemonBroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            foregroundService();
        }
        registerProtectBroadcast();
        return START_STICKY;
    }

    private void foregroundService() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            InnerService.startForeground(this);
            startService(new Intent(this, InnerService.class));
        } else {
            startForeground(0x999, new Notification());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void registerProtectBroadcast() {
        if (mReceiver != null) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        mReceiver = new DaemonBroadcastReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    public class DaemonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent == null ? "" : intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    ActivityManager.getInstance().findActivity(DaemonActivity.class);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    DaemonActivity.startActivity(context);
                    break;
            }

        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        stopForeground(true);
        super.onDestroy();
    }

    public static class InnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(this);
            stopForeground(true);
            // stop self to clear the notification
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        public static void startForeground(Service context) {
            Notification.Builder builder = new Notification.Builder(context);
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                    new Intent(context, MainActivity.class), 0);
//            builder.setContentIntent(contentIntent);
            builder.setOngoing(true);
            builder.setSmallIcon(context.getApplicationContext().getApplicationInfo().icon);
            builder.setTicker("Foreground Service Start");
            builder.setContentTitle(context.getString(context.getApplicationContext().getApplicationInfo().labelRes) + "被叫服务");
            builder.setContentText("保证视频呼叫顺畅，请勿关闭应用");
            Notification notification = builder.build();
            context.startForeground(0x999,notification);
        }
    }
}
