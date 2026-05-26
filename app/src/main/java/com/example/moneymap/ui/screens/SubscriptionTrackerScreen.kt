package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
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
import com.example.moneymap.data.model.SubscriptionDto
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var subscriptions by remember { mutableStateOf<List<SubscriptionDto>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refreshSubscriptions() {
        scope.launch {
            isLoading = true
            repository.getSubscriptions()
                .onSuccess { subscriptions = it }
                .onFailure { errorMessage = it.message ?: "Could not load subscriptions." }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshSubscriptions()
    }

    val monthlyTotal = subscriptions
        .filter { it.isActive }
        .sumOf { sub ->
            when (sub.billingCycle) {
                "WEEKLY" -> sub.amount * 4.0
                "QUARTERLY" -> sub.amount / 3.0
                "YEARLY" -> sub.amount / 12.0
                else -> sub.amount
            }
        }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Subscriptions", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF2563EB))
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Total Monthly Bills",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", monthlyTotal)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${subscriptions.count { it.isActive }} Active Subscriptions",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Active Subscriptions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                items(subscriptions) { sub ->
                    SubscriptionItem(sub.toUiSubscription())
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { name, amount, cycle, date ->
                scope.launch {
                    errorMessage = null
                    repository.createSubscription(name, amount, cycle, normalizeSubscriptionDateForApi(date))
                        .onSuccess {
                            showAddDialog = false
                            refreshSubscriptions()
                        }
                        .onFailure { errorMessage = it.message ?: "Could not create subscription." }
                }
            }
        )
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(subscription.color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = subscription.color)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subscription.billingCycle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        Text(
            text = subscription.amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}

data class Subscription(
    val name: String,
    val billingCycle: String,
    val amount: String,
    val color: Color
)

@Composable
private fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Double, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var billingCycle by remember { mutableStateOf("MONTHLY") }
    var nextBillingDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subscription") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = billingCycle,
                    onValueChange = { billingCycle = it.uppercase(Locale.US) },
                    label = { Text("Billing Cycle") },
                    placeholder = { Text("WEEKLY, MONTHLY, QUARTERLY, YEARLY") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = nextBillingDate,
                    onValueChange = { nextBillingDate = it },
                    label = { Text("Next Billing Date") },
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true,
                )
                validationMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    when {
                        name.isBlank() -> validationMessage = "Enter a subscription name."
                        parsedAmount == null || parsedAmount <= 0.0 -> validationMessage = "Enter a valid amount."
                        billingCycle !in setOf("WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY") -> {
                            validationMessage = "Use WEEKLY, MONTHLY, QUARTERLY, or YEARLY."
                        }
                        nextBillingDate.isBlank() -> validationMessage = "Enter the next billing date."
                        else -> onCreate(name.trim(), parsedAmount, billingCycle, nextBillingDate)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun SubscriptionDto.toUiSubscription(): Subscription {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (_: IllegalArgumentException) {
        Color(0xFF3B82F6)
    }

    return Subscription(
        name = name,
        billingCycle = "${billingCycle.lowercase(Locale.US).replaceFirstChar { it.uppercase() }} - Next billing ${nextBillingDate.take(10)}",
        amount = "$${String.format(Locale.US, "%.2f", amount)}",
        color = parsedColor,
    )
}

private fun normalizeSubscriptionDateForApi(input: String): String {
    val trimmed = input.trim()
    return if (trimmed.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
        val parts = trimmed.split("-")
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else {
        trimmed
    }
}
