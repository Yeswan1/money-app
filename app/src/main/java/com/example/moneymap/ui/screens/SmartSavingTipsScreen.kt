package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSavingTipsScreen(onBack: () -> Unit) {
    val tips = listOf(
        SavingTip(
            "Reduce Dining Out",
            "You spent $450 on food this month. Cooking at home 3 more times a week could save you $120/mo.",
            Color(0xFF3B82F6),
            "High Impact"
        ),
        SavingTip(
            "Cancel Unused Subscriptions",
            "You have 2 streaming services you haven't used in 30 days. Canceling them saves $25/mo.",
            Color(0xFF10B981),
            "Medium Impact"
        ),
        SavingTip(
            "Energy Efficiency",
            "Your utility bill is higher than average. Try adjusting your thermostat to save up to 10%.",
            Color(0xFFF59E0B),
            "Low Impact"
        )
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Smart Saving Tips", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Powered",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.padding(end = 24.dp)
                    )
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

            Spacer(modifier = Modifier.height(24.dp))
            
            // AI Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F3FF))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AI-Powered Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C1D95)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Based on your recent spending habits, here are personalized suggestions to help you reach your goals faster.",
                    fontSize = 14.sp,
                    color = Color(0xFF6D28D9),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tips) { tip ->
                    TipItem(tip)
                }
            }
        }
    }
}

@Composable
fun TipItem(tip: SavingTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(tip.color)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tip.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Text(
                    text = tip.impact,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = tip.color
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = tip.message,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = tip.color),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Take Action", fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class SavingTip(
    val title: String,
    val message: String,
    val color: Color,
    val impact: String
)
