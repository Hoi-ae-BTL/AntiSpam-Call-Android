package com.hoiaebtl.antispam_call_android.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hoiaebtl.antispam_call_android.data.dao.*;
import com.hoiaebtl.antispam_call_android.data.entity.*;

@Database(
        entities = {
                User.class,
                Category.class,
                SpamNumber.class,
                Report.class,
                PersonalList.class,
                BlockRule.class,
                CallLog.class
        },
        version = 5, // Tăng lên 5 do thêm category_id vào CallLog
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract SpamNumberDao spamNumberDao();
    public abstract ReportDao reportDao();
    public abstract PersonalListDao personalListDao();
    public abstract BlockRuleDao blockRuleDao();
    public abstract CallLogDao callLogDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "antispam_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}