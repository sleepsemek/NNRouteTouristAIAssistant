package com.sleepsemek.nnroutetouristaiassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sleepsemek.nnroutetouristaiassistant.ui.theme.NNRouteTouristAIAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NNRouteTouristAIAssistantTheme {
                RoutePlanningScreen()
            }
        }
    }
}