package com.currencyconverter.offline.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface RateResult {
    /** [offline] = true, если курс взят из локальной базы. */
    data class Success(val rate: Double, val offline: Boolean, val updatedAt: Long) : RateResult
    data object NoData : RateResult
}

class RatesRepository(private val dao: RatesDao) {

    suspend fun getRate(from: String, to: String): RateResult {
        val fresh = runCatching { fetchRates(from) }.getOrNull()
        val freshRate = fresh?.takeIf { it.has(to) }?.getDouble(to)

        if (fresh != null && freshRate != null) {
            val now = System.currentTimeMillis()
            dao.upsert(CachedRates(from, fresh.toString(), now))
            return RateResult.Success(freshRate, offline = false, updatedAt = now)
        }

        // Нет сети / API не отвечает -> последний сохранённый курс
        val cached = dao.get(from) ?: return RateResult.NoData
        val cachedRate = runCatching { JSONObject(cached.ratesJson) }
            .getOrNull()?.takeIf { it.has(to) }?.getDouble(to)
            ?: return RateResult.NoData
        return RateResult.Success(cachedRate, offline = true, updatedAt = cached.updatedAt)
    }

    private suspend fun fetchRates(base: String): JSONObject = withContext(Dispatchers.IO) {
        val conn = URL("https://open.er-api.com/v6/latest/$base").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.requestMethod = "GET"
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            if (json.optString("result") != "success") error("API error")
            json.getJSONObject("rates")
        } finally {
            conn.disconnect()
        }
    }
}
