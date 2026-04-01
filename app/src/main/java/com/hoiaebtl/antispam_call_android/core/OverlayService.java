package com.hoiaebtl.antispam_call_android.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.hoiaebtl.antispam_call_android.R;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private View layoutCollapsed;
    private View layoutExpanded;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String spamNumber = intent.getStringExtra("spam_number");
        showOverlay(spamNumber);
        return START_NOT_STICKY;
    }

    private void showOverlay(String spamNumber) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.overlay_spam_alert, null);

        layoutCollapsed = overlayView.findViewById(R.id.layout_collapsed);
        layoutExpanded = overlayView.findViewById(R.id.layout_expanded);

        TextView tvNumberSmall = overlayView.findViewById(R.id.tv_spam_number_small);
        TextView tvNumberFull = overlayView.findViewById(R.id.tv_spam_number_full);

        String displayText = "Số: " + (spamNumber != null ? spamNumber : "Lừa đảo");
        tvNumberSmall.setText(displayText);
        tvNumberFull.setText(displayText);

        // KHI NHẤN VÀO BANNER NHỎ -> HIỆN CẢNH BÁO CHI TIẾT
        layoutCollapsed.setOnClickListener(v -> {
            layoutCollapsed.setVisibility(View.GONE);
            layoutExpanded.setVisibility(View.VISIBLE);
        });

        // KHI NHẤN "ĐÃ HIỂU" TRONG CẢNH BÁO CHI TIẾT -> ĐÓNG OVERLAY
        Button btnCloseFull = overlayView.findViewById(R.id.btn_close_full);
        btnCloseFull.setOnClickListener(v -> stopSelf());

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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP;
        params.y = 150; // Vị trí Banner hiện ngay dưới thanh trạng thái

        windowManager.addView(overlayView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}