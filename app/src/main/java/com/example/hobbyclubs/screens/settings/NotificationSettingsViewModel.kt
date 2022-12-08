package com.example.hobbyclubs.screens.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.database.EventAlarmDBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "SettingsViewModel"
    }

    private val settingsPref: SharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val eventNotificationHelper = EventAlarmDBHelper(context = application)
    val retrievedSettings = MutableLiveData<List<Pair<String, Boolean>>>()
    val settingValues = MutableLiveData<List<Pair<String, Boolean>>>()

    init {
        retrieveSettings()
    }

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

    fun getBoolVal(key: String) = settingsPref.getBoolean(key, false)

    private fun retrieveSettings() {
        val retrieved = NotificationSetting.values().map {
            Pair(it.name, getBoolVal(it.name))
        }
        retrievedSettings.value = retrieved
        settingValues.value = retrieved
    }

    fun onSave(currentSettings: List<Pair<String, Boolean>>) {
            currentSettings.forEach { setting ->
                saveOptionToPref(setting.first, setting.second)
            }
            viewModelScope.launch(Dispatchers.IO) {
                eventNotificationHelper.updateAlarms()
            }
            retrievedSettings.value = currentSettings
    }

    private fun saveOptionToPref(settingName: String, isActive: Boolean) {
        settingsPref.edit().apply {
            putBoolean(settingName, isActive)
            apply()
        }
    }
}