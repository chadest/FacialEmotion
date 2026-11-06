package com.example.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.model.FaceEmotion
import com.example.model.FaceOverlayData
import com.example.model.FacePoint
import com.example.model.FacePoint3D
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX ImageAnalysis analyzer using Google MediaPipe Face Landmarker.
 * Produces a high-density 478-point 3D face mesh and 52 ARKit-grade blendshapes.
 */
class FaceAnalyzer(
  private val context: Context,
  private val isFrontCamera: () -> Boolean,
  private val onFaceDetected: (FaceOverlayData) -> Unit
) : ImageAnalysis.Analyzer {

  companion object {
    // Key landmark indices on the 478 MediaPipe 3D face mesh
    val FACE_OVAL_INDICES = intArrayOf(
      10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378,
      400, 377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21,
      54, 103, 67, 109, 10
    )
    val LEFT_EYE_INDICES = intArrayOf(
      33, 7, 163, 144, 145, 153, 154, 155, 133, 173, 157, 158, 159, 160, 161, 246, 33
    )
    val RIGHT_EYE_INDICES = intArrayOf(
      362, 382, 381, 380, 374, 373, 390, 249, 263, 466, 388, 387, 386, 385, 384, 398, 362
    )
    val NOSE_BRIDGE_INDICES = intArrayOf(
      168, 6, 197, 195, 5, 4, 1, 19, 94, 2
    )
    val LIPS_OUTER_INDICES = intArrayOf(
      61, 146, 91, 181, 84, 17, 314, 405, 321, 375, 291, 308, 324, 318, 402, 317, 14, 87, 178, 88, 95, 61
    )
  }

  private var faceLandmarker: FaceLandmarker? = null
  private val isProcessing = AtomicBoolean(false)

  init {
    initializeMediaPipe()
  }

  private fun initializeMediaPipe() {
    try {
      val baseOptions = BaseOptions.builder()
        .setModelAssetPath("face_landmarker.task")
        .build()

      val options = FaceLandmarker.FaceLandmarkerOptions.builder()
        .setBaseOptions(baseOptions)
        .setOutputFaceBlendshapes(true)
        .setOutputFacialTransformationMatrixes(true)
        .setRunningMode(RunningMode.LIVE_STREAM)
        .setNumFaces(1)
        .setResultListener { result: FaceLandmarkerResult, input: MPImage ->
          handleResult(result, input.width, input.height)
          isProcessing.set(false)
        }
        .setErrorListener { error ->
          error.printStackTrace()
          isProcessing.set(false)
        }
        .build()

      faceLandmarker = FaceLandmarker.createFromOptions(context, options)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    if (isProcessing.get() || faceLandmarker == null) {
      imageProxy.close()
      return
    }

    isProcessing.set(true)
    val frameTime = SystemClock.uptimeMillis()

    try {
      val bitmap = imageProxy.toBitmap()
      val rotationDegrees = imageProxy.imageInfo.rotationDegrees

      val rotatedBitmap = if (rotationDegrees != 0) {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
      } else {
        bitmap
      }

      val mpImage = BitmapImageBuilder(rotatedBitmap).build()
      faceLandmarker?.detectAsync(mpImage, frameTime)
    } catch (e: Exception) {
      e.printStackTrace()
      isProcessing.set(false)
    } finally {
      imageProxy.close()
    }
  }

  /**
   * Processes MediaPipe FaceLandmarkerResult with 478 3D landmarks and blendshapes.
   */
  private fun handleResult(result: FaceLandmarkerResult, width: Int, height: Int) {
    val landmarksList = result.faceLandmarks()
    if (landmarksList.isEmpty()) {
      onFaceDetected(
        FaceOverlayData(
          hasFace = false,
          imageWidth = width,
          imageHeight = height,
          isFrontCamera = isFrontCamera()
        )
      )
      return
    }

    val primaryLandmarks = landmarksList[0]

    // Convert 478 points
    val all3D = primaryLandmarks.map { lm ->
      FacePoint3D(x = lm.x(), y = lm.y(), z = lm.z())
    }

    // Extract blendshapes dictionary
    val blendshapesMap = mutableMapOf<String, Float>()
    val blendshapesOptional = result.faceBlendshapes()
    if (blendshapesOptional.isPresent && blendshapesOptional.get().isNotEmpty()) {
      val categories = blendshapesOptional.get()[0]
      for (cat in categories) {
        blendshapesMap[cat.categoryName()] = cat.score()
      }
    }

    // Derive emotion and confidence from 52 blendshapes
    val (emotion, confidence) = classifyEmotionFromBlendshapes(blendshapesMap)

    // Helper to get pixel coordinates
    fun getPoint(idx: Int): FacePoint {
      val lm = primaryLandmarks.getOrNull(idx) ?: return FacePoint(0f, 0f)
      return FacePoint(lm.x() * width, lm.y() * height)
    }

    val faceContours = FACE_OVAL_INDICES.map { getPoint(it) }
    val leftEyeContours = LEFT_EYE_INDICES.map { getPoint(it) }
    val rightEyeContours = RIGHT_EYE_INDICES.map { getPoint(it) }
    val noseBridgeContours = NOSE_BRIDGE_INDICES.map { getPoint(it) }
    val lipsContours = LIPS_OUTER_INDICES.map { getPoint(it) }

    val leftCheek = getPoint(116)
    val rightCheek = getPoint(345)
    val noseTip = getPoint(1)

    val smileScore = ((blendshapesMap["mouthSmileLeft"] ?: 0f) + (blendshapesMap["mouthSmileRight"] ?: 0f)) / 2f
    val jawOpen = blendshapesMap["jawOpen"] ?: 0f
    val eyeBlinkLeft = blendshapesMap["eyeBlinkLeft"] ?: 0f
    val eyeBlinkRight = blendshapesMap["eyeBlinkRight"] ?: 0f

    onFaceDetected(
      FaceOverlayData(
        hasFace = true,
        emotion = emotion,
        confidence = confidence,
        smileProb = smileScore,
        leftEyeOpenProb = (1f - eyeBlinkLeft).coerceIn(0f, 1f),
        rightEyeOpenProb = (1f - eyeBlinkRight).coerceIn(0f, 1f),
        jawOpenScore = jawOpen,
        allLandmarks3D = all3D,
        blendshapes = blendshapesMap,
        faceContours = faceContours,
        leftEyeContours = leftEyeContours,
        rightEyeContours = rightEyeContours,
        noseBridgeContours = noseBridgeContours,
        upperLipContours = lipsContours.take(lipsContours.size / 2),
        lowerLipContours = lipsContours.drop(lipsContours.size / 2),
        leftCheek = leftCheek,
        rightCheek = rightCheek,
        noseTip = noseTip,
        imageWidth = width,
        imageHeight = height,
        isFrontCamera = isFrontCamera()
      )
    )
  }

  /**
   * High-precision emotion classification using ARKit/MediaPipe blendshape metrics.
   */
  private fun classifyEmotionFromBlendshapes(blendshapes: Map<String, Float>): Pair<FaceEmotion, Float> {
    val smileLeft = blendshapes["mouthSmileLeft"] ?: 0f
    val smileRight = blendshapes["mouthSmileRight"] ?: 0f
    val avgSmile = (smileLeft + smileRight) / 2f

    val blinkLeft = blendshapes["eyeBlinkLeft"] ?: 0f
    val blinkRight = blendshapes["eyeBlinkRight"] ?: 0f

    val jawOpen = blendshapes["jawOpen"] ?: 0f
    val eyeWideLeft = blendshapes["eyeWideLeft"] ?: 0f
    val eyeWideRight = blendshapes["eyeWideRight"] ?: 0f
    val avgEyeWide = (eyeWideLeft + eyeWideRight) / 2f

    val browDownLeft = blendshapes["browDownLeft"] ?: 0f
    val browDownRight = blendshapes["browDownRight"] ?: 0f
    val avgBrowDown = (browDownLeft + browDownRight) / 2f

    // 1. Playful / Wink: one eye blinking while other eye is wide open
    val blinkDiff = kotlin.math.abs(blinkLeft - blinkRight)
    if (blinkDiff > 0.40f && (blinkLeft > 0.50f || blinkRight > 0.50f)) {
      val conf = (blinkDiff * 100f).coerceIn(65f, 98f)
      return FaceEmotion.WINK to conf
    }

    // 2. Happy / Smiling
    if (avgSmile > 0.45f) {
      val conf = (avgSmile * 100f).coerceIn(65f, 99f)
      return FaceEmotion.HAPPY to conf
    }

    // 3. Surprised: jaw dropped + wide open eyes
    if (jawOpen > 0.35f && (avgEyeWide > 0.25f || jawOpen > 0.55f)) {
      val conf = ((jawOpen + avgEyeWide) * 60f).coerceIn(70f, 96f)
      return FaceEmotion.SURPRISED to conf
    }

    // 4. Serene: gentle smile with relaxed jaw
    if (avgSmile in 0.18f..0.45f && jawOpen < 0.25f) {
      val conf = ((avgSmile + 0.4f) * 100f).coerceIn(60f, 92f)
      return FaceEmotion.SERENE to conf
    }

    // 5. Focused: furrowed brows
    if (avgBrowDown > 0.35f) {
      val conf = (avgBrowDown * 100f).coerceIn(65f, 90f)
      return FaceEmotion.FOCUSED to conf
    }

    // 6. Neutral default
    return FaceEmotion.NEUTRAL to 75f
  }
}
