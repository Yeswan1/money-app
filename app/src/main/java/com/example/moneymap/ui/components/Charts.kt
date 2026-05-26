package com.example.moneymap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF3B82F6),
    textColor: Color = Color(0xFF64748B)
) {
    val textMeasurer = rememberTextMeasurer()
    val maxDataValue = data.maxOfOrNull { it.second } ?: 1f
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val barCount = data.size
        val barSpacing = 16.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = (canvasWidth - totalSpacing) / barCount
        
        val textHeight = 24.dp.toPx()
        val availableChartHeight = canvasHeight - textHeight
        
        data.forEachIndexed { index, pair ->
            val label = pair.first
            val value = pair.second
            
            val barHeight = (value / maxDataValue) * availableChartHeight
            val startX = index * (barWidth + barSpacing)
            val startY = availableChartHeight - barHeight
            
            // Draw bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(startX, startY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            // Draw label
            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(fontSize = 12.sp, color = textColor)
            )
            
            val textStartX = startX + (barWidth - textLayoutResult.size.width) / 2
            val textStartY = availableChartHeight + 8.dp.toPx()
            
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(textStartX, textStartY),
                style = TextStyle(fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun CustomPieChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second.toDouble() }.toFloat()
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2
        val center = Offset(canvasWidth / 2, canvasHeight / 2)
        
        var startAngle = -90f
        
        data.forEachIndexed { index, pair ->
            val sweepAngle = (pair.second / total) * 360f
            
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Butt)
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CustomAreaChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF8B5CF6),
    textColor: Color = Color(0xFF64748B)
) {
    val textMeasurer = rememberTextMeasurer()
    val maxDataValue = data.maxOfOrNull { it.second } ?: 1f
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val pointCount = data.size
        val xSpacing = canvasWidth / (pointCount - 1)
        
        val textHeight = 24.dp.toPx()
        val availableChartHeight = canvasHeight - textHeight
        
        val path = Path()
        val fillPath = Path()
        
        fillPath.moveTo(0f, availableChartHeight)
        
        var prevX = 0f
        var prevY = availableChartHeight
        
        data.forEachIndexed { index, pair ->
            val label = pair.first
            val value = pair.second
            
            val x = index * xSpacing
            val y = availableChartHeight - ((value / maxDataValue) * availableChartHeight)
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                // Smooth curve
                val controlX = (prevX + x) / 2
                path.cubicTo(controlX, prevY, controlX, y, x, y)
                fillPath.cubicTo(controlX, prevY, controlX, y, x, y)
            }
            
            prevX = x
            prevY = y
            
            // Draw label
            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(fontSize = 12.sp, color = textColor)
            )
            
            val textStartX = x - (textLayoutResult.size.width / 2)
            val textStartY = availableChartHeight + 8.dp.toPx()
            
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(textStartX, textStartY),
                style = TextStyle(fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
            )
        }
        
        fillPath.lineTo(canvasWidth, availableChartHeight)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            color = lineColor.copy(alpha = 0.2f)
        )
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
