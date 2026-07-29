package com.example.moneymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moneymap.data.session.AuthSession
import com.example.moneymap.ui.screens.*
import com.example.moneymap.ui.theme.MoneymapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneymapTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(onAnimationFinished = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("onboarding") {
            OnboardingScreen(onFinished = {
                navController.navigate("login") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoogleLoginSuccess = {
                    navController.navigate("role_selection") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable("signup") {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate("role_selection") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("role_selection") {
            RoleSelectionScreen(onRoleSelected = { role ->
                navController.navigate("setup/$role")
            })
        }
        composable("setup/student") {
            StudentSetupScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("budget_setup/student") }
            )
        }
        composable("setup/professional") {
            EmployeeSetupScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("budget_setup/professional") }
            )
        }
        composable("setup/homemaker") {
            HomemakerSetupScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("budget_setup/homemaker") }
            )
        }
        composable("setup/personal") {
            GeneralSetupScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("budget_setup/personal") }
            )
        }
        composable("budget_setup/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "personal"
            BudgetSetupScreen(
                role = role,
                onBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate("profile_completion")
                }
            )
        }
        composable("profile_completion") {
            ProfileCompletionScreen(
                onContinue = {
                    navController.navigate("notification_permission")
                }
            )
        }
        composable("notification_permission") {
            NotificationPermissionScreen(
                onNext = {
                    navController.navigate("main") {
                        popUpTo("role_selection") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainContent(
                onAddTransaction = { navController.navigate("add_transaction") },
                onNavigateToHistory = { navController.navigate("history") },
                onChatClick = { navController.navigate("chatbot") },
                onTransactionClick = { transactionId -> navController.navigate("expense_details/$transactionId") },
                onSearchClick = { navController.navigate("search") },
                onFilterClick = { navController.navigate("filters") },
                onCategoriesClick = { navController.navigate("categories") },
                onSubscriptionsClick = { navController.navigate("subscriptions") },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onTransactionClick = { transaction -> navController.navigate("expense_details/${transaction.id}") },
                onSearchClick = { navController.navigate("search") },
                onFilterClick = { navController.navigate("filters") },
                onCategoriesClick = { navController.navigate("categories") },
                onSubscriptionsClick = { navController.navigate("subscriptions") }
            )
        }
        composable("add_transaction") {
            AddTransactionScreen(
                onBack = { navController.popBackStack() },
                onSave = {
                    navController.navigate("transaction_success") {
                        popUpTo("add_transaction") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("chatbot") {
            ChatbotScreen(onBack = { navController.popBackStack() })
        }
        composable("transaction_success") {
            TransactionSuccessScreen(
                onDone = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAddAnother = {
                    navController.navigate("add_transaction") {
                        popUpTo("transaction_success") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("expense_details/{transactionId}") { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            ExpenseDetailsScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("edit_expense/$transactionId") },
                onDeleteSuccess = { navController.popBackStack() }
            )
        }
        composable("edit_expense/{transactionId}") { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            EditExpenseScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStack() },
                onSave = {
                    navController.navigate("transaction_success") {
                        popUpTo("edit_expense/{transactionId}") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("search") {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onTransactionClick = { transaction -> navController.navigate("expense_details/${transaction.id}") }
            )
        }
        composable("filters") {
            FilterScreen(onBack = { navController.popBackStack() })
        }
        composable("categories") {
            CategoryListScreen(
                onBack = { navController.popBackStack() },
                onCategoryClick = { categoryName ->
                    navController.navigate("category_details/$categoryName")
                }
            )
        }
        composable("category_details/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Category"
            CategoryDetailsScreen(
                categoryName = categoryName,
                onBack = { navController.popBackStack() },
                onTransactionClick = { transaction -> navController.navigate("expense_details/${transaction.id}") }
            )
        }
        composable("subscriptions") {
            SubscriptionTrackerScreen(onBack = { navController.popBackStack() })
        }
        composable("monthly_budget") {
            MonthlyBudgetScreen(onBack = { navController.popBackStack() })
        }
        composable("weekly_budget") {
            WeeklyBudgetScreen(onBack = { navController.popBackStack() })
        }
        composable("savings_goals") {
            SavingsGoalsScreen(
                onBack = { navController.popBackStack() },
                onAddGoalClick = { navController.navigate("add_goal") },
                onGoalClick = { goalName -> navController.navigate("goal_progress/$goalName") }
            )
        }
        composable("add_goal") {
            AddGoalScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable("goal_progress/{goalName}") { backStackEntry ->
            val goalName = backStackEntry.arguments?.getString("goalName") ?: "Goal"
            GoalProgressScreen(
                goalName = goalName,
                onBack = { navController.popBackStack() }
            )
        }
        composable("budget_alerts") {
            BudgetAlertsScreen(onBack = { navController.popBackStack() })
        }
        composable("forgot_password") { ForgotPasswordScreen(onBack = { navController.popBackStack() }) }
        composable("overspending_warning") { 
            OverspendingWarningScreen(
                onContinue = {
                    navController.navigate("transaction_success") {
                        popUpTo("overspending_warning") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = { navController.popBackStack() }
            ) 
        }
        composable("weekly_report_details") { WeeklyReportScreen(onBack = { navController.popBackStack() }) }
        composable("monthly_report_details") { MonthlyReportScreen(onBack = { navController.popBackStack() }) }
        composable("expense_charts") { ExpenseChartsScreen(onBack = { navController.popBackStack() }) }
        composable("spending_trends") { SpendingTrendsScreen(onBack = { navController.popBackStack() }) }
        composable("category_analytics") { CategoryAnalyticsScreen(onBack = { navController.popBackStack() }) }
    }
}

@Composable
fun MainContent(
    onAddTransaction: () -> Unit, 
    onNavigateToHistory: () -> Unit, 
    onChatClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val session = remember { AuthSession(context.applicationContext) }
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomScreen.Dashboard,
        BottomScreen.Reports,
        BottomScreen.Wallet,
        BottomScreen.Budget,
        BottomScreen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(90.dp)
            ) {
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    if (screen is BottomScreen.Wallet) {
                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-8).dp)
                                        .size(60.dp)
                                        .background(Color(0xFF3B82F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = screen.filledIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            },
                            label = null,
                            selected = isSelected,
                            onClick = onAddTransaction,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF3B82F6) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF9CA3AF),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            bottomNavController,
            startDestination = BottomScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomScreen.Dashboard.route) {
                DashboardScreen(
                    onAddTransaction = onAddTransaction,
                    onSeeAllTransactions = onNavigateToHistory,
                    onNotificationClick = { onNavigate("budget_alerts") },
                    onChatClick = onChatClick,
                    onBudgetClick = {
                        bottomNavController.navigate(BottomScreen.Budget.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onGroceryListClick = { onNavigate("categories") },
                    onFamilyAllowancesClick = { onNavigate("savings_goals") }
                )
            }
            composable(BottomScreen.Reports.route) {
                ReportsScreen()
            }
            composable(BottomScreen.Wallet.route) {
                HistoryScreen(
                    onBack = { }, 
                    onTransactionClick = { transaction -> onTransactionClick(transaction.id) },
                    onSearchClick = onSearchClick,
                    onFilterClick = onFilterClick,
                    onCategoriesClick = onCategoriesClick,
                    onSubscriptionsClick = onSubscriptionsClick
                ) 
            }
            composable(BottomScreen.Budget.route) {
                BudgetScreen(
                    onChatClick = onChatClick,
                    onWeeklyPlanClick = { onNavigate("weekly_budget") },
                    onMonthlyPlanClick = { onNavigate("monthly_budget") },
                    onViewAllGoalsClick = { onNavigate("savings_goals") },
                    onCreateGoalClick = { onNavigate("add_goal") },
                    onGoalClick = { goalName -> onNavigate("goal_progress/$goalName") },
                    onAlertsClick = { onNavigate("budget_alerts") }
                )
            }
            composable(BottomScreen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        session.clear()
                        onNavigate("login")
                    },
                    onPaymentMethodsClick = { onNavigate("subscriptions") }
                )
            }
        }
    }
}

sealed class BottomScreen(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    data object Dashboard : BottomScreen("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Reports : BottomScreen("reports", "Reports", Icons.Filled.PieChart, Icons.Outlined.PieChart)
    data object Wallet : BottomScreen("wallet", "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
    data object Budget : BottomScreen("budget", "Budget", Icons.Filled.Wallet, Icons.Outlined.Wallet)
    data object Profile : BottomScreen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}
