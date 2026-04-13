package com.hoiaebtl.antispam_call_android.core;

import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.CallLog;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class AppCallScreeningService extends CallScreeningService {
    private static final String TAG = "AppCallScreening";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void saveCallLogLocally(String number, boolean isSpam, int categoryId) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                CallLog log = new CallLog();
                log.setPhoneNumber(number);
                log.setCallTime(System.currentTimeMillis());
                log.setSpam(isSpam);
                log.setUserId(1); // Mặc định
                if (isSpam) {
                    log.setCategoryId(categoryId);
                }
                db.callLogDao().insert(log);
                Log.d(TAG, "Đã lưu lịch sử cuộc gọi thật: SPAM=" + isSpam);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lưu lịch sử: " + e.getMessage());
            }
        });
    }

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        if (callDetails.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
            return;
        }

        String incomingNumber = "";
        if (callDetails.getHandle() != null) {
            incomingNumber = callDetails.getHandle().getSchemeSpecificPart();
        }

        Log.d(TAG, "NATIVE CALL SCREENING Đang kiểm tra cuộc gọi từ: " + incomingNumber);

        // Chuẩn hóa số điện thoại
        String normalizedNumber = NumberNormalizer.normalize(incomingNumber, "VN");
        final String finalIncomingNumber = incomingNumber; // Sửa lỗi Java Lambda

        HybridSpamChecker checker = new HybridSpamChecker(getApplicationContext());
        checker.checkCallerInfo(normalizedNumber, info -> {
            // Ghi nhận trực tiếp thành cuộc gọi thật
            saveCallLogLocally(normalizedNumber, info.isSpam, info.categoryId);
            
            if (info.isSpam) {
                // Kiểm tra công tắc "Tự động chặn" từ SharedPreferences
                android.content.SharedPreferences prefs = getSharedPreferences("SafeCallPrefs", android.content.Context.MODE_PRIVATE);
                boolean isAutoBlock = prefs.getBoolean("auto_block", false);

                if (isAutoBlock) {
                    Log.w(TAG, "Đây là số SPAM và ĐÃ BẬT Chặn tự động -> Rớt đài!");
                    CallResponse response = new CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .setSkipCallLog(false)
                            .setSkipNotification(true)
                            .build();
                    respondToCall(callDetails, response);
                } else {
                    Log.w(TAG, "Là số SPAM nhưng chưa bật Tự Động Chặn -> Chỉ báo Overlay.");
                    CallResponse response = new CallResponse.Builder().build();
                    respondToCall(callDetails, response);
                    
                    // Chỉ hiện cảnh báo nếu không bị chặn ẩn
                    checker.showOverlay(finalIncomingNumber, info);
                }
                Log.d(TAG, "Đã check xong: Không phải Spam.");
                CallResponse response = new CallResponse.Builder().build();
                respondToCall(callDetails, response);
                
                // Nếu là CallerID An Toàn thì báo Xanh
                if (info.isVerifiedSafe && info.name != null) {
                    checker.showOverlay(finalIncomingNumber, info);
                }
            }
        });
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
