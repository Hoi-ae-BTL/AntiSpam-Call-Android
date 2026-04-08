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
                
                if (db.categoryDao().getCount() == 0) {
                    seedCategories(db);
                }
                
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

        Category c4 = new Category();
        c4.name = "Đòi nợ";
        c4.severity_level = 3;
        categories.add(c4);

        db.categoryDao().insertAll(categories);
        Log.d("DatabaseSeeder", "Inserted categories");
    }

    private static void seedSpamNumbers(AppDatabase db) {
        List<SpamNumber> spamNumbers = new ArrayList<>();
        Random random = new Random();
        String[] carriers = {"Viettel", "Vinaphone", "Mobifone", "Vietnamobile"};
        String[] regions = {"Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng"};
        
        for (int i = 0; i < 100; i++) {
            String phone = "09" + String.format(Locale.US, "%08d", random.nextInt(100000000));
            int categoryId = random.nextInt(4) + 1;
            int trustScore = random.nextInt(50);
            int totalReports = random.nextInt(500) + 10;
            String carrier = carriers[random.nextInt(carriers.length)];
            String region = regions[random.nextInt(regions.length)];
            String lastReported = String.format(Locale.US, "2024-12-%02d 10:00:00", random.nextInt(30) + 1);

            spamNumbers.add(new SpamNumber(phone, categoryId, trustScore, totalReports, carrier, region, lastReported));
        }

        db.spamNumberDao().insertAll(spamNumbers);
        Log.d("DatabaseSeeder", "Inserted 100 spam numbers");
    }
}