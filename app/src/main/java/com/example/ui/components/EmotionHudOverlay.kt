package com.example.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FaceEmotion
import com.example.model.FaceOverlayData
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Top HUD bar and bottom interactive emotion sender with quick reactions.
 */
@Composable
fun EmotionHudOverlay(
  faceData: FaceOverlayData,
  isMeshVisible: Boolean,
  currentMeshColor: Color,
  onToggleMesh: () -> Unit,
  onCycleColor: () -> Unit,
  onFlipCamera: () -> Unit,
  onSendReaction: (FaceEmotion) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  fun triggerHaptic() {
    try {
      val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35)
      }
    } catch (_: Exception) {}
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // Top Bar: Live Emotion Badge + Utility Controls
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Live Emotion Badge with Confidence Gauge
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
        modifier = Modifier.testTag("emotion_badge")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (faceData.hasFace) {
            Icon(
              imageVector = faceData.emotion.icon,
              contentDescription = faceData.emotion.label,
              tint = faceData.emotion.color,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = faceData.emotion.label,
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Confidence gauge
            Box(contentAlignment = Alignment.Center) {
              CircularProgressIndicator(
                progress = { faceData.confidence / 100f },
                modifier = Modifier.size(22.dp),
                color = faceData.emotion.color,
                strokeWidth = 2.5.dp,
                trackColor = SurfaceVariant
              )
              Text(
                text = "${faceData.confidence.toInt()}%",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          } else {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAB308))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Recherche visage",
              color = TextSecondary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Action buttons: Mesh toggle, Color cycle, Flip camera
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Mesh
        IconButton(
          onClick = onToggleMesh,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DarkSurface)
            .border(1.dp, SurfaceVariant, CircleShape)
            .testTag("toggle_mesh_button")
        ) {
          Icon(
            imageVector = if (isMeshVisible) Icons.Default.Layers else Icons.Default.LayersClear,
            contentDescription = "Basculer le masque",
            tint = if (isMeshVisible) currentMeshColor else TextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Cycle Mesh Color
        IconButton(
          onClick = onCycleColor,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DarkSurface)
            .border(1.dp, currentMeshColor, CircleShape)
            .testTag("cycle_color_button")
        ) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = "Changer couleur du masque",
            tint = currentMeshColor,
            modifier = Modifier.size(20.dp)
          )
        }

        // Flip Camera
        IconButton(
          onClick = onFlipCamera,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DarkSurface)
            .border(1.dp, SurfaceVariant, CircleShape)
            .testTag("flip_camera_button")
        ) {
          Icon(
            imageVector = Icons.Default.Autorenew,
            contentDescription = "Inverser la caméra",
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // Bottom Action Area: Quick selector + Big Primary Emotion Send Button
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Row of quick emotion buttons
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FaceEmotion.values().take(5).forEach { emotion ->
            val isCurrent = faceData.hasFace && faceData.emotion == emotion
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isCurrent) SurfaceVariant else Color.Transparent)
                .border(
                  width = if (isCurrent) 1.5.dp else 0.dp,
                  color = if (isCurrent) emotion.color else Color.Transparent,
                  shape = CircleShape
                )
                .clickable {
                  triggerHaptic()
                  onSendReaction(emotion)
                }
                .testTag("quick_emotion_${emotion.name.lowercase()}"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = emotion.icon,
                contentDescription = emotion.label,
                tint = emotion.color,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }

      // Primary Real-time Emotion Send Button
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(faceData.emotion.color)
          .clickable {
            triggerHaptic()
            onSendReaction(faceData.emotion)
          }
          .testTag("send_emotion_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = faceData.emotion.icon,
          contentDescription = "Envoyer l'émotion ${faceData.emotion.label}",
          tint = DarkSurface,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Toucher pour diffuser l'émotion",
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
