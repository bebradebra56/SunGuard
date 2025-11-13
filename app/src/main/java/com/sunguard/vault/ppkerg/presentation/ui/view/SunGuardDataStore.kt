package com.sunguard.vault.ppkerg.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class SunGuardDataStore : ViewModel(){
    val sunGuardViList: MutableList<SunGuardVi> = mutableListOf()
    var sunGuardIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var sunGuardContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var sunGuardView: SunGuardVi

}