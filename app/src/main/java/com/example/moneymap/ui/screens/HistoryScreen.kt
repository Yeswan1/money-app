package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import com.example.moneymap.data.model.TransactionDto
import com.example.moneymap.data.repository.MoneyMapRepository
import java.text.NumberFormat
import java.util.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onTransactionClick: (HistoryTransaction) -> Unit,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onSubscriptionsClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<HistoryTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val totalExpenses = transactions
        .filter { it.isExpense }
        .sumOf { it.rawAmount }
    val totalIncome = transactions
        .filter { !it.isExpense }
        .sumOf { it.rawAmount }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    if (transactions.isEmpty()) {
                        isLoading = true
                    }
                    repository.getRecentTransactions(50)
                        .onSuccess { loaded ->
                            transactions = loaded.map { it.toHistoryTransaction() }
                        }
                        .onFailure { errorMessage = it.message ?: "Could not load transactions." }
                    isLoading = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("All Transactions", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onChatClick,
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Chat",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Search and Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .clickable { onSearchClick() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search", color = Color(0xFF94A3B8))
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2563EB))
                        .clickable { onFilterClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                }
            }

            // Summary Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Expenses", color = Color(0xFF64748B), fontSize = 14.sp)
                    Text("-${formatHistoryMoney(totalExpenses)}", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Income", color = Color(0xFF64748B), fontSize = 14.sp)
                    Text("+${formatHistoryMoney(totalIncome)}", color = Color(0xFF10B981), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Feature Quick Links
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCategoriesClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Categories", fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = onSubscriptionsClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Subscriptions", fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Transactions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                errorMessage?.let { message ->
                    item {
                        Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }

                if (!isLoading && transactions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No transactions yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Add an expense and it will show here.", color = Color(0xFF64748B), fontSize = 14.sp)
                        }
                    }
                }

                items(transactions) { transaction ->
                    HistoryTransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                }
            }
        }
    }
}

@Composable
fun HistoryTransactionItem(
    transaction: HistoryTransaction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(transaction.iconBgColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${transaction.category} • ${transaction.date}",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }
        
        Text(
            text = if (transaction.isExpense) transaction.amount else "+${transaction.amount}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isExpense) Color(0xFF0F172A) else Color(0xFF10B981)
        )
    }
}

data class HistoryTransaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val date: String,
    val iconBgColor: Color,
    val isExpense: Boolean,
    val rawAmount: Double = amount.replace("$", "").replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
)

private fun TransactionDto.toHistoryTransaction(): HistoryTransaction {
    val categoryName = category?.name ?: type
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(category?.color ?: "#64748B"))
    } catch (_: IllegalArgumentException) {
        Color(0xFF64748B)
    }

    return HistoryTransaction(
        id = id,
        title = description?.takeIf { it.isNotBlank() } ?: categoryName,
        category = categoryName,
        amount = formatHistoryMoney(amount),
        date = transactionDate.take(10),
        iconBgColor = parsedColor,
        isExpense = type == "EXPENSE",
        rawAmount = amount,
    )
}

private fun formatHistoryMoney(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
}

val allTransactions = listOf(
    HistoryTransaction("1", "No database item", "Food", "₹0.00", "Open app", Color(0xFFFF7A00), true)
)
