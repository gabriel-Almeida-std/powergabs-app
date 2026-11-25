package com.gabriel.powergabs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gabriel.powergabs.presentation.navigation.PowerGabsRoot
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Desenha atrás das barras do sistema; os insets são tratados pelos Scaffolds das telas.
        enableEdgeToEdge()
        setContent {
            PowerGabsTheme {
                PowerGabsRoot()
            }
        }
    }
}
