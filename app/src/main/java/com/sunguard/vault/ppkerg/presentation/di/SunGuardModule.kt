package com.sunguard.vault.ppkerg.presentation.di

import com.sunguard.vault.ppkerg.data.repo.SunGuardRepository
import com.sunguard.vault.ppkerg.data.shar.SunGuardSharedPreference
import com.sunguard.vault.ppkerg.data.utils.SunGuardPushToken
import com.sunguard.vault.ppkerg.data.utils.SunGuardSystemService
import com.sunguard.vault.ppkerg.domain.usecases.SunGuardGetAllUseCase
import com.sunguard.vault.ppkerg.presentation.pushhandler.SunGuardPushHandler
import com.sunguard.vault.ppkerg.presentation.ui.load.SunGuardLoadViewModel
import com.sunguard.vault.ppkerg.presentation.ui.view.SunGuardViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sunGuardModule = module {
    factory {
        SunGuardPushHandler()
    }
    single {
        SunGuardRepository()
    }
    single {
        SunGuardSharedPreference(get())
    }
    factory {
        SunGuardPushToken()
    }
    factory {
        SunGuardSystemService(get())
    }
    factory {
        SunGuardGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        SunGuardViFun(get())
    }
    viewModel {
        SunGuardLoadViewModel(get(), get(), get())
    }
}