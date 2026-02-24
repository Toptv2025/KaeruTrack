package com.kaeru.app.tracking

import com.google.gson.annotations.SerializedName

data class MelhorEnvioResponse(
    @SerializedName("data") val data: MelhorEnvioData?
)
data class MelhorEnvioData(
    @SerializedName("result") val result: MelhorEnvioResult?
)
data class MelhorEnvioResult(
    @SerializedName("trackingEvents") val events: List<MelhorEnvioEvent>?
)
data class MelhorEnvioEvent(
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("from") val fromLocation: String?,
    @SerializedName("description") val description: String?
)