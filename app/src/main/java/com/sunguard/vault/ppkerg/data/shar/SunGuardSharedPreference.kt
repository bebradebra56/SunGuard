package com.sunguard.vault.ppkerg.data.shar

import android.content.Context
import androidx.core.content.edit

class SunGuardSharedPreference(context: Context) {
    private val sunGuardPrefs = context.getSharedPreferences("sunGuardSharedPrefsAb", Context.MODE_PRIVATE)

    var sunGuardSavedUrl: String
        get() = sunGuardPrefs.getString(SUN_GUARD_SAVED_URL, "") ?: ""
        set(value) = sunGuardPrefs.edit { putString(SUN_GUARD_SAVED_URL, value) }

    var sunGuardExpired : Long
        get() = sunGuardPrefs.getLong(SUN_GUARD_EXPIRED, 0L)
        set(value) = sunGuardPrefs.edit { putLong(SUN_GUARD_EXPIRED, value) }

    var sunGuardAppState: Int
        get() = sunGuardPrefs.getInt(SUN_GUARD_APPLICATION_STATE, 0)
        set(value) = sunGuardPrefs.edit { putInt(SUN_GUARD_APPLICATION_STATE, value) }

    var sunGuardNotificationRequest: Long
        get() = sunGuardPrefs.getLong(SUN_GUARD_NOTIFICAITON_REQUEST, 0L)
        set(value) = sunGuardPrefs.edit { putLong(SUN_GUARD_NOTIFICAITON_REQUEST, value) }

    var sunGuardNotificationRequestedBefore: Boolean
        get() = sunGuardPrefs.getBoolean(SUN_GUARD_NOTIFICATION_REQUEST_BEFORE, false)
        set(value) = sunGuardPrefs.edit { putBoolean(
            SUN_GUARD_NOTIFICATION_REQUEST_BEFORE, value) }

    companion object {
        private const val SUN_GUARD_SAVED_URL = "sunGuardSavedUrl"
        private const val SUN_GUARD_EXPIRED = "sunGuardExpired"
        private const val SUN_GUARD_APPLICATION_STATE = "sunGuardApplicationState"
        private const val SUN_GUARD_NOTIFICAITON_REQUEST = "sunGuardNotificationRequest"
        private const val SUN_GUARD_NOTIFICATION_REQUEST_BEFORE = "sunGuardNotificationRequestedBefore"
    }
}