package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "SpamNumbers",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "category_id",
                childColumns = "primary_category_id"
        )
)
public class SpamNumber {

    @PrimaryKey
    @NonNull
    public String phone_number;

    public int primary_category_id;

    public int trust_score;

    public int total_reports;

    public String carrier;

    public String region;

    public String last_reported_at;
}