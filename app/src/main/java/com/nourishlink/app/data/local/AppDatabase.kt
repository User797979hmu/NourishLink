package com.nourishlink.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The single on-device database that replaces the old REST backend: accounts, listings,
 * claims, logistics messages and notifications all live here.
 */
@Database(
    entities = [
        UserEntity::class,
        ListingEntity::class,
        ClaimEntity::class,
        MessageEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun claimDao(): ClaimDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nourishlink.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
            }
    }
}
