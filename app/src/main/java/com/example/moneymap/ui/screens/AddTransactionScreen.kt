package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.CategoryDto
import com.example.moneymap.data.model.BudgetSummaryDto
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(onBack: () -> Unit, onSave: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var tags by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryDto?>(null) }
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var budgets by remember { mutableStateOf<List<BudgetSummaryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var transactionType by remember { mutableStateOf("EXPENSE") }

    var showWarningDialog by remember { mutableStateOf(false) }
    var warningCategoryName by remember { mutableStateOf("") }
    var warningAmount by remember { mutableStateOf(0.0) }
    var warningBudgetRemaining by remember { mutableStateOf(0.0) }

    val filteredCategories = remember(categories, transactionType) {
        if (transactionType == "INCOME") {
            categories.filter { it.name.lowercase(Locale.US) == "income" }
        } else {
            categories.filter { it.name.lowercase(Locale.US) != "income" }
        }
    }

    LaunchedEffect(filteredCategories) {
        selectedCategory = filteredCategories.firstOrNull()
    }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getCategories()
            .onSuccess { loaded ->
                categories = loaded
            }
            .onFailure { errorMessage = it.message ?: "Could not load categories." }
        repository.getDashboardStats()
            .onSuccess { stats ->
                budgets = stats.budgets
            }
        isLoading = false
    }

    val saveTransaction = {
        val parsedAmount = amount.toDoubleOrNull()
        val category = selectedCategory
        if (parsedAmount != null && category != null) {
            scope.launch {
                isSaving = true
                val cleanTags = tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                val result = repository.createTransaction(
                    categoryId = category.id,
                    amount = parsedAmount,
                    type = transactionType,
                    description = description,
                    transactionDate = normalizeDateForApi(date),
                    tags = cleanTags,
                )
                isSaving = false
                result
                    .onSuccess { onSave() }
                    .onFailure { errorMessage = it.message ?: "Could not save transaction." }
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text(if (transactionType == "EXPENSE") "Add Expense" else "Add Income", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        errorMessage = null
                        val parsedAmount = amount.toDoubleOrNull()
                        val category = selectedCategory
                        when {
                            parsedAmount == null || parsedAmount <= 0.0 -> {
                                errorMessage = "Enter a valid amount."
                            }
                            category == null -> {
                                errorMessage = "Choose a category."
                            }
                            date.isBlank() -> {
                                errorMessage = "Enter a date as yyyy-MM-dd."
                            }
                            else -> {
                                val budget = budgets.find { it.categoryName.equals(category.name, ignoreCase = true) }
                                if (transactionType == "EXPENSE" && budget != null && budget.limit > 0.0 && (budget.spent + parsedAmount) > budget.limit) {
                                    warningCategoryName = category.name
                                    warningAmount = parsedAmount
                                    warningBudgetRemaining = budget.limit - budget.spent
                                    showWarningDialog = true
                                } else {
                                    saveTransaction()
                                }
                            }
                        }
                    },
                    enabled = !isSaving && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(if (transactionType == "EXPENSE") "Add Expense" else "Add Income", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            Spacer(modifier = Modifier.height(24.dp))

            // Expense / Income Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                val expenseBgColor = if (transactionType == "EXPENSE") Color.White else Color.Transparent
                val expenseTextColor = if (transactionType == "EXPENSE") Color(0xFF0F172A) else Color(0xFF64748B)
                val incomeBgColor = if (transactionType == "INCOME") Color.White else Color.Transparent
                val incomeTextColor = if (transactionType == "INCOME") Color(0xFF0F172A) else Color(0xFF64748B)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(expenseBgColor)
                        .clickable { transactionType = "EXPENSE" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Expense", fontWeight = FontWeight.Bold, color = expenseTextColor, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(incomeBgColor)
                        .clickable { transactionType = "INCOME" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Income", fontWeight = FontWeight.Bold, color = incomeTextColor, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Amount", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = { Text("0.00", color = Color(0xFF94A3B8)) },
                leadingIcon = { Text("₹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(start = 16.dp, end = 8.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(if (transactionType == "EXPENSE") "Category" else "Income Source", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { category ->
                        val categoryColor = try {
                            Color(android.graphics.Color.parseColor(category.color))
                        } catch (_: Exception) {
                            Color(0xFF2563EB)
                        }
                        val isSelected = selectedCategory?.id == category.id
                        val borderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                        val backgroundColor = if (isSelected) categoryColor.copy(alpha = 0.1f) else Color.White
                        
                        Box(
                            modifier = Modifier
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(backgroundColor)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(categoryColor, CircleShape)
                                )
                                Text(
                                    text = category.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Date", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                placeholder = { Text("yyyy-MM-dd", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Description", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("What did you buy?", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF94A3B8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Tags (comma-separated)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                placeholder = { Text("e.g. food, lunch", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
            
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showWarningDialog) {
            AlertDialog(
                onDismissRequest = { showWarningDialog = false },
                title = { Text("Budget Warning!", fontWeight = FontWeight.Bold, color = Color(0xFF92400E)) },
                text = {
                    Column {
                        Text("Adding this expense will exceed your monthly budget for $warningCategoryName.", color = Color(0xFF4B5563))
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Current Expense", color = Color(0xFF92400E))
                                    Text("₹$warningAmount", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Budget Remaining", color = Color(0xFF92400E))
                                    Text("₹$warningBudgetRemaining", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showWarningDialog = false
                            saveTransaction()
                        }
                    ) {
                        Text("Add anyway", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWarningDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }
    }
}

private fun normalizeDateForApi(input: String): String {
    val trimmed = input.trim()
    return if (trimmed.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
        val parts = trimmed.split("-")
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else {
        trimmed
    }
}
