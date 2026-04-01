package com.hoiaebtl.antispam_call_android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.room.Room;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.CallLog;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "SafeCall_Core";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                
                if (incomingNumber != null) {
                    Log.d(TAG, "PHÁT HIỆN CUỘC GỌI ĐẾN: " + incomingNumber);
                    checkSpamAndProcess(context, incomingNumber);
                }
            }
        }
    }

    private void checkSpamAndProcess(Context context, String number) {
        executorService.execute(() -> {
            try {
                // 1. Kết nối Database
                AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "database-name")
                        .fallbackToDestructiveMigration()
                        .build();

                // 2. Truy vấn số điện thoại từ danh sách đen
                SpamNumber spam = db.spamNumberDao().findByPhone(number);
                boolean isSpam = (spam != null);

                // 3. Lưu nhật ký cuộc gọi
                CallLog log = new CallLog();
                log.setPhoneNumber(number);
                log.setCallTime(System.currentTimeMillis());
                log.setSpam(isSpam);
                log.setUserId("default_user");
                db.callLogDao().insert(log);

                if (isSpam) {
                    Log.w(TAG, "CẢNH BÁO: Số lừa đảo " + number);

                    // 4. Kiểm tra xem người dùng có bật "Tự động chặn" không
                    SharedPreferences prefs = context.getSharedPreferences("SafeCallPrefs", Context.MODE_PRIVATE);
                    boolean isAutoBlockEnabled = prefs.getBoolean("auto_block", false);

                    if (isAutoBlockEnabled) {
                        Log.w(TAG, "Đang thực hiện TỰ ĐỘNG CHẶN cuộc gọi...");
                        endCall(context);
                    } else {
                        // Nếu không chặn thì hiện Overlay cảnh báo (Module Tuấn)
                        Intent overlayIntent = new Intent(context, OverlayService.class);
                        overlayIntent.putExtra("spam_number", number);
                        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startService(overlayIntent);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xử lý: " + e.getMessage());
            }
        });
    }

    private void endCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                try {
                    // Quyền ANSWER_PHONE_CALLS đã được Thế xin ở Manifest
                    telecomManager.endCall();
                    Log.d(TAG, "Cuộc gọi đã bị ngắt tự động bằng TelecomManager.");
                } catch (SecurityException e) {
                    Log.e(TAG, "Không có quyền ngắt cuộc gọi: " + e.getMessage());
                }
            }
        } else {
            // Đối với Android thấp hơn 9, việc ngắt cuộc gọi phức tạp hơn (cần dùng Reflection)
            Log.w(TAG, "Tính năng chặn tự động chưa hỗ trợ đầy đủ trên Android dưới 9.");
        }
    }
}