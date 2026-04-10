package com.hoiaebtl.antispam_call_android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Legacy Fallback: Dùng riêng cho các máy thuộc hệ điều hành Android 9 trở xuống.
 * Từ Android 10 trở lên, AppCallScreeningService sẽ gánh vác xử lý chính.
 */
public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver_Legacy";

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
                        if (info.isSpam) {
                            Log.w(TAG, "CẢNH BÁO: Số lừa đảo " + normalizedNumber);
                            
                            SharedPreferences prefs = context.getSharedPreferences("SafeCallPrefs", Context.MODE_PRIVATE);
                            boolean isAutoBlockEnabled = prefs.getBoolean("auto_block", false);

                            if (isAutoBlockEnabled) {
                                endCall(context); // Dùng TelecomManager (dễ lỗi trên Android 10+)
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