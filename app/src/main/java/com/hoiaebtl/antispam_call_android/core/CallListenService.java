package com.hoiaebtl.antispam_call_android.core;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hoiaebtl.antispam_call_android.R;

public class CallListenService extends Service {

    private CallReceiver callReceiver;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        // 1. Tạo "trạm gác" động
        callReceiver = new CallReceiver();
        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, filter);

        // 2. Chuyển Service thành Foreground để Android không "giết" nó
        startForeground(1, createNotification());
    }

    private Notification createNotification() {
        String channelId = "SafeCallChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Trạng thái SafeCall",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("SafeCall đang hoạt động")
                .setContentText("Đang bảo vệ bạn khỏi cuộc gọi rác")
                .setSmallIcon(R.mipmap.ic_launcher) // Thế có thể đổi icon sau
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Đảm bảo Service tự khởi động lại nếu bị tắt
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Không dùng Binding
    }
}