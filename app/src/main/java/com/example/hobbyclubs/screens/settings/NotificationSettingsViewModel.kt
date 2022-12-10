package com.example.hobbyclubs.screens.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.database.EventAlarmDBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * View model for the NotificationSettingsScreen
 *
 * @param application
 */
class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "SettingsViewModel"
    }

    private val settingsPref: SharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val eventNotificationHelper = EventAlarmDBHelper(context = application)
    val retrievedSettings = MutableLiveData<List<Pair<String, Boolean>>>()
    val settingValues = MutableLiveData<List<Pair<String, Boolean>>>()

    init {
        retrieveSettings()
    }

    /**
     * Toggle a notification setting
     *
     * @param currentSettings
     * @param settingName
     * @param isActive
     */
    fun changeSetting(
        currentSettings: List<Pair<String, Boolean>>,
        settingName: String,
        isActive: Boolean
    ) {
        val newList = currentSettings.map { setting ->
            if (setting.first == settingName) Pair(settingName, isActive) else setting
        }
        settingValues.value = newList
    }

    /**
     * Returns a boolean value corresponding to a notification setting stored in SharedPreferences
     *
     * @param key
     */
    private fun getBoolVal(key: String) = settingsPref.getBoolean(key, false)

    /**
     * Retrieves the notification settings stored in SharedPreferences
     *
     */
    private fun retrieveSettings() {
        val retrieved = NotificationSetting.values().map {
            Pair(it.name, getBoolVal(it.name))
        }
        retrievedSettings.value = retrieved
        settingValues.value = retrieved
    }

    /**
     * Saves the notification settings in SharedPreferences and updates event reminder alarms
     *
     * @param currentSettings
     */
    fun onSave(currentSettings: List<Pair<String, Boolean>>) {
            currentSettings.forEach { setting ->
                saveSettingToPref(setting.first, setting.second)
            }
            viewModelScope.launch(Dispatchers.IO) {
                eventNotificationHelper.updateAlarms()
            }
            retrievedSettings.value = currentSettings
    }

    /**
     * Saves a notification setting to SharedPreferences
     *
     * @param settingName
     * @param isActive
     */
    private fun saveSettingToPref(settingName: String, isActive: Boolean) {
        settingsPref.edit().apply {
            putBoolean(settingName, isActive)
            apply()
        }
    }
}