package com.project.cryptowallet.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavController


@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    val navigateToHome by viewModel.navigateToHome.collectAsState()
    val authError by viewModel.authError.collectAsState()

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            navController.navigate("portfolio") {
                popUpTo("login") { inclusive = true }
            }
            viewModel.resetNavigation()
        }
    }

    AuthScreen(
        title = "Welcome Back",
        subtitle = "Sign in to continue",
        primaryButtonText = "Sign In",
        secondaryText = "Don't have an account? Sign up here",
        onPrimaryButtonClick = { email, password -> viewModel.login(email, password) },
        onSecondaryButtonClick = { navController.navigate("register") },
        authError = authError,
        resetError = { viewModel.resetError() }
    )
}




@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
//    LoginScreen()

}



