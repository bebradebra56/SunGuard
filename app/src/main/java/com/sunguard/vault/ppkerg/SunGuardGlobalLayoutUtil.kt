package com.sunguard.vault.ppkerg

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication

class SunGuardGlobalLayoutUtil {

    private var sunGuardMChildOfContent: View? = null
    private var sunGuardUsableHeightPrevious = 0

    fun sunGuardAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        sunGuardMChildOfContent = content.getChildAt(0)

        sunGuardMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val sunGuardUsableHeightNow = sunGuardComputeUsableHeight()
        if (sunGuardUsableHeightNow != sunGuardUsableHeightPrevious) {
            val sunGuardUsableHeightSansKeyboard = sunGuardMChildOfContent?.rootView?.height ?: 0
            val sunGuardHeightDifference = sunGuardUsableHeightSansKeyboard - sunGuardUsableHeightNow

            if (sunGuardHeightDifference > (sunGuardUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(SunGuardApplication.sunGuardInputMode)
            } else {
                activity.window.setSoftInputMode(SunGuardApplication.sunGuardInputMode)
            }
//            mChildOfContent?.requestLayout()
            sunGuardUsableHeightPrevious = sunGuardUsableHeightNow
        }
    }

    private fun sunGuardComputeUsableHeight(): Int {
        val r = Rect()
        sunGuardMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}