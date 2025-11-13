package com.sunguard.vault.ppkerg.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.sunguard.vault.ppkerg.presentation.di.sunGuardModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface SunGuardAppsFlyerState {
    data object SunGuardDefault : SunGuardAppsFlyerState
    data class SunGuardSuccess(val sunGuardData: MutableMap<String, Any>?) :
        SunGuardAppsFlyerState

    data object SunGuardError : SunGuardAppsFlyerState
}

interface SunGuardAppsApi {
    @Headers("Content-Type: application/json")
    @GET(SUN_GUARD_LIN)
    fun sunGuardGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val SUN_GUARD_APP_DEV = "XADYKMbEEGKsB3f9HtTUYg"
private const val SUN_GUARD_LIN = "com.sunguard.vault"

class SunGuardApplication : Application() {
    private var sunGuardIsResumed = false
    private var sunGuardConversionTimeoutJob: Job? = null
    private var sunGuardDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        sunGuardSetDebufLogger(appsflyer)
        sunGuardMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        sunGuardExtractDeepMap(p0.deepLink)
                        Log.d(SUN_GUARD_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(SUN_GUARD_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(SUN_GUARD_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            SUN_GUARD_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    sunGuardConversionTimeoutJob?.cancel()
                    Log.d(SUN_GUARD_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val api = sunGuardGetApi(
                                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                    null
                                )
                                val response = api.sunGuardGetClient(
                                    devkey = SUN_GUARD_APP_DEV,
                                    deviceId = sunGuardGetAppsflyerId()
                                ).awaitResponse()

                                val resp = response.body()
                                Log.d(SUN_GUARD_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status") == "Organic" || resp?.get("af_status") == null) {
                                    sunGuardResume(SunGuardAppsFlyerState.SunGuardError)
                                } else {
                                    sunGuardResume(
                                        SunGuardAppsFlyerState.SunGuardSuccess(resp)
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(SUN_GUARD_MAIN_TAG, "Error: ${d.message}")
                                sunGuardResume(SunGuardAppsFlyerState.SunGuardError)
                            }
                        }
                    } else {
                        sunGuardResume(SunGuardAppsFlyerState.SunGuardSuccess(p0))
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    sunGuardConversionTimeoutJob?.cancel()
                    Log.d(SUN_GUARD_MAIN_TAG, "onConversionDataFail: $p0")
                    sunGuardResume(SunGuardAppsFlyerState.SunGuardError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(SUN_GUARD_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(SUN_GUARD_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, SUN_GUARD_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(SUN_GUARD_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(SUN_GUARD_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
        sunGuardStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@SunGuardApplication)
            modules(
                listOf(
                    sunGuardModule
                )
            )
        }
    }

    private fun sunGuardExtractDeepMap(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(SUN_GUARD_MAIN_TAG, "Extracted DeepLink data: $map")
        sunGuardDeepLinkData = map
    }

    private fun sunGuardStartConversionTimeout() {
        sunGuardConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (!sunGuardIsResumed) {
                Log.d(SUN_GUARD_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
                sunGuardResume(SunGuardAppsFlyerState.SunGuardError)
            }
        }
    }

    private fun sunGuardResume(state: SunGuardAppsFlyerState) {
        sunGuardConversionTimeoutJob?.cancel()
        if (state is SunGuardAppsFlyerState.SunGuardSuccess) {
            val convData = state.sunGuardData ?: mutableMapOf()
            val deepData = sunGuardDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!sunGuardIsResumed) {
                sunGuardIsResumed = true
                sunGuardConversionFlow.value = SunGuardAppsFlyerState.SunGuardSuccess(merged)
            }
        } else {
            if (!sunGuardIsResumed) {
                sunGuardIsResumed = true
                sunGuardConversionFlow.value = state
            }
        }
    }

    private fun sunGuardGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(SUN_GUARD_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun sunGuardSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun sunGuardMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun sunGuardGetApi(url: String, client: OkHttpClient?): SunGuardAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    companion object {
        var sunGuardInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val sunGuardConversionFlow: MutableStateFlow<SunGuardAppsFlyerState> = MutableStateFlow(
            SunGuardAppsFlyerState.SunGuardDefault
        )
        var SUN_GUARD_FB_LI: String? = null
        const val SUN_GUARD_MAIN_TAG = "SunGuardMainTag"
    }
}