package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.CategoryDto
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.ui.theme.Primary
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun BudgetSetupScreen(role: String, onBack: () -> Unit, onFinished: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    val calendar = remember { Calendar.getInstance() }
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    val budgetItems = remember { mutableStateListOf<BudgetData>() }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getCategories()
            .onSuccess { categories ->
                budgetItems.clear()
                categories
                    .filter { it.name.lowercase(Locale.US) != "income" }
                    .take(8)
                    .forEach { category ->
                        budgetItems.add(
                            BudgetData(
                                categoryId = category.id,
                                title = category.name,
                                icon = budgetIconFor(category.name),
                                iconBgColor = budgetColorFromHex(category.color),
                                amount = defaultBudgetFor(category.name),
                            )
                        )
                    }
            }
            .onFailure { errorMessage = it.message ?: "Could not load categories." }
        isLoading = false
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back", color = Color(0xFF6B7280), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Set Your Budget",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Allocate monthly budget for each category",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                itemsIndexed(budgetItems) { index, item ->
                    BudgetCategoryCard(
                        data = item,
                        onAmountChange = { newAmount ->
                            budgetItems[index] = item.copy(amount = newAmount)
                        }
                    )
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    errorMessage = null
                    val validItems = budgetItems.mapNotNull { item ->
                        val parsed = item.amount.toDoubleOrNull()
                        if (parsed != null && parsed >= 0.0) item to parsed else null
                    }

                    if (validItems.isEmpty()) {
                        errorMessage = "Enter at least one budget amount."
                        return@Button
                    }

                    scope.launch {
                        isSaving = true
                        var failure: Throwable? = null
                        for ((item, parsedAmount) in validItems) {
                            val result = repository.setBudget(
                                categoryId = item.categoryId,
                                amount = parsedAmount,
                                month = month,
                                year = year,
                            )
                            result.onFailure {
                                failure = it
                                return@onFailure
                            }
                            if (failure != null) break
                        }
                        isSaving = false
                        if (failure == null) {
                            onFinished()
                        } else {
                            errorMessage = failure?.message ?: "Could not save budgets."
                        }
                    }
                },
                enabled = !isSaving && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Complete Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BudgetCategoryCard(data: BudgetData, onAmountChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(data.iconBgColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = data.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = data.amount,
                onValueChange = onAmountChange,
                placeholder = { Text("0", color = Color(0xFF9CA3AF)) },
                prefix = { Text("$ ", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                )
            )
        }
    }
}

data class BudgetData(
    val categoryId: String,
    val title: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val amount: String
)

private fun budgetColorFromHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        Color(0xFF64748B)
    }
}

private fun defaultBudgetFor(categoryName: String): String {
    return when (categoryName.lowercase(Locale.US)) {
        "food", "groceries" -> "300"
        "transport", "transportation" -> "150"
        "shopping" -> "200"
        else -> ""
    }
}

private fun budgetIconFor(categoryName: String): ImageVector {
    return when (categoryName.lowercase(Locale.US)) {
        "food", "groceries" -> Icons.Default.RestaurantMenu
        "transport", "transportation" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "utilities", "bills" -> Icons.Default.FlashOn
        "health", "healthcare" -> Icons.Default.MedicalServices
        "education" -> Icons.Default.School
        "entertainment" -> Icons.Default.Movie
        else -> Icons.Default.Category
    }
}
