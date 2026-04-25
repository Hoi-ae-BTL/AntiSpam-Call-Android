package com.hoiaebtl.antispam_call_android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.CallLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Legacy Fallback: Dùng riêng cho các máy thuộc hệ điều hành Android 9 trở xuống.
 * Từ Android 10 trở lên, AppCallScreeningService sẽ gánh vác xử lý chính.
 */
public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver_Legacy";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void saveCallLogLocally(Context context, String number, boolean isSpam, int categoryId) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                CallLog log = new CallLog();
                log.setPhoneNumber(number);
                log.setCallTime(System.currentTimeMillis());
                log.setSpam(isSpam);
                log.setUserId(1); // Mặc định
                if (isSpam) {
                    log.setCategoryId(categoryId);
                }
                db.callLogDao().insert(log);
                Log.d(TAG, "Đã lưu lịch sử cuộc gọi thật (Legacy): SPAM=" + isSpam);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lưu lịch sử: " + e.getMessage());
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                
                if (incomingNumber != null) {
                    Log.d(TAG, "PHÁT HIỆN CUỘC GỌI ĐẾN (LEGACY): " + incomingNumber);
                    
                    String normalizedNumber = NumberNormalizer.normalize(incomingNumber, "VN");
                    HybridSpamChecker checker = new HybridSpamChecker(context);
                    
                    checker.checkCallerInfo(normalizedNumber, info -> {
                        // Ghi nhận thành một cuộc gọi thực tế đi vào lịch sử
                        saveCallLogLocally(context, normalizedNumber, info.isSpam, info.categoryId);
                        
                        if (info.isSpam) {
                            Log.w(TAG, "CẢNH BÁO: Số lừa đảo " + normalizedNumber);
                            
                            SharedPreferences prefs = context.getSharedPreferences("SafeCallPrefs", Context.MODE_PRIVATE);
                            boolean isAutoBlockEnabled = prefs.getBoolean("auto_block", false);

                            if (isAutoBlockEnabled) {
                                endCall(context); // Dùng TelecomManager (dễ lỗi trên Android 10+)
                                // Bỏ đi việc hiện báo cáo Overlay để không làm phiền người dùng
                            } else {
                                checker.showOverlay(incomingNumber, info);
                            }
                        } else if (info.isVerifiedSafe && info.name != null) {
                            // Chỉ hiện màn hình Caller ID (không chặn)
                            checker.showOverlay(incomingNumber, info);
                        }
                    });
                }
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                // Khi cuộc gọi kết thúc (IDLE) hoặc người dùng nhấc máy (OFFHOOK) -> Đóng Overlay
                Log.d(TAG, "Cuộc gọi kết thúc / Đã nhấc máy -> Tắt Overlay.");
                Intent stopIntent = new Intent(context, OverlayService.class);
                context.stopService(stopIntent);
            }
        }
    }

    private void endCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                try {
                    telecomManager.endCall();
                    Log.d(TAG, "Cuộc gọi đã bị ngắt tự động bằng TelecomManager.");
                } catch (SecurityException e) {
                    Log.e(TAG, "Lỗi Security do máy đời mới chặn TelecomManager: " + e.getMessage());
                }
            }
        }
    }
}