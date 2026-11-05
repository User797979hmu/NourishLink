package com.nourishlink.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query(
        "SELECT * FROM users WHERE leaderboardOptIn = 1 AND impactPoints > 0 AND authProvider != 'guest' " +
            "ORDER BY impactPoints DESC LIMIT 20"
    )
    suspend fun getLeaderboard(): List<UserEntity>
}
