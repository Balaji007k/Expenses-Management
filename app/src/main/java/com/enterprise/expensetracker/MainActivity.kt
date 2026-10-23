package com.enterprise.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.EnterpriseExpenseTheme
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark800
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.feature.auth.AuthScreen
import com.enterprise.expensetracker.feature.dashboard.DashboardScreen
import com.enterprise.expensetracker.feature.expenses.ExpensesScreen
import com.enterprise.expensetracker.feature.family.FamilyScreen
import com.enterprise.expensetracker.feature.insights.InsightsScreen
import com.enterprise.expensetracker.feature.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Expenses : NavigationItem("expenses", "Expenses", Icons.Default.ReceiptLong)
    data object Insights : NavigationItem("insights", "Insights", Icons.Default.AutoAwesome)
    data object Family : NavigationItem("family", "Family", Icons.Default.FamilyRestroom)
    data object Settings : NavigationItem("settings", "Settings", Icons.Default.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseExpenseTheme {
                val currentUser by getCurrentUserUseCase().collectAsStateWithLifecycle(initialValue = null)

                if (currentUser == null) {
                    AuthScreen(
                        viewModel = hiltViewModel(),
                        onAuthSuccess = {}
                    )
                } else {
                    MainAppScaffold(currentUser = currentUser)
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(currentUser: UserProfile?) {
    val navController = rememberNavController()
    val navItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Expenses,
        NavigationItem.Insights,
        NavigationItem.Family,
        NavigationItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateDark800,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBlue,
                            selectedTextColor = BrandBlue,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark,
                            indicatorColor = SlateDark700
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(NavigationItem.Dashboard.route) {
                DashboardScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToExpenses = { navController.navigate(NavigationItem.Expenses.route) },
                    onNavigateToInsights = { navController.navigate(NavigationItem.Insights.route) }
                )
            }
            composable(NavigationItem.Expenses.route) {
                ExpensesScreen(viewModel = hiltViewModel())
            }
            composable(NavigationItem.Insights.route) {
                InsightsScreen(viewModel = hiltViewModel())
            }
            composable(NavigationItem.Family.route) {
                FamilyScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToAuth = {}
                )
            }
            composable(NavigationItem.Settings.route) {
                SettingsScreen(viewModel = hiltViewModel())
            }
        }
    }
}
