package com.example.ui.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.model.AlignmentGuidance
import com.example.data.model.PoseOutlineType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs

/**
 * On-device ML Kit Pose Detection analyzer.
 * Compares detected shoulders, elbows, wrists, hips, knees, and ankles against the
 * selected pose outline without storing frames or guessing identity, gender, or age.
 */
class MlKitPoseAnalyzer(
    private val isEnabledProvider: () -> Boolean,
    private val isFrontCameraProvider: () -> Boolean,
    private val outlineTypeProvider: () -> PoseOutlineType,
    private val scaleProvider: () -> Float,
    private val onGuidance: (AlignmentGuidance) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector by lazy {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    private var lastProcessedTimeMs = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isEnabledProvider()) {
            imageProxy.close()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastProcessedTimeMs < 220L) {
            imageProxy.close()
            return
        }
        lastProcessedTimeMs = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val uprightWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val uprightHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                val guidance = evaluatePoseAlignment(
                    pose = pose,
                    frameWidth = uprightWidth.toFloat().coerceAtLeast(1f),
                    frameHeight = uprightHeight.toFloat().coerceAtLeast(1f),
                    isFrontCamera = isFrontCameraProvider(),
                    outlineType = outlineTypeProvider(),
                    targetScale = scaleProvider()
                )
                onGuidance(guidance)
            }
            .addOnFailureListener {
                onGuidance(
                    AlignmentGuidance(
                        message = "Align inside the white pose outline.",
                        isAligned = false,
                        hasDetectedPerson = false
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        runCatching { detector.close() }
    }
}

fun evaluatePoseAlignment(
    pose: Pose,
    frameWidth: Float,
    frameHeight: Float,
    isFrontCamera: Boolean,
    outlineType: PoseOutlineType,
    targetScale: Float
): AlignmentGuidance {
    val lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val lHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
    val rHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
    val lWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
    val rWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
    val lElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val rElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    val lKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
    val rKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
    val lAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    val rAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

    val coreLandmarks = listOfNotNull(lShoulder, rShoulder, lHip, rHip)
    if (coreLandmarks.size < 2) {
        return AlignmentGuidance(
            message = "Step into frame to match ${outlineType.displayName}.",
            isAligned = false,
            hasDetectedPerson = false
        )
    }

    // Normalized horizontal center (mirrored for front camera)
    val rawCenterX = coreLandmarks.map { it.position.x / frameWidth }.average().toFloat()
    val normX = if (isFrontCamera) 1f - rawCenterX else rawCenterX

    // Horizontal positioning check
    if (normX < 0.36f) {
        return AlignmentGuidance(
            message = if (isFrontCamera) "Move slightly to your right." else "Move slightly to your right.",
            isAligned = false,
            hasDetectedPerson = true
        )
    }
    if (normX > 0.64f) {
        return AlignmentGuidance(
            message = if (isFrontCamera) "Move slightly to your left." else "Move slightly to your left.",
            isAligned = false,
            hasDetectedPerson = true
        )
    }

    // Distance / scale check via shoulder width or torso height
    if (lShoulder != null && rShoulder != null) {
        val shoulderSpan = abs(lShoulder.position.x - rShoulder.position.x) / frameWidth
        val expectedMinSpan = 0.14f * targetScale
        val expectedMaxSpan = 0.44f * targetScale
        if (shoulderSpan < expectedMinSpan && outlineType != PoseOutlineType.SIDE_PROFILE) {
            return AlignmentGuidance(
                message = "Move a little closer.",
                isAligned = false,
                hasDetectedPerson = true
            )
        }
        if (shoulderSpan > expectedMaxSpan) {
            return AlignmentGuidance(
                message = "Step back slightly.",
                isAligned = false,
                hasDetectedPerson = true
            )
        }
    }

    // Pose-specific gentle limb check
    if (outlineType == PoseOutlineType.HANDS_ON_WAIST && lWrist != null && lHip != null && lElbow != null) {
        val wristNearHip = abs(lWrist.position.y - lHip.position.y) / frameHeight < 0.14f
        val elbowOut = abs(lElbow.position.x - lHip.position.x) / frameWidth > 0.06f
        if (!wristNearHip || !elbowOut) {
            return AlignmentGuidance(
                message = "Adjust your arm position.",
                isAligned = false,
                hasDetectedPerson = true
            )
        }
    }

    if ((outlineType == PoseOutlineType.RELAXED_CAFE_SITTING || outlineType == PoseOutlineType.CUTE_SITTING_POSE) &&
        lHip != null && lKnee != null
    ) {
        val kneeDrop = abs(lKnee.position.y - lHip.position.y) / frameHeight
        if (kneeDrop > 0.25f) {
            return AlignmentGuidance(
                message = "Take a comfortable seat to match the outline.",
                isAligned = false,
                hasDetectedPerson = true
            )
        }
    }

    if (outlineType == PoseOutlineType.WALKING_POSE && lAnkle != null && rAnkle != null) {
        val strideGap = abs(lAnkle.position.x - rAnkle.position.x) / frameWidth
        if (strideGap < 0.04f) {
            return AlignmentGuidance(
                message = "Take one gentle step forward for the walking pose.",
                isAligned = false,
                hasDetectedPerson = true
            )
        }
    }

    return AlignmentGuidance(
        message = "Perfect! Your pose is aligned.",
        isAligned = true,
        hasDetectedPerson = true
    )
}
