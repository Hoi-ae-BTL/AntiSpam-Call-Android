package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {
    @PrimaryKey
    public int user_id;
    public String phone_number;
    public int reputation_score;
    public int is_premium;
    public String created_at;
}
