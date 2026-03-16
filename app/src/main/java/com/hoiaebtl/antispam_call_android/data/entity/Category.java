package com.hoiaebtl.antispam_call_android.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    public int category_id;

    public String name;

    public int severity_level;

    public String icon_url;
}
