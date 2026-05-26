package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean
)

@Composable
fun ChatbotScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var activeSessionId by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var messages by remember { 
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "1", 
                    text = "Hi! I'm your AI financial assistant. How can I help you manage your money today?", 
                    isUser = false
                )
            )
        )
    }

    fun sendMessage(text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank() || isSending) return

        errorMessage = null
        messages = messages + ChatMessage(System.currentTimeMillis().toString(), cleanText, true)
        scope.launch {
            isSending = true
            val result = repository.sendChatMessage(activeSessionId, cleanText)
            isSending = false
            result
                .onSuccess { response ->
                    activeSessionId = response.sessionId
                    messages = messages + ChatMessage(
                        id = response.message.id,
                        text = response.message.content,
                        isUser = false,
                    )
                }
                .onFailure {
                    errorMessage = it.message ?: "Could not send message."
                }
        }
    }

    val suggestedQuestions = listOf(
        "Show my spending this week",
        "How much did I spend on food?",
        "Help me save $500",
        "What's my biggest expense?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
    ) {
        // Custom Gradient Top Bar
        ChatTopBar(onBack = onBack)

        // Chat Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            
            // Suggested Questions section at the bottom of the list
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SuggestedQuestionsSection(
                    questions = suggestedQuestions,
                    onQuestionClick = { question -> sendMessage(question) }
                )
            }

            if (isSending) {
                item {
                    ChatBubble(ChatMessage("loading", "Thinking...", false))
                }
            }

            errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // Input Area
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    placeholder = { 
                        Text(
                            "Ask me anything about your finances...", 
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSending) Color(0xFF94A3B8) else Color(0xFF6366F1))
                        .clickable {
                            if (inputText.isNotBlank()) {
                                sendMessage(inputText)
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatTopBar(onBack: () -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4C6FFF),
            Color(0xFF8A4DFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Titles
                Column {
                    Text(
                        text = "AI Assistant",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your financial advisor",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Sparkle Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isUser) Color(0xFF1D4ED8) else Color.White
    val textColor = if (message.isUser) Color.White else Color(0xFF0F172A)
    
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = backgroundColor,
            shadowElevation = if (message.isUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
fun SuggestedQuestionsSection(
    questions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Suggested questions",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestedQuestionCard(
                text = questions[0],
                modifier = Modifier.weight(1f),
                onClick = { onQuestionClick(questions[0]) }
            )
            SuggestedQuestionCard(
                text = questions[1],
                modifier = Modifier.weight(1f),
                onClick = { onQuestionClick(questions[1]) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestedQuestionCard(
                text = questions[2],
                modifier = Modifier.weight(1f),
                onClick = { onQuestionClick(questions[2]) }
            )
            SuggestedQuestionCard(
                text = questions[3],
                modifier = Modifier.weight(1f),
                onClick = { onQuestionClick(questions[3]) }
            )
        }
    }
}

@Composable
fun SuggestedQuestionCard(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color(0xFF0F172A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )
    }
}
