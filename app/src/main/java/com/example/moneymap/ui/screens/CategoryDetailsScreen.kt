package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.repository.MoneyMapRepository
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(
    categoryName: String,
    onBack: () -> Unit,
    onTransactionClick: (HistoryTransaction) -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var isLoading by remember { mutableStateOf(true) }
    var spentAmount by remember { mutableStateOf(0.0) }
    var budgetAmount by remember { mutableStateOf(0.0) }
    var progress by remember { mutableStateOf(0.0f) }
    var filteredTransactions by remember { mutableStateOf<List<HistoryTransaction>>(emptyList()) }
    var categoryColor by remember { mutableStateOf(Color(0xFF6366F1)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categoryName) {
        isLoading = true
        repository.getCategories()
            .onSuccess { categories ->
                val matching = categories.find { it.name.equals(categoryName, ignoreCase = true) }
                val categoryId = matching?.id
                val hexColor = matching?.color ?: "#6366F1"
                categoryColor = try {
                    Color(android.graphics.Color.parseColor(hexColor))
                } catch (_: IllegalArgumentException) {
                    Color(0xFF6366F1)
                }

                repository.getDashboardStats()
                    .onSuccess { stats ->
                        val budget = stats.budgets.find { it.categoryName.equals(categoryName, ignoreCase = true) }
                        budgetAmount = budget?.limit ?: 0.0
                    }

                repository.getRecentTransactions(50, categoryId = categoryId)
                    .onSuccess { transactions ->
                        filteredTransactions = transactions.map { t ->
                            val parsedColor = try {
                                Color(android.graphics.Color.parseColor(t.category?.color ?: "#64748B"))
                            } catch (_: IllegalArgumentException) {
                                Color(0xFF64748B)
                            }
                            HistoryTransaction(
                                id = t.id,
                                title = t.description?.takeIf { it.isNotBlank() } ?: (t.category?.name ?: t.type),
                                category = t.category?.name ?: t.type,
                                amount = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(t.amount),
                                date = t.transactionDate.take(10),
                                iconBgColor = parsedColor,
                                isExpense = t.type == "EXPENSE",
                                rawAmount = t.amount
                            )
                        }
                        spentAmount = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        progress = if (budgetAmount > 0.0) {
                            (spentAmount / budgetAmount).toFloat().coerceIn(0.0f, 1.0f)
                        } else {
                            0.0f
                        }
                    }
                    .onFailure {
                        errorMessage = it.message ?: "Failed to load category transactions."
                    }
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load categories."
            }
        isLoading = false
    }

    val spentText = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(spentAmount)
    val budgetText = if (budgetAmount > 0.0) {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(budgetAmount)
    } else {
        "No budget set"
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("$categoryName Details", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Total Spent this Month",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = spentText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Budget: $budgetText", fontSize = 13.sp, color = Color(0xFF64748B))
                            if (budgetAmount > 0.0) {
                                Text("${(progress * 100).toInt()}% Used", fontSize = 13.sp, color = categoryColor, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (budgetAmount > 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = progress)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(categoryColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No transactions logged in this category.", color = Color(0xFF64748B), fontSize = 15.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredTransactions) { transaction ->
                            HistoryTransactionItem(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction) }
                            )
                        }
                    }
                }
            }
        }
    }
}
