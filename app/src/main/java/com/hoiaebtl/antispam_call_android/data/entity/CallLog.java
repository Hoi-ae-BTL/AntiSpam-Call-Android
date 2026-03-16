package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "CallLogs",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id"
        )
)
public class CallLog {

    @PrimaryKey(autoGenerate = true)
    public int log_id;

    public String user_id;

    public String phone_number;

    public int duration;

    public String action_taken;

    public String call_time;
}