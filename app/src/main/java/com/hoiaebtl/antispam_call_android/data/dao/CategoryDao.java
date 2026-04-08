package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hoiaebtl.antispam_call_android.data.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Category> categories);

    @Query("SELECT * FROM Categories")
    List<Category> getAllCategories();

    @Query("SELECT COUNT(*) FROM Categories")
    int getCount();
}