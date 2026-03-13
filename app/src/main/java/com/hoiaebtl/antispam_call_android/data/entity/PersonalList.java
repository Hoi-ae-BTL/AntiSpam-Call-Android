package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "PersonalLists",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id"
        )
)
public class PersonalList {

    @PrimaryKey(autoGenerate = true)
    public int list_id;

    public String user_id;

    public String phone_number;

    public String list_type;

    public String note;

    public String created_at;
}