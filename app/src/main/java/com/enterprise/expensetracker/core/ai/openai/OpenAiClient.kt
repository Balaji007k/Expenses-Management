package com.enterprise.expensetracker.core.ai.openai

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAiMessage>,
    val temperature: Double = 0.2
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage
)

@Serializable
data class OpenAiChatResponse(
    val id: String? = null,
    val choices: List<OpenAiChoice> = emptyList()
)

/**
 * Interface contract for OpenAI HTTP client.
 * Can be backed by Retrofit or OkHttp.
 */
interface OpenAiClient {
    suspend fun createChatCompletion(apiKey: String, request: OpenAiChatRequest): OpenAiChatResponse
}
