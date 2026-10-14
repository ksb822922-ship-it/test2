package com.currencyconverter.offline.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/** Последние успешно загруженные курсы для базовой валюты (все курсы одним JSON). */
@Entity(tableName = "cached_rates")
data class CachedRates(
    @PrimaryKey val base: String,
    val ratesJson: String,
    val updatedAt: Long
)

@Dao
interface RatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CachedRates)

    @Query("SELECT * FROM cached_rates WHERE base = :base LIMIT 1")
    suspend fun get(base: String): CachedRates?
}

@Database(entities = [CachedRates::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ratesDao(): RatesDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rates.db"
                ).build().also { instance = it }
            }
    }
}
