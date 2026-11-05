package com.nourishlink.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {

    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Insert
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC, id DESC LIMIT 50")
    suspend fun getForUser(userId: Int): List<NotificationEntity>

    @Query("SELECT COALESCE(MAX(id), 0) FROM notifications WHERE userId = :userId")
    suspend fun latestId(userId: Int): Int

    @Query("SELECT * FROM notifications WHERE userId = :userId AND id > :afterId ORDER BY id ASC")
    suspend fun getNewerThan(userId: Int, afterId: Int): List<NotificationEntity>
}
