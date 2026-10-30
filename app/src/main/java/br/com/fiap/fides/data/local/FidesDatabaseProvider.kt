package br.com.fiap.fides.data.local

import android.content.Context
import androidx.room.Room

object FidesDatabaseProvider {
    @Volatile
    private var instance: FidesDatabase? = null

    fun get(context: Context): FidesDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            FidesDatabase::class.java,
            "fides.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7).build().also { instance = it }
    }
}
