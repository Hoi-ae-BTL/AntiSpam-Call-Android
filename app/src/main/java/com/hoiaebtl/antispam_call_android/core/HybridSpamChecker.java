package com.hoiaebtl.antispam_call_android.core;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HybridSpamChecker {
    private static final String TAG = "HybridSpamChecker";
    private final Context context;
    private final ExecutorService executorService;
    private final FirebaseFirestore firestore;

    public static class CallerInfo {
        public boolean isSpam;
        public boolean isVerifiedSafe;
        public String name;
        public String label;
        public int reportCount;
        public int categoryId;
        
        public boolean hasData() { return isSpam || isVerifiedSafe; }
    }

    public interface CallerResultCallback {
        void onResult(CallerInfo info);
    }

    public HybridSpamChecker(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void checkCallerInfo(String normalizedNumber, CallerResultCallback callback) {
        executorService.execute(() -> {
            CallerInfo result = new CallerInfo();
            
            // Bước 0: Quét danh bạ cục bộ (Không bao giờ chặn số có trong danh bạ)
            if (isNumberInContacts(normalizedNumber)) {
                Log.d(TAG, "Đã tìm thấy trong Danh bạ. An toàn tuyệt đối!");
                result.isVerifiedSafe = true;
                result.name = getContactName(normalizedNumber);
                result.label = "Đã lưu trong Danh bạ";
                callback.onResult(result);
                return;
            }

            // Bước 1: Query Local Room DB
            AppDatabase db = AppDatabase.getInstance(context);
            SpamNumber spam = db.spamNumberDao().findByPhone(normalizedNumber);
            
            if (spam != null) {
                Log.d(TAG, "Đã tìm thấy trong Local DB: Thằng này là Spam!");
                result.isSpam = true;
                result.label = "Spam/Lừa đảo (Cộng đồng)";
                result.categoryId = spam.primary_category_id;
                callback.onResult(result);
                return;
            }

            com.hoiaebtl.antispam_call_android.data.entity.PersonalList personalBlock = db.personalListDao().findByPhone(normalizedNumber);
            if (personalBlock != null) {
                Log.d(TAG, "Đã tìm thấy trong Danh sách chặn cá nhân!");
                result.isSpam = true;
                result.label = personalBlock.note != null && !personalBlock.note.isEmpty() ? personalBlock.note : "Số bị chặn (cá nhân)";
                callback.onResult(result);
                return;
            }

            Log.d(TAG, "Local DB không có, gọi Firebase Fallback...");
            
            // Bước 2: Firebase Fallback (Cả spam_numbers và user_profiles)
            Task<DocumentSnapshot> spamTask = firestore.collection("spam_numbers").document(normalizedNumber).get();
            Task<DocumentSnapshot> safeTask = firestore.collection("user_profiles").document(normalizedNumber).get();

            try {
                Tasks.await(Tasks.whenAllComplete(spamTask, safeTask));
                
                DocumentSnapshot spamDoc = spamTask.isSuccessful() ? spamTask.getResult() : null;
                DocumentSnapshot safeDoc = safeTask.isSuccessful() ? safeTask.getResult() : null;

                boolean isSpamInDb = spamDoc != null && spamDoc.exists();
                boolean isSafeInDb = safeDoc != null && safeDoc.exists();

                long spamReports = 0;
                if (isSpamInDb) {
                    if (spamDoc.contains("report_count")) {
                        spamReports = spamDoc.getLong("report_count");
                    } else if (spamDoc.contains("total_reports")) {
                        spamReports = spamDoc.getLong("total_reports");
                    } else {
                        spamReports = 1;
                    }
                }
                
                long safeReports = isSafeInDb && safeDoc.contains("report_count") ? safeDoc.getLong("report_count") : (isSafeInDb ? 1 : 0);

                if (isSpamInDb || isSafeInDb) {
                    if (spamReports > safeReports) {
                        Log.d(TAG, "Tìm thấy trên Firebase: Trọng số Spam (" + spamReports + ") vượt Danh Tính (" + safeReports + ")");
                        int catId = spamDoc.getLong("primary_category_id") != null ? spamDoc.getLong("primary_category_id").intValue() : 0;
                        result.isSpam = true;
                        result.reportCount = (int)spamReports;
                        result.categoryId = catId;
                        result.label = spamDoc.getString("label") != null ? spamDoc.getString("label") : "Cảnh báo Lừa đảo";
                        callback.onResult(result);
                    } else if (isSafeInDb && safeReports >= spamReports) {
                        Log.d(TAG, "Tìm thấy trên Firebase: Trọng số An Toàn (" + safeReports + ") vượt Spam (" + spamReports + ")");
                        result.isVerifiedSafe = true;
                        result.name = safeDoc.getString("name");
                        result.label = safeDoc.getString("company");
                        if (result.name == null) result.name = "Người dùng xác thực";
                        if (result.label == null) result.label = "Đã xác thực danh tính";
                        callback.onResult(result);
                    }
                } else {
                    Log.d(TAG, "Số ngoại vi hoàn toàn mờ xỉn (Không Rác cũng không VIP).");
                    callback.onResult(result);
                }

            } catch (Exception e) {
                Log.e(TAG, "Firebase truy xuất lỗi: " + e.getMessage());
                callback.onResult(result);
            }
        });
    }

    private boolean isNumberInContacts(String number) {
        return getContactName(number) != null;
    }

    private String getContactName(String number) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String name = cursor.getString(0);
                    cursor.close();
                    return name;
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đọc danh bạ: " + e.getMessage());
        }
        return null;
    }

    public void showOverlay(String number, CallerInfo info) {
        Intent overlayIntent = new Intent(context, OverlayService.class);
        overlayIntent.putExtra("spam_number", number);
        overlayIntent.putExtra("isSpam", info.isSpam);
        overlayIntent.putExtra("isVerifiedSafe", info.isVerifiedSafe);
        overlayIntent.putExtra("name", info.name);
        overlayIntent.putExtra("label", info.label);
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startService(overlayIntent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi start Overlay: " + e.getMessage());
        }
    }
}
