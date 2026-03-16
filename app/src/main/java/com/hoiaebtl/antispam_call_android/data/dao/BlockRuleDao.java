package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import com.hoiaebtl.antispam_call_android.data.entity.BlockRule;

import java.util.List;

@Dao
public interface BlockRuleDao {

    @Insert
    void insert(BlockRule rule);

    @Query("SELECT * FROM BlockRules WHERE is_active = 1")
    List<BlockRule> getActiveRules();
}