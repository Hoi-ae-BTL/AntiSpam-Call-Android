package com.hoiaebtl.antispam_call_android.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
        if (intent == null) return START_NOT_STICKY;

        String spamNumber = intent.getStringExtra("spam_number");
        String spamLabel = intent.getStringExtra("spam_label");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e(TAG, "Chưa có quyền Overlay");
            return START_NOT_STICKY;
        }

        showOverlay(spamNumber, spamLabel);
        return START_NOT_STICKY;
    }

    private void showOverlay(String spamNumber, String spamLabel) {
        // Gỡ bỏ overlay cũ nếu đang hiển thị
        if (overlayView != null && windowManager != null) {
            try { windowManager.removeView(overlayView); } catch (Exception ignored) {}
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.Theme_AntiSpamCallAndroid);
        LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
        
        overlayView = inflater.inflate(R.layout.overlay_spam_alert, null);

        // Hiển thị số điện thoại
        TextView tvNumber = overlayView.findViewById(R.id.tv_spam_number);
        if (spamNumber != null) tvNumber.setText(spamNumber);

        // Hiển thị nhãn lừa đảo (Lấy từ Firebase hoặc mặc định)
        TextView tvType = overlayView.findViewById(R.id.tv_spam_type);
        if (spamLabel != null) {
            tvType.setText(spamLabel);
        } else {
            tvType.setText("Cảnh báo lừa đảo!");
        }

        ImageButton btnClose = overlayView.findViewById(R.id.btn_close_overlay);
        btnClose.setOnClickListener(v -> stopSelf());

        int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP;
        params.y = 100;

        windowManager.addView(overlayView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            try { windowManager.removeView(overlayView); } catch (Exception ignored) {}
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
