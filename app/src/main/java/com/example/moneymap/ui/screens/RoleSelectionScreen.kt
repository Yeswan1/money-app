package com.example.moneymap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.ui.theme.*

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    var selectedRole by remember { mutableStateOf<RoleItem?>(null) }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "I am a...",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            
            Text(
                text = "Select your profile type for personalized experience",
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 12.dp, bottom = 40.dp),
                lineHeight = 22.sp
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(roles) { role ->
                    RoleCard(
                        role = role,
                        isSelected = selectedRole == role,
                        onClick = { selectedRole = role }
                    )
                }
            }

            // Pagination Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == 0) 24.dp else 8.dp, 8.dp)
                            .background(
                                color = if (index == 0) Primary else Color(0xFFE5E7EB),
                                shape = CircleShape
                            )
                    )
                }
            }

            Button(
                onClick = { selectedRole?.let { onRoleSelected(it.id) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedRole != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Gray200
                )
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun RoleCard(
    role: RoleItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Primary else Color(0xFFF3F4F6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = role.gradient,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = role.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = role.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            )
        }
    }
}

data class RoleItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val gradient: Brush
)

val roles = listOf(
    RoleItem(
        "student",
        "Student",
        Icons.Default.School,
        Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2DD4BF)))
    ),
    RoleItem(
        "professional",
        "Professional",
        Icons.Default.BusinessCenter,
        Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFFEC4899)))
    ),
    RoleItem(
        "homemaker",
        "Homemaker",
        Icons.Default.Home,
        Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF10B981)))
    ),
    RoleItem(
        "personal",
        "Personal",
        Icons.Default.Person,
        Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEF4444)))
    )
)
