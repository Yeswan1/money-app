package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.NotificationsActive
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAlertsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var alerts by remember { mutableStateOf<List<BudgetAlert>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getDashboardStats().onSuccess { stats ->
            alerts = stats.alerts.map {
                BudgetAlert(
                    title = it.title,
                    message = it.message,
                    isCritical = it.isCritical,
                    time = it.time
                )
            }
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Budget Alerts", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
            HorizontalDivider(color = Color(0xFFF1F5F9))

            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No alerts at the moment.", color = Color(0xFF64748B), fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(alerts) { alert ->
                        AlertItem(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItem(alert: BudgetAlert) {
    val bgColor = if (alert.isCritical) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
    val iconColor = if (alert.isCritical) Color(0xFFEF4444) else Color(0xFFF59E0B)
    val iconBg = if (alert.isCritical) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (alert.isCritical) Icons.Default.Warning else Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = alert.time,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alert.message,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 20.sp
            )
            
            if (alert.isCritical) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Adjust Budget",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }
        }
    }
}

data class BudgetAlert(
    val title: String,
    val message: String,
    val isCritical: Boolean,
    val time: String
)
