package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hoiaebtl.antispam_call_android.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("select * from Users")
    List<User> getAllUsers();

    @Query("select * from Users where user_id == :id")
    User getUserById(int id);

    @Query("delete from Users where user_id == :id")
    void deleteUser(int id);

}
