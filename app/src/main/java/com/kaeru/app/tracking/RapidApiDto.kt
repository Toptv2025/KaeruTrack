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
    @SerializedName("descricao") val description: String?, // "Objeto em transferência..."
    @SerializedName("dtHrCriado") val createdAt: CreatedAt?, // Objeto de Data
    @SerializedName("unidade") val unit: UnitObject?,       // Onde está
    @SerializedName("unidadeDestino") val destination: UnitObject? // Para onde vai
)
data class CreatedAt(
    @SerializedName("date") val dateIso: String? // "2025-12-15 16:22:19.000000"
)
data class UnitObject(
    @SerializedName("tipo") val type: String?, // "Agência dos Correios"
    @SerializedName("endereco") val address: AddressObject?
)
data class AddressObject(
    @SerializedName("cidade") val city: String?,
    @SerializedName("uf") val state: String?
)