package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        RevisionNoteEntity::class,
        SpacedRepetitionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class KovaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun revisionDao(): RevisionDao

    companion object {
        @Volatile
        private var instance: KovaDatabase? = null

        fun getInstance(context: Context): KovaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KovaDatabase::class.java,
                    "kova_study_companion.db",
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
