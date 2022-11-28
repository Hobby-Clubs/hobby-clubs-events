package com.example.hobbyclubs.screens.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.hobbyclubs.database.EventAlarmDBHelper
import com.example.hobbyclubs.screens.settings.NotificationSetting.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPref: SharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val eventSetting = MutableLiveData(false)
    val newsSetting = MutableLiveData(false)
    val requestSetting = MutableLiveData(false)
    val eventNotificationHelper = EventAlarmDBHelper(context = application)

    init {
        retrieveSettings()
    }

    fun getBoolSetting(setting: NotificationSetting) = settingsPref.getBoolean(setting.name, false)

    private fun retrieveSettings() {
        eventSetting.value = getBoolSetting(EVENT_NOTIFICATIONS)
        newsSetting.value = getBoolSetting(NEWS_NOTIFICATIONS)
        requestSetting.value = getBoolSetting(REQUEST_NOTIFICATIONS)
    }

    fun updateOption(setting: NotificationSetting, isActive: Boolean) {
        when (setting) {
            EVENT_NOTIFICATIONS -> {
                eventSetting.value = isActive
            }
            NEWS_NOTIFICATIONS -> {
                newsSetting.value = isActive
            }
            REQUEST_NOTIFICATIONS -> {
                requestSetting.value = isActive
            }
        }
    }

    fun savePrefs() {
        saveOptionToPref(EVENT_NOTIFICATIONS.name, eventSetting.value ?: false)
        saveOptionToPref(NEWS_NOTIFICATIONS.name, newsSetting.value ?: false)
        saveOptionToPref(REQUEST_NOTIFICATIONS.name, requestSetting.value ?: false)
        eventSetting.value?.let {
            handleEventAlarms(it)
        }
    }

    private fun saveOptionToPref(settingName: String, isActive: Boolean) {
        settingsPref.edit().apply {
            putBoolean(settingName, isActive)
            apply()
        }
    }

    fun handleEventAlarms(isActive: Boolean) = eventNotificationHelper.toggleNotifications(isActive)
}

enum class NotificationSetting {
    EVENT_NOTIFICATIONS,
    NEWS_NOTIFICATIONS,
    REQUEST_NOTIFICATIONS
}