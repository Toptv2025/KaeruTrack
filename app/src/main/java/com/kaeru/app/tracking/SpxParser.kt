package com.kaeru.app.tracking

import android.util.Log
import org.jsoup.Jsoup

object SpxParser {

    fun parseHtml(html: String, code: String): TrackingResponse? {
        val events = mutableListOf<TrackingEvent>()
        try {
            val doc = Jsoup.parse(html)
            val items = doc.select("div.nss-comp-tracking-item")
            if (items.isEmpty()) {
                Log.e("SPX", "não encontrou a div :/")
                return null
            }
            for (item in items) {
                val leftColumn = item.selectFirst("div.left")
                val timeRaw = leftColumn?.selectFirst("div.second")?.text()?.trim() ?: ""
                val dateRaw = leftColumn?.selectFirst("div.day")?.text()?.trim() ?: ""
                val status = item.selectFirst("div.right div.message")?.text()?.trim() ?: ""
                Log.d("SPX", "Data: $dateRaw | Hora: $timeRaw | Status: $status")
                if (status.isNotBlank()) {
                    events.add(
                        TrackingEvent(
                            status = status,
                            date = dateRaw,
                            time = timeRaw,
                            location = "Shopee Xpress",
                            subStatus = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SPX", "erro no parse")
            return null
        }
        return if (events.isNotEmpty()) TrackingResponse(tracking_code = code, events = events) else null
    }
}