package com.sunguard.vault.ppkerg.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication

class SunGuardPushHandler {
    fun sunGuardHandlePush(extras: Bundle?) {
        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = sunGuardBundleToMap(extras)
            Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    SunGuardApplication.SUN_GUARD_FB_LI = map["url"]
                    Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Push data no!")
        }
    }

    private fun sunGuardBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}