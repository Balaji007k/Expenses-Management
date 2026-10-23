package com.enterprise.expensetracker.di

import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.core.database.MongoDbManagerImpl
import com.enterprise.expensetracker.core.database.dao.ExpenseMongoDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindMongoDbManager(
        impl: MongoDbManagerImpl
    ): MongoDbManager

    companion object {
        @Provides
        @Singleton
        fun provideExpenseMongoDao(
            manager: MongoDbManager
        ): ExpenseMongoDao {
            return manager.expenseDao
        }
    }
}
