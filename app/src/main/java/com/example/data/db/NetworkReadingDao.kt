package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NetworkReading
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkReadingDao {

    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<NetworkReading>>

    @Query("SELECT * FROM readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(): Flow<NetworkReading?>

    @Query("SELECT * FROM readings WHERE timestamp >= :startTimeMs AND timestamp <= :endTimeMs ORDER BY timestamp ASC")
    fun getReadingsInTimeRange(startTimeMs: Long, endTimeMs: Long): Flow<List<NetworkReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: NetworkReading): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<NetworkReading>)

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM readings")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM readings")
    suspend fun getCount(): Int
}
