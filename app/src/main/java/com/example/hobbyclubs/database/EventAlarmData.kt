package com.example.hobbyclubs.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.google.firebase.Timestamp
import java.io.Serializable

@Entity
data class EventAlarmData(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val eventId: String,
    val eventTime: Timestamp,
    val eventName: String,
): Serializable

@Dao
interface EventAlarmDataDao {
    @Insert
    fun addData(data: EventAlarmData): Long

    @Delete
    fun removeData(data: EventAlarmData)

    @Query("SELECT * FROM eventalarmdata WHERE eventId = :eventId")
    fun getDataByEventId(eventId: String): List<EventAlarmData>

    @Query("SELECT * FROM eventalarmdata")
    fun getAllData(): List<EventAlarmData>

    @Update
    fun updateData(data: EventAlarmData)
}