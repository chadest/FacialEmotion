package com.example

import com.example.model.FaceEmotion
import com.example.model.FaceOverlayData
import com.example.model.FacePoint
import com.example.model.FloatingReactionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating facial data models, emotion enums, and reaction generators.
 */
class ExampleUnitTest {

  @Test
  fun testFaceEmotionProperties() {
    val happy = FaceEmotion.HAPPY
    assertEquals("Joie", happy.label)
    assertNotNull(happy.icon)

    val surprised = FaceEmotion.SURPRISED
    assertEquals("Surprise", surprised.label)

    val wink = FaceEmotion.WINK
    assertEquals("Complice", wink.label)
  }

  @Test
  fun testFaceOverlayDataDefaults() {
    val data = FaceOverlayData()
    assertEquals(false, data.hasFace)
    assertEquals(FaceEmotion.NEUTRAL, data.emotion)
    assertEquals(0f, data.confidence, 0.01f)
    assertTrue(data.faceContours.isEmpty())
  }

  @Test
  fun testFaceOverlayDataWithPoints() {
    val points = listOf(FacePoint(10f, 20f), FacePoint(30f, 40f))
    val data = FaceOverlayData(
      hasFace = true,
      emotion = FaceEmotion.HAPPY,
      confidence = 94.5f,
      faceContours = points
    )
    assertTrue(data.hasFace)
    assertEquals(2, data.faceContours.size)
    assertEquals(FaceEmotion.HAPPY, data.emotion)
  }

  @Test
  fun testFloatingReactionItem() {
    val reaction = FloatingReactionItem(
      id = 12345L,
      emotion = FaceEmotion.HAPPY,
      xFraction = 0.5f
    )
    assertEquals(12345L, reaction.id)
    assertEquals(FaceEmotion.HAPPY, reaction.emotion)
    assertEquals(0.5f, reaction.xFraction, 0.001f)
  }

  @Test
  fun testFacePoint3DAndBlendshapes() {
    val point3D = com.example.model.FacePoint3D(0.5f, 0.5f, -0.1f)
    assertEquals(0.5f, point3D.x, 0.001f)
    assertEquals(-0.1f, point3D.z, 0.001f)

    val data = FaceOverlayData(
      hasFace = true,
      allLandmarks3D = listOf(point3D),
      blendshapes = mapOf("mouthSmileLeft" to 0.85f, "mouthSmileRight" to 0.88f)
    )
    assertEquals(1, data.allLandmarks3D.size)
    assertEquals(0.85f, data.blendshapes["mouthSmileLeft"] ?: 0f, 0.001f)
  }
}
