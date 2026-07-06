package com.dskochilov.cosmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dskochilov.cosmos.core.designsystem.theme.CosmosTheme
import com.dskochilov.cosmos.ui.CosmosApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CosmosTheme {
                CosmosApp()
            }
        }
    }
}
