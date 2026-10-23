package com.enterprise.expensetracker.di

import com.enterprise.expensetracker.core.ai.AiEngineFactory
import com.enterprise.expensetracker.core.ai.gemini.GeminiAiEngine
import com.enterprise.expensetracker.core.ai.mock.MockAiEngine
import com.enterprise.expensetracker.core.ai.openai.OpenAiEngine
import com.enterprise.expensetracker.core.remoteconfig.FirebaseConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiEngineFactory(
        geminiAiEngine: GeminiAiEngine,
        openAiEngine: OpenAiEngine,
        mockAiEngine: MockAiEngine,
        firebaseConfigManager: FirebaseConfigManager
    ): AiEngineFactory {
        return AiEngineFactory(
            geminiAiEngine = geminiAiEngine,
            openAiEngine = openAiEngine,
            mockAiEngine = mockAiEngine,
            firebaseConfigManager = firebaseConfigManager
        )
    }
}
