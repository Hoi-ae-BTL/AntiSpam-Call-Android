package com.hoiaebtl.antispam_call_android.data.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hoiaebtl.antispam_call_android.data.database.AppDatabase;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

public class SpamSyncWorker extends Worker {
    private static final String TAG = "SpamSyncWorker";

    public SpamSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Bắt đầu Background Sync dữ liệu Spam từ Firebase...");

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            AppDatabase roomDb = AppDatabase.getInstance(getApplicationContext());

            // Chờ fetch toàn bộ list một cách đồng bộ vì Worker chạy background
            Task<QuerySnapshot> spamTask = db.collection("spam_numbers").get();
            Task<QuerySnapshot> safeTask = db.collection("user_profiles").get();

            Tasks.await(Tasks.whenAllComplete(spamTask, safeTask));
            
            if (spamTask.isSuccessful() && spamTask.getResult() != null) {
                QuerySnapshot spamDocs = spamTask.getResult();
                QuerySnapshot safeDocs = safeTask.isSuccessful() ? safeTask.getResult() : null;

                for (DocumentSnapshot doc : spamDocs.getDocuments()) {
                    String phone = doc.getId(); // Lấy Document ID là số điện thoại
                    int catId = doc.getLong("primary_category_id") != null ? doc.getLong("primary_category_id").intValue() : 0;
                    int trust = doc.getLong("trust_score") != null ? doc.getLong("trust_score").intValue() : 0;
                    
                    int spamCount = 1;
                    if (doc.contains("report_count")) {
                        spamCount = doc.getLong("report_count").intValue();
                    } else if (doc.contains("total_reports")) {
                        spamCount = doc.getLong("total_reports").intValue();
                    }
                    
                    int safeCount = 0;
                    if (safeDocs != null) {
                        for (DocumentSnapshot safeDoc : safeDocs.getDocuments()) {
                            if (safeDoc.getId().equals(phone)) {
                                safeCount = safeDoc.contains("report_count") ? safeDoc.getLong("report_count").intValue() : 1;
                                break;
                            }
                        }
                    }

                    if (spamCount > safeCount) {
                        SpamNumber spam = new SpamNumber(phone, catId, trust, spamCount, "", "", "");
                        roomDb.spamNumberDao().insert(spam);
                    } else {
                        SpamNumber exist = roomDb.spamNumberDao().findByPhone(phone);
                        if (exist != null) {
                            roomDb.spamNumberDao().delete(exist);
                        }
                    }
                }
                Log.d(TAG, "Sync dữ liệu thành công: " + spamDocs.size() + " records.");
                return Result.success();
            } else {
                return Result.success(); 
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đồng bộ Firebase -> Room: " + e.getMessage());
            return Result.retry();
        }
    }
}
