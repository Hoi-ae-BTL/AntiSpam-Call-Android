package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

import java.util.List;

@Dao
public interface SpamNumberDao {

    @Insert
    void insert(SpamNumber spamNumber);

    @Query("SELECT * FROM SpamNumbers WHERE phone_number = :phone")
    SpamNumber findByPhone(String phone);

    @Query("SELECT * FROM SpamNumbers ORDER BY total_reports DESC")
    List<SpamNumber> getTopSpamNumbers();
}