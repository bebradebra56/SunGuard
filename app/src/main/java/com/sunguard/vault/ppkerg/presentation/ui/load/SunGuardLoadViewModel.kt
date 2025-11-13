package com.sunguard.vault.ppkerg.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunguard.vault.ppkerg.data.shar.SunGuardSharedPreference
import com.sunguard.vault.ppkerg.data.utils.SunGuardSystemService
import com.sunguard.vault.ppkerg.domain.usecases.SunGuardGetAllUseCase
import com.sunguard.vault.ppkerg.presentation.app.SunGuardAppsFlyerState
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SunGuardLoadViewModel(
    private val sunGuardGetAllUseCase: SunGuardGetAllUseCase,
    private val sunGuardSharedPreference: SunGuardSharedPreference,
    private val sunGuardSystemService: SunGuardSystemService
) : ViewModel() {

    private val _sunGuardHomeScreenState: MutableStateFlow<SunGuardHomeScreenState> =
        MutableStateFlow(SunGuardHomeScreenState.SunGuardLoading)
    val sunGuardHomeScreenState = _sunGuardHomeScreenState.asStateFlow()

    private var sunGuardGetApps = false


    init {
        viewModelScope.launch {
            when (sunGuardSharedPreference.sunGuardAppState) {
                0 -> {
                    if (sunGuardSystemService.sunGuardIsOnline()) {
                        SunGuardApplication.sunGuardConversionFlow.collect {
                            when(it) {
                                SunGuardAppsFlyerState.SunGuardDefault -> {}
                                SunGuardAppsFlyerState.SunGuardError -> {
                                    sunGuardSharedPreference.sunGuardAppState = 2
                                    _sunGuardHomeScreenState.value =
                                        SunGuardHomeScreenState.SunGuardError
                                    sunGuardGetApps = true
                                }
                                is SunGuardAppsFlyerState.SunGuardSuccess -> {
                                    if (!sunGuardGetApps) {
                                        sunGuardGetData(it.sunGuardData)
                                        sunGuardGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _sunGuardHomeScreenState.value =
                            SunGuardHomeScreenState.SunGuardNotInternet
                    }
                }
                1 -> {
                    if (sunGuardSystemService.sunGuardIsOnline()) {
                        if (SunGuardApplication.SUN_GUARD_FB_LI != null) {
                            _sunGuardHomeScreenState.value =
                                SunGuardHomeScreenState.SunGuardSuccess(
                                    SunGuardApplication.SUN_GUARD_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > sunGuardSharedPreference.sunGuardExpired) {
                            Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Current time more then expired, repeat request")
                            SunGuardApplication.sunGuardConversionFlow.collect {
                                when(it) {
                                    SunGuardAppsFlyerState.SunGuardDefault -> {}
                                    SunGuardAppsFlyerState.SunGuardError -> {
                                        _sunGuardHomeScreenState.value =
                                            SunGuardHomeScreenState.SunGuardSuccess(
                                                sunGuardSharedPreference.sunGuardSavedUrl
                                            )
                                        sunGuardGetApps = true
                                    }
                                    is SunGuardAppsFlyerState.SunGuardSuccess -> {
                                        if (!sunGuardGetApps) {
                                            sunGuardGetData(it.sunGuardData)
                                            sunGuardGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Current time less then expired, use saved url")
                            _sunGuardHomeScreenState.value =
                                SunGuardHomeScreenState.SunGuardSuccess(
                                    sunGuardSharedPreference.sunGuardSavedUrl
                                )
                        }
                    } else {
                        _sunGuardHomeScreenState.value =
                            SunGuardHomeScreenState.SunGuardNotInternet
                    }
                }
                2 -> {
                    _sunGuardHomeScreenState.value =
                        SunGuardHomeScreenState.SunGuardError
                }
            }
        }
    }


    private suspend fun sunGuardGetData(conversation: MutableMap<String, Any>?) {
        val sunGuardData = sunGuardGetAllUseCase.invoke(conversation)
        if (sunGuardSharedPreference.sunGuardAppState == 0) {
            if (sunGuardData == null) {
                sunGuardSharedPreference.sunGuardAppState = 2
                _sunGuardHomeScreenState.value =
                    SunGuardHomeScreenState.SunGuardError
            } else {
                sunGuardSharedPreference.sunGuardAppState = 1
                sunGuardSharedPreference.apply {
                    sunGuardExpired = sunGuardData.sunGuardExpires
                    sunGuardSavedUrl = sunGuardData.sunGuardUrl
                }
                _sunGuardHomeScreenState.value =
                    SunGuardHomeScreenState.SunGuardSuccess(sunGuardData.sunGuardUrl)
            }
        } else  {
            if (sunGuardData == null) {
                _sunGuardHomeScreenState.value =
                    SunGuardHomeScreenState.SunGuardSuccess(sunGuardSharedPreference.sunGuardSavedUrl)
            } else {
                sunGuardSharedPreference.apply {
                    sunGuardExpired = sunGuardData.sunGuardExpires
                    sunGuardSavedUrl = sunGuardData.sunGuardUrl
                }
                _sunGuardHomeScreenState.value =
                    SunGuardHomeScreenState.SunGuardSuccess(sunGuardData.sunGuardUrl)
            }
        }
    }


    sealed class SunGuardHomeScreenState {
        data object SunGuardLoading : SunGuardHomeScreenState()
        data object SunGuardError : SunGuardHomeScreenState()
        data class SunGuardSuccess(val data: String) : SunGuardHomeScreenState()
        data object SunGuardNotInternet: SunGuardHomeScreenState()
    }
}