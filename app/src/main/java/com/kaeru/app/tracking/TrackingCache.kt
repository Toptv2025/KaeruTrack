package com.kaeru.app.tracking

import android.util.Log

object TrackingCache {
    private data class CachedData(
        val response: TrackingResponse,
        val timestamp: Long
    )
    private val cache = mutableMapOf<String, CachedData>()
    private const val CACHE_EXPIRATION_MS = 5 * 60 * 1000
    fun get(code: String): TrackingResponse? {
        val cached = cache[code] ?: return null
        val isExpired = (System.currentTimeMillis() - cached.timestamp) > CACHE_EXPIRATION_MS
        if (isExpired) {
            Log.d("CACHE", "cache expirado")
            cache.remove(code)
            return null
        }
        Log.d("CACHE", "cache válido")
        return cached.response
    }
    fun save(code: String, response: TrackingResponse) {
        cache[code] = CachedData(response, System.currentTimeMillis())
        Log.d("CACHE", "cache salvo")
    }
    fun clear() {
        cache.clear()
    }
}