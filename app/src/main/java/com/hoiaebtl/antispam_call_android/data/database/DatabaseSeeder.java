package com.hoiaebtl.antispam_call_android.data.database;

import android.content.Context;
import android.util.Log;

import com.hoiaebtl.antispam_call_android.data.entity.Category;
import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;

public class DatabaseSeeder {

    public static void seedIfNeeded(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                
                // Kiểm tra xem đã có category chưa
                if (db.categoryDao().getCount() == 0) {
                    seedCategories(db);
                }
                
                // Kiểm tra xem đã có số spam chưa
                if (db.spamNumberDao().getCount() == 0) {
                    seedSpamNumbers(db);
                }
            } catch (Exception e) {
                Log.e("DatabaseSeeder", "Error seeding database: " + e.getMessage());
            }
        });
    }

    private static void seedCategories(AppDatabase db) {
        List<Category> categories = new ArrayList<>();
        
        Category c1 = new Category();
        c1.name = "Lừa đảo tài chính";
        c1.severity_level = 5;
        categories.add(c1);

        Category c2 = new Category();
        c2.name = "Quảng cáo làm phiền";
        c2.severity_level = 2;
        categories.add(c2);

        Category c3 = new Category();
        c3.name = "Mạo danh cơ quan chức năng";
        c3.severity_level = 5;
        categories.add(c3);

        db.categoryDao().insertAll(categories);
        Log.d("DatabaseSeeder", "Đã khởi tạo danh mục vi phạm.");
    }

    private static void seedSpamNumbers(AppDatabase db) {
        List<SpamNumber> spamNumbers = new ArrayList<>();
        
        // THÊM SỐ TEST CỐ ĐỊNH ĐỂ DỄ DÀNG KIỂM TRA
        // Số này sẽ luôn bị chặn/cảnh báo
        spamNumbers.add(new SpamNumber("123456789", 1, 10, 999, "Hệ thống", "Toàn quốc", "2024-01-01 12:00:00"));
        spamNumbers.add(new SpamNumber("+84123456789", 1, 10, 999, "Hệ thống", "Toàn quốc", "2024-01-01 12:00:00"));

        // Thêm 50 số ngẫu nhiên khác
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            String phone = "09" + String.format(Locale.US, "%08d", random.nextInt(100000000));
            spamNumbers.add(new SpamNumber(phone, random.nextInt(3) + 1, 20, 50, "Nhà mạng", "Việt Nam", "2024-01-01 10:00:00"));
        }

        db.spamNumberDao().insertAll(spamNumbers);
        Log.d("DatabaseSeeder", "Đã thêm 2 số test và 50 số ngẫu nhiên vào danh sách đen.");
    }
}
