package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Reports",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id"
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "category_id",
                        childColumns = "category_id"
                ),
                @ForeignKey(
                        entity = SpamNumber.class,
                        parentColumns = "phone_number",
                        childColumns = "phone_number"
                )
        }
)
public class Report {

    @PrimaryKey(autoGenerate = true)
    public int report_id;

    public int user_id;

    @NonNull
    public String phone_number;

    public int category_id;

    public String comment;

    public String status;

    public String created_at;
}