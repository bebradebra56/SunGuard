package com.sunguard.vault.ppkerg.domain.model

import com.google.gson.annotations.SerializedName


private const val SUN_GUARD_A = "com.sunguard.vault"
private const val SUN_GUARD_B = "sunguard-71eb5"
data class SunGuardParam (
    @SerializedName("af_id")
    val sunGuardAfId: String,
    @SerializedName("bundle_id")
    val sunGuardBundleId: String = SUN_GUARD_A,
    @SerializedName("os")
    val sunGuardOs: String = "Android",
    @SerializedName("store_id")
    val sunGuardStoreId: String = SUN_GUARD_A,
    @SerializedName("locale")
    val sunGuardLocale: String,
    @SerializedName("push_token")
    val sunGuardPushToken: String,
    @SerializedName("firebase_project_id")
    val sunGuardFirebaseProjectId: String = SUN_GUARD_B,

    )