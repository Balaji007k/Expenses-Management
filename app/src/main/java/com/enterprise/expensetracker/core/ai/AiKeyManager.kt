package com.enterprise.expensetracker.core.ai

import android.content.Context
import com.enterprise.expensetracker.core.ai.gemini.GeminiAiEngine
import com.enterprise.expensetracker.core.ai.openai.OpenAiEngine
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user-configured API keys for real AI providers (Google Gemini & OpenAI).
 * Automatically initializes engines and provides connection testing.
 */
@Singleton
class AiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiAiEngine: GeminiAiEngine,
    private val openAiEngine: OpenAiEngine
) {
    private val prefs = context.getSharedPreferences("ai_user_config_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        val geminiKey = getGeminiKey()
        if (geminiKey.isNotBlank()) {
            geminiAiEngine.configureApiKey(geminiKey)
        }
        val openAiKey = getOpenAiKey()
        if (openAiKey.isNotBlank()) {
            openAiEngine.configureApiKey(openAiKey)
        }
    }

    fun getGeminiKey(): String = prefs.getString("gemini_api_key", "") ?: ""

    fun saveGeminiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("gemini_api_key", trimmed).apply()
        geminiAiEngine.configureApiKey(trimmed)
    }

    fun getOpenAiKey(): String = prefs.getString("openai_api_key", "") ?: ""

    fun saveOpenAiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("openai_api_key", trimmed).apply()
        openAiEngine.configureApiKey(trimmed)
    }

    suspend fun testConnection(provider: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (provider.uppercase()) {
                "GEMINI" -> {
                    val key = getGeminiKey()
                    if (key.isBlank()) {
                        return@withContext Result.failure(IllegalArgumentException("Please enter a Gemini API Key first."))
                    }
                    val model = GenerativeModel(
                        modelName = "gemini-1.5-flash",
                        apiKey = key
                    )
                    val response = model.generateContent("Give a 1-sentence tip on family monthly budget in India.")
                    val text = response.text?.trim() ?: "Connection verified successfully!"
                    Result.success("Gemini 1.5 Flash Connected: \"$text\"")
                }
                "OPENAI" -> {
                    val key = getOpenAiKey()
                    if (key.isBlank()) {
                        return@withContext Result.failure(IllegalArgumentException("Please enter an OpenAI API Key first."))
                    }
                    val jsonBody = JSONObject().apply {
                        put("model", "gpt-4o-mini")
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "Give a 1-sentence tip on family monthly budget in India.")
                            })
                        })
                        put("max_tokens", 60)
                    }
                    val request = Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer $key")
                        .addHeader("Content-Type", "application/json")
                        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("OpenAI error (HTTP ${response.code}): $responseBody"))
                    }
                    val resJson = JSONObject(responseBody)
                    val choices = resJson.optJSONArray("choices")
                    val message = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content", "Connected successfully!")?.trim()
                    Result.success("OpenAI GPT-4o-mini Connected: \"$message\"")
                }
                else -> {
                    Result.success("Local Mock Engine: Ready (Simulated responses)")
                }
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
