package com.hoiaebtl.antispam_call_android.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.hoiaebtl.antispam_call_android.R;

public class MainActivity extends AppCompatActivity {

    // Mã định danh để nhận biết kết quả trả về khi người dùng bấm "Cho phép"
    private static final int PERMISSION_REQUEST_CODE = 100;
    private com.hoiaebtl.antispam_call_android.core.CallReceiver callReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ngay khi mở app lên, gọi hàm kiểm tra và xin quyền
        checkAndRequestPermissions();
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Khởi tạo Receiver và giăng lưới bắt sự kiện PHONE_STATE
        callReceiver = new com.hoiaebtl.antispam_call_android.core.CallReceiver();
        android.content.IntentFilter filter = new android.content.IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(callReceiver, filter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Khi ẩn app thì thu lưới lại
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
    }

    private void checkAndRequestPermissions() {
        // 1. Danh sách các quyền cốt lõi cho module của Thế
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ANSWER_PHONE_CALLS
        };

        boolean needRequest = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }

        // Hiện bảng popup mặc định của Android hỏi người dùng
        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }

        // 2. Xin quyền vẽ đè màn hình (Dọn đường sẵn cho module Overlay của Tuấn)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Vui lòng cấp quyền Hiển thị trên ứng dụng khác", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    // Hàm này bắt sự kiện khi người dùng bấm "Cho phép" hoặc "Từ chối"
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền! Bắt đầu kích hoạt bảo vệ...", Toast.LENGTH_SHORT).show();

                // === THÊM 2 DÒNG NÀY VÀO ===
                Intent serviceIntent = new Intent(this, com.hoiaebtl.antispam_call_android.core.CallListenService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
                // ===========================

            } else {
                Toast.makeText(this, "App cần quyền để hoạt động. Vui lòng cấp quyền!", Toast.LENGTH_LONG).show();
            }
        }
    }
}