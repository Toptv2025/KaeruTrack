package com.kaeru.app.tracking

import com.google.gson.annotations.SerializedName

data class TotalExpressResponse(
    @SerializedName("data") val data: TotalExpressData?
)

data class TotalExpressData(
    @SerializedName("layouts") val layouts: List<TotalExpressLayout>?
)

data class TotalExpressLayout(
    @SerializedName("etapas") val etapas: List<TotalExpressEtapa>?
)

data class TotalExpressEtapa(
    @SerializedName("listaStatus") val listaStatus: List<TotalExpressStatus>?
)

data class TotalExpressStatus(
    @SerializedName("statusDescricao") val statusDescricao: String?,
    @SerializedName("data") val data: String?,
    @SerializedName("hora") val hora: String?,
    @SerializedName("mensagemEvaTraducao") val mensagemEvaTraducao: TotalExpressMensagem?
)

data class TotalExpressMensagem(
    @SerializedName("mensagemEva") val mensagemEva: String?
)