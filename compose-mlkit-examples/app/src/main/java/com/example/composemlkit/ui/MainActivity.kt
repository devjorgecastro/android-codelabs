package com.example.composemlkit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.composemlkit.common.nav.AppNavHost
import com.example.composemlkit.ui.theme.ComposeMLKitExamplesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ComposeMLKitExamplesApp() }
    }
}

@Composable
fun ComposeMLKitExamplesApp() {
    ComposeMLKitExamplesTheme {
        val navController = rememberNavController()
        AppNavHost(navController)
    }
}

