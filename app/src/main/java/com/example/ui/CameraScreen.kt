package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.FaceEmotion
import com.example.model.FaceOverlayData
import com.example.model.FloatingReactionItem
import com.example.ui.components.CameraPreviewView
import com.example.ui.components.EmotionHudOverlay
import com.example.ui.components.FloatingReactionsOverlay
import com.example.ui.components.GeometricMeshCanvas
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

/**
 * Main Camera AR Screen managing camera lifecycle, permissions, face tracking, and reactions.
 */
@Composable
fun CameraScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Permission state
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Camera and Mask Controls
  var isFrontCamera by remember { mutableStateOf(true) }
  var isMeshVisible by remember { mutableStateOf(true) }
  var colorIndex by remember { mutableIntStateOf(0) }

  val meshColors = remember {
    listOf(
      CyanPrimary,
      MintSecondary,
      AmberAccent,
      Color.White
    )
  }
  val currentMeshColor = meshColors[colorIndex % meshColors.size]

  // Face Tracking Data
  var faceData by remember { mutableStateOf(FaceOverlayData()) }

  // Active floating reactions pool
  val reactions = remember { mutableStateListOf<FloatingReactionItem>() }

  // Function to broadcast floating reactions
  fun spawnReaction(emotion: FaceEmotion) {
    val burstCount = 4
    val now = System.currentTimeMillis()
    for (i in 0 until burstCount) {
      val randomOffsetX = (Random.nextFloat() - 0.5f) * 0.35f
      val baseCenterX = 0.5f
      val item = FloatingReactionItem(
        id = now + i * 100 + Random.nextLong(1000),
        emotion = emotion,
        xFraction = (baseCenterX + randomOffsetX).coerceIn(0.15f, 0.85f),
        startYFraction = 0.82f + (Random.nextFloat() * 0.05f),
        driftXFraction = (Random.nextFloat() - 0.5f) * 0.12f,
        scale = 0.85f + (Random.nextFloat() * 0.4f)
      )
      reactions.add(item)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    if (hasCameraPermission) {
      // 1. Fullscreen Camera Preview
      CameraPreviewView(
        isFrontCamera = isFrontCamera,
        onFaceDetected = { data ->
          faceData = data
        },
        modifier = Modifier.fillMaxSize()
      )

      // 2. AR Geometric Face Mesh Overlay
      GeometricMeshCanvas(
        faceData = faceData,
        meshColor = currentMeshColor,
        isMeshVisible = isMeshVisible,
        modifier = Modifier.fillMaxSize()
      )

      // 3. Floating Reactions Particle System
      FloatingReactionsOverlay(
        reactions = reactions,
        onReactionFinished = { reactionId ->
          reactions.removeAll { it.id == reactionId }
        },
        modifier = Modifier.fillMaxSize()
      )

      // 4. Modern Minimalist HUD & Realtime Emotion Controls
      EmotionHudOverlay(
        faceData = faceData,
        isMeshVisible = isMeshVisible,
        currentMeshColor = currentMeshColor,
        onToggleMesh = { isMeshVisible = !isMeshVisible },
        onCycleColor = { colorIndex = (colorIndex + 1) % meshColors.size },
        onFlipCamera = { isFrontCamera = !isFrontCamera },
        onSendReaction = { emotion ->
          spawnReaction(emotion)
        },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Permission Rationale Card
      CameraPermissionFallback(
        onRequestPermission = {
          permissionLauncher.launch(Manifest.permission.CAMERA)
        }
      )
    }
  }
}

/**
 * Minimalist permission fallback card when camera access is not yet granted.
 */
@Composable
private fun CameraPermissionFallback(
  onRequestPermission: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = DarkSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(SurfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Permission caméra",
            tint = CyanPrimary,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Accès Caméra Requis",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "FacePulse AR a besoin de la caméra pour afficher le masque géométrique et détecter les émotions faciales en temps réel.",
          fontSize = 14.sp,
          color = TextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onRequestPermission,
          colors = ButtonDefaults.buttonColors(
            containerColor = CyanPrimary,
            contentColor = DarkBackground
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("request_camera_permission_button")
        ) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = "Activer la caméra",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
          )
        }
      }
    }
  }
}
