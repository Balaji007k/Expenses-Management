package com.enterprise.expensetracker.core.ai

enum class AiProviderType(val displayName: String, val identifier: String) {
    GEMINI("Google Gemini", "GEMINI"),
    OPENAI("OpenAI GPT", "OPENAI"),
    MOCK("Enterprise Offline Simulator", "MOCK");

    companion object {
        fun fromIdentifier(id: String): AiProviderType {
            return entries.firstOrNull { it.identifier.equals(id, ignoreCase = true) } ?: GEMINI
        }
    }
}
