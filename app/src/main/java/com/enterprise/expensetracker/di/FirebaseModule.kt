package com.enterprise.expensetracker.di

import com.enterprise.expensetracker.core.remoteconfig.FirebaseConfigManager
import com.enterprise.expensetracker.core.remoteconfig.FirebaseConfigManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseConfigManager(
        impl: FirebaseConfigManagerImpl
    ): FirebaseConfigManager
}
