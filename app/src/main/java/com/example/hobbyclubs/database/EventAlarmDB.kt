package com.example.hobbyclubs.database

import android.content.Context
import android.util.Log
import androidx.room.*
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.notifications.AlarmHelper
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

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