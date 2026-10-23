package com.enterprise.expensetracker.core.ai

import com.enterprise.expensetracker.core.ai.gemini.GeminiAiEngine
import com.enterprise.expensetracker.core.ai.mock.MockAiEngine
import com.enterprise.expensetracker.core.ai.openai.OpenAiEngine
import com.enterprise.expensetracker.core.remoteconfig.FirebaseConfigManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Dynamic AI Factory.
 * Selects AI Engine (Gemini vs OpenAI vs Local Simulator) at runtime
 * driven by live Firebase Remote Configuration.
 */
@Singleton
class AiEngineFactory @Inject constructor(
    private val geminiAiEngine: GeminiAiEngine,
    private val openAiEngine: OpenAiEngine,
    private val mockAiEngine: MockAiEngine,
    private val firebaseConfigManager: FirebaseConfigManager
) {

    fun getActiveEngine(): AiEngine {
        val configuredProvider = firebaseConfigManager.dynamicConfig.value.activeAiProvider
        return when (AiProviderType.fromIdentifier(configuredProvider)) {
            AiProviderType.GEMINI -> geminiAiEngine
            AiProviderType.OPENAI -> openAiEngine
            AiProviderType.MOCK -> mockAiEngine
        }
    }

    fun observeActiveEngine(): Flow<AiEngine> {
        return firebaseConfigManager.dynamicConfig.map { config ->
            when (AiProviderType.fromIdentifier(config.activeAiProvider)) {
                AiProviderType.GEMINI -> geminiAiEngine
                AiProviderType.OPENAI -> openAiEngine
                AiProviderType.MOCK -> mockAiEngine
            }
        }
    }
}
