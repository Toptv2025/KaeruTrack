package com.kaeru.app.tracking

import com.google.gson.annotations.SerializedName

data class CainiaoResponse(
    @SerializedName("module") val modules: List<CainiaoModule>?,
    @SerializedName("success") val success: Boolean?
)
data class CainiaoModule(
    @SerializedName("mailNo") val code: String?,
    @SerializedName("detailList") val details: List<CainiaoDetail>?
)
data class CainiaoDetail(
    @SerializedName("timeStr") val dateString: String?,
    @SerializedName("standerdDesc") val statusDescription: String?,
    @SerializedName("desc") val altDescription: String?
)