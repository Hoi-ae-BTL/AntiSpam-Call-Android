package com.hoiaebtl.antispam_call_android.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.hoiaebtl.antispam_call_android.R;

public class OverlayService extends Service {
    private static final String TAG = "OverlayService";
    private WindowManager windowManager;
    private View overlayView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String spamNumber = intent.getStringExtra("spam_number");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e(TAG, "LỖI: Chưa có quyền hiển thị trên ứng dụng khác!");
            return START_NOT_STICKY;
        }

        try {
            showOverlay(spamNumber);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị Overlay: " + e.getMessage());
            e.printStackTrace();
        }
        
        return START_NOT_STICKY;
    }

    private void showOverlay(String spamNumber) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Sử dụng ContextThemeWrapper để áp dụng Theme MaterialComponents cho Service
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.Theme_AntiSpamCallAndroid);
        LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
        
        overlayView = inflater.inflate(R.layout.overlay_spam_alert, null);

        TextView tvNumber = overlayView.findViewById(R.id.tv_spam_number);
        if (spamNumber != null && tvNumber != null) {
            tvNumber.setText(spamNumber);
        }

        ImageButton btnClose = overlayView.findViewById(R.id.btn_close_overlay);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> stopSelf());
        }

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP;
        params.y = 100;

        windowManager.addView(overlayView, params);
        Log.d(TAG, "Overlay đã được hiển thị.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi gỡ bỏ Overlay: " + e.getMessage());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
