package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hoiaebtl.antispam_call_android.data.entity.SpamNumber;

import java.util.List;

@Dao
public interface SpamNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SpamNumber spamNumber);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpamNumber> spamNumbers);

    @Delete
    void delete(SpamNumber spamNumber);

    @Query("SELECT * FROM SpamNumbers WHERE phone_number = :phone")
    SpamNumber findByPhone(String phone);

    @Query("SELECT * FROM SpamNumbers ORDER BY last_reported_at DESC")
    List<SpamNumber> getAllSpamNumbers();

    @Query("SELECT * FROM SpamNumbers ORDER BY total_reports DESC")
    List<SpamNumber> getTopSpamNumbers();

    @Query("SELECT COUNT(*) FROM SpamNumbers")
    int getCount();
}