package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getCategories()
            .onSuccess { loaded ->
                categories = loaded.filter { it.name.lowercase(Locale.US) != "income" }
                selectedCategory = categories.firstOrNull()
            }
            .onFailure { errorMessage = it.message ?: "Could not load categories." }
        isLoading = false
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                                scope.launch {
                                    isSaving = true
                                    val cleanTags = tags.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                    val result = repository.createTransaction(
                                        categoryId = category.id,
                                        amount = parsedAmount,
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
                        Text("Add Expense", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            Text("Amount", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold) },
                placeholder = { Text("0.00", color = Color(0xFFCBD5E1)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Text("Category", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    categories.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowItems.forEach { category ->
                                AddCategoryItem(
                                    name = category.name,
                                    color = colorFromHex(category.color),
                                    isSelected = selectedCategory?.id == category.id,
                                ) {
                                    selectedCategory = category
                                }
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.width(100.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Field
            Text("Date", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8)) },
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF0F172A)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description Field
            Text("Description", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Add a note (optional)", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF94A3B8)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tags Field
            Text("Tags", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add tags (optional)", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFF94A3B8)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                )
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

private fun colorFromHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        Color(0xFF64748B)
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

@Composable
fun AddCategoryItem(name: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
    
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color, RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
        }
    }
}
