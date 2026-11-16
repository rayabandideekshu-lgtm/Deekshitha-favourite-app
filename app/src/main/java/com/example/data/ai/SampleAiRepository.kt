package com.example.data.ai

import android.content.Context
import com.example.R
import com.example.data.model.CaptionResult
import com.example.data.model.PoseCategory
import com.example.data.model.PoseDifficulty
import com.example.data.model.PoseInspirationBatch
import com.example.data.model.PoseOutlineType
import com.example.data.model.PoseSuggestion
import kotlinx.coroutines.delay

/**
 * Offline Sample AI implementation used when Firebase AI Logic is not enabled.
 * Provides distinct pose suggestions mapped to all 10 white-line pose outlines
 * and structured 3-line captions using only user-supplied details.
 */
class SampleAiRepository(private val context: Context) : AiRepository {

    private val noticeText: String
        get() = context.getString(R.string.sample_ai_notice)

    override suspend fun suggestPoses(
        category: PoseCategory,
        batchIndex: Int,
        hasCameraFrame: Boolean
    ): PoseInspirationBatch {
        delay(180)
        val pool = posesForCategory(category)
        val start = (batchIndex.coerceAtLeast(0) * 3) % pool.size
        val selected = (0 until 3).map { offset ->
            pool[(start + offset) % pool.size]
        }
        val frameNote = if (hasCameraFrame) {
            "Sample pose ideas for ${category.label} (offline guide). $noticeText"
        } else {
            noticeText
        }
        return PoseInspirationBatch(
            suggestions = selected,
            isSampleMode = true,
            notice = frameNote
        )
    }

    override suspend fun suggestVideoPoses(
        category: PoseCategory,
        batchIndex: Int
    ): PoseInspirationBatch {
        delay(180)
        val pool = videoPosesForCategory(category)
        val start = (batchIndex.coerceAtLeast(0) * 3) % pool.size
        val selected = (0 until 3).map { offset ->
            pool[(start + offset) % pool.size]
        }
        return PoseInspirationBatch(
            suggestions = selected,
            isSampleMode = true,
            notice = "Video movement poses for ${category.label} • $noticeText"
        )
    }

    override suspend fun generateThreeLineCaption(
        place: String,
        dateTimeFormatted: String,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String
    ): CaptionResult {
        delay(220)
        val cleanPlace = place.trim()
        val cleanDate = dateTimeFormatted.trim()
        val cleanOutfit = outfit.trim()
        val cleanGoodOne = goodThingOne.trim()
        val cleanGoodTwo = goodThingTwo.trim()

        val line1 = when {
            cleanPlace.isNotEmpty() && cleanDate.isNotEmpty() ->
                "Captured at $cleanPlace on $cleanDate."
            cleanPlace.isNotEmpty() ->
                "Captured at $cleanPlace."
            cleanDate.isNotEmpty() ->
                "Captured on $cleanDate."
            else ->
                "A quiet moment kept in time."
        }

        val line2 = if (cleanOutfit.isNotEmpty()) {
            "Wearing $cleanOutfit."
        } else {
            "Dressed simply for the moment."
        }

        val line3 = when {
            cleanGoodOne.isNotEmpty() && cleanGoodTwo.isNotEmpty() ->
                "Grateful today for $cleanGoodOne, and $cleanGoodTwo."
            cleanGoodOne.isNotEmpty() ->
                "A happy note from today: $cleanGoodOne."
            cleanGoodTwo.isNotEmpty() ->
                "A happy note from today: $cleanGoodTwo."
            else ->
                "Holding onto today's small joys."
        }

        return CaptionResult(
            threeLineCaption = "$line1\n$line2\n$line3",
            isSampleMode = true,
            notice = noticeText
        )
    }

    private fun posesForCategory(category: PoseCategory): List<PoseSuggestion> {
        return when (category) {
            PoseCategory.WOMEN -> listOf(
                PoseSuggestion(
                    id = "w1",
                    title = "Relaxed Café Sitting",
                    instruction = "Sit comfortably on the chair, keep your shoulders relaxed and place your hands naturally.",
                    framingTip = "Angle your chair 45 degrees toward soft window light.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
                ),
                PoseSuggestion(
                    id = "w2",
                    title = "Hands on Waist",
                    instruction = "Stand tall with a soft smile, rest your hands lightly on your waist, and shift weight to one foot.",
                    framingTip = "Keep elbows slightly back so your silhouette feels airy.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.HANDS_ON_WAIST
                ),
                PoseSuggestion(
                    id = "w3",
                    title = "Looking Over Shoulder",
                    instruction = "Turn slightly away from the camera and glance warmly back over one shoulder.",
                    framingTip = "Relax your chin slightly down and leave comfortable headroom.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.LOOKING_OVER_SHOULDER
                ),
                PoseSuggestion(
                    id = "w4",
                    title = "Walking Pose",
                    instruction = "Take two gentle steps forward while swinging your arms naturally and looking softly ahead.",
                    framingTip = "Match your stride inside the white leg guide.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.WALKING_POSE
                ),
                PoseSuggestion(
                    id = "w5",
                    title = "Mirror Selfie",
                    instruction = "Hold your phone near chest level inside the mirror outline and tilt slightly for balance.",
                    framingTip = "Keep your posture upright and the background uncluttered.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.MIRROR_SELFIE
                ),
                PoseSuggestion(
                    id = "w6",
                    title = "Cute Sitting Pose",
                    instruction = "Sit with knees tucked slightly to one side, hands folded softly in your lap, and a warm smile.",
                    framingTip = "Great for benches, steps, or cozy picnic spots.",
                    category = category,
                    difficulty = PoseDifficulty.CREATIVE,
                    outlineType = PoseOutlineType.CUTE_SITTING_POSE
                )
            )

            PoseCategory.MEN -> listOf(
                PoseSuggestion(
                    id = "m1",
                    title = "Standing Portrait",
                    instruction = "Stand comfortably facing the lens with relaxed shoulders and arms hanging naturally at your sides.",
                    framingTip = "Align your shoulders evenly with the white outline.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.STANDING_PORTRAIT
                ),
                PoseSuggestion(
                    id = "m2",
                    title = "Casual Lean",
                    instruction = "Lean one shoulder lightly against a wall or railing, cross one ankle casually, and relax your hands.",
                    framingTip = "Works effortlessly along brick walls, cafés, or doorways.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.CASUAL_LEAN
                ),
                PoseSuggestion(
                    id = "m3",
                    title = "Walking Pose",
                    instruction = "Walk forward at a calm, natural pace while keeping your gaze steady.",
                    framingTip = "Give yourself a full stride inside the lower outline.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.WALKING_POSE
                ),
                PoseSuggestion(
                    id = "m4",
                    title = "Relaxed Café Sitting",
                    instruction = "Sit comfortably on a chair or bench, rest forearms near your lap, and share a calm smile.",
                    framingTip = "Keep the camera near eye level.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
                ),
                PoseSuggestion(
                    id = "m5",
                    title = "Side Profile",
                    instruction = "Turn sideways to trace a clean profile toward window light or an outdoor view.",
                    framingTip = "Keep your chin level and posture open.",
                    category = category,
                    difficulty = PoseDifficulty.CREATIVE,
                    outlineType = PoseOutlineType.SIDE_PROFILE
                ),
                PoseSuggestion(
                    id = "m6",
                    title = "Mirror Selfie",
                    instruction = "Stand centered in the mirror outline with your phone steady at chest height.",
                    framingTip = "Clean vertical lines make minimal mirror shots shine.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.MIRROR_SELFIE
                )
            )

            PoseCategory.CHILDREN -> listOf(
                PoseSuggestion(
                    id = "c1",
                    title = "Standing Portrait",
                    instruction = "Stand happily inside the white outline with a big cheerful smile or gentle wave.",
                    framingTip = "Lower the camera to the child's eye level for warmth.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.STANDING_PORTRAIT
                ),
                PoseSuggestion(
                    id = "c2",
                    title = "Cute Sitting Pose",
                    instruction = "Sit comfortably on a picnic blanket or step holding a storybook, flower, or favorite toy.",
                    framingTip = "Keep knees relaxed and capture bright curiosity.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.CUTE_SITTING_POSE
                ),
                PoseSuggestion(
                    id = "c3",
                    title = "Walking Pose",
                    instruction = "Take a cheerful walk along a safe path while smiling toward the camera.",
                    framingTip = "Use fast daylight to keep playful motion crisp.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.WALKING_POSE
                ),
                PoseSuggestion(
                    id = "c4",
                    title = "Hands on Waist",
                    instruction = "Strike a proud superhero stance with hands on waist and a joyful laugh.",
                    framingTip = "Count one-two-three together for genuine smiles.",
                    category = category,
                    difficulty = PoseDifficulty.CREATIVE,
                    outlineType = PoseOutlineType.HANDS_ON_WAIST
                ),
                PoseSuggestion(
                    id = "c5",
                    title = "Looking Over Shoulder",
                    instruction = "Look cheerfully back over your shoulder before turning around.",
                    framingTip = "Great for bright sunny parks and gardens.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.LOOKING_OVER_SHOULDER
                ),
                PoseSuggestion(
                    id = "c6",
                    title = "Group Pose",
                    instruction = "Stand side-by-side with siblings or friends inside the matching outlines.",
                    framingTip = "Leave breathing room above everyone's heads.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.GROUP_POSE
                )
            )

            PoseCategory.COUPLES -> listOf(
                PoseSuggestion(
                    id = "cp1",
                    title = "Group Pose",
                    instruction = "Stand close together inside the side-by-side silhouettes and lean shoulders gently inward.",
                    framingTip = "Center both people evenly inside the white lines.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.GROUP_POSE
                ),
                PoseSuggestion(
                    id = "cp2",
                    title = "Walking Pose",
                    instruction = "Walk forward together at a relaxed pace while chatting and smiling.",
                    framingTip = "Keep both footsteps aligned cleanly.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.WALKING_POSE
                ),
                PoseSuggestion(
                    id = "cp3",
                    title = "Relaxed Café Sitting",
                    instruction = "Sit side by side on a bench or café chairs angled slightly toward one another.",
                    framingTip = "Capture a cozy medium distance shot.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
                ),
                PoseSuggestion(
                    id = "cp4",
                    title = "Looking Over Shoulder",
                    instruction = "Take two steps away and glance back together over your shoulders.",
                    framingTip = "Ideal for scenic pathways and sunset light.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.LOOKING_OVER_SHOULDER
                ),
                PoseSuggestion(
                    id = "cp5",
                    title = "Casual Lean",
                    instruction = "Lean comfortably together against a railing or wall while sharing a laugh.",
                    framingTip = "Natural expressions always outshine stiff poses.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.CASUAL_LEAN
                ),
                PoseSuggestion(
                    id = "cp6",
                    title = "Mirror Selfie",
                    instruction = "Stand close in front of a mirror inside the frame and capture a warm reflection.",
                    framingTip = "Hold the phone steady at chest height.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.MIRROR_SELFIE
                )
            )

            PoseCategory.FRIENDS, PoseCategory.FAMILY, PoseCategory.GROUPS -> listOf(
                PoseSuggestion(
                    id = "g1",
                    title = "Group Pose",
                    instruction = "Align each person with one of the human silhouettes so every face stays clearly visible.",
                    framingTip = "Step back slightly or scale the outline down to fit everyone.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.GROUP_POSE
                ),
                PoseSuggestion(
                    id = "g2",
                    title = "Walking Pose",
                    instruction = "Walk together side-by-side along the path while talking and laughing naturally.",
                    framingTip = "Use the 5s or 10s timer for effortless group movement.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.WALKING_POSE
                ),
                PoseSuggestion(
                    id = "g3",
                    title = "Cute Sitting Pose",
                    instruction = "Gather seated on steps, a blanket, or a bench angled warmly toward the camera.",
                    framingTip = "Keep everyone close enough to fill the frame warmly.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.CUTE_SITTING_POSE
                ),
                PoseSuggestion(
                    id = "g4",
                    title = "Standing Portrait",
                    instruction = "Stand tall together shoulder-to-shoulder with relaxed arms and bright smiles.",
                    framingTip = "Center the group inside the guide before counting down.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.STANDING_PORTRAIT
                ),
                PoseSuggestion(
                    id = "g5",
                    title = "Mirror Selfie",
                    instruction = "Gather cleanly inside the mirror outline and raise the camera to chest level.",
                    framingTip = "Check corners so nobody is cropped at the edge.",
                    category = category,
                    difficulty = PoseDifficulty.CREATIVE,
                    outlineType = PoseOutlineType.MIRROR_SELFIE
                ),
                PoseSuggestion(
                    id = "g6",
                    title = "Looking Over Shoulder",
                    instruction = "Half-turn away together and glance warmly back toward the lens on three.",
                    framingTip = "Creates a lively, editorial keepsake photo.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.LOOKING_OVER_SHOULDER
                )
            )

            PoseCategory.SOLO_PORTRAITS -> listOf(
                PoseSuggestion(
                    id = "s1",
                    title = "Standing Portrait",
                    instruction = "Stand naturally in the center of the outline with soft shoulders and a warm, relaxed smile.",
                    framingTip = "Face soft daylight for gentle catchlights.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.STANDING_PORTRAIT
                ),
                PoseSuggestion(
                    id = "s2",
                    title = "Relaxed Café Sitting",
                    instruction = "Sit comfortably on a chair, keep your shoulders relaxed and place your hands naturally.",
                    framingTip = "Matches classic café window seating.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
                ),
                PoseSuggestion(
                    id = "s3",
                    title = "Side Profile",
                    instruction = "Turn gracefully to the side so your silhouette traces a clean, peaceful profile.",
                    framingTip = "Look gently toward a window, garden, or sky.",
                    category = category,
                    difficulty = PoseDifficulty.CREATIVE,
                    outlineType = PoseOutlineType.SIDE_PROFILE
                ),
                PoseSuggestion(
                    id = "s4",
                    title = "Casual Lean",
                    instruction = "Rest lightly against a wall or doorframe with relaxed hands and angled posture.",
                    framingTip = "Creates natural depth along architectural lines.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.CASUAL_LEAN
                ),
                PoseSuggestion(
                    id = "s5",
                    title = "Hands on Waist",
                    instruction = "Place hands confidently yet softly on your waist for a crisp full-body portrait.",
                    framingTip = "Keep feet grounded inside the lower markers.",
                    category = category,
                    difficulty = PoseDifficulty.EASY,
                    outlineType = PoseOutlineType.HANDS_ON_WAIST
                ),
                PoseSuggestion(
                    id = "s6",
                    title = "Looking Over Shoulder",
                    instruction = "Turn slightly away and glance softly back over your shoulder.",
                    framingTip = "Highlights outfit details and natural posture.",
                    category = category,
                    difficulty = PoseDifficulty.MEDIUM,
                    outlineType = PoseOutlineType.LOOKING_OVER_SHOULDER
                )
            )
        }
    }

    private fun videoPosesForCategory(category: PoseCategory): List<PoseSuggestion> {
        val base = posesForCategory(category)
        val motionFocus = listOf(
            PoseSuggestion(
                id = "vid_walk_${category.name}",
                title = "Walking Pose (Video)",
                instruction = "Start standing inside the outline, then take 4 smooth, natural steps forward while smiling.",
                framingTip = "Toggle VIDEO POSE GUIDE off anytime mid-recording without stopping the clip.",
                category = category,
                difficulty = PoseDifficulty.EASY,
                outlineType = PoseOutlineType.WALKING_POSE
            ),
            PoseSuggestion(
                id = "vid_turn_${category.name}",
                title = "Looking Over Shoulder (Video)",
                instruction = "Begin facing slightly away, pause for a beat, and turn slowly over your shoulder toward the lens.",
                framingTip = "Slow turns feel dreamy and cinematic in short video memories.",
                category = category,
                difficulty = PoseDifficulty.MEDIUM,
                outlineType = PoseOutlineType.LOOKING_OVER_SHOULDER
            ),
            PoseSuggestion(
                id = "vid_cafe_${category.name}",
                title = "Relaxed Café Sitting (Video)",
                instruction = "Settle comfortably into your seat, lift a cup or wave gently, and share a natural smile.",
                framingTip = "Keep movements gentle and centered in the guide.",
                category = category,
                difficulty = PoseDifficulty.EASY,
                outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
            )
        )
        return motionFocus + base
    }
}
