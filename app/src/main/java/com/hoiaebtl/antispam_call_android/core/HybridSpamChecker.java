package com.hoiaebtl.antispam_call_android.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.CallLog;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HybridSpamChecker {
    private static final String TAG = "HybridSpamChecker";
    private final Context context;
    private final ExecutorService executorService;
    private final FirebaseFirestore firestore;

    public interface SpamResultCallback {
        void onResult(boolean isSpam);
    }

    public HybridSpamChecker(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void checkIsSpam(String normalizedNumber, SpamResultCallback callback) {
        // Bước 1: Query Local Room DB cực nhanh
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            SpamNumber spam = db.spamNumberDao().findByPhone(normalizedNumber);
            
            if (spam != null) {
                Log.d(TAG, "Đã tìm thấy trong Local DB: Thằng này là Spam!");
                saveCallLog(db, normalizedNumber, true, spam.primary_category_id);
                callback.onResult(true);
            } else {
                Log.d(TAG, "Local DB không có, gọi Firebase Fallback...");
                // Bước 2: Truy xuất Firebase Firestore
                firestore.collection("spam_numbers").document(normalizedNumber).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Log.d(TAG, "Tìm thấy trên Firebase: Là Spam!");
                            
                            // Có thể Async parse dữ liệu và lưu Cache vào Room
                            int catId = 0;
                            if (document.getLong("primary_category_id") != null) {
                                catId = document.getLong("primary_category_id").intValue();
                            }

                            // Lưu log nội bộ
                            int finalCatId = catId;
                            executorService.execute(() -> {
                                saveCallLog(db, normalizedNumber, true, finalCatId);
                                
                                // Cache lại để offline cũng tra được
                                SpamNumber newSpam = new SpamNumber(normalizedNumber, finalCatId, 0, 0, "", "", "");
                                db.spamNumberDao().insert(newSpam);
                            });

                            callback.onResult(true);
                        } else {
                            Log.d(TAG, "Số ngoại vi hoàn toàn an toàn.");
                            executorService.execute(() -> saveCallLog(db, normalizedNumber, false, 0));
                            callback.onResult(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firebase truy xuất lỗi (Mất kết nối?): " + e.getMessage());
                        executorService.execute(() -> saveCallLog(db, normalizedNumber, false, 0));
                        callback.onResult(false);
                    });
            }
        });
    }

    private void saveCallLog(AppDatabase db, String number, boolean isSpam, int categoryId) {
        CallLog log = new CallLog();
        log.setPhoneNumber(number);
        log.setCallTime(System.currentTimeMillis());
        log.setSpam(isSpam);
        log.setUserId(1); // Mặc định
        if (isSpam) {
            log.setCategoryId(categoryId);
        }
        db.callLogDao().insert(log);
    }
    
    public void showSpamOverlay(String number) {
        Intent overlayIntent = new Intent(context, OverlayService.class);
        overlayIntent.putExtra("spam_number", number);
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(overlayIntent);
    }
}
