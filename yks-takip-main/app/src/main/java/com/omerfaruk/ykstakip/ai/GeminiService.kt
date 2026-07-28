package com.omerfaruk.ykstakip.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.omerfaruk.ykstakip.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    // GenerativeModel'i lazy başlatıyoruz çünkü BuildConfig.GEMINI_API_KEY 
    // uygulama ilk ayağa kalktığında hemen hazır olmayabilir veya boş olabilir.
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            systemInstruction = content {
                text("Sen bir YKS (Yükseköğretim Kurumları Sınavı) koçusun. " +
                     "Görevin kullanıcıyı motive etmek, kısa ve enerjik mesajlar yazmaktır. " +
                     "Mesajların samimi ve destekleyici olmalı, maksimum 2-3 cümle sürmelidir. " +
                     "Kullanıcı TYT ve AYT sınavlarına hazırlanıyor.")
            }
        )
    }

    suspend fun generateMotivationMessage(
        userTitle: String,
        overallProgress: Float,
        subjectProgresses: Map<String, Float>
    ): String = withContext(Dispatchers.IO) {
        // BuildConfig kontrolü
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "null") {
            Log.e("GeminiService", "GEMINI_API_KEY is blank or null in BuildConfig")
            return@withContext "API anahtarı yüklenemedi. Lütfen projeyi Rebuild yapın."
        }

        val progressText = subjectProgresses.entries.joinToString { "${it.key}: %${(it.value * 100).toInt()}" }
        
        val prompt = """
            Kullanıcıya '${userTitle}' şeklinde hitap ederek başla. 
            Genel ilerleme durumu: %${(overallProgress * 100).toInt()}.
            Şu anki ders bazlı detaylar: $progressText.
            
            Önemli: 
            - Eğer bir derste %50'yi geçtiyse o dersin yarısını devirdiğini belirterek tebrik et.
            - Kullanıcıyı her zaman hedefine odakla.
        """.trimIndent()

        try {
            Log.d("GeminiService", "AI isteği gönderiliyor. Hitap: $userTitle, İlerleme: %${(overallProgress * 100).toInt()}")
            val response = generativeModel.generateContent(prompt)
            val result = response.text
            
            if (!result.isNullOrBlank()) {
                Log.d("GeminiService", "AI Yanıtı Başarılı: $result")
                result
            } else {
                Log.w("GeminiService", "AI yanıtı boş geldi.")
                "Harika gidiyorsun $userTitle, çalışmaya devam!"
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Gemini Hatası: ${e.message}", e)
            "Sınav yolculuğunda yanındayım $userTitle, başarılar dilerim!"
        }
    }
}
