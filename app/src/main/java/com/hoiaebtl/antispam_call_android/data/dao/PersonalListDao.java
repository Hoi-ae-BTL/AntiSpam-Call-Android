package com.hoiaebtl.antispam_call_android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import com.hoiaebtl.antispam_call_android.data.entity.PersonalList;

import java.util.List;

@Dao
public interface PersonalListDao {

    @Insert
    void insert(PersonalList list);

    @Query("SELECT * FROM PersonalLists WHERE user_id = :user")
    List<PersonalList> getUserList(String user);

    @Query("DELETE FROM PersonalLists WHERE phone_number = :phone")
    void delete(String phone);
}