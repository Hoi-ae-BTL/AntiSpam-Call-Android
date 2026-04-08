package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hoiaebtl.antispam_call_android.data.entity.CallLog;

import java.util.List;

@Dao
public interface CallLogDao {

    @Insert
    void insert(CallLog log);

    @Query("SELECT * FROM CallLogs WHERE user_id = :userId ORDER BY call_time DESC")
    List<CallLog> getUserLogs(int userId);

    @Query("SELECT COUNT(*) FROM CallLogs WHERE isSpam = 1")
    int getTotalSpamCalls();
}