package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.FaceOverlayData
import com.example.model.FacePoint
import kotlin.math.max

/**
 * High-performance Compose Canvas rendering a sleek, minimalist geometric AR face mesh.
 * Maps coordinates from the camera sensor frame to the viewport, accounting for aspect ratio and front-camera mirror.
 */
@Composable
fun GeometricMeshCanvas(
  faceData: FaceOverlayData,
  meshColor: Color,
  isMeshVisible: Boolean,
  modifier: Modifier = Modifier
) {
  if (!faceData.hasFace || !isMeshVisible) return

  Canvas(modifier = modifier.fillMaxSize()) {
    val canvasW = size.width
    val canvasH = size.height

    if (faceData.imageWidth <= 0 || faceData.imageHeight <= 0) return@Canvas

    // Compute aspect-fill scaling and centering offset
    val scaleX = canvasW / faceData.imageWidth.toFloat()
    val scaleY = canvasH / faceData.imageHeight.toFloat()
    val scale = max(scaleX, scaleY)
    val offsetX = (canvasW - faceData.imageWidth * scale) / 2f
    val offsetY = (canvasH - faceData.imageHeight * scale) / 2f

    // Coordinate mapping functions
    fun mapX(x: Float): Float {
      val px = x * scale + offsetX
      return if (faceData.isFrontCamera) canvasW - px else px
    }

    fun mapY(y: Float): Float = y * scale + offsetY

    fun FacePoint.toOffset(): Offset = Offset(mapX(x), mapY(y))

    // 1. Draw Outer Jaw & Face Contour Polygon
    val facePoints = faceData.faceContours.map { it.toOffset() }
    if (facePoints.size > 5) {
      val facePath = Path().apply {
        moveTo(facePoints.first().x, facePoints.first().y)
        for (i in 1 until facePoints.size) {
          lineTo(facePoints[i].x, facePoints[i].y)
        }
      }
      drawPath(
        path = facePath,
        color = meshColor,
        style = Stroke(width = 2.5f)
      )

      // Geometric Nodes (Vertices) along face perimeter
      for (i in facePoints.indices step 2) {
        val pt = facePoints[i]
        drawCircle(
          color = meshColor,
          radius = 3.5f,
          center = pt
        )
      }
    }

    // 2. Draw Eyebrows & Forehead Geometrics
    val leftEyePoints = faceData.leftEyeContours.map { it.toOffset() }
    val rightEyePoints = faceData.rightEyeContours.map { it.toOffset() }

    if (leftEyePoints.isNotEmpty()) {
      drawClosedPolygon(leftEyePoints, meshColor, strokeWidth = 2f)
      val eyeCenter = computeCentroid(leftEyePoints)
      drawCrosshair(eyeCenter, meshColor, radius = 8f)
    }

    if (rightEyePoints.isNotEmpty()) {
      drawClosedPolygon(rightEyePoints, meshColor, strokeWidth = 2f)
      val eyeCenter = computeCentroid(rightEyePoints)
      drawCrosshair(eyeCenter, meshColor, radius = 8f)
    }

    // 3. Nose Bridge and Cyber Poly Structure
    val nosePoints = faceData.noseBridgeContours.map { it.toOffset() }
    if (nosePoints.size >= 2) {
      for (i in 0 until nosePoints.size - 1) {
        drawLine(
          color = meshColor,
          start = nosePoints[i],
          end = nosePoints[i + 1],
          strokeWidth = 2f
        )
      }
    }

    // 4. Interconnecting Poly Wireframe Lines
    val noseTipOffset = faceData.noseTip?.toOffset()
    val leftCheekOffset = faceData.leftCheek?.toOffset()
    val rightCheekOffset = faceData.rightCheek?.toOffset()

    if (noseTipOffset != null) {
      // Connect nose tip to cheeks
      if (leftCheekOffset != null) {
        drawLine(
          color = meshColor.copy(alpha = 0.6f),
          start = noseTipOffset,
          end = leftCheekOffset,
          strokeWidth = 1.5f
        )
        drawCircle(color = meshColor, radius = 3f, center = leftCheekOffset)
      }
      if (rightCheekOffset != null) {
        drawLine(
          color = meshColor.copy(alpha = 0.6f),
          start = noseTipOffset,
          end = rightCheekOffset,
          strokeWidth = 1.5f
        )
        drawCircle(color = meshColor, radius = 3f, center = rightCheekOffset)
      }

      // Connect nose bridge top to eyes
      if (nosePoints.isNotEmpty()) {
        val noseTop = nosePoints.first()
        if (leftEyePoints.isNotEmpty()) {
          drawLine(
            color = meshColor.copy(alpha = 0.5f),
            start = noseTop,
            end = leftEyePoints.first(),
            strokeWidth = 1.5f
          )
        }
        if (rightEyePoints.isNotEmpty()) {
          drawLine(
            color = meshColor.copy(alpha = 0.5f),
            start = noseTop,
            end = rightEyePoints.last(),
            strokeWidth = 1.5f
          )
        }
      }
    }

    // 4b. MediaPipe 3D Polygonal Triangulation
    if (faceData.allLandmarks3D.size >= 468) {
      fun get3DOffset(idx: Int): Offset {
        val lm = faceData.allLandmarks3D[idx]
        val px = lm.x * faceData.imageWidth * scale + offsetX
        val screenX = if (faceData.isFrontCamera) canvasW - px else px
        val screenY = lm.y * faceData.imageHeight * scale + offsetY
        return Offset(screenX, screenY)
      }

      // Cyber triangulation segments across forehead, temples, and cheeks
      val polyEdges = listOf(
        // Forehead & Brow bridge
        10 to 67, 10 to 297, 67 to 109, 297 to 338, 109 to 10, 338 to 10,
        // Temples & Cheekbones
        103 to 116, 332 to 345, 116 to 1, 345 to 1,
        // Chin & Jaw triangulation
        152 to 148, 152 to 377, 148 to 176, 377 to 400,
        // Nose apex diamond
        1 to 168, 1 to 2, 2 to 94, 94 to 1
      )

      val wireframeColor = meshColor.copy(alpha = 0.45f)
      for ((startIdx, endIdx) in polyEdges) {
        if (startIdx < faceData.allLandmarks3D.size && endIdx < faceData.allLandmarks3D.size) {
          drawLine(
            color = wireframeColor,
            start = get3DOffset(startIdx),
            end = get3DOffset(endIdx),
            strokeWidth = 1.2f
          )
        }
      }
    }

    // 5. Reactive Mouth Polygon
    val upperLipPoints = faceData.upperLipContours.map { it.toOffset() }
    val lowerLipPoints = faceData.lowerLipContours.map { it.toOffset() }

    if (upperLipPoints.isNotEmpty() && lowerLipPoints.isNotEmpty()) {
      val mouthPath = Path().apply {
        moveTo(upperLipPoints.first().x, upperLipPoints.first().y)
        for (pt in upperLipPoints) lineTo(pt.x, pt.y)
        for (pt in lowerLipPoints.reversed()) lineTo(pt.x, pt.y)
        close()
      }
      drawPath(
        path = mouthPath,
        color = meshColor,
        style = Stroke(width = 2.5f)
      )

      // Mouth corner diamond accents
      val leftCorner = upperLipPoints.first()
      val rightCorner = upperLipPoints.last()
      drawDiamond(leftCorner, meshColor, 5f)
      drawDiamond(rightCorner, meshColor, 5f)
    }

    // 6. Cyber Face Corner Brackets (Minimalist HUD)
    faceData.boundingBox?.let { box ->
      val left = mapX(box.left.toFloat())
      val right = mapX(box.right.toFloat())
      val top = mapY(box.top.toFloat())
      val bottom = mapY(box.bottom.toFloat())

      val minX = kotlin.math.min(left, right)
      val maxX = kotlin.math.max(left, right)
      val minY = kotlin.math.min(top, bottom)
      val maxY = kotlin.math.max(top, bottom)

      val bracketLen = (maxX - minX) * 0.15f
      val bracketColor = meshColor.copy(alpha = 0.7f)

      // Top-Left
      drawLine(bracketColor, Offset(minX, minY), Offset(minX + bracketLen, minY), 2f)
      drawLine(bracketColor, Offset(minX, minY), Offset(minX, minY + bracketLen), 2f)

      // Top-Right
      drawLine(bracketColor, Offset(maxX, minY), Offset(maxX - bracketLen, minY), 2f)
      drawLine(bracketColor, Offset(maxX, minY), Offset(maxX, minY + bracketLen), 2f)

      // Bottom-Left
      drawLine(bracketColor, Offset(minX, maxY), Offset(minX + bracketLen, maxY), 2f)
      drawLine(bracketColor, Offset(minX, maxY), Offset(minX, maxY - bracketLen), 2f)

      // Bottom-Right
      drawLine(bracketColor, Offset(maxX, maxY), Offset(maxX - bracketLen, maxY), 2f)
      drawLine(bracketColor, Offset(maxX, maxY), Offset(maxX, maxY - bracketLen), 2f)
    }
  }
}

/**
 * Helper to draw closed polygon outlines.
 */
private fun DrawScope.drawClosedPolygon(points: List<Offset>, color: Color, strokeWidth: Float) {
  if (points.size < 2) return
  val path = Path().apply {
    moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
      lineTo(points[i].x, points[i].y)
    }
    close()
  }
  drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}

/**
 * Helper to compute centroid of points list.
 */
private fun computeCentroid(points: List<Offset>): Offset {
  if (points.isEmpty()) return Offset.Zero
  var sumX = 0f
  var sumY = 0f
  for (pt in points) {
    sumX += pt.x
    sumY += pt.y
  }
  return Offset(sumX / points.size, sumY / points.size)
}

/**
 * Draws a minimal cyber crosshair marker.
 */
private fun DrawScope.drawCrosshair(center: Offset, color: Color, radius: Float) {
  drawCircle(color = color, radius = 2f, center = center)
  drawLine(color = color, start = Offset(center.x - radius, center.y), end = Offset(center.x + radius, center.y), strokeWidth = 1.5f)
  drawLine(color = color, start = Offset(center.x, center.y - radius), end = Offset(center.x, center.y + radius), strokeWidth = 1.5f)
}

/**
 * Draws a small diamond indicator at a vertex.
 */
private fun DrawScope.drawDiamond(center: Offset, color: Color, size: Float) {
  val path = Path().apply {
    moveTo(center.x, center.y - size)
    lineTo(center.x + size, center.y)
    lineTo(center.x, center.y + size)
    lineTo(center.x - size, center.y)
    close()
  }
  drawPath(path = path, color = color)
}
