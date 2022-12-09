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

/**
 * Represents an alarm set for an event reminder. When notifications for event reminders are active,
 * the user will receive timed notifications one hour and/or one day before the start of events
 * they've joined or liked.
 * This data class holds the data relevant to setting up an alarm with the device's AlarmManager
 * which will trigger those notifications in the future.
 *
 * @property id
 * @property eventId
 * @property eventTime
 * @property eventName
 * @property hoursBefore amount of hours the reminder should ring prior to event
 */
@Entity
data class EventAlarmData(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val eventId: String,
    val eventTime: Timestamp,
    val eventName: String,
    val hoursBefore: Int
): Serializable

/**
 * Data access object for the event alarm database
 *
 */
@Dao
interface EventAlarmDataDao {
    @Insert
    fun addData(data: EventAlarmData): Long

    @Delete
    fun removeData(data: EventAlarmData)

    @Query("SELECT * FROM eventalarmdata WHERE eventId = :eventId")
    fun getDataByEventId(eventId: String): List<EventAlarmData>

    @Query("SELECT * FROM eventalarmdata WHERE hoursBefore = :hoursBefore")
    fun getDataByHoursBefore(hoursBefore: Int): List<EventAlarmData>

    @Query("SELECT * FROM eventalarmdata")
    fun getAllData(): List<EventAlarmData>

    @Update
    fun updateData(data: EventAlarmData)

    @Query("DELETE FROM eventalarmdata WHERE hoursBefore = :hoursBefore")
    fun deleteByHoursBefore(hoursBefore: Int)


}