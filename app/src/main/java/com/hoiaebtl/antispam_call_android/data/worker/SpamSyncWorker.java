package com.hoiaebtl.antispam_call_android.data.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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

            // Chờ fetch toàn bộ list spam_numbers một cách đồng bộ vì Worker chạy background
            QuerySnapshot snapshot = Tasks.await(db.collection("spam_numbers").get());
            
            if (snapshot != null && !snapshot.isEmpty()) {
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String phone = doc.getId(); // Lấy Document ID là số điện thoại
                    int catId = doc.getLong("primary_category_id") != null ? doc.getLong("primary_category_id").intValue() : 0;
                    int trust = doc.getLong("trust_score") != null ? doc.getLong("trust_score").intValue() : 0;
                    int reports = doc.getLong("total_reports") != null ? doc.getLong("total_reports").intValue() : 0;

                    SpamNumber spam = new SpamNumber(phone, catId, trust, reports, "", "", "");
                    // Insert (Ignore/Update) vào Room
                    // Room sẽ overwrite trùng lặp nếu define @Insert(onConflict = OnConflictStrategy.REPLACE)
                    roomDb.spamNumberDao().insert(spam);
                }
                Log.d(TAG, "Sync dữ liệu thành công: " + snapshot.size() + " records.");
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
