package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Family::class,
        Child::class,
        Task::class,
        Prayer::class,
        Recitation::class,
        Reward::class,
        Redemption::class,
        Notification::class,
        EncouragingMessage::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NujoomDatabase : RoomDatabase() {
    abstract fun nujoomDao(): NujoomDao

    companion object {
        @Volatile
        private var INSTANCE: NujoomDatabase? = null

        fun getDatabase(context: Context): NujoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NujoomDatabase::class.java,
                    "nujoom_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
