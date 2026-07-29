package com.example.moneymap.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.UpdateProfileRequest
import com.example.moneymap.data.model.UpdateSettingsRequest
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun EmployeeSetupScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
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
                text = "Employee Profile",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Share your employment details",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Company Name
            Text("Company Name", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                placeholder = { Text("Enter company name", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Job Title
            Text("Job Title", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = jobTitle,
                onValueChange = { jobTitle = it },
                placeholder = { Text("e.g. Software Engineer", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly Income
            Text("Monthly Income", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = monthlyIncome,
                onValueChange = { monthlyIncome = it },
                placeholder = { Text("5000", color = Color(0xFF9CA3AF)) },
                prefix = { Text("₹ ", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    scope.launch {
                        val settingsRes = repository.updateSettings(
                            UpdateSettingsRequest(role = "PROFESSIONAL")
                        )
                        settingsRes.onSuccess {
                            val profileRes = repository.updateProfile(
                                UpdateProfileRequest(
                                    companyName = companyName.trim().takeIf { it.isNotBlank() },
                                    jobTitle = jobTitle.trim().takeIf { it.isNotBlank() },
                                    monthlyIncome = monthlyIncome.toDoubleOrNull() ?: 0.0
                                )
                            )
                            profileRes.onSuccess {
                                isSaving = false
                                onContinue()
                            }.onFailure {
                                isSaving = false
                                errorMessage = it.message ?: "Failed to save profile details."
                            }
                        }.onFailure {
                            isSaving = false
                            errorMessage = it.message ?: "Failed to update role settings."
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
