package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.moneymap.data.repository.MoneyMapRepository
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onTransactionClick: (HistoryTransaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var searchResults by remember { mutableStateOf<List<HistoryTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isLoading = true
            repository.getRecentTransactions(50, search = searchQuery)
                .onSuccess { transactions ->
                    searchResults = transactions.map { t ->
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
                    errorMessage = null
                }
                .onFailure {
                    errorMessage = it.message ?: "Failed to execute search."
                }
            isLoading = false
        } else {
            searchResults = emptyList()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = { Text("Search transactions...", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
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

                if (searchQuery.isNotBlank()) {
                    Text(
                        text = "Found ${searchResults.size} results",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No transactions found matching your search.", color = Color(0xFF64748B), fontSize = 15.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { transaction ->
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
