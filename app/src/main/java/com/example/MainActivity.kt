package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.CameraScreen
import com.example.ui.theme.MyApplicationTheme

/**
 * Main Activity hosting the FacePulse AR camera experience with edge-to-edge support.
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CameraScreen(modifier = Modifier.fillMaxSize())
      }
    }
  }
}
