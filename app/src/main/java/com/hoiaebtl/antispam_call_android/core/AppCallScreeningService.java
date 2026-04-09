package com.hoiaebtl.antispam_call_android.core;

import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class AppCallScreeningService extends CallScreeningService {
    private static final String TAG = "AppCallScreening";

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
        checker.checkIsSpam(normalizedNumber, isSpam -> {
            if (isSpam) {
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
                }
                
                // Luôn hiện Overlay cảnh báo nếu có cuộc gọi spam (chặn hay ko vẫn có thể hiện hoặc ko)
                checker.showSpamOverlay(finalIncomingNumber);
            } else {
                Log.d(TAG, "Đã check xong: Số an toàn.");
                CallResponse response = new CallResponse.Builder().build();
                respondToCall(callDetails, response);
            }
        });
    }
}
