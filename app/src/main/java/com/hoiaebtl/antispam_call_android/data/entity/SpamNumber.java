package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "SpamNumbers")
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

    public SpamNumber() {
    }

    public SpamNumber(@NonNull String phone_number, int primary_category_id, int trust_score, int total_reports, String carrier, String region, String last_reported_at) {
        this.phone_number = phone_number;
        this.primary_category_id = primary_category_id;
        this.trust_score = trust_score;
        this.total_reports = total_reports;
        this.carrier = carrier;
        this.region = region;
        this.last_reported_at = last_reported_at;
    }
}