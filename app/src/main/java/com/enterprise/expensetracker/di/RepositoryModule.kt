package com.enterprise.expensetracker.di

import com.enterprise.expensetracker.data.repository.AiInsightsRepositoryImpl
import com.enterprise.expensetracker.data.repository.BudgetRepositoryImpl
import com.enterprise.expensetracker.data.repository.DynamicConfigRepositoryImpl
import com.enterprise.expensetracker.data.repository.ExpenseRepositoryImpl
import com.enterprise.expensetracker.data.repository.ItemCatalogRepositoryImpl
import com.enterprise.expensetracker.domain.repository.AiInsightsRepository
import com.enterprise.expensetracker.domain.repository.BudgetRepository
import com.enterprise.expensetracker.domain.repository.DynamicConfigRepository
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import com.enterprise.expensetracker.domain.repository.ItemCatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindItemCatalogRepository(
        impl: ItemCatalogRepositoryImpl
    ): ItemCatalogRepository

    @Binds
    @Singleton
    abstract fun bindDynamicConfigRepository(
        impl: DynamicConfigRepositoryImpl
    ): DynamicConfigRepository

    @Binds
    @Singleton
    abstract fun bindAiInsightsRepository(
        impl: AiInsightsRepositoryImpl
    ): AiInsightsRepository
}
