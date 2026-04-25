package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "CallLogs")
public class CallLog {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    private int logId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "action_taken")
    private String actionTaken;

    @ColumnInfo(name = "call_time")
    private long callTime;

    @ColumnInfo(name = "isSpam")
    private boolean isSpam;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public long getCallTime() { return callTime; }
    public void setCallTime(long callTime) { this.callTime = callTime; }

    public boolean isSpam() { return isSpam; }
    public void setSpam(boolean spam) { isSpam = spam; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}