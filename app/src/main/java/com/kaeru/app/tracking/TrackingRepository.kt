package com.kaeru.app.tracking

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.kaeru.app.data.scraper.LinketrackWebViewScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class TrackingRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    fun isCarrierSupported(code: String): Boolean {
        val cleanCode = code.trim().uppercase()
        return when {
            listOf("MZ", "LJ", "LOG").any { cleanCode.startsWith(it) } && cleanCode.length >= 12 -> true
            listOf("NN", "LP").any { cleanCode.startsWith(it) } && cleanCode.length >= 12 -> true
            listOf("ME").any { cleanCode.startsWith(it) } && cleanCode.length >= 12 -> true
            listOf("AJ").any { cleanCode.startsWith(it) } && cleanCode.length >= 12 -> true
            listOf("BR", "SPX").any { cleanCode.startsWith(it) } && cleanCode.length >= 12 -> true
            listOf("TX").any { cleanCode.startsWith(it) } || listOf("TX").any { cleanCode.endsWith(it) } -> true
            listOf("AD", "AB", "AN").any { cleanCode.startsWith(it) } && cleanCode.length >= 12 -> true
            else -> false
        }
    }
    suspend fun trackPackage(code: String, forceRefresh: Boolean = false, carrier: String = "Auto"): TrackingResponse? {
        val cleanCode = code.trim().uppercase()

        if (!forceRefresh) {
            val cached = TrackingCache.get(cleanCode)
            if (cached != null) {
                Log.d("TRACKING", "Recuperado da memória RAM: $cleanCode")
                return cached
            }
        }

        Log.d("TRACKING", "Baixando da internet: $cleanCode")

        val result = when (carrier) {
            "Correios" -> trackViaLinketrackWebView(cleanCode)
            "Loggi" -> trackViaLoggi(cleanCode)
            "Shopee" -> trackViaSpxWebView(cleanCode)
            "AliExpress" -> trackCainiaoFree(cleanCode)
            "Shein" -> trackViaAnjun(cleanCode)
            "Melhor Envio" -> trackMelhorRastreioFree(cleanCode)
            "Total Express" -> trackViaTotalExpress(cleanCode)
            else -> {
                when {
                    listOf("MZ", "LJ", "LOG").any { cleanCode.startsWith(it) } -> { trackViaLoggi(cleanCode) }
                    listOf("NN", "LP").any { cleanCode.startsWith(it) } -> { trackCainiaoFree(cleanCode) }
                    listOf("ME").any { cleanCode.startsWith(it) } -> { trackMelhorRastreioFree(cleanCode) }
                    listOf("AJ").any { cleanCode.startsWith(it) } -> { trackViaAnjun(cleanCode) }
                    listOf("BR", "SPX").any { cleanCode.startsWith(it) } -> { trackViaSpxWebView(cleanCode) }
                    listOf("TX").any { cleanCode.startsWith(it) } || listOf("TX").any { cleanCode.endsWith(it) } -> { trackViaTotalExpress(cleanCode) }
                    listOf("AD", "AB", "AN").any { cleanCode.startsWith(it) } -> { trackViaLinketrackWebView(cleanCode) }
                    else -> { null }
                }
            }
        }

        if (result != null && !result.events.isNullOrEmpty()) {
            TrackingCache.save(cleanCode, result)
        }

        return result
    }

    private suspend fun trackViaTotalExpress(code: String): TrackingResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://totalconecta.totalexpress.com.br/mfe-rastreio/api/order-data?awb=$code&language=pt"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                    .addHeader("Sec-Ch-Ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                    .addHeader("Sec-Ch-Ua-Mobile", "?0")
                    .addHeader("Sec-Ch-Ua-Platform", "\"Windows\"")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-site")
                    .addHeader("Referer", "https://totalconecta.totalexpress.com.br/mfe-rastreio/")
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                if (!response.isSuccessful) {
                    return@withContext null
                }
                if (body.isNullOrBlank()) {
                    return@withContext null
                }
                val rawData = gson.fromJson(body, TotalExpressResponse::class.java)
                val layouts = rawData.data?.layouts
                if (layouts.isNullOrEmpty()) {
                    return@withContext null
                }
                val events = mutableListOf<TrackingEvent>()

                layouts.forEach { layout ->
                    layout.etapas?.forEach { etapa ->
                        etapa.listaStatus?.forEach { status ->
                            val dateParts = status.data?.split("-")
                            val formattedDate = if (dateParts != null && dateParts.size == 3) {
                                "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
                            } else {
                                status.data ?: ""
                            }

                            events.add(
                                TrackingEvent(
                                    status = status.statusDescricao ?: "Atualização",
                                    date = formattedDate,
                                    time = status.hora ?: "",
                                    location = status.mensagemEvaTraducao?.mensagemEva ?: "Total Express",
                                    subStatus = null
                                )
                            )
                        }
                    }
                }

                if (events.isEmpty()) {
                    Log.e("TOTAL_EXPRESS", "Eventos vazios após mapeamento.")
                    return@withContext null
                }

                Log.d("TOTAL_EXPRESS", "Sucesso! Foram mapeados ${events.size} eventos.")
                return@withContext TrackingResponse(tracking_code = code, events = events.reversed())

            } catch (e: Exception) {
                Log.e("TOTAL_EXPRESS", "Erro fatal no rastreio: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun trackViaLinketrackWebView(code: String): TrackingResponse? {
        val cleanCode = code.substringBefore("|").trim()

        Log.d("TRACKING", "Iniciando Linketrack WebView Scraper para $cleanCode...")

        val scraper = LinketrackWebViewScraper(context)

        val html = scraper.fetchHtml(cleanCode)

        if (html.isNullOrBlank()) {
            Log.e("TRACKING", "Linketrack Scraper retornou vazio (Timeout ou Erro).")
            return null
        }

        Log.d("TRACKING", "HTML recebido! Iniciando parse...")
        return LinketrackParser.parseHtml(html, cleanCode)
    }

    private suspend fun trackViaSpxWebView(code: String): TrackingResponse? {
        val cleanCode = code.substringBefore("|").trim()
        Log.d("TRACKING", "Iniciando SPX Scraper para $cleanCode...")
        val scraper = SpxScraper(context)
        val html = scraper.fetchHtml(cleanCode)

        if (html.isNullOrBlank()) {
            Log.e("TRACKING", "SPX Scraper retornou vazio.")
            return null
        }

        return SpxParser.parseHtml(html, cleanCode)
    }

    private suspend fun trackViaLoggi(code: String): TrackingResponse? {
        val scraper = LoggiScraper(context)
        val html = scraper.fetchHtml(code)
        if (html.isNullOrBlank()) return null
        return parseLoggiHtml(html, code)
    }

    private fun parseLoggiHtml(html: String, code: String): TrackingResponse? {
        val events = mutableListOf<TrackingEvent>()
        try {
            val doc = Jsoup.parse(html)
            val steps = doc.select("div.MuiStep-root")
            if (steps.isEmpty()) return null
            for (step in steps) {
                val dateRaw = step.select(".MuiTypography-overline").text().trim()
                var status = step.select(".MuiTypography-subtitleLarge").text().trim()
                if (status.isEmpty()) status = step.select(".MuiTypography-subtitleMedium").text().trim()
                var location = step.select(".MuiTypography-bodyTextMedium").text().trim()
                if (location.startsWith("-")) location = location.removePrefix("-").trim()
                if (status.isNotEmpty()) {
                    events.add(TrackingEvent(status = status, date = dateRaw, time = "", location = location.ifBlank { "Loggi" }, subStatus = null))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        if (events.isEmpty()) return null
        return TrackingResponse(tracking_code = code, events = events)
    }

    private suspend fun trackViaAnjun(code: String): TrackingResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://anjunexpress.com/trackPackage?tracking=$code"
                val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(30000).get()
                val items = doc.select(".progress-item")
                if (items.isEmpty()) return@withContext null
                val events = items.map { item ->
                    val parts = item.select(".progress-time").text().trim().split(" ")
                    TrackingEvent(status = item.select(".progress-title").text().trim().ifBlank { "Status" }, date = parts.getOrNull(0) ?: "", time = parts.getOrNull(1) ?: "", location = item.select(".progress-desc").text().trim().ifBlank { "Em trânsito" }, subStatus = null)
                }.toMutableList()
                return@withContext TrackingResponse(tracking_code = code, events = events)
            } catch (e: Exception) { null }
        }
    }

    private suspend fun trackCainiaoFree(code: String): TrackingResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url("https://global.cainiao.com/global/detail.json?mailNos=$code&lang=pt-BR").build()
                val response = client.newCall(request).execute()
                val rawData = gson.fromJson(response.body?.string(), CainiaoResponse::class.java)
                val packageData = rawData.modules?.firstOrNull() ?: return@withContext null
                val uiEvents = packageData.details?.map { detail ->
                    val parts = (detail.dateString ?: "").split(" ")
                    val d = try { val s = parts.getOrElse(0){""}.split("-"); "${s[2]}/${s[1]}/${s[0]}" } catch(e:Exception){parts.getOrElse(0){""}}
                    var loc = "Internacional"; var stat = detail.statusDescription ?: detail.altDescription ?: "Status"
                    val m = Regex("^\\[(.*?)\\](.*)").find(stat)
                    if(m!=null){ loc=m.groupValues[1].trim(); stat=m.groupValues[2].trim().removePrefix("-").trim() }
                    TrackingEvent(status=stat, date=d, time=parts.getOrElse(1){""}, location=loc, subStatus=null)
                }
                return@withContext TrackingResponse(tracking_code = code, events = uiEvents)
            } catch (e: Exception) { null }
        }
    }

    private suspend fun trackMelhorRastreioFree(code: String): TrackingResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val body = """{"operationName":"searchParcel","variables":{"tracker":{"trackingCode":"$code","type":"melhorenvio"}},"query":"mutation searchParcel(${"$"}tracker:TrackerSearchInput!){result:searchParcel(tracker:${"$"}tracker){trackingEvents{title createdAt from description}}}"}""".toRequestBody("application/json".toMediaType())
                val req = Request.Builder().url("https://melhor-rastreio-api.melhorrastreio.com.br/graphql").post(body).header("User-Agent","Mozilla/5.0").build()
                val res = client.newCall(req).execute()
                val data = gson.fromJson(res.body?.string(), MelhorEnvioResponse::class.java)
                val evs = data?.data?.result?.events?.map {
                    val iso = it.createdAt?:""; val p = if(iso.contains("T")) iso.split("T") else listOf(iso,"")
                    val d = try{val s=p[0].split("-"); "${s[2]}/${s[1]}/${s[0]}"}catch(e:Exception){p[0]}
                    TrackingEvent(status=it.title?:"", date=d, time=p[1].take(5), location=it.fromLocation?:"", subStatus=null)
                }?.reversed() ?: return@withContext null
                return@withContext TrackingResponse(tracking_code = code, events = evs)
            } catch(e:Exception){ null }
        }
    }
}