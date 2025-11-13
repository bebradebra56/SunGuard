package com.sunguard.vault.ppkerg.domain.usecases

import android.util.Log
import com.sunguard.vault.ppkerg.data.repo.SunGuardRepository
import com.sunguard.vault.ppkerg.data.utils.SunGuardPushToken
import com.sunguard.vault.ppkerg.data.utils.SunGuardSystemService
import com.sunguard.vault.ppkerg.domain.model.SunGuardEntity
import com.sunguard.vault.ppkerg.domain.model.SunGuardParam
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication

class SunGuardGetAllUseCase(
    private val sunGuardRepository: SunGuardRepository,
    private val sunGuardSystemService: SunGuardSystemService,
    private val sunGuardPushToken: SunGuardPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : SunGuardEntity?{
        val params = SunGuardParam(
            sunGuardLocale = sunGuardSystemService.sunGuardGetLocale(),
            sunGuardPushToken = sunGuardPushToken.sunGuardGetToken(),
            sunGuardAfId = sunGuardSystemService.sunGuardGetAppsflyerId()
        )
        Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Params for request: $params")
        return sunGuardRepository.sunGuardGetClient(params, conversion)
    }



}