package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SampleAiBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun PastelFloralHeaderAccent(
    modifier: Modifier = Modifier
) {
    val petalColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val starColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    val centerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)

    Canvas(modifier = modifier.size(44.dp)) {
        val cx = size.width * 0.45f
        val cy = size.height * 0.52f
        val petalRadius = size.minDimension * 0.17f
        val orbit = size.minDimension * 0.22f
        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72).toDouble())
            val px = cx + (orbit * cos(angle)).toFloat()
            val py = cy + (orbit * sin(angle)).toFloat()
            drawCircle(color = petalColor, radius = petalRadius, center = Offset(px, py))
        }
        drawCircle(color = centerColor, radius = petalRadius * 0.65f, center = Offset(cx, cy))

        val sx = size.width * 0.82f
        val sy = size.height * 0.24f
        val sr = size.minDimension * 0.13f
        val starPath = Path().apply {
            moveTo(sx, sy - sr)
            quadraticTo(sx, sy, sx + sr, sy)
            quadraticTo(sx, sy, sx, sy + sr)
            quadraticTo(sx, sy, sx - sr, sy)
            quadraticTo(sx, sy, sx, sy - sr)
            close()
        }
        drawPath(starPath, color = starColor)
    }
}
