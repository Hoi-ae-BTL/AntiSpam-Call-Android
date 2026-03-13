package com.hoiaebtl.antispam_call_android.data.database;

import androidx.room.Database;
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
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract CategoryDao categoryDao();

    public abstract SpamNumberDao spamNumberDao();

    public abstract ReportDao reportDao();

    public abstract PersonalListDao personalListDao();

    public abstract BlockRuleDao blockRuleDao();

    public abstract CallLogDao callLogDao();
}