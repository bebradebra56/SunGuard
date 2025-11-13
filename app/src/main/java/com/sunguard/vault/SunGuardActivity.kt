package com.sunguard.vault

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.sunguard.vault.ppkerg.SunGuardGlobalLayoutUtil
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication
import com.sunguard.vault.ppkerg.presentation.pushhandler.SunGuardPushHandler
import com.sunguard.vault.ppkerg.sunGuardSetupSystemBars
import org.koin.android.ext.android.inject

class SunGuardActivity : AppCompatActivity() {

    private val sunGuardPushHandler by inject<SunGuardPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sunGuardSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_sun_guard)

        val sunGuardRootView = findViewById<View>(android.R.id.content)
        SunGuardGlobalLayoutUtil().sunGuardAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(sunGuardRootView) { sunGuardView, sunGuardInsets ->
            val sunGuardSystemBars = sunGuardInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val sunGuardDisplayCutout = sunGuardInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val sunGuardIme = sunGuardInsets.getInsets(WindowInsetsCompat.Type.ime())


            val sunGuardTopPadding = maxOf(sunGuardSystemBars.top, sunGuardDisplayCutout.top)
            val sunGuardLeftPadding = maxOf(sunGuardSystemBars.left, sunGuardDisplayCutout.left)
            val sunGuardRightPadding = maxOf(sunGuardSystemBars.right, sunGuardDisplayCutout.right)
            window.setSoftInputMode(SunGuardApplication.sunGuardInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "ADJUST PUN")
                val sunGuardBottomInset = maxOf(sunGuardSystemBars.bottom, sunGuardDisplayCutout.bottom)

                sunGuardView.setPadding(sunGuardLeftPadding, sunGuardTopPadding, sunGuardRightPadding, 0)

                sunGuardView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = sunGuardBottomInset
                }
            } else {
                Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "ADJUST RESIZE")

                val sunGuardBottomInset = maxOf(sunGuardSystemBars.bottom, sunGuardDisplayCutout.bottom, sunGuardIme.bottom)

                sunGuardView.setPadding(sunGuardLeftPadding, sunGuardTopPadding, sunGuardRightPadding, 0)

                sunGuardView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = sunGuardBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Activity onCreate()")
        sunGuardPushHandler.sunGuardHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            sunGuardSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        sunGuardSetupSystemBars()
    }
}