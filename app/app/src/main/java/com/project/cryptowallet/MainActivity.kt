package com.project.cryptowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier

import androidx.navigation.compose.rememberNavController

import com.project.cryptowallet.core.navigation.AppNavigation
import com.project.cryptowallet.login.presentation.AuthViewModel
import com.project.cryptowallet.ui.theme.CryptoTrackerTheme
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Get AuthViewModel using Koin
        val authViewModel: AuthViewModel = getViewModel()


        setContent {
            CryptoTrackerTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}



