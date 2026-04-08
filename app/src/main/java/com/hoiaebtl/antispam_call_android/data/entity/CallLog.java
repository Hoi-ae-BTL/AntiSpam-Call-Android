package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "CallLogs"
)
public class CallLog {

    @PrimaryKey(autoGenerate = true)
    private int log_id;

    private int user_id;

    private String phone_number;

    private int duration;

    private String action_taken;

    private long call_time; // Chuyển sang long để lưu timestamp

    private boolean isSpam; // Thêm trường này để biết cuộc gọi có phải lừa đảo không

    // Getters and Setters
    public int getLog_id() { return log_id; }
    public void setLog_id(int log_id) { this.log_id = log_id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getPhone_number() { return phone_number; }
    public void setPhone_number(String phone_number) { this.phone_number = phone_number; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getAction_taken() { return action_taken; }
    public void setAction_taken(String action_taken) { this.action_taken = action_taken; }

    public long getCall_time() { return call_time; }
    public void setCall_time(long call_time) { this.call_time = call_time; }

    public boolean isSpam() { return isSpam; }
    public void setSpam(boolean spam) { isSpam = spam; }

    // Backward compatibility aliases for Room or other parts of the code if needed
    public int getUserId() { return getUser_id(); }
    public void setUserId(int user_id) { setUser_id(user_id); }

    public String getPhoneNumber() { return getPhone_number(); }
    public void setPhoneNumber(String phone_number) { setPhone_number(phone_number); }

    public String getActionTaken() { return getAction_taken(); }
    public void setActionTaken(String action_taken) { setAction_taken(action_taken); }

    public long getCallTime() { return getCall_time(); }
    public void setCallTime(long call_time) { setCall_time(call_time); }
}