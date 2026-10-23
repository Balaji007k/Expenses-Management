package com.enterprise.expensetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Enterprise Application Entry Point.
 * Initializes Hilt Dependency Injection container, Firebase, and logging hooks.
 */
@HiltAndroidApp
class ExpenseTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // App-wide enterprise initialization hooks
    }
}
