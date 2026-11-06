package com.example.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ml.FaceAnalyzer
import com.example.model.FaceOverlayData
import java.util.concurrent.Executors

/**
 * CameraX Preview integrated into Compose with continuous real-time FaceAnalyzer pipeline.
 */
@Composable
fun CameraPreviewView(
  isFrontCamera: Boolean,
  onFaceDetected: (FaceOverlayData) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

  val analyzer = remember(isFrontCamera) {
    FaceAnalyzer(
      context = context,
      isFrontCamera = { isFrontCamera },
      onFaceDetected = onFaceDetected
    )
  }

  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { ctx ->
      val previewView = PreviewView(ctx).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
      }

      val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
      cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
          it.surfaceProvider = previewView.surfaceProvider
        }

        val imageAnalysis = ImageAnalysis.Builder()
          .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
          .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
          .build()
          .also {
            it.setAnalyzer(cameraExecutor, analyzer)
          }

        val lensFacing = if (isFrontCamera) {
          CameraSelector.LENS_FACING_FRONT
        } else {
          CameraSelector.LENS_FACING_BACK
        }

        val cameraSelector = CameraSelector.Builder()
          .requireLensFacing(lensFacing)
          .build()

        try {
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
          )
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }, ContextCompat.getMainExecutor(ctx))

      previewView
    },
    update = { _ ->
      // Re-binding on camera flip is managed via keys and remember(isFrontCamera)
    }
  )

  DisposableEffect(Unit) {
    onDispose {
      cameraExecutor.shutdown()
    }
  }
}
