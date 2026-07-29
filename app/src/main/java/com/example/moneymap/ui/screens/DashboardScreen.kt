package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.DashboardStatsResponse
import com.example.moneymap.data.model.DashboardTransactionDto
import com.example.moneymap.data.model.UserProfileResponse
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.ui.theme.*
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    role: String = "personal",
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onNotificationClick: () -> Unit,
    onChatClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onGroceryListClick: () -> Unit = onSeeAllTransactions,
    onFamilyAllowancesClick: () -> Unit = onBudgetClick,
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var stats by remember { mutableStateOf<DashboardStatsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    if (profile == null) {
                        isLoading = true
                    }
                    repository.getProfile()
                        .onSuccess { profile = it }
                        .onFailure { errorMessage = it.message ?: "Could not load profile." }
                    repository.getDashboardStats()
                        .onSuccess { stats = it }
                        .onFailure { errorMessage = it.message ?: "Could not load dashboard." }
                    isLoading = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    RealDashboardContent(
        profile = profile,
        stats = stats,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onAddTransaction = onAddTransaction,
        onSeeAllTransactions = onSeeAllTransactions,
        onChatClick = onChatClick,
        onBudgetClick = onBudgetClick,
        onGroceryListClick = onGroceryListClick,
        onFamilyAllowancesClick = onFamilyAllowancesClick,
    )
}

@Composable
private fun RealDashboardContent(
    profile: UserProfileResponse?,
    stats: DashboardStatsResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onChatClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onGroceryListClick: () -> Unit,
    onFamilyAllowancesClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                val role = profile?.role?.uppercase(Locale.US) ?: "PERSONAL"
                val greeting = remember {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    when {
                        hour < 12 -> "Good Morning"
                        hour < 17 -> "Good Afternoon"
                        else -> "Good Evening"
                    }
                }
                val greetingName = remember(profile?.name, role) {
                    if (role == "STUDENT") {
                        (profile?.name ?: "MoneyMap") + " 👋"
                    } else {
                        profile?.name ?: "MoneyMap User"
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(text = greeting, fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = greetingName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                val totalBudget = stats?.budgets?.sumOf { it.limit } ?: 0.0
                val monthlySpent = stats?.monthlySpent ?: 0.0
                val monthlyIncome = stats?.monthlyIncome ?: 0.0
                val netSavings = stats?.netSavings ?: 0.0

                val balanceTitle: String
                val balanceAmount: String
                val rightTitle: String
                val rightAmount: String

                when (role) {
                    "STUDENT" -> {
                        val allowance = profile?.profile?.monthlyAllowance ?: 5000.0
                        balanceTitle = "Student Allowance"
                        balanceAmount = formatMoney(allowance + monthlyIncome)
                        rightTitle = "Saved"
                        rightAmount = formatMoney((allowance + monthlyIncome - monthlySpent).coerceAtLeast(0.0))
                    }
                    "PROFESSIONAL" -> {
                        val salary = profile?.profile?.monthlyIncome ?: 0.0
                        balanceTitle = "Available Balance"
                        balanceAmount = formatMoney((salary + monthlyIncome - monthlySpent).coerceAtLeast(0.0))
                        rightTitle = "Salary"
                        rightAmount = formatMoney(salary)
                    }
                    "HOMEMAKER" -> {
                        val homemakerBudget = profile?.profile?.monthlyBudget ?: 25000.0
                        balanceTitle = "Household Budget"
                        balanceAmount = formatMoney(homemakerBudget)
                        rightTitle = "Remaining"
                        rightAmount = formatMoney((homemakerBudget + monthlyIncome - monthlySpent).coerceAtLeast(0.0))
                    }
                    else -> {
                        val hasBudget = totalBudget > 0.0
                        val headlineAmount = if (hasBudget) totalBudget else monthlyIncome
                        val remaining = if (hasBudget) (totalBudget + monthlyIncome - monthlySpent) else netSavings
                        
                        balanceTitle = if (hasBudget) "Monthly Budget" else "This Month"
                        balanceAmount = formatMoney(headlineAmount)
                        rightTitle = if (hasBudget) "Remaining" else "Saved"
                        rightAmount = formatMoney(remaining.coerceAtLeast(0.0))
                    }
                }

                BalanceCard(
                    title = balanceTitle,
                    amount = balanceAmount,
                    sub1Title = "Spent",
                    sub1Amount = formatMoney(monthlySpent),
                    sub2Title = rightTitle,
                    sub2Amount = rightAmount
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    val role = profile?.role?.uppercase(Locale.US) ?: "PERSONAL"
                    val monthlySpent = stats?.monthlySpent ?: 0.0
                    val monthlyIncome = stats?.monthlyIncome ?: 0.0
                    val netSavings = stats?.netSavings ?: 0.0
                    val totalBudget = stats?.budgets?.sumOf { it.limit } ?: 0.0

                    if (role == "PROFESSIONAL") {
                        val salary = profile?.profile?.monthlyIncome ?: 0.0
                        val budgetLimitVal = if (totalBudget > 0.0) totalBudget else if (salary > 0.0) salary else 1.0
                        val pct = ((monthlySpent / budgetLimitVal) * 100.0).toInt().coerceIn(0, 100)
                        val savedAmount = if (monthlyIncome > 0.0) netSavings else (salary - monthlySpent)

                        Text(
                            text = "Professional Dashboard Stats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                title = "Saved This Month",
                                value = formatMoney(savedAmount.coerceAtLeast(0.0)),
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Budget Used",
                                value = "$pct%",
                                icon = Icons.Default.PieChart,
                                iconColor = Color(0xFF8B5CF6),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else if (role == "STUDENT") {
                        Text(
                            text = "Quick Actions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionCard(
                                title = "Add Expense",
                                icon = Icons.AutoMirrored.Filled.TrendingDown,
                                iconBg = Color(0xFFDBEAFE),
                                iconTint = Color(0xFF3B82F6),
                                onClick = onAddTransaction,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Budget Limits",
                                icon = Icons.Default.PieChart,
                                iconBg = Color(0xFFEDE9FE),
                                iconTint = Color(0xFF8B5CF6),
                                onClick = onBudgetClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = if (role == "HOMEMAKER") "Household Tools" else "Tools",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionCard(
                                title = "Add Expense",
                                icon = Icons.AutoMirrored.Filled.TrendingDown,
                                iconBg = Color(0xFFDBEAFE),
                                iconTint = Color(0xFF3B82F6),
                                onClick = onAddTransaction,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Grocery List",
                                icon = Icons.Default.ShoppingCart,
                                iconBg = Color(0xFFDCFCE7),
                                iconTint = Color(0xFF16A34A),
                                onClick = onGroceryListClick,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Family Allowances",
                                icon = Icons.Default.FamilyRestroom,
                                iconBg = Color(0xFFFCE7F3),
                                iconTint = Color(0xFFDB2777),
                                onClick = onFamilyAllowancesClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "View All ->",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.clickable { onSeeAllTransactions() }
                    )
                }
            }

            val transactions = stats?.recentTransactions.orEmpty()
            if (!isLoading && transactions.isEmpty()) {
                item {
                    EmptyDashboardState(onAddTransaction)
                }
            } else {
                items(transactions) { transaction ->
                    DashboardTransactionItem(transaction.toDashboardTransaction())
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(60.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                    shape = CircleShape
                )
                .clickable { onChatClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "AI Assistant",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun EmptyDashboardState(onAddTransaction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No transactions yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Add your first expense to make this dashboard come alive.", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddTransaction) {
            Text("Add Expense")
        }
    }
}

private fun DashboardTransactionDto.toDashboardTransaction(): DashboardTransaction {
    return DashboardTransaction(
        title = description?.takeIf { it.isNotBlank() } ?: category,
        category = category,
        date = transactionDate.toShortDisplayDate(),
        amount = NumberFormat.getNumberInstance(Locale("en", "IN")).format(amount),
        icon = iconForCategory(category),
        iconBg = colorFromBackend(color),
        isIncome = type == "INCOME",
    )
}

private fun formatMoney(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
}

private fun String.toShortDisplayDate(): String {
    return take(10)
}

private fun colorFromBackend(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        Color(0xFF64748B)
    }
}

private fun iconForCategory(category: String): ImageVector {
    return when (category.lowercase(Locale.US)) {
        "food", "groceries" -> Icons.Default.ShoppingCart
        "bills", "utilities" -> Icons.Default.Receipt
        "education" -> Icons.Default.School
        "transport", "transportation" -> Icons.Default.DirectionsCar
        "health", "healthcare" -> Icons.Default.MedicalServices
        "shopping" -> Icons.Default.ShoppingBag
        "income" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.AccountBalanceWallet
    }
}

@Composable
fun StudentHeader() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Good Morning", fontSize = 14.sp, color = Color.Gray)
        Text("Arjun 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@Composable
fun EmployeeHeader() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Good Afternoon", fontSize = 14.sp, color = Color.Gray)
        Text("Sarah", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@Composable
fun BalanceCard(
    title: String,
    amount: String,
    sub1Title: String,
    sub1Amount: String,
    sub2Title: String,
    sub2Amount: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(200.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = amount,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BalanceSubItem(
                        title = sub1Title,
                        amount = sub1Amount,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        modifier = Modifier.weight(1f)
                    )
                    BalanceSubItem(
                        title = sub2Title,
                        amount = sub2Amount,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceSubItem(title: String, amount: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
            Text(
                text = amount,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EmployeeStatsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Saved",
            value = "₹1,250",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconColor = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Budget Used",
            value = "65%",
            icon = Icons.Default.PieChart,
            iconColor = Color(0xFF8B5CF6),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionsSection(onAddExpense: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(
                title = "Add Expense",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                iconBg = Color(0xFFDBEAFE),
                iconTint = Color(0xFF3B82F6),
                onClick = onAddExpense,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Budget",
                icon = Icons.Default.AccountBalanceWallet,
                iconBg = Color(0xFFEDE9FE),
                iconTint = Color(0xFF8B5CF6),
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
fun DashboardTransactionItem(transaction: DashboardTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(Color(0xFFF9FAFB), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(transaction.iconBg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "${transaction.category} • ${transaction.date}",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }
        Text(
            text = (if (transaction.isIncome) "+" else "") + "₹" + transaction.amount,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isIncome) Color(0xFF10B981) else Color(0xFF111827)
        )
    }
}

data class DashboardTransaction(
    val title: String,
    val category: String,
    val date: String,
    val amount: String,
    val icon: ImageVector,
    val iconBg: Color,
    val isIncome: Boolean = false
)

val studentTransactions = listOf(
    DashboardTransaction("Lunch at Campus", "Food", "Today", "12.50", Icons.Default.AccountBalanceWallet, Color(0xFFF97316)),
    DashboardTransaction("Bus Pass", "Transport", "Yesterday", "45.00", Icons.Default.AccountBalanceWallet, Color(0xFF3B82F6)),
    DashboardTransaction("Books", "Education", "May 7", "78.00", Icons.Default.AccountBalanceWallet, Color(0xFF8B5CF6)),
    DashboardTransaction("Allowance", "Income", "May 1", "800.00", Icons.Default.AccountBalanceWallet, Color(0xFF10B981), true)
)

val employeeTransactions = listOf(
    DashboardTransaction("Coffee Shop", "Food", "Today", "6.50", Icons.Default.AccountBalanceWallet, Color(0xFFF97316)),
    DashboardTransaction("Netflix Subscription", "Entertainment", "Today", "15.99", Icons.Default.AccountBalanceWallet, Color(0xFFEF4444)),
    DashboardTransaction("Grocery Store", "Shopping", "Yesterday", "127.45", Icons.Default.AccountBalanceWallet, Color(0xFFEC4899)),
    DashboardTransaction("Salary Deposit", "Income", "May 1", "7,000.00", Icons.Default.AccountBalanceWallet, Color(0xFF10B981), true)
)
