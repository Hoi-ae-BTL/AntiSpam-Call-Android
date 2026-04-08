package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "BlockRules",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id"
        )
)
public class BlockRule {

    @PrimaryKey(autoGenerate = true)
    public int rule_id;

    public int user_id;

    public String pattern;

    public String rule_type;

    public String action;

    public int is_active;
}