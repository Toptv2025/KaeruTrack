package com.kaeru.app.tracking

import com.google.gson.annotations.SerializedName

data class SpxResponse(
    @SerializedName("retcode") val retcode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: SpxData?
)
data class SpxData(
    @SerializedName("tracking_list") val trackingList: List<SpxEvent>?
)
data class SpxEvent(
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String?
)