package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary

/**
 * Facial emotion classification based on facial landmarks and MediaPipe blendshapes.
 */
enum class FaceEmotion(
  val label: String,
  val icon: ImageVector,
  val color: Color,
  val fallbackEmoji: String
) {
  HAPPY("Joie", Icons.Default.SentimentVerySatisfied, AmberAccent, "😄"),
  SURPRISED("Surprise", Icons.Default.Mood, CyanPrimary, "😮"),
  WINK("Complice", Icons.Default.Visibility, AccentPink, "😉"),
  SERENE("Sérénité", Icons.Default.SentimentSatisfied, MintSecondary, "😌"),
  FOCUSED("Concentré", Icons.Default.Face, Color(0xFF818CF8), "🤔"),
  NEUTRAL("Neutre", Icons.Default.Face, Color(0xFF94A3B8), "😐")
}

/**
 * 2D coordinate point for facial landmarks.
 */
data class FacePoint(
  val x: Float,
  val y: Float
)

/**
 * 3D normalized coordinate point (MediaPipe 478-point face mesh).
 * Coordinates are normalized in [0, 1] relative to image dimensions.
 */
data class FacePoint3D(
  val x: Float,
  val y: Float,
  val z: Float = 0f
)

/**
 * Extracted face data for AR geometric 3D mesh (478 vertices) and emotion computation.
 */
data class FaceOverlayData(
  val hasFace: Boolean = false,
  val boundingBox: android.graphics.Rect? = null,
  val emotion: FaceEmotion = FaceEmotion.NEUTRAL,
  val confidence: Float = 0f,
  val smileProb: Float = 0f,
  val leftEyeOpenProb: Float = 0f,
  val rightEyeOpenProb: Float = 0f,
  val jawOpenScore: Float = 0f,
  val headEulerY: Float = 0f,
  val headEulerZ: Float = 0f,
  // 478 MediaPipe 3D normalized landmarks
  val allLandmarks3D: List<FacePoint3D> = emptyList(),
  // 52 Blendshapes dictionary (ARKit standard)
  val blendshapes: Map<String, Float> = emptyMap(),
  // Key facial contour points for rendering
  val faceContours: List<FacePoint> = emptyList(),
  val leftEyeContours: List<FacePoint> = emptyList(),
  val rightEyeContours: List<FacePoint> = emptyList(),
  val noseBridgeContours: List<FacePoint> = emptyList(),
  val upperLipContours: List<FacePoint> = emptyList(),
  val lowerLipContours: List<FacePoint> = emptyList(),
  val leftCheek: FacePoint? = null,
  val rightCheek: FacePoint? = null,
  val noseTip: FacePoint? = null,
  val imageWidth: Int = 1,
  val imageHeight: Int = 1,
  val rotationDegrees: Int = 0,
  val isFrontCamera: Boolean = true
)

/**
 * Particle entity for floating on-screen emotion reactions.
 */
data class FloatingReactionItem(
  val id: Long,
  val emotion: FaceEmotion,
  val xFraction: Float, // Normalized 0..1 position on screen width
  val startYFraction: Float = 0.85f,
  val driftXFraction: Float = 0f,
  val scale: Float = 1f,
  val alpha: Float = 1f
)
