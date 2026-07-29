 package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditExpenseScreen(
    transactionId: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") }
    var selectedCategory by remember { mutableStateOf<CategoryDto?>(null) }
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(transactionId) {
        isLoading = true
        repository.getCategories()
            .onSuccess { loadedCategories ->
                categories = loadedCategories
                if (transactionId.isNotBlank()) {
                    repository.getTransaction(transactionId)
                        .onSuccess { t ->
                            amount = String.format(java.util.Locale.US, "%.2f", t.amount)
                            description = t.description.orEmpty()
                            transactionType = t.type
                            date = if (t.transactionDate.length >= 10) {
                                val ymd = t.transactionDate.take(10) // YYYY-MM-DD
                                val parts = ymd.split("-")
                                if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else ymd
                            } else {
                                ""
                            }
                            tags = t.tags?.joinToString(", ").orEmpty()
                            selectedCategory = loadedCategories.find { it.id == t.category?.id }
                        }
                        .onFailure {
                            errorMessage = it.message ?: "Failed to load transaction details."
                        }
                }
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load categories."
            }
        isLoading = false
    }

    val filteredCategories = remember(categories, transactionType) {
        if (transactionType == "INCOME") {
            categories.filter { it.name.equals("Income", ignoreCase = true) }
        } else {
            categories.filter { !it.name.equals("Income", ignoreCase = true) }
        }
    }

    LaunchedEffect(transactionType, filteredCategories) {
        if (transactionType == "INCOME" && filteredCategories.isNotEmpty()) {
            selectedCategory = filteredCategories.first()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (transactionType == "INCOME") "Edit Income" else "Edit Expense", 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF0F172A)
                    ) 
                },
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
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0) {
                            errorMessage = "Please enter a valid amount."
                            return@Button
                        }
                        val categoryId = selectedCategory?.id
                        if (categoryId == null) {
                            errorMessage = "Please select a category."
                            return@Button
                        }
                        val apiDate = normalizeDateForApi(date)
                        if (apiDate.isBlank()) {
                            errorMessage = "Please enter a valid date (DD-MM-YYYY)."
                            return@Button
                        }
                        
                        val parsedTags = tags.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        scope.launch {
                            isSaving = true
                            repository.updateTransaction(
                                id = transactionId,
                                categoryId = categoryId,
                                amount = parsedAmount,
                                type = transactionType,
                                description = description,
                                transactionDate = apiDate,
                                tags = parsedTags
                            )
                                .onSuccess {
                                    isSaving = false
                                    onSave()
                                }
                                .onFailure {
                                    isSaving = false
                                    errorMessage = it.message ?: "Failed to save changes."
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9))
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

                Text("Amount", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
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

                Text(if (transactionType == "EXPENSE") "Category" else "Income Source", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(12.dp))
                
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

                Spacer(modifier = Modifier.height(24.dp))

                Text("Date", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    placeholder = { Text("DD-MM-YYYY", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8)) },
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

                Text("Description", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("e.g. Dinner with friends", color = Color(0xFF94A3B8)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF94A3B8)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Tags", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = { Text("e.g. food, lunch", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFF94A3B8)) },
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

                Spacer(modifier = Modifier.height(40.dp))
            }
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
