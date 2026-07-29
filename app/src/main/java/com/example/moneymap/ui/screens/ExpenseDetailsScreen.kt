package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseDetailsScreen(
    transactionId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("0.00") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categoryColor by remember { mutableStateOf(Color(0xFF64748B)) }
    var date by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var isExpense by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(transactionId) {
        if (transactionId.isNotBlank()) {
            isLoading = true
            repository.getTransaction(transactionId)
                .onSuccess { t ->
                    amount = String.format(java.util.Locale.US, "%.2f", t.amount)
                    description = t.description.orEmpty()
                    category = t.category?.name ?: t.type
                    categoryColor = try {
                        Color(android.graphics.Color.parseColor(t.category?.color ?: "#64748B"))
                    } catch (_: IllegalArgumentException) {
                        Color(0xFF64748B)
                    }
                    date = t.transactionDate.take(10)
                    tags = t.tags ?: emptyList()
                    isExpense = t.type == "EXPENSE"
                }
                .onFailure {
                    errorMessage = it.message ?: "Could not load transaction details."
                }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isExpense) "Expense Details" else "Income Details", 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A)
                            )
                        }
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(56.dp)) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
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
                    .padding(24.dp)
            ) {
                errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isExpense) Color(0xFFF45B5B) else Color(0xFF10B981), RoundedCornerShape(24.dp))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isExpense) "💸" else "💰", fontSize = 28.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (isExpense) "Expense Amount" else "Income Amount",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = "₹$amount",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = description,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailItem(
                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = categoryColor) },
                    iconBgColor = categoryColor.copy(alpha = 0.1f),
                    label = if (isExpense) "Category" else "Income Source",
                    value = category
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailItem(
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF3B82F6)) },
                    iconBgColor = Color(0xFFEFF6FF),
                    label = "Date",
                    value = date
                )
                
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Tags",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    TagPill(tag, Color(0xFFDBEAFE), Color(0xFF2563EB))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isDeleting
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                color = Color(0xFFEF4444),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                isExpense = isExpense,
                amount = amount,
                category = category,
                onConfirm = {
                    showDeleteDialog = false
                    scope.launch {
                        isDeleting = true
                        repository.deleteTransaction(transactionId)
                            .onSuccess {
                                isDeleting = false
                                onDeleteSuccess()
                            }
                            .onFailure {
                                isDeleting = false
                                errorMessage = it.message ?: "Could not delete transaction."
                            }
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun DetailItem(
    icon: (@Composable () -> Unit)? = null,
    iconBgColor: Color = Color.Transparent,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        
        Column {
            Text(
                text = label,
                color = Color(0xFF64748B),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = if (icon != null) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TagPill(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    isExpense: Boolean,
    amount: String,
    category: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", color = Color(0xFF64748B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFFEE2E2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = "Warning",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isExpense) "Delete Expense?" else "Delete Income?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isExpense) {
                            "Are you sure you want to delete this expense? This action cannot be undone."
                        } else {
                            "Are you sure you want to delete this income? This action cannot be undone."
                        },
                        fontSize = 15.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount", color = Color(0xFF64748B), fontSize = 14.sp)
                                Text("₹$amount", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isExpense) "Category" else "Income Source", color = Color(0xFF64748B), fontSize = 14.sp)
                                Text(category, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Yes, Delete", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
