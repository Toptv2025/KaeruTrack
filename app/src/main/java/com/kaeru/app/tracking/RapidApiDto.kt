package com.kaeru.app.tracking

import com.google.gson.annotations.SerializedName

data class RapidApiResponse(
    @SerializedName("correios_object") val correiosObject: CorreiosObject?
)
data class CorreiosObject(
    @SerializedName("codObjeto") val code: String?,
    @SerializedName("eventos") val events: List<RapidApiEvent>?
)
data class RapidApiEvent(
    @SerializedName("descricao") val description: String?,
    @SerializedName("dtHrCriado") val createdAt: CreatedAt?,
    @SerializedName("unidade") val unit: UnitObject?,
    @SerializedName("unidadeDestino") val destination: UnitObject?
)
data class CreatedAt(
    @SerializedName("date") val dateIso: String?
)
data class UnitObject(
    @SerializedName("tipo") val type: String?,
    @SerializedName("endereco") val address: AddressObject?
)
data class AddressObject(
    @SerializedName("cidade") val city: String?,
    @SerializedName("uf") val state: String?
)