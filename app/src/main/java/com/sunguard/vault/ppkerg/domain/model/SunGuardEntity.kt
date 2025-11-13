package com.sunguard.vault.ppkerg.domain.model

import com.google.gson.annotations.SerializedName


data class SunGuardEntity (
    @SerializedName("ok")
    val sunGuardOk: String,
    @SerializedName("url")
    val sunGuardUrl: String,
    @SerializedName("expires")
    val sunGuardExpires: Long,
)