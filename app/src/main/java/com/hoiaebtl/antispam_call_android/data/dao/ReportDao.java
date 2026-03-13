package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hoiaebtl.antispam_call_android.data.entity.Report;

import java.util.List;

@Dao
public interface ReportDao {

    @Insert
    void insert(Report report);

    @Query("SELECT * FROM Reports WHERE phone_number = :phone")
    List<Report> getReportsByPhone(String phone);

    @Query("SELECT * FROM Reports WHERE user_id = :user")
    List<Report> getReportsByUser(String user);
}