package com.example.hobbyclubs.database

import android.content.Context
import androidx.room.*
import com.google.firebase.Timestamp
import java.util.*

/**
 * Database containing all the alarms set as event reminders.
 * Keeps track of alarms set
 *
 */
@Database(entities = [EventAlarmData::class], version = 1)
@TypeConverters(Converters::class)
abstract class EventAlarmDB : RoomDatabase() {
    abstract fun eventNotificationDao(): EventAlarmDataDao

    companion object {
        fun getInstance(context: Context): EventAlarmDB {
            return Room.databaseBuilder(
                context,
                EventAlarmDB::class.java,
                "event_alarm_db"
            ).build()
        }
    }
}

/**
 * Converters for [EventAlarmDB]
 *
 */
class Converters {
    @TypeConverter
    fun toTimeStamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }

    @TypeConverter
    fun fromTimeStamp(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
}