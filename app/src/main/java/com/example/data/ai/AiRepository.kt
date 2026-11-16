package com.example.data.ai

import com.example.data.model.CaptionResult
import com.example.data.model.PoseCategory
import com.example.data.model.PoseInspirationBatch

interface AiRepository {
    suspend fun suggestPoses(
        category: PoseCategory,
        batchIndex: Int,
        hasCameraFrame: Boolean = false
    ): PoseInspirationBatch

    suspend fun suggestVideoPoses(
        category: PoseCategory,
        batchIndex: Int
    ): PoseInspirationBatch

    suspend fun generateThreeLineCaption(
        place: String,
        dateTimeFormatted: String,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String
    ): CaptionResult
}
