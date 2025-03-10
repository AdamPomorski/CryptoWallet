package com.project.cryptowallet.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    val navigateToHome by viewModel.navigateToHome.collectAsState()
    val authError by viewModel.authError.collectAsState()

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            navController.navigate("portfolio") {
                popUpTo("register") { inclusive = true }
            }
            viewModel.resetNavigation()
        }
    }

    AuthScreen(
        title = "Create Account",
        subtitle = "Sign up to continue",
        primaryButtonText = "Sign Up",
        secondaryText = "Already have an account? Sign in here",
        onPrimaryButtonClick = { email, password -> viewModel.register(email, password) },
        onSecondaryButtonClick = { navController.navigate("login") },
        authError = authError,
        resetError = { viewModel.resetError() }
    )
}



@Preview
@Composable
private fun RegisterScreenPreview() {

   // RegisterScreen()

}