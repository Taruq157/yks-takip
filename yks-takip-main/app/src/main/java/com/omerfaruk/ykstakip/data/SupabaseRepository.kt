package com.omerfaruk.ykstakip.data

import com.omerfaruk.ykstakip.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter

data class AuthResponse(val accessToken: String?, val refreshToken: String?, val error: String?)
data class SupabaseTopic(val id: String, val title: String, val subjectName: String, val category: String)
data class SupabaseSnippet(val id: String, val topicId: String, val content: String, val type: String, val createdAt: String, val likesCount: Int = 0, val knowsCount: Int = 0, val savesCount: Int = 0)
data class SupabaseYigilma(val puanTuru: String, val puan: Float, val yil: Int, val siralama: Int)

object SupabaseRepository {
    private val PROJECT_URL = BuildConfig.SUPABASE_URL
    private val BASE_URL = "${PROJECT_URL}/rest/v1"
    private val AUTH_URL = "${PROJECT_URL}/auth/v1"
    private val API_KEY = BuildConfig.SUPABASE_KEY

    private suspend fun fetchJsonArray(endpoint: String): JSONArray = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        connection.setRequestProperty("Accept", "application/json")

        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.d("SupabaseRepo", "Response from $endpoint: $response")
                JSONArray(response)
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("SupabaseRepo", "Error from $endpoint: ${connection.responseCode} $error")
                JSONArray()
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Exception for $endpoint", e)
            JSONArray()
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getTopics(): List<SupabaseTopic> {
        val array = fetchJsonArray("topics?select=*")
        val list = mutableListOf<SupabaseTopic>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SupabaseTopic(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    subjectName = obj.optString("subject_name"),
                    category = obj.optString("category")
                )
            )
        }
        return list
    }

    suspend fun getSnippets(topicId: String): List<SupabaseSnippet> {
        val array = fetchJsonArray("snippets?topic_id=eq.$topicId&select=*")
        val list = mutableListOf<SupabaseSnippet>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SupabaseSnippet(
                    id = obj.optString("id"),
                    topicId = obj.optString("topic_id"),
                    content = obj.optString("content"),
                    type = obj.optString("type", "text"),
                    createdAt = obj.optString("created_at"),
                    likesCount = obj.optInt("likes_count", 0),
                    knowsCount = obj.optInt("knows_count", 0),
                    savesCount = obj.optInt("saves_count", 0)
                )
            )
        }
        return list
    }

    suspend fun getAllSnippets(): List<SupabaseSnippet> {
        val list = mutableListOf<SupabaseSnippet>()
        var offset = 0
        val pageSize = 1000
        var hasMore = true
        while (hasMore) {
            val array = fetchJsonArray("snippets?select=*&limit=$pageSize&offset=$offset")
            if (array.length() == 0) {
                hasMore = false
            } else {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SupabaseSnippet(
                            id = obj.optString("id"),
                            topicId = obj.optString("topic_id"),
                            content = obj.optString("content"),
                            type = obj.optString("type", "text"),
                            createdAt = obj.optString("created_at"),
                            likesCount = obj.optInt("likes_count", 0),
                            knowsCount = obj.optInt("knows_count", 0),
                            savesCount = obj.optInt("saves_count", 0)
                        )
                    )
                }
                if (array.length() < pageSize) {
                    hasMore = false
                } else {
                    offset += pageSize
                }
            }
        }
        return list
    }

    suspend fun signUp(email: String, pass: String): AuthResponse = withContext(Dispatchers.IO) {
        val url = URL("$AUTH_URL/signup")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("email", email)
            put("password", pass)
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                AuthResponse(
                    accessToken = json.optString("access_token", null),
                    refreshToken = json.optString("refresh_token", null),
                    error = null
                )
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                val msg = try { JSONObject(error).optString("msg") } catch(e: Exception) { error }
                AuthResponse(null, null, msg ?: "Bilinmeyen Hata")
            }
        } catch (e: Exception) {
            AuthResponse(null, null, e.message)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun signIn(email: String, pass: String): AuthResponse = withContext(Dispatchers.IO) {
        val url = URL("$AUTH_URL/token?grant_type=password")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("email", email)
            put("password", pass)
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                AuthResponse(
                    accessToken = json.optString("access_token", null),
                    refreshToken = json.optString("refresh_token", null),
                    error = null
                )
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                val msg = try { JSONObject(error).optString("error_description") } catch(e: Exception) { error }
                AuthResponse(null, null, msg ?: "Bilinmeyen Hata")
            }
        } catch (e: Exception) {
            AuthResponse(null, null, e.message)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResponse = withContext(Dispatchers.IO) {
        val url = URL("$AUTH_URL/token?grant_type=id_token")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("id_token", idToken)
            put("provider", "google")
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            if (connection.responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val access = if (json.has("access_token")) json.getString("access_token") else null
                val refresh = if (json.has("refresh_token")) json.getString("refresh_token") else null
                
                if (access != null && access.isNotEmpty()) {
                    AuthResponse(access, refresh, null)
                } else {
                    AuthResponse(null, null, "Başarılı kod ama token yok: $responseText")
                }
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                var msg = "Bilinmeyen Hata"
                try {
                    val errorJson = JSONObject(errorText)
                    msg = errorJson.optString("error_description", "")
                    if (msg.isEmpty()) msg = errorJson.optString("msg", "")
                    if (msg.isEmpty()) msg = errorJson.optString("message", "")
                } catch(e: Exception) {}
                
                if (msg.isEmpty() || msg == "Bilinmeyen Hata") {
                    msg = if (errorText.isNotEmpty()) errorText else "HTTP Kodu: ${connection.responseCode}"
                }
                AuthResponse(null, null, msg)
            }
        } catch (e: Exception) {
            AuthResponse(null, null, "Bağlantı Hatası: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getProfile(accessToken: String): JSONObject? = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/profiles?select=*")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                if (array.length() > 0) array.getJSONObject(0) else null
            } else null
        } catch (e: Exception) { null } finally { connection.disconnect() }
    }

    suspend fun getUserId(accessToken: String): String? = withContext(Dispatchers.IO) {
        val url = URL("$AUTH_URL/user")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(response).optString("id")
            } else null
        } catch (e: Exception) { null } finally { connection.disconnect() }
    }

    suspend fun updateProfile(
        accessToken: String,
        firstName: String,
        lastName: String,
        title: String,
        examYear: String,
        major: String,
        displayName: String = "",
        obp: Float = 100f
    ): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken)
        if (userId == null) return@withContext Pair(false, "Kullanıcı ID'si alınamadı (Token geçersiz olabilir)")

        val success = tryUpdateProfile(accessToken, userId, firstName, lastName, title, examYear, major, displayName, obp)
        if (success.first) {
            success
        } else {
            val errorText = success.second ?: ""
            if (errorText.contains("obp", ignoreCase = true)) {
                tryUpdateProfile(accessToken, userId, firstName, lastName, title, examYear, major, displayName, null)
            } else {
                success
            }
        }
    }

    private suspend fun tryUpdateProfile(
        accessToken: String,
        userId: String,
        firstName: String,
        lastName: String,
        title: String,
        examYear: String,
        major: String,
        displayName: String,
        obp: Float?
    ): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/profiles")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("id", userId)
            put("first_name", firstName)
            put("last_name", lastName)
            put("title", title)
            put("exam_year", examYear)
            put("major", major)
            put("display_name", displayName)
            if (obp != null) {
                put("obp", obp.toDouble())
            }
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                Pair(true, null)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Pair(false, "HTTP $responseCode: $errorText")
            }
        } catch (e: Exception) {
            Pair(false, "İstisna: ${e.message}")
        } finally { connection.disconnect() }
    }

    // --- Konu İlerleme Senkronizasyonu ---

    suspend fun syncTopicProgress(
        accessToken: String,
        topicTitle: String,
        subjectName: String,
        category: String,
        isCompleted: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false

        // Önce Supabase topics tablosundan topic_id'yi bul
        val topicId = getSupabaseTopicId(topicTitle, subjectName, category) ?: return@withContext false

        val url = URL("$BASE_URL/user_topic_progress")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("user_id", userId)
            put("topic_id", topicId)
            put("is_completed", isCompleted)
            if (isCompleted) {
                put("completed_at", java.time.OffsetDateTime.now().toString())
            } else {
                put("completed_at", JSONObject.NULL)
            }
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            connection.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncTopicProgress error", e)
            false
        } finally { connection.disconnect() }
    }

    private suspend fun getSupabaseTopicId(title: String, subjectName: String, category: String): Int? = withContext(Dispatchers.IO) {
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val encodedSubject = java.net.URLEncoder.encode(subjectName, "UTF-8").replace("+", "%20")
        val encodedCategory = java.net.URLEncoder.encode(category, "UTF-8").replace("+", "%20")
        val url = URL("$BASE_URL/topics?title=eq.$encodedTitle&subject_name=eq.$encodedSubject&category=eq.$encodedCategory&select=id&limit=1")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                if (array.length() > 0) array.getJSONObject(0).getInt("id") else null
            } else null
        } catch (e: Exception) { null } finally { connection.disconnect() }
    }

    suspend fun fetchAllProgress(accessToken: String): List<Triple<String, String, String>> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Triple<String, String, String>>()
        val url = URL("$BASE_URL/user_topic_progress?is_completed=eq.true&select=topic_id,topics(title,subject_name,category)")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val topic = obj.optJSONObject("topics") ?: continue
                    result.add(Triple(
                        topic.optString("title"),
                        topic.optString("subject_name"),
                        topic.optString("category")
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllProgress error", e)
        } finally { connection.disconnect() }
        result
    }

    private suspend fun getSupabaseTopicIdWithoutCategory(title: String, subjectName: String): Int? = withContext(Dispatchers.IO) {
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val encodedSubject = java.net.URLEncoder.encode(subjectName, "UTF-8").replace("+", "%20")
        val url = URL("$BASE_URL/topics?title=eq.$encodedTitle&subject_name=eq.$encodedSubject&select=id&limit=1")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                if (array.length() > 0) array.getJSONObject(0).getInt("id") else null
            } else null
        } catch (e: Exception) { null } finally { connection.disconnect() }
    }

    suspend fun syncStudyTime(
        accessToken: String,
        topicTitle: String,
        subjectName: String,
        durationSeconds: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false
        val topicId = getSupabaseTopicIdWithoutCategory(topicTitle, subjectName) ?: return@withContext false

        val url = URL("$BASE_URL/study_times")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("user_id", userId)
            put("topic_id", topicId)
            put("duration_seconds", durationSeconds)
        }

        try {
            java.io.OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            connection.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncStudyTime error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun fetchAllStudyTimes(accessToken: String): List<Triple<String, String, Long>> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Triple<String, String, Long>>()
        val url = URL("$BASE_URL/study_times?select=duration_seconds,topics(title,subject_name)")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val duration = obj.optLong("duration_seconds", 0L)
                    val topic = obj.optJSONObject("topics") ?: continue
                    result.add(Triple(
                        topic.optString("title"),
                        topic.optString("subject_name"),
                        duration
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllStudyTimes error", e)
        } finally { connection.disconnect() }
        result
    }

    private suspend fun getDersId(dersAdi: String, dersAlani: String, accessToken: String): Int? = withContext(Dispatchers.IO) {
        val encodedName = java.net.URLEncoder.encode(dersAdi, "UTF-8").replace("+", "%20")
        val encodedAlani = java.net.URLEncoder.encode(dersAlani, "UTF-8").replace("+", "%20")
        val url = URL("$BASE_URL/dersler?ders_adi=eq.$encodedName&ders_alani=eq.$encodedAlani&select=id&limit=1")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                if (array.length() > 0) array.getJSONObject(0).getInt("id") else {
                    android.util.Log.e("SupabaseRepo", "getDersId not found for $dersAdi $dersAlani. Response: $response")
                    null
                }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("SupabaseRepo", "getDersId HTTP error ${connection.responseCode}: $error")
                null
            }
        } catch (e: Exception) { 
            android.util.Log.e("SupabaseRepo", "getDersId exception", e)
            null 
        } finally { connection.disconnect() }
    }

    suspend fun syncNetResult(
        accessToken: String,
        netResult: com.omerfaruk.ykstakip.data.local.NetResultEntity
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false
        val denemeId = java.util.UUID.randomUUID().toString()

        val detailMap = mutableMapOf<String, Pair<Int, Int>>()
        if (netResult.type == "BRANS") {
            if (netResult.details.contains(":")) {
                val parts = netResult.details.split(":")
                val counts = parts[1].split("/")
                val dogru = counts.getOrNull(0)?.toIntOrNull() ?: 0
                val yanlis = counts.getOrNull(1)?.toIntOrNull() ?: 0
                detailMap[parts[0]] = dogru to yanlis
            }
        } else {
            netResult.details.split(" ").forEach {
                if (it.contains(":")) {
                    val kv = it.split(":")
                    val counts = kv[1].split("/")
                    val dogru = counts.getOrNull(0)?.toIntOrNull() ?: 0
                    val yanlis = counts.getOrNull(1)?.toIntOrNull() ?: 0
                    detailMap[kv[0]] = dogru to yanlis
                }
            }
        }

        val jsonArray = JSONArray()
        for ((key, counts) in detailMap) {
            val isTYT = netResult.type == "TYT" || key.startsWith("TYT")
            val isAYT = netResult.type == "AYT" || key.startsWith("AYT")
            val dersAlani = if (isTYT) "TYT" else if (isAYT) "AYT" else "TYT"
            
            val dersAdi = when (key) {
                "T" -> "Türkçe"
                "S" -> "Sosyal"
                "M" -> "Matematik"
                "F" -> if (isTYT) "Fen" else "Fizik"
                "K" -> "Kimya"
                "B" -> "Biyoloji"
                "E" -> "Edebiyat"
                "T1" -> "Tarih-1"
                "C1" -> "Coğrafya-1"
                "T2" -> "Tarih-2"
                "C2" -> "Coğrafya-2"
                "Fel" -> "Felsefe Grb."
                "D" -> "Din"
                else -> key.replace("TYT ", "").replace("AYT ", "").replace("Felsefe Grubu", "Felsefe Grb.").replace("Din Kültürü", "Din")
            }

            val dersId = getDersId(dersAdi, dersAlani, accessToken)
            if (dersId != null) {
                val obj = JSONObject().apply {
                    put("user_id", userId)
                    put("deneme_id", denemeId)
                    put("ders_id", dersId)
                    put("deneme_turu", netResult.type)
                    put("dogru_sayisi", counts.first)
                    put("yanlis_sayisi", counts.second)
                    val isoDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date(netResult.date))
                    put("olusturulma_tarihi", isoDate)
                }
                jsonArray.put(obj)
            }
        }

        if (jsonArray.length() == 0) return@withContext false

        val url = URL("$BASE_URL/net_takibi")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        try {
            java.io.OutputStreamWriter(connection.outputStream).use { it.write(jsonArray.toString()) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("SupabaseRepo", "syncNetResult HTTP Error: $code, $errorMsg")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncNetResult error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun fetchAllNetResults(accessToken: String): List<com.omerfaruk.ykstakip.data.local.NetResultEntity> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<com.omerfaruk.ykstakip.data.local.NetResultEntity>()
        val url = URL("$BASE_URL/net_takibi?select=deneme_id,deneme_turu,dogru_sayisi,yanlis_sayisi,olusturulma_tarihi,dersler(ders_adi,ders_alani)")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                
                val denemeGroups = mutableMapOf<String, MutableList<JSONObject>>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val denemeId = obj.optString("deneme_id")
                    if (denemeId.isNotEmpty()) {
                        denemeGroups.getOrPut(denemeId) { mutableListOf() }.add(obj)
                    }
                }
                
                val mapNameToKey = fun(dersAdi: String, type: String): String {
                    if (type == "BRANS") return dersAdi
                    return when (dersAdi) {
                        "Türkçe" -> "T"
                        "Sosyal" -> "S"
                        "Matematik" -> "M"
                        "Fen" -> "F"
                        "Fizik" -> "F"
                        "Kimya" -> "K"
                        "Biyoloji" -> "B"
                        "Edebiyat" -> "E"
                        "Tarih-1" -> "T1"
                        "Coğrafya-1" -> "C1"
                        "Tarih-2" -> "T2"
                        "Coğrafya-2" -> "C2"
                        "Felsefe Grb." -> "Fel"
                        "Din" -> "D"
                        else -> dersAdi
                    }
                }

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

                for ((_, rows) in denemeGroups) {
                    if (rows.isEmpty()) continue
                    val firstRow = rows.first()
                    val type = firstRow.optString("deneme_turu", "TYT")
                    val dateStr = firstRow.optString("olusturulma_tarihi")
                    val date = try { sdf.parse(dateStr)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
                    
                    var totalNet = 0f
                    val detailsBuilder = StringBuilder()
                    
                    if (type == "BRANS") {
                        val row = rows.first()
                        val dersAdi = row.optJSONObject("dersler")?.optString("ders_adi") ?: ""
                        val dersAlani = row.optJSONObject("dersler")?.optString("ders_alani", "TYT") ?: "TYT"
                        val dogru = row.optInt("dogru_sayisi", 0)
                        val yanlis = row.optInt("yanlis_sayisi", 0)
                        totalNet += dogru - (yanlis / 4f)
                        
                        val prefix = if (dersAdi.startsWith("TYT") || dersAdi.startsWith("AYT")) "" else "$dersAlani "
                        detailsBuilder.append("$prefix$dersAdi:$dogru/$yanlis")
                    } else {
                        val partsList = mutableListOf<String>()
                        for (row in rows) {
                            val dersAdi = row.optJSONObject("dersler")?.optString("ders_adi") ?: continue
                            val dogru = row.optInt("dogru_sayisi", 0)
                            val yanlis = row.optInt("yanlis_sayisi", 0)
                            totalNet += dogru - (yanlis / 4f)
                            
                            val key = mapNameToKey(dersAdi, type)
                            partsList.add("$key:$dogru/$yanlis")
                        }
                        detailsBuilder.append(partsList.joinToString(" "))
                    }
                    
                    resultList.add(com.omerfaruk.ykstakip.data.local.NetResultEntity(
                        date = date,
                        type = type,
                        totalNet = totalNet,
                        details = detailsBuilder.toString()
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllNetResults error", e)
        } finally {
            connection.disconnect()
        }
        resultList
    }

    fun getLocalYigilmaData(): List<SupabaseYigilma> {
        val list = mutableListOf<SupabaseYigilma>()
        fun add(puanTuru: String, yil: Int, data: List<Pair<Float, Int>>) {
            for (p in data) {
                list.add(SupabaseYigilma(puanTuru, p.first, yil, p.second))
            }
        }

        // ==================== 2023 SINAV PUANLARI (HAM) ====================
        add("TYT", 2023, listOf(
            500f to 1, 480f to 857, 460f to 6995, 440f to 21750, 420f to 43784,
            400f to 73371, 380f to 111109, 360f to 159562, 340f to 224069,
            320f to 314087, 300f to 438380, 280f to 609429, 260f to 833742,
            240f to 1111243, 220f to 1433162, 200f to 1795427, 180f to 2189426,
            160f to 2599464, 140f to 2858305, 120f to 2894255, 100f to 2895128
        ))
        add("SAY", 2023, listOf(
            500f to 2, 480f to 1821, 460f to 9110, 440f to 20929, 420f to 35986,
            400f to 53760, 380f to 73124, 360f to 94745, 340f to 119950,
            320f to 149647, 300f to 186170, 280f to 232352, 260f to 292779,
            240f to 373863, 220f to 485081, 200f to 645976, 180f to 890468,
            160f to 1187792, 140f to 1428564, 120f to 1499064, 100f to 1500310
        ))
        add("SOZ", 2023, listOf(
            500f to 1, 480f to 14, 460f to 76, 440f to 249, 420f to 685,
            400f to 1919, 380f to 5437, 360f to 14635, 340f to 33339,
            320f to 66265, 300f to 119295, 280f to 199146, 260f to 313220,
            240f to 470220, 220f to 669811, 200f to 903190, 180f to 1142139,
            160f to 1351813, 140f to 1500795, 120f to 1545532, 100f to 1546776
        ))
        add("EA", 2023, listOf(
            500f to 1, 480f to 90, 460f to 491, 440f to 1348, 420f to 2839,
            400f to 5486, 380f to 10537, 360f to 24893, 340f to 54240,
            320f to 97472, 300f to 156174, 280f to 239138, 260f to 354704,
            240f to 511828, 220f to 716883, 200f to 972399, 180f to 1270341,
            160f to 1575669, 140f to 1805707, 120f to 1858633, 100f to 1859614
        ))

        // ==================== 2023 YERLEŞTİRME PUANLARI ====================
        add("Y-TYT", 2023, listOf(
            550f to 64, 530f to 2574, 510f to 11351, 490f to 27393, 470f to 50565,
            450f to 80143, 430f to 117105, 410f to 164636, 390f to 226946,
            370f to 311136, 350f to 424626, 330f to 575456, 310f to 768691,
            290f to 1008139, 270f to 1290708, 250f to 1614577, 230f to 1978202,
            210f to 2377980, 190f to 2743018, 170f to 2883521, 150f to 2894979,
            130f to 2895128, 115f to 2895128
        ))
        add("Y-SAY", 2023, listOf(
            550f to 231, 530f to 4106, 510f to 12729, 490f to 25220, 470f to 40564,
            450f to 58212, 430f to 77195, 410f to 98718, 390f to 123578,
            370f to 152827, 350f to 188759, 330f to 234032, 310f to 292003,
            290f to 368410, 270f to 469700, 250f to 610788, 230f to 810947,
            210f to 1063434, 190f to 1312454, 170f to 1475827, 150f to 1499955,
            130f to 1500309, 115f to 1500310
        ))
        add("Y-SOZ", 2023, listOf(
            550f to 1, 530f to 25, 510f to 100, 490f to 304, 470f to 775,
            450f to 2063, 430f to 5488, 410f to 13975, 390f to 31218,
            370f to 61459, 350f to 110009, 330f to 182059, 310f to 284236,
            290f to 423732, 270f to 601767, 250f to 812802, 230f to 1038510,
            210f to 1253532, 190f to 1430913, 170f to 1531079, 150f to 1546493,
            130f to 1546776, 115f to 1546776
        ))
        add("Y-EA", 2023, listOf(
            550f to 18, 530f to 186, 510f to 702, 490f to 1641, 470f to 3370,
            450f to 6403, 430f to 13013, 410f to 31346, 390f to 62001,
            370f to 104670, 350f to 161313, 330f to 239808, 310f to 346263,
            290f to 487493, 270f to 666303, 250f to 885705, 230f to 1144891,
            210f to 1429561, 190f to 1696776, 170f to 1842354, 150f to 1859353,
            130f to 1859614, 115f to 1859614
        ))

        // ==================== 2024 SINAV PUANLARI (HAM) ====================
        add("TYT", 2024, listOf(
            500f to 1, 480f to 986, 460f to 8234, 440f to 23564, 420f to 45685,
            400f to 74365, 380f to 110048, 360f to 156162, 340f to 216563,
            320f to 300825, 300f to 417417, 280f to 585243, 260f to 821185,
            240f to 1118205, 220f to 1461314, 200f to 1825318, 180f to 2202319,
            160f to 2550017, 140f to 2736068, 120f to 2754938, 100f to 2755277
        ))
        add("SAY", 2024, listOf(
            500f to 1, 480f to 1069, 460f to 4750, 440f to 11529, 420f to 21345,
            400f to 34156, 380f to 49575, 360f to 67370, 340f to 88385,
            320f to 112870, 300f to 142684, 280f to 180424, 260f to 230040,
            240f to 297460, 220f to 394384, 200f to 549385, 180f to 814732,
            160f to 1105934, 140f to 1281163, 120f to 1306785, 100f to 1307007
        ))
        add("SOZ", 2024, listOf(
            500f to 1, 480f to 20, 460f to 119, 440f to 400, 420f to 1083,
            400f to 3088, 380f to 8722, 360f to 21721, 340f to 46571,
            320f to 86742, 300f to 147193, 280f to 234001, 260f to 353014,
            240f to 511134, 220f to 711504, 200f to 937848, 180f to 1152096,
            160f to 1315055, 140f to 1404667, 120f to 1423444, 100f to 1423849
        ))
        add("EA", 2024, listOf(
            500f to 1, 480f to 36, 460f to 211, 440f to 703, 420f to 1630,
            400f to 3269, 380f to 8084, 360f to 19423, 340f to 39621,
            320f to 71063, 300f to 116952, 280f to 184104, 260f to 285679,
            240f to 432666, 220f to 635607, 200f to 903984, 180f to 1218069,
            160f to 1508650, 140f to 1679335, 120f to 1703533, 100f to 1703833
        ))

        // ==================== 2024 YERLEŞTİRME PUANLARI ====================
        add("Y-TYT", 2024, listOf(
            550f to 59, 530f to 3017, 510f to 12996, 490f to 29976, 470f to 53253,
            450f to 82281, 430f to 118095, 410f to 163769, 390f to 223427,
            370f to 304035, 350f to 412255, 330f to 560622, 310f to 761711,
            290f to 1017130, 270f to 1318668, 250f to 1652661, 230f to 2011925,
            210f to 2377153, 190f to 2662249, 170f to 2749663, 150f to 2755201,
            130f to 2755276, 115f to 2755277
        ))
        add("Y-SAY", 2024, listOf(
            550f to 162, 530f to 2271, 510f to 7029, 490f to 14673, 470f to 25274,
            450f to 38578, 430f to 54307, 410f to 72418, 390f to 93485,
            370f to 118001, 350f to 148110, 330f to 185681, 310f to 234882,
            290f to 300531, 270f to 392302, 250f to 531055, 230f to 746123,
            210f to 1000554, 190f to 1208531, 170f to 1300183, 150f to 1306920,
            130f to 1307007, 115f to 1307007
        ))
        add("Y-SOZ", 2024, listOf(
            550f to 3, 530f to 31, 510f to 142, 490f to 476, 470f to 1292,
            450f to 3424, 430f to 8952, 410f to 21706, 390f to 45376,
            370f to 83547, 350f to 139921, 330f to 219810, 310f to 328161,
            290f to 471676, 270f to 652313, 250f to 860119, 230f to 1068917,
            210f to 1247074, 190f to 1368579, 170f to 1418232, 150f to 1423773,
            130f to 1423849, 115f to 1423849
        ))
        add("Y-EA", 2024, listOf(
            550f to 5, 530f to 80, 510f to 340, 490f to 940, 470f to 1992,
            450f to 4269, 430f to 11111, 410f to 24612, 390f to 46809,
            370f to 79522, 350f to 126223, 330f to 191807, 310f to 287762,
            290f to 422338, 270f to 601868, 250f to 831379, 230f to 1104426,
            210f to 1385617, 190f to 1610447, 170f to 1697303, 150f to 1703735,
            130f to 1703833, 115f to 1703833
        ))

        // ==================== 2025 SINAV PUANLARI (HAM) ====================
        add("TYT", 2025, listOf(
            500f to 1, 480f to 180, 460f to 2050, 440f to 8163, 420f to 21061,
            400f to 44193, 380f to 79260, 360f to 127655, 340f to 193064,
            320f to 282276, 300f to 404024, 280f to 570335, 260f to 794784,
            240f to 1073527, 220f to 1379866, 200f to 1686626, 180f to 1977665,
            160f to 2210463, 140f to 2303695, 120f to 2310493, 100f to 2310579
        ))
        add("SAY", 2025, listOf(
            500f to 1, 480f to 701, 460f to 4715, 440f to 12449, 420f to 24779,
            400f to 40857, 380f to 60085, 360f to 81946, 340f to 106251,
            320f to 134493, 300f to 169418, 280f to 213365, 260f to 270804,
            240f to 348345, 220f to 458302, 200f to 627659, 180f to 892884,
            160f to 1149472, 140f to 1277493, 120f to 1291435, 100f to 1291531
        ))
        add("SOZ", 2025, listOf(
            500f to 1, 480f to 4, 460f to 21, 440f to 76, 420f to 227,
            400f to 652, 380f to 1782, 360f to 4912, 340f to 12653,
            320f to 29315, 300f to 60680, 280f to 115851, 260f to 205996,
            240f to 338388, 220f to 515827, 200f to 723293, 180f to 920945,
            160f to 1070609, 140f to 1155714, 120f to 1173742, 100f to 1174047
        ))
        add("EA", 2025, listOf(
            500f to 1, 480f to 32, 460f to 175, 440f to 560, 420f to 1325,
            400f to 2823, 380f to 6028, 360f to 15691, 340f to 35436,
            320f to 68083, 300f to 115961, 280f to 185253, 260f to 285967,
            240f to 431085, 220f to 629436, 200f to 875112, 180f to 1134243,
            160f to 1350772, 140f to 1474465, 120f to 1494355, 100f to 1494612
        ))

        // ==================== 2025 YERLEŞTİRME PUANLARI ====================
        add("Y-TYT", 2025, listOf(
            550f to 14, 530f to 601, 510f to 3648, 490f to 11733, 470f to 27141,
            450f to 52500, 430f to 88915, 410f to 137133, 390f to 201126,
            370f to 286158, 350f to 397823, 330f to 545280, 310f to 737144,
            290f to 976188, 270f to 1251236, 250f to 1542701, 230f to 1835041,
            210f to 2099883, 190f to 2270224, 170f to 2308495, 150f to 2310553,
            130f to 2310579, 115f to 2310579
        ))
        add("Y-SAY", 2025, listOf(
            550f to 57, 530f to 1930, 510f to 7081, 490f to 16140, 470f to 29410,
            450f to 46142, 430f to 65449, 410f to 87117, 390f to 111498,
            370f to 139619, 350f to 174355, 330f to 217778, 310f to 274252,
            290f to 348397, 270f to 449880, 250f to 596635, 230f to 811201,
            210f to 1052783, 190f to 1228141, 170f to 1287932, 150f to 1291491,
            130f to 1291531, 115f to 1291531
        ))
        add("Y-SOZ", 2025, listOf(
            550f to 1, 530f to 7, 510f to 26, 490f to 77, 470f to 254,
            450f to 721, 430f to 1926, 410f to 5036, 390f to 12522,
            370f to 28323, 350f to 57848, 330f to 108894, 310f to 190203,
            290f to 309551, 270f to 468930, 250f to 658973, 230f to 849410,
            210f to 1010174, 190f to 1121933, 170f to 1168866, 150f to 1173955,
            130f to 1174046, 115f to 1174047
        ))
        add("Y-EA", 2025, listOf(
            550f to 4, 530f to 58, 510f to 261, 490f to 742, 470f to 1629,
            450f to 3422, 430f to 8145, 410f to 20244, 390f to 42590,
            370f to 76959, 350f to 125389, 330f to 192949, 310f to 287630,
            290f to 418975, 270f to 592803, 250f to 806796, 230f to 1042314,
            210f to 1262545, 190f to 1425544, 170f to 1489209, 150f to 1494521,
            130f to 1494611, 115f to 1494612
        ))

        return list
    }

    suspend fun getYigilmaVerileri(accessToken: String? = null): List<SupabaseYigilma> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SupabaseYigilma>()
        val url = URL("$BASE_URL/yks_yigilma_verileri?select=*&limit=5000")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        val token = accessToken ?: API_KEY
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        SupabaseYigilma(
                            puanTuru = obj.optString("puan_turu"),
                            puan = obj.optDouble("puan", 0.0).toFloat(),
                            yil = obj.optInt("yil", 0),
                            siralama = obj.optInt("siralama", 0)
                        )
                    )
                }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("SupabaseRepo", "getYigilmaVerileri HTTP error ${connection.responseCode}: $error")
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "getYigilmaVerileri error", e)
        } finally { connection.disconnect() }
        
        if (result.isEmpty()) {
            android.util.Log.d("SupabaseRepo", "getYigilmaVerileri is empty, using local fallback data.")
            getLocalYigilmaData()
        } else {
            result
        }
    }

    suspend fun syncCalculation(
        accessToken: String,
        obp: Float,
        inputsJson: String,
        resultsJson: String
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false

        val url = URL("$BASE_URL/yks_hesaplama_gecmisi")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("user_id", userId)
            put("obp", obp)
            put("inputs_json", inputsJson)
            put("results_json", resultsJson)
        }

        try {
            java.io.OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            connection.responseCode in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncCalculation error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun fetchAllCalculations(accessToken: String): List<com.omerfaruk.ykstakip.data.local.CalculationHistoryEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<com.omerfaruk.ykstakip.data.local.CalculationHistoryEntity>()
        val url = URL("$BASE_URL/yks_hesaplama_gecmisi?select=*&order=created_at.desc")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val dateStr = obj.optString("created_at")
                    val date = try {
                        java.time.OffsetDateTime.parse(dateStr).toInstant().toEpochMilli()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    result.add(
                        com.omerfaruk.ykstakip.data.local.CalculationHistoryEntity(
                            id = obj.optInt("id", 0),
                            date = date,
                            obp = obj.optDouble("obp", 0.0).toFloat(),
                            inputsJson = obj.optString("inputs_json", ""),
                            resultsJson = obj.optString("results_json", "")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllCalculations error", e)
        } finally { connection.disconnect() }
        result
    }

    suspend fun syncSnippetInteraction(
        accessToken: String,
        snippetId: String,
        topicId: String,
        subjectName: String,
        isLiked: Boolean,
        isKnown: Boolean,
        isSaved: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false

        val url = URL("$BASE_URL/user_snippet_interactions?on_conflict=user_id,snippet_id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("user_id", userId)
            put("snippet_id", snippetId)
            put("topic_id", topicId)
            put("subject_name", subjectName)
            put("is_liked", isLiked)
            put("is_known", isKnown)
            put("is_saved", isSaved)
            put("updated_at", java.time.OffsetDateTime.now().toString())
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                android.util.Log.e("SupabaseRepo", "syncSnippetInteraction HTTP $code error body: $errorMsg")
            } else {
                android.util.Log.d("SupabaseRepo", "syncSnippetInteraction code: $code")
            }
            code in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncSnippetInteraction error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun fetchAllSnippetInteractions(accessToken: String): List<com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity>()
        val url = URL("$BASE_URL/user_snippet_interactions?select=*")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val updatedAtStr = obj.optString("updated_at")
                    val updatedAt = try {
                        java.time.OffsetDateTime.parse(updatedAtStr).toInstant().toEpochMilli()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    result.add(
                        com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity(
                            snippetId = obj.optString("snippet_id"),
                            topicId = obj.optString("topic_id"),
                            subjectName = obj.optString("subject_name"),
                            isLiked = obj.optBoolean("is_liked", false),
                            isKnown = obj.optBoolean("is_known", false),
                            isSaved = obj.optBoolean("is_saved", false),
                            updatedAt = updatedAt
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllSnippetInteractions error", e)
        } finally { connection.disconnect() }
        result
    }

    suspend fun reportSnippet(
        accessToken: String?,
        snippetId: String,
        reason: String,
        explanation: String
    ): Boolean = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/snippet_reports")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
        } else {
            connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        }
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val userId = accessToken?.let { getUserId(it) }

        val body = JSONObject().apply {
            if (userId != null) {
                put("user_id", userId)
            }
            put("snippet_id", snippetId)
            put("report_reason", reason)
            put("explanation", explanation)
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            val code = connection.responseCode
            android.util.Log.d("SupabaseRepo", "reportSnippet code: $code")
            code in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "reportSnippet error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun syncFollowedSubject(
        accessToken: String,
        subjectName: String,
        isFollowed: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false

        val url = URL("$BASE_URL/user_followed_subjects?on_conflict=user_id,subject_name")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("user_id", userId)
            put("subject_name", subjectName)
            put("is_followed", isFollowed)
            put("updated_at", java.time.OffsetDateTime.now().toString())
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                android.util.Log.e("SupabaseRepo", "syncFollowedSubject HTTP $code error body: $errorMsg")
            } else {
                android.util.Log.d("SupabaseRepo", "syncFollowedSubject code: $code")
            }
            code in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncFollowedSubject error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun fetchAllFollowedSubjects(accessToken: String): List<com.omerfaruk.ykstakip.data.local.FollowedSubjectEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<com.omerfaruk.ykstakip.data.local.FollowedSubjectEntity>()
        val url = URL("$BASE_URL/user_followed_subjects?select=*")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = org.json.JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        com.omerfaruk.ykstakip.data.local.FollowedSubjectEntity(
                            subjectName = obj.getString("subject_name"),
                            isFollowed = obj.getBoolean("is_followed"),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllFollowedSubjects error", e)
        } finally { connection.disconnect() }
        result
    }

    suspend fun getSubjectFollowerCount(accessToken: String?, subjectName: String): Int = withContext(Dispatchers.IO) {
        val encodedSubject = java.net.URLEncoder.encode(subjectName, "UTF-8")
        val url = URL("$BASE_URL/user_followed_subjects?select=id&subject_name=eq.$encodedSubject&is_followed=eq.true")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
        } else {
            connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        }
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = org.json.JSONArray(response)
                array.length()
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                android.util.Log.e("SupabaseRepo", "getSubjectFollowerCount HTTP error: $errorMsg")
                0
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "getSubjectFollowerCount error", e)
            0
        } finally { connection.disconnect() }
    }

    suspend fun syncQuestionLog(
        accessToken: String,
        subjectName: String,
        topicTitle: String,
        correctCount: Int,
        wrongCount: Int,
        dateMillis: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val userId = getUserId(accessToken) ?: return@withContext false

        val url = URL("$BASE_URL/user_question_logs")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val instant = java.time.Instant.ofEpochMilli(dateMillis)
        val isoDate = java.time.OffsetDateTime.ofInstant(instant, java.time.ZoneOffset.UTC).toString()

        val body = JSONObject().apply {
            put("user_id", userId)
            put("log_date", isoDate)
            put("subject_name", subjectName)
            put("topic_title", topicTitle)
            put("correct_count", correctCount)
            put("wrong_count", wrongCount)
            put("updated_at", java.time.OffsetDateTime.now().toString())
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                android.util.Log.e("SupabaseRepo", "syncQuestionLog HTTP $code error body: $errorMsg")
            } else {
                android.util.Log.d("SupabaseRepo", "syncQuestionLog code: $code")
            }
            code in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "syncQuestionLog error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun deleteQuestionLog(
        accessToken: String,
        dateMillis: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val instant = java.time.Instant.ofEpochMilli(dateMillis)
        val isoDate = java.time.OffsetDateTime.ofInstant(instant, java.time.ZoneOffset.UTC).toString()
        val url = URL("$BASE_URL/user_question_logs?log_date=eq.$isoDate")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        try {
            val code = connection.responseCode
            code in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "deleteQuestionLog error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun updateQuestionLog(
        accessToken: String,
        dateMillis: Long,
        correctCount: Int,
        wrongCount: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val instant = java.time.Instant.ofEpochMilli(dateMillis)
        val isoDate = java.time.OffsetDateTime.ofInstant(instant, java.time.ZoneOffset.UTC).toString()
        val url = URL("$BASE_URL/user_question_logs?log_date=eq.$isoDate")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PATCH"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("correct_count", correctCount)
            put("wrong_count", wrongCount)
            put("updated_at", java.time.OffsetDateTime.now().toString())
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }
            val code = connection.responseCode
            code in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "updateQuestionLog error", e)
            false
        } finally { connection.disconnect() }
    }

    suspend fun fetchAllQuestionLogs(accessToken: String): List<com.omerfaruk.ykstakip.data.local.QuestionLogEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<com.omerfaruk.ykstakip.data.local.QuestionLogEntity>()
        val url = URL("$BASE_URL/user_question_logs?select=*")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", API_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val array = org.json.JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    
                    val dateStr = obj.optString("log_date")
                    val dateMillis = try {
                        java.time.OffsetDateTime.parse(dateStr).toInstant().toEpochMilli()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    result.add(
                        com.omerfaruk.ykstakip.data.local.QuestionLogEntity(
                            date = dateMillis,
                            subjectName = obj.getString("subject_name"),
                            topicTitle = obj.getString("topic_title"),
                            correctCount = obj.getInt("correct_count"),
                            wrongCount = obj.getInt("wrong_count"),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "fetchAllQuestionLogs error", e)
        } finally { connection.disconnect() }
        result
    }

}
