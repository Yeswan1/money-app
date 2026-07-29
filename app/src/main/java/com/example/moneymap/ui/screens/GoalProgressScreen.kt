package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.data.model.SavingsGoalDto
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalProgressScreen(goalName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    
    var goal by remember { mutableStateOf<SavingsGoalDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadGoalDetails() {
        scope.launch {
            isLoading = true
            repository.getSavingsGoals()
                .onSuccess { loaded ->
                    goal = loaded.find { it.name.equals(goalName, ignoreCase = true) }
                }
                .onFailure { errorMessage = it.message ?: "Failed to load savings goal details." }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadGoalDetails()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text(goalName, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                    goal?.let { g ->
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(40.dp)
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        repository.deleteSavingsGoal(g.id)
                                            .onSuccess { onBack() }
                                            .onFailure { errorMessage = it.message ?: "Failed to delete savings goal." }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            goal?.let { g ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                repository.updateSavingsGoal(g.id, g.currentAmount + 50.0)
                                    .onSuccess {
                                        goal = it
                                    }
                                    .onFailure {
                                        errorMessage = it.message ?: "Failed to add funds."
                                    }
                                isSaving = false
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add ₹50.00", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
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
        ) {
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            Spacer(modifier = Modifier.height(32.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else if (goal == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Savings goal not found.", color = Color.Gray)
                }
            } else {
                val g = goal!!
                val percentVal = if (g.targetAmount > 0.0) (g.currentAmount / g.targetAmount) else 0.0
                val percentText = String.format("%.0f%%", percentVal * 100)
                val color = try {
                    if (g.color != null) {
                        Color(android.graphics.Color.parseColor(g.color))
                    } else {
                        Color(0xFF3B82F6)
                    }
                } catch (e: Exception) {
                    Color(0xFF3B82F6)
                }

                // Progress Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = Math.min(1.0f, percentVal.toFloat()),
                        modifier = Modifier.size(200.dp),
                        color = color,
                        trackColor = color.copy(alpha = 0.15f),
                        strokeWidth = 16.dp,
                        strokeCap = StrokeCap.Round
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconForGoal(g.name), contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(percentText, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saved so far", fontSize = 14.sp, color = Color(0xFF64748B))
                    Text(String.format("₹%.2f", g.currentAmount), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(String.format("Target: ₹%.2f", g.targetAmount), fontSize = 14.sp, color = Color(0xFF64748B))
                    
                    g.targetDate?.let { date ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Target Date: ${date.split("T").firstOrNull() ?: ""}", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Recent Contributions
                Text(
                    text = "Savings Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Initial Target", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                    Text(String.format("₹%.2f", g.targetAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Balance", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E40AF))
                    Text(String.format("₹%.2f", g.currentAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

private fun iconForGoal(name: String): ImageVector {
    return when (name.lowercase(Locale.US)) {
        "vacation", "flight", "trip" -> Icons.Default.Flight
        "car", "vehicle" -> Icons.Default.DirectionsCar
        "home", "house" -> Icons.Default.Home
        else -> Icons.Default.Security
    }
}
