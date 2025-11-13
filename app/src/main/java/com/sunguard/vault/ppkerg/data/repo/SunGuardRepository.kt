package com.sunguard.vault.ppkerg.data.repo

import android.util.Log
import com.sunguard.vault.ppkerg.domain.model.SunGuardEntity
import com.sunguard.vault.ppkerg.domain.model.SunGuardParam
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication.Companion.SUN_GUARD_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SunGuardApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun sunGuardGetClient(
        @Body jsonString: JsonObject,
    ): Call<SunGuardEntity>
}


private const val SUN_GUARD_MAIN = "https://sunguarrd.com/"
class SunGuardRepository {

    suspend fun sunGuardGetClient(
        sunGuardParam: SunGuardParam,
        sunGuardConversion: MutableMap<String, Any>?
    ): SunGuardEntity? {
        val gson = Gson()
        val api = sunGuardGetApi(SUN_GUARD_MAIN, null)

        val sunGuardJsonObject = gson.toJsonTree(sunGuardParam).asJsonObject
        sunGuardConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            sunGuardJsonObject.add(key, element)
        }
        return try {
            val sunGuardRequest: Call<SunGuardEntity> = api.sunGuardGetClient(
                jsonString = sunGuardJsonObject,
            )
            val sunGuardResult = sunGuardRequest.awaitResponse()
            Log.d(SUN_GUARD_MAIN_TAG, "Retrofit: Result code: ${sunGuardResult.code()}")
            if (sunGuardResult.code() == 200) {
                Log.d(SUN_GUARD_MAIN_TAG, "Retrofit: Get request success")
                Log.d(SUN_GUARD_MAIN_TAG, "Retrofit: Code = ${sunGuardResult.code()}")
                Log.d(SUN_GUARD_MAIN_TAG, "Retrofit: ${sunGuardResult.body()}")
                sunGuardResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(SUN_GUARD_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(SUN_GUARD_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun sunGuardGetApi(url: String, client: OkHttpClient?) : SunGuardApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
