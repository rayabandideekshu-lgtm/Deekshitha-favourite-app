package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.media.ExifInterface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.PoseOutlineType
import kotlin.math.min

/**
 * Draws a transparent-interior, thin white-line human pose outline on the live camera preview.
 * Scales cleanly to any screen size and supports customization of size, vertical offset, and alpha.
 */
@Composable
fun WhiteLinePoseOverlay(
    outlineType: PoseOutlineType,
    scale: Float = 1.0f,
    verticalOffsetFraction: Float = 0f,
    alpha: Float = 0.88f,
    lineColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawPoseOutlineCompose(
            outlineType = outlineType,
            color = lineColor.copy(alpha = alpha.coerceIn(0.15f, 1f)),
            scale = scale,
            verticalOffsetFraction = verticalOffsetFraction,
            strokeWidthPx = (min(size.width, size.height) * 0.0065f).coerceIn(3.5f, 9f)
        )
    }
}

/**
 * Compact preview badge for pose cards using the exact same outline matching the live camera guide.
 */
@Composable
fun PoseOutlineCardPreview(
    outlineType: PoseOutlineType,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.White,
    backgroundColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(66.dp)
                .padding(5.dp)
        ) {
            drawPoseOutlineCompose(
                outlineType = outlineType,
                color = lineColor.copy(alpha = 0.95f),
                scale = 1.05f,
                verticalOffsetFraction = 0f,
                strokeWidthPx = 3.2f
            )
        }
    }
}

private data class P2(val x: Float, val y: Float)

private data class HumanFigureSpec(
    val headCenter: P2,
    val headRadius: Float,
    val neck: P2,
    val lShoulder: P2,
    val rShoulder: P2,
    val lElbow: P2,
    val rElbow: P2,
    val lWrist: P2,
    val rWrist: P2,
    val lHandTip: P2,
    val rHandTip: P2,
    val lHip: P2,
    val rHip: P2,
    val lKnee: P2,
    val rKnee: P2,
    val lAnkle: P2,
    val rAnkle: P2,
    val lFootTip: P2,
    val rFootTip: P2
)

private fun DrawScope.drawPoseOutlineCompose(
    outlineType: PoseOutlineType,
    color: Color,
    scale: Float,
    verticalOffsetFraction: Float,
    strokeWidthPx: Float
) {
    val boxSize = min(size.width, size.height) * 0.72f * scale.coerceIn(0.55f, 1.45f)
    val left = (size.width - boxSize) / 2f
    val top = (size.height - boxSize) / 2f + (size.height * verticalOffsetFraction)

    fun pt(nx: Float, ny: Float) = Offset(left + nx * boxSize, top + ny * boxSize)

    val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val softPropColor = color.copy(alpha = (color.alpha * 0.65f))

    fun drawFigure(spec: HumanFigureSpec) {
        // Head (hollow circle - transparent inside)
        drawCircle(
            color = color,
            radius = spec.headRadius * boxSize,
            center = pt(spec.headCenter.x, spec.headCenter.y),
            style = stroke
        )
        // Neck & Shoulder line
        drawLine(color, pt(spec.headCenter.x, spec.headCenter.y + spec.headRadius), pt(spec.neck.x, spec.neck.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.lShoulder.x, spec.lShoulder.y), pt(spec.rShoulder.x, spec.rShoulder.y), strokeWidthPx, StrokeCap.Round)

        // Torso outline (hollow trapezoid from shoulders to hips)
        val torso = Path().apply {
            val ls = pt(spec.lShoulder.x, spec.lShoulder.y)
            val rs = pt(spec.rShoulder.x, spec.rShoulder.y)
            val rh = pt(spec.rHip.x, spec.rHip.y)
            val lh = pt(spec.lHip.x, spec.lHip.y)
            moveTo(ls.x, ls.y)
            lineTo(rs.x, rs.y)
            lineTo(rh.x, rh.y)
            lineTo(lh.x, lh.y)
            close()
        }
        drawPath(torso, color = color, style = stroke)

        // Left & Right Arms + Hands
        drawLine(color, pt(spec.lShoulder.x, spec.lShoulder.y), pt(spec.lElbow.x, spec.lElbow.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.lElbow.x, spec.lElbow.y), pt(spec.lWrist.x, spec.lWrist.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.lWrist.x, spec.lWrist.y), pt(spec.lHandTip.x, spec.lHandTip.y), strokeWidthPx * 0.9f, StrokeCap.Round)
        drawCircle(color, radius = strokeWidthPx * 0.95f, center = pt(spec.lHandTip.x, spec.lHandTip.y), style = stroke)

        drawLine(color, pt(spec.rShoulder.x, spec.rShoulder.y), pt(spec.rElbow.x, spec.rElbow.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.rElbow.x, spec.rElbow.y), pt(spec.rWrist.x, spec.rWrist.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.rWrist.x, spec.rWrist.y), pt(spec.rHandTip.x, spec.rHandTip.y), strokeWidthPx * 0.9f, StrokeCap.Round)
        drawCircle(color, radius = strokeWidthPx * 0.95f, center = pt(spec.rHandTip.x, spec.rHandTip.y), style = stroke)

        // Left & Right Legs + Feet
        drawLine(color, pt(spec.lHip.x, spec.lHip.y), pt(spec.lKnee.x, spec.lKnee.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.lKnee.x, spec.lKnee.y), pt(spec.lAnkle.x, spec.lAnkle.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.lAnkle.x, spec.lAnkle.y), pt(spec.lFootTip.x, spec.lFootTip.y), strokeWidthPx, StrokeCap.Round)

        drawLine(color, pt(spec.rHip.x, spec.rHip.y), pt(spec.rKnee.x, spec.rKnee.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.rKnee.x, spec.rKnee.y), pt(spec.rAnkle.x, spec.rAnkle.y), strokeWidthPx, StrokeCap.Round)
        drawLine(color, pt(spec.rAnkle.x, spec.rAnkle.y), pt(spec.rFootTip.x, spec.rFootTip.y), strokeWidthPx, StrokeCap.Round)
    }

    when (outlineType) {
        PoseOutlineType.RELAXED_CAFE_SITTING -> {
            // Subtle café chair outline
            drawLine(softPropColor, pt(0.28f, 0.38f), pt(0.32f, 0.65f), strokeWidthPx * 0.8f, StrokeCap.Round)
            drawLine(softPropColor, pt(0.32f, 0.65f), pt(0.58f, 0.65f), strokeWidthPx * 0.8f, StrokeCap.Round)
            drawLine(softPropColor, pt(0.34f, 0.65f), pt(0.34f, 0.90f), strokeWidthPx * 0.8f, StrokeCap.Round)
            drawLine(softPropColor, pt(0.56f, 0.65f), pt(0.56f, 0.90f), strokeWidthPx * 0.8f, StrokeCap.Round)
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.42f, 0.16f), headRadius = 0.068f, neck = P2(0.42f, 0.26f),
                    lShoulder = P2(0.35f, 0.28f), rShoulder = P2(0.49f, 0.28f),
                    lElbow = P2(0.33f, 0.42f), rElbow = P2(0.55f, 0.40f),
                    lWrist = P2(0.44f, 0.50f), rWrist = P2(0.62f, 0.50f),
                    lHandTip = P2(0.48f, 0.52f), rHandTip = P2(0.65f, 0.52f),
                    lHip = P2(0.36f, 0.58f), rHip = P2(0.48f, 0.58f),
                    lKnee = P2(0.62f, 0.60f), rKnee = P2(0.67f, 0.63f),
                    lAnkle = P2(0.63f, 0.86f), rAnkle = P2(0.71f, 0.87f),
                    lFootTip = P2(0.70f, 0.89f), rFootTip = P2(0.78f, 0.89f)
                )
            )
        }

        PoseOutlineType.STANDING_PORTRAIT -> {
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.50f, 0.12f), headRadius = 0.068f, neck = P2(0.50f, 0.22f),
                    lShoulder = P2(0.40f, 0.24f), rShoulder = P2(0.60f, 0.24f),
                    lElbow = P2(0.35f, 0.39f), rElbow = P2(0.65f, 0.39f),
                    lWrist = P2(0.34f, 0.52f), rWrist = P2(0.66f, 0.52f),
                    lHandTip = P2(0.34f, 0.56f), rHandTip = P2(0.66f, 0.56f),
                    lHip = P2(0.43f, 0.52f), rHip = P2(0.57f, 0.52f),
                    lKnee = P2(0.43f, 0.71f), rKnee = P2(0.57f, 0.71f),
                    lAnkle = P2(0.42f, 0.89f), rAnkle = P2(0.58f, 0.89f),
                    lFootTip = P2(0.37f, 0.91f), rFootTip = P2(0.63f, 0.91f)
                )
            )
        }

        PoseOutlineType.HANDS_ON_WAIST -> {
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.50f, 0.12f), headRadius = 0.068f, neck = P2(0.50f, 0.22f),
                    lShoulder = P2(0.40f, 0.24f), rShoulder = P2(0.60f, 0.24f),
                    lElbow = P2(0.28f, 0.37f), rElbow = P2(0.72f, 0.37f),
                    lWrist = P2(0.41f, 0.48f), rWrist = P2(0.59f, 0.48f),
                    lHandTip = P2(0.44f, 0.50f), rHandTip = P2(0.56f, 0.50f),
                    lHip = P2(0.43f, 0.52f), rHip = P2(0.57f, 0.52f),
                    lKnee = P2(0.42f, 0.71f), rKnee = P2(0.58f, 0.71f),
                    lAnkle = P2(0.41f, 0.89f), rAnkle = P2(0.59f, 0.89f),
                    lFootTip = P2(0.36f, 0.91f), rFootTip = P2(0.64f, 0.91f)
                )
            )
        }

        PoseOutlineType.WALKING_POSE -> {
            drawLine(softPropColor, pt(0.22f, 0.92f), pt(0.78f, 0.92f), strokeWidthPx * 0.7f, StrokeCap.Round)
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.52f, 0.12f), headRadius = 0.066f, neck = P2(0.51f, 0.22f),
                    lShoulder = P2(0.42f, 0.24f), rShoulder = P2(0.60f, 0.25f),
                    lElbow = P2(0.32f, 0.37f), rElbow = P2(0.68f, 0.38f),
                    lWrist = P2(0.27f, 0.49f), rWrist = P2(0.72f, 0.49f),
                    lHandTip = P2(0.25f, 0.53f), rHandTip = P2(0.74f, 0.53f),
                    lHip = P2(0.44f, 0.52f), rHip = P2(0.56f, 0.52f),
                    lKnee = P2(0.36f, 0.70f), rKnee = P2(0.64f, 0.70f),
                    lAnkle = P2(0.28f, 0.89f), rAnkle = P2(0.71f, 0.88f),
                    lFootTip = P2(0.22f, 0.90f), rFootTip = P2(0.77f, 0.90f)
                )
            )
        }

        PoseOutlineType.LOOKING_OVER_SHOULDER -> {
            // Gaze indicator arrow near head
            drawCircle(softPropColor, radius = strokeWidthPx * 1.1f, center = pt(0.31f, 0.14f), style = stroke)
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.45f, 0.13f), headRadius = 0.068f, neck = P2(0.48f, 0.23f),
                    lShoulder = P2(0.39f, 0.26f), rShoulder = P2(0.61f, 0.23f),
                    lElbow = P2(0.35f, 0.41f), rElbow = P2(0.65f, 0.38f),
                    lWrist = P2(0.37f, 0.53f), rWrist = P2(0.62f, 0.51f),
                    lHandTip = P2(0.38f, 0.57f), rHandTip = P2(0.62f, 0.55f),
                    lHip = P2(0.44f, 0.53f), rHip = P2(0.57f, 0.52f),
                    lKnee = P2(0.43f, 0.72f), rKnee = P2(0.58f, 0.71f),
                    lAnkle = P2(0.42f, 0.90f), rAnkle = P2(0.59f, 0.89f),
                    lFootTip = P2(0.37f, 0.91f), rFootTip = P2(0.64f, 0.91f)
                )
            )
        }

        PoseOutlineType.CUTE_SITTING_POSE -> {
            drawLine(softPropColor, pt(0.25f, 0.86f), pt(0.75f, 0.86f), strokeWidthPx * 0.7f, StrokeCap.Round)
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.46f, 0.22f), headRadius = 0.068f, neck = P2(0.46f, 0.32f),
                    lShoulder = P2(0.38f, 0.34f), rShoulder = P2(0.54f, 0.34f),
                    lElbow = P2(0.33f, 0.47f), rElbow = P2(0.61f, 0.47f),
                    lWrist = P2(0.43f, 0.58f), rWrist = P2(0.54f, 0.58f),
                    lHandTip = P2(0.46f, 0.60f), rHandTip = P2(0.52f, 0.60f),
                    lHip = P2(0.39f, 0.62f), rHip = P2(0.52f, 0.62f),
                    lKnee = P2(0.59f, 0.56f), rKnee = P2(0.65f, 0.63f),
                    lAnkle = P2(0.56f, 0.82f), rAnkle = P2(0.68f, 0.83f),
                    lFootTip = P2(0.62f, 0.85f), rFootTip = P2(0.74f, 0.85f)
                )
            )
        }

        PoseOutlineType.MIRROR_SELFIE -> {
            // Arched mirror frame + phone rectangle
            val mTopLeft = pt(0.18f, 0.05f)
            val mSize = Size(0.64f * boxSize, 0.90f * boxSize)
            drawRoundRect(
                color = softPropColor,
                topLeft = mTopLeft,
                size = mSize,
                cornerRadius = CornerRadius(boxSize * 0.26f, boxSize * 0.26f),
                style = Stroke(width = strokeWidthPx * 0.85f)
            )
            // Phone outline in hand
            drawRoundRect(
                color = color,
                topLeft = pt(0.54f, 0.28f),
                size = Size(0.08f * boxSize, 0.13f * boxSize),
                cornerRadius = CornerRadius(6f, 6f),
                style = stroke
            )
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.47f, 0.18f), headRadius = 0.062f, neck = P2(0.47f, 0.27f),
                    lShoulder = P2(0.38f, 0.29f), rShoulder = P2(0.56f, 0.29f),
                    lElbow = P2(0.34f, 0.43f), rElbow = P2(0.64f, 0.42f),
                    lWrist = P2(0.36f, 0.54f), rWrist = P2(0.58f, 0.35f),
                    lHandTip = P2(0.37f, 0.58f), rHandTip = P2(0.58f, 0.33f),
                    lHip = P2(0.40f, 0.55f), rHip = P2(0.53f, 0.55f),
                    lKnee = P2(0.39f, 0.72f), rKnee = P2(0.54f, 0.72f),
                    lAnkle = P2(0.38f, 0.89f), rAnkle = P2(0.56f, 0.89f),
                    lFootTip = P2(0.34f, 0.91f), rFootTip = P2(0.60f, 0.91f)
                )
            )
        }

        PoseOutlineType.SIDE_PROFILE -> {
            // Subtle nose directional tick on profile head
            drawLine(color, pt(0.56f, 0.12f), pt(0.60f, 0.135f), strokeWidthPx * 0.9f, StrokeCap.Round)
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.50f, 0.12f), headRadius = 0.066f, neck = P2(0.48f, 0.22f),
                    lShoulder = P2(0.45f, 0.24f), rShoulder = P2(0.53f, 0.24f),
                    lElbow = P2(0.43f, 0.39f), rElbow = P2(0.52f, 0.39f),
                    lWrist = P2(0.46f, 0.52f), rWrist = P2(0.54f, 0.52f),
                    lHandTip = P2(0.47f, 0.56f), rHandTip = P2(0.55f, 0.56f),
                    lHip = P2(0.46f, 0.52f), rHip = P2(0.52f, 0.52f),
                    lKnee = P2(0.45f, 0.71f), rKnee = P2(0.53f, 0.71f),
                    lAnkle = P2(0.44f, 0.89f), rAnkle = P2(0.53f, 0.89f),
                    lFootTip = P2(0.50f, 0.91f), rFootTip = P2(0.60f, 0.91f)
                )
            )
        }

        PoseOutlineType.CASUAL_LEAN -> {
            // Wall edge line on left
            drawLine(softPropColor, pt(0.28f, 0.06f), pt(0.28f, 0.92f), strokeWidthPx * 0.9f, StrokeCap.Round)
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.42f, 0.13f), headRadius = 0.066f, neck = P2(0.40f, 0.23f),
                    lShoulder = P2(0.31f, 0.25f), rShoulder = P2(0.49f, 0.27f),
                    lElbow = P2(0.30f, 0.40f), rElbow = P2(0.55f, 0.41f),
                    lWrist = P2(0.36f, 0.51f), rWrist = P2(0.50f, 0.52f),
                    lHandTip = P2(0.38f, 0.54f), rHandTip = P2(0.51f, 0.55f),
                    lHip = P2(0.35f, 0.53f), rHip = P2(0.48f, 0.53f),
                    lKnee = P2(0.44f, 0.72f), rKnee = P2(0.52f, 0.71f),
                    lAnkle = P2(0.46f, 0.89f), rAnkle = P2(0.57f, 0.89f),
                    lFootTip = P2(0.52f, 0.91f), rFootTip = P2(0.63f, 0.91f)
                )
            )
        }

        PoseOutlineType.GROUP_POSE -> {
            // Left person
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.24f, 0.21f), headRadius = 0.052f, neck = P2(0.24f, 0.29f),
                    lShoulder = P2(0.16f, 0.31f), rShoulder = P2(0.32f, 0.31f),
                    lElbow = P2(0.13f, 0.44f), rElbow = P2(0.35f, 0.43f),
                    lWrist = P2(0.14f, 0.55f), rWrist = P2(0.37f, 0.53f),
                    lHandTip = P2(0.14f, 0.58f), rHandTip = P2(0.38f, 0.56f),
                    lHip = P2(0.18f, 0.55f), rHip = P2(0.29f, 0.55f),
                    lKnee = P2(0.18f, 0.72f), rKnee = P2(0.29f, 0.72f),
                    lAnkle = P2(0.18f, 0.89f), rAnkle = P2(0.29f, 0.89f),
                    lFootTip = P2(0.14f, 0.91f), rFootTip = P2(0.33f, 0.91f)
                )
            )
            // Center person
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.50f, 0.16f), headRadius = 0.055f, neck = P2(0.50f, 0.24f),
                    lShoulder = P2(0.41f, 0.26f), rShoulder = P2(0.59f, 0.26f),
                    lElbow = P2(0.38f, 0.40f), rElbow = P2(0.62f, 0.40f),
                    lWrist = P2(0.39f, 0.52f), rWrist = P2(0.61f, 0.52f),
                    lHandTip = P2(0.39f, 0.55f), rHandTip = P2(0.61f, 0.55f),
                    lHip = P2(0.44f, 0.52f), rHip = P2(0.56f, 0.52f),
                    lKnee = P2(0.44f, 0.71f), rKnee = P2(0.56f, 0.71f),
                    lAnkle = P2(0.44f, 0.89f), rAnkle = P2(0.56f, 0.89f),
                    lFootTip = P2(0.40f, 0.91f), rFootTip = P2(0.60f, 0.91f)
                )
            )
            // Right person
            drawFigure(
                HumanFigureSpec(
                    headCenter = P2(0.76f, 0.21f), headRadius = 0.052f, neck = P2(0.76f, 0.29f),
                    lShoulder = P2(0.68f, 0.31f), rShoulder = P2(0.84f, 0.31f),
                    lElbow = P2(0.65f, 0.43f), rElbow = P2(0.87f, 0.44f),
                    lWrist = P2(0.63f, 0.53f), rWrist = P2(0.86f, 0.55f),
                    lHandTip = P2(0.62f, 0.56f), rHandTip = P2(0.86f, 0.58f),
                    lHip = P2(0.71f, 0.55f), rHip = P2(0.82f, 0.55f),
                    lKnee = P2(0.71f, 0.72f), rKnee = P2(0.82f, 0.72f),
                    lAnkle = P2(0.71f, 0.89f), rAnkle = P2(0.82f, 0.89f),
                    lFootTip = P2(0.67f, 0.91f), rFootTip = P2(0.86f, 0.91f)
                )
            )
        }
    }
}

/**
 * Composites the selected white-line pose outline onto a captured JPEG file ONLY when
 * the user explicitly enables "Save Photo With Guide".
 */
fun compositeGuideOntoPhotoFile(
    sourcePath: String,
    destPath: String,
    outlineType: PoseOutlineType,
    scale: Float,
    verticalOffsetFraction: Float,
    alpha: Float
) {
    val rawBitmap = BitmapFactory.decodeFile(sourcePath) ?: return
    val rotated = applyExifOrientation(rawBitmap, sourcePath)
    val mutable = rotated.copy(Bitmap.Config.ARGB_8888, true)
    if (rotated !== rawBitmap) rotated.recycle()
    rawBitmap.recycle()

    val canvas = android.graphics.Canvas(mutable)
    val w = mutable.width.toFloat()
    val h = mutable.height.toFloat()
    val boxSize = min(w, h) * 0.72f * scale.coerceIn(0.55f, 1.45f)
    val left = (w - boxSize) / 2f
    val top = (h - boxSize) / 2f + (h * verticalOffsetFraction)

    val strokePx = (min(w, h) * 0.0065f).coerceIn(4f, 14f)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        this.alpha = (alpha.coerceIn(0.15f, 1f) * 255).toInt()
        style = Paint.Style.STROKE
        strokeWidth = strokePx
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun px(nx: Float) = left + nx * boxSize
    fun py(ny: Float) = top + ny * boxSize

    fun drawAndroidFigure(spec: HumanFigureSpec) {
        canvas.drawCircle(px(spec.headCenter.x), py(spec.headCenter.y), spec.headRadius * boxSize, paint)
        canvas.drawLine(px(spec.headCenter.x), py(spec.headCenter.y + spec.headRadius), px(spec.neck.x), py(spec.neck.y), paint)
        val torsoPath = android.graphics.Path().apply {
            moveTo(px(spec.lShoulder.x), py(spec.lShoulder.y))
            lineTo(px(spec.rShoulder.x), py(spec.rShoulder.y))
            lineTo(px(spec.rHip.x), py(spec.rHip.y))
            lineTo(px(spec.lHip.x), py(spec.lHip.y))
            close()
        }
        canvas.drawPath(torsoPath, paint)
        // Arms
        canvas.drawLine(px(spec.lShoulder.x), py(spec.lShoulder.y), px(spec.lElbow.x), py(spec.lElbow.y), paint)
        canvas.drawLine(px(spec.lElbow.x), py(spec.lElbow.y), px(spec.lWrist.x), py(spec.lWrist.y), paint)
        canvas.drawLine(px(spec.lWrist.x), py(spec.lWrist.y), px(spec.lHandTip.x), py(spec.lHandTip.y), paint)
        canvas.drawLine(px(spec.rShoulder.x), py(spec.rShoulder.y), px(spec.rElbow.x), py(spec.rElbow.y), paint)
        canvas.drawLine(px(spec.rElbow.x), py(spec.rElbow.y), px(spec.rWrist.x), py(spec.rWrist.y), paint)
        canvas.drawLine(px(spec.rWrist.x), py(spec.rWrist.y), px(spec.rHandTip.x), py(spec.rHandTip.y), paint)
        // Legs
        canvas.drawLine(px(spec.lHip.x), py(spec.lHip.y), px(spec.lKnee.x), py(spec.lKnee.y), paint)
        canvas.drawLine(px(spec.lKnee.x), py(spec.lKnee.y), px(spec.lAnkle.x), py(spec.lAnkle.y), paint)
        canvas.drawLine(px(spec.lAnkle.x), py(spec.lAnkle.y), px(spec.lFootTip.x), py(spec.lFootTip.y), paint)
        canvas.drawLine(px(spec.rHip.x), py(spec.rHip.y), px(spec.rKnee.x), py(spec.rKnee.y), paint)
        canvas.drawLine(px(spec.rKnee.x), py(spec.rKnee.y), px(spec.rAnkle.x), py(spec.rAnkle.y), paint)
        canvas.drawLine(px(spec.rAnkle.x), py(spec.rAnkle.y), px(spec.rFootTip.x), py(spec.rFootTip.y), paint)
    }

    when (outlineType) {
        PoseOutlineType.RELAXED_CAFE_SITTING -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.42f, 0.16f), 0.068f, P2(0.42f, 0.26f),
                    P2(0.35f, 0.28f), P2(0.49f, 0.28f),
                    P2(0.33f, 0.42f), P2(0.55f, 0.40f),
                    P2(0.44f, 0.50f), P2(0.62f, 0.50f),
                    P2(0.48f, 0.52f), P2(0.65f, 0.52f),
                    P2(0.36f, 0.58f), P2(0.48f, 0.58f),
                    P2(0.62f, 0.60f), P2(0.67f, 0.63f),
                    P2(0.63f, 0.86f), P2(0.71f, 0.87f),
                    P2(0.70f, 0.89f), P2(0.78f, 0.89f)
                )
            )
        }
        PoseOutlineType.HANDS_ON_WAIST -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.50f, 0.12f), 0.068f, P2(0.50f, 0.22f),
                    P2(0.40f, 0.24f), P2(0.60f, 0.24f),
                    P2(0.28f, 0.37f), P2(0.72f, 0.37f),
                    P2(0.41f, 0.48f), P2(0.59f, 0.48f),
                    P2(0.44f, 0.50f), P2(0.56f, 0.50f),
                    P2(0.43f, 0.52f), P2(0.57f, 0.52f),
                    P2(0.42f, 0.71f), P2(0.58f, 0.71f),
                    P2(0.41f, 0.89f), P2(0.59f, 0.89f),
                    P2(0.36f, 0.91f), P2(0.64f, 0.91f)
                )
            )
        }
        PoseOutlineType.WALKING_POSE -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.52f, 0.12f), 0.066f, P2(0.51f, 0.22f),
                    P2(0.42f, 0.24f), P2(0.60f, 0.25f),
                    P2(0.32f, 0.37f), P2(0.68f, 0.38f),
                    P2(0.27f, 0.49f), P2(0.72f, 0.49f),
                    P2(0.25f, 0.53f), P2(0.74f, 0.53f),
                    P2(0.44f, 0.52f), P2(0.56f, 0.52f),
                    P2(0.36f, 0.70f), P2(0.64f, 0.70f),
                    P2(0.28f, 0.89f), P2(0.71f, 0.88f),
                    P2(0.22f, 0.90f), P2(0.77f, 0.90f)
                )
            )
        }
        PoseOutlineType.MIRROR_SELFIE -> {
            canvas.drawRoundRect(
                RectF(px(0.18f), py(0.05f), px(0.82f), py(0.95f)),
                boxSize * 0.26f, boxSize * 0.26f, paint
            )
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.47f, 0.18f), 0.062f, P2(0.47f, 0.27f),
                    P2(0.38f, 0.29f), P2(0.56f, 0.29f),
                    P2(0.34f, 0.43f), P2(0.64f, 0.42f),
                    P2(0.36f, 0.54f), P2(0.58f, 0.35f),
                    P2(0.37f, 0.58f), P2(0.58f, 0.33f),
                    P2(0.40f, 0.55f), P2(0.53f, 0.55f),
                    P2(0.39f, 0.72f), P2(0.54f, 0.72f),
                    P2(0.38f, 0.89f), P2(0.56f, 0.89f),
                    P2(0.34f, 0.91f), P2(0.60f, 0.91f)
                )
            )
        }
        PoseOutlineType.GROUP_POSE -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.24f, 0.21f), 0.052f, P2(0.24f, 0.29f),
                    P2(0.16f, 0.31f), P2(0.32f, 0.31f),
                    P2(0.13f, 0.44f), P2(0.35f, 0.43f),
                    P2(0.14f, 0.55f), P2(0.37f, 0.53f),
                    P2(0.14f, 0.58f), P2(0.38f, 0.56f),
                    P2(0.18f, 0.55f), P2(0.29f, 0.55f),
                    P2(0.18f, 0.72f), P2(0.29f, 0.72f),
                    P2(0.18f, 0.89f), P2(0.29f, 0.89f),
                    P2(0.14f, 0.91f), P2(0.33f, 0.91f)
                )
            )
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.50f, 0.16f), 0.055f, P2(0.50f, 0.24f),
                    P2(0.41f, 0.26f), P2(0.59f, 0.26f),
                    P2(0.38f, 0.40f), P2(0.62f, 0.40f),
                    P2(0.39f, 0.52f), P2(0.61f, 0.52f),
                    P2(0.39f, 0.55f), P2(0.61f, 0.55f),
                    P2(0.44f, 0.52f), P2(0.56f, 0.52f),
                    P2(0.44f, 0.71f), P2(0.56f, 0.71f),
                    P2(0.44f, 0.89f), P2(0.56f, 0.89f),
                    P2(0.40f, 0.91f), P2(0.60f, 0.91f)
                )
            )
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.76f, 0.21f), 0.052f, P2(0.76f, 0.29f),
                    P2(0.68f, 0.31f), P2(0.84f, 0.31f),
                    P2(0.65f, 0.43f), P2(0.87f, 0.44f),
                    P2(0.63f, 0.53f), P2(0.86f, 0.55f),
                    P2(0.62f, 0.56f), P2(0.86f, 0.58f),
                    P2(0.71f, 0.55f), P2(0.82f, 0.55f),
                    P2(0.71f, 0.72f), P2(0.82f, 0.72f),
                    P2(0.71f, 0.89f), P2(0.82f, 0.89f),
                    P2(0.67f, 0.91f), P2(0.86f, 0.91f)
                )
            )
        }
        PoseOutlineType.LOOKING_OVER_SHOULDER -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.45f, 0.13f), 0.068f, P2(0.48f, 0.23f),
                    P2(0.39f, 0.26f), P2(0.61f, 0.23f),
                    P2(0.35f, 0.41f), P2(0.65f, 0.38f),
                    P2(0.37f, 0.53f), P2(0.62f, 0.51f),
                    P2(0.38f, 0.57f), P2(0.62f, 0.55f),
                    P2(0.44f, 0.53f), P2(0.57f, 0.52f),
                    P2(0.43f, 0.72f), P2(0.58f, 0.71f),
                    P2(0.42f, 0.90f), P2(0.59f, 0.89f),
                    P2(0.37f, 0.91f), P2(0.64f, 0.91f)
                )
            )
        }
        PoseOutlineType.CUTE_SITTING_POSE -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.46f, 0.22f), 0.068f, P2(0.46f, 0.32f),
                    P2(0.38f, 0.34f), P2(0.54f, 0.34f),
                    P2(0.33f, 0.47f), P2(0.61f, 0.47f),
                    P2(0.43f, 0.58f), P2(0.54f, 0.58f),
                    P2(0.46f, 0.60f), P2(0.52f, 0.60f),
                    P2(0.39f, 0.62f), P2(0.52f, 0.62f),
                    P2(0.59f, 0.56f), P2(0.65f, 0.63f),
                    P2(0.56f, 0.82f), P2(0.68f, 0.83f),
                    P2(0.62f, 0.85f), P2(0.74f, 0.85f)
                )
            )
        }
        PoseOutlineType.SIDE_PROFILE -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.50f, 0.12f), 0.066f, P2(0.48f, 0.22f),
                    P2(0.45f, 0.24f), P2(0.53f, 0.24f),
                    P2(0.43f, 0.39f), P2(0.52f, 0.39f),
                    P2(0.46f, 0.52f), P2(0.54f, 0.52f),
                    P2(0.47f, 0.56f), P2(0.55f, 0.56f),
                    P2(0.46f, 0.52f), P2(0.52f, 0.52f),
                    P2(0.45f, 0.71f), P2(0.53f, 0.71f),
                    P2(0.44f, 0.89f), P2(0.53f, 0.89f),
                    P2(0.50f, 0.91f), P2(0.60f, 0.91f)
                )
            )
        }
        PoseOutlineType.CASUAL_LEAN -> {
            canvas.drawLine(px(0.28f), py(0.06f), px(0.28f), py(0.92f), paint)
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.42f, 0.13f), 0.066f, P2(0.40f, 0.23f),
                    P2(0.31f, 0.25f), P2(0.49f, 0.27f),
                    P2(0.30f, 0.40f), P2(0.55f, 0.41f),
                    P2(0.36f, 0.51f), P2(0.50f, 0.52f),
                    P2(0.38f, 0.54f), P2(0.51f, 0.55f),
                    P2(0.35f, 0.53f), P2(0.48f, 0.53f),
                    P2(0.44f, 0.72f), P2(0.52f, 0.71f),
                    P2(0.46f, 0.89f), P2(0.57f, 0.89f),
                    P2(0.52f, 0.91f), P2(0.63f, 0.91f)
                )
            )
        }
        PoseOutlineType.STANDING_PORTRAIT -> {
            drawAndroidFigure(
                HumanFigureSpec(
                    P2(0.50f, 0.12f), 0.068f, P2(0.50f, 0.22f),
                    P2(0.40f, 0.24f), P2(0.60f, 0.24f),
                    P2(0.35f, 0.39f), P2(0.65f, 0.39f),
                    P2(0.34f, 0.52f), P2(0.66f, 0.52f),
                    P2(0.34f, 0.56f), P2(0.66f, 0.56f),
                    P2(0.43f, 0.52f), P2(0.57f, 0.52f),
                    P2(0.43f, 0.71f), P2(0.57f, 0.71f),
                    P2(0.42f, 0.89f), P2(0.58f, 0.89f),
                    P2(0.37f, 0.91f), P2(0.63f, 0.91f)
                )
            )
        }
    }

    java.io.FileOutputStream(destPath).use { out ->
        mutable.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }
    mutable.recycle()
}

private fun applyExifOrientation(bitmap: Bitmap, path: String): Bitmap {
    val orientation = runCatching {
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
