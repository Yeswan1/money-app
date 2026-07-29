package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<CategorySummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    val colors = listOf("#0D9488", "#8B5CF6", "#EC4899", "#3B82F6", "#F59E0B", "#EF4444")
    var selectedColor by remember { mutableStateOf(colors.first()) }
    var isCreating by remember { mutableStateOf(false) }

    fun loadCategories() {
        scope.launch {
            isLoading = true
            repository.getCategories()
                .onSuccess { loaded ->
                    categories = loaded.map {
                        CategorySummary(
                            name = it.name,
                            icon = iconForCategorySummary(it.name),
                            color = categoryColor(it.color),
                            totalAmount = if (it.isSystem) "System" else "Custom",
                            transactionCount = if (it.isSystem) 0 else 1
                        )
                    }
                }
                .onFailure { errorMessage = it.message ?: "Could not load categories." }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCategories()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("All Categories", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Category", tint = Color(0xFF2563EB))
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

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { category ->
                        CategorySummaryCard(
                            category = category,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCreating) showCreateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            isCreating = true
                            scope.launch {
                                repository.createCategory(newCategoryName.trim(), selectedColor)
                                    .onSuccess {
                                        showCreateDialog = false
                                        newCategoryName = ""
                                        loadCategories()
                                    }
                                    .onFailure {
                                        errorMessage = it.message ?: "Failed to create category."
                                    }
                                isCreating = false
                            }
                        }
                    },
                    enabled = !isCreating && newCategoryName.isNotBlank()
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Create")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }, enabled = !isCreating) {
                    Text("Cancel")
                }
            },
            title = { Text("Create Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Color", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { hexColor ->
                            val color = Color(android.graphics.Color.parseColor(hexColor))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = hexColor }
                                    .let { m ->
                                        if (selectedColor == hexColor) {
                                            m.border(2.dp, Color.Black, CircleShape)
                                        } else m
                                    }
                            )
                        }
                    }
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun CategorySummaryCard(category: CategorySummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = 18.sp, color = category.color, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = category.totalAmount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = category.color
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (category.transactionCount == 0) "From database" else "Custom category",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

data class CategorySummary(
    val name: String,
    val icon: String,
    val color: Color,
    val totalAmount: String,
    val transactionCount: Int
)

private fun categoryColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        Color(0xFF64748B)
    }
}

private fun iconForCategorySummary(name: String): String {
    return when (name.lowercase()) {
        "food", "groceries" -> "F"
        "transport", "transportation" -> "T"
        "shopping" -> "S"
        "bills", "utilities" -> "B"
        "health", "healthcare" -> "H"
        "education" -> "E"
        "entertainment" -> "M"
        "income" -> "+"
        else -> "#"
    }
}
