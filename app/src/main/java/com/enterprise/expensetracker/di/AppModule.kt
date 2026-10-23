package com.enterprise.expensetracker.di

import com.enterprise.expensetracker.core.common.dispatchers.AppDispatchers
import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return AppDispatchers()
    }
}
