package com.kaeru.app.tracking

import android.util.Log
import org.jsoup.Jsoup

object LinketrackParser {
    fun parseHtml(html: String, code: String): TrackingResponse? {
        val events = mutableListOf<TrackingEvent>()
        try {
            val doc = Jsoup.parse(html)
            val items = doc.select("ul.evento-collection")

            if (items.isEmpty()) {
                Log.e("LINKETRACK", "evento nao encontrado")
                return null
            }

            for (item in items) {
                val statusRaw = item.select("span.eventoStatus").text().trim()
                val dateLiText = item.select("li:contains(Data:)").text()
                val dateRegex = Regex("""(\d{2}/\d{2}/\d{4})""")
                val timeRegex = Regex("""(\d{2}:\d{2})""")
                val dateFormatted = dateRegex.find(dateLiText)?.value ?: ""
                val timeFormatted = timeRegex.find(dateLiText)?.value ?: ""
                val localLi = item.select("li.status-box").first()
                localLi?.select("br")?.append(" -> ")
                val locationRaw = localLi?.text() ?: "Local desconhecido"
                val locationClean = locationRaw
                    .replace("Local:", "")
                    .replace("Destino:", "")
                    .replace(" -> ", " ➔ ")
                    .trim()
                val statusLi = item.select("li:has(span.eventoStatus)").first()
                var subStatusRaw = statusLi?.text()?.replace(statusRaw, "")?.trim() ?: ""
                if (subStatusRaw.startsWith("Data:") || subStatusRaw.contains("ajuda")) {
                    subStatusRaw = ""
                }
                if (statusRaw.isNotBlank()) {
                    events.add(
                        TrackingEvent(
                            status = statusRaw,
                            date = dateFormatted,
                            time = timeFormatted,
                            location = locationClean,
                            subStatus = if (subStatusRaw.isNotEmpty()) listOf(subStatusRaw) else null
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("LINKETRACK", "erro ${e.message}")
            e.printStackTrace()
            return null
        }

        if (events.isEmpty()) return null

        return TrackingResponse(
            tracking_code = code,
            events = events
        )
    }
}