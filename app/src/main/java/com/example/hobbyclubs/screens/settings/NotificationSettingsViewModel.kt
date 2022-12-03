package com.example.hobbyclubs.screens.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.Request
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
    val requestAmount = MutableLiveData<Int>()
    val retrievedSettings = MutableLiveData<List<Boolean>>()
    val toggleSettings = MutableLiveData<List<NotificationSetting>>()
    val hasChanged = MutableLiveData(false)

    init {
        retrieveSettings()
    }

    fun changeSetting(
        currentSettings: List<NotificationSetting>,
        setting: NotificationSetting,
        isActive: Boolean
    ) {
        toggleSettings.value = listOf()
        toggleSettings.value = currentSettings.apply {
            find { it.title == setting.title }?.isActive = isActive
        }
        hasChanged.value =
            currentSettings.map { it.isActive } != retrievedSettings.value
    }

    fun getBoolVal(key: String) = settingsPref.getBoolean(key, false)

    private fun retrieveSettings() {
        val retrieved = NotificationSetting.values().toList()
            .map { setting ->
                setting.apply { isActive = getBoolVal(setting.name) }
            }
        retrievedSettings.value = retrieved.map { it.isActive }
        toggleSettings.value = retrieved
    }

    fun onSave(currentSettings: List<NotificationSetting>) {
            currentSettings.forEach { setting ->
                saveOptionToPref(setting.name, setting.isActive)
            }
            viewModelScope.launch(Dispatchers.IO) {
                eventNotificationHelper.updateAlarms()
            }
            retrievedSettings.value = currentSettings.map { it.isActive }
            hasChanged.value = false
    }

    private fun saveOptionToPref(settingName: String, isActive: Boolean) {
        settingsPref.edit().apply {
            putBoolean(settingName, isActive)
            apply()
        }
    }
}