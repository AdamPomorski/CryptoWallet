package com.project.cryptowallet.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.project.cryptowallet.login.presentation.AuthViewModel
import com.project.cryptowallet.login.presentation.LoginScreen
import com.project.cryptowallet.login.presentation.RegisterScreen
import com.project.cryptowallet.portfolio.presentation.PortfolioScreen

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.project.cryptowallet.my_coins.presentation.AddAssetScreen
import com.project.cryptowallet.my_coins.presentation.EditAssetScreen
import com.project.cryptowallet.my_coins.presentation.MyAssetsScreen
import com.project.cryptowallet.portfolio.presentation.PortfolioViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    portfolioViewModel: PortfolioViewModel = koinViewModel() // Inject ViewModel once
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in listOf("coin_list", "portfolio")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "portfolio",
            modifier = modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("register") { RegisterScreen(navController, authViewModel) }
            composable("coin_list") { AdaptiveCoinListDetailPanel() }
            composable("portfolio") { PortfolioScreen(navController = navController, viewModel = portfolioViewModel) }  // Pass ViewModel
            composable("my_assets") { MyAssetsScreen(navController = navController, viewModel = portfolioViewModel) }   // Pass ViewModel
            composable("addAssetScreen") { AddAssetScreen(navController = navController, viewModel = portfolioViewModel) } // Pass ViewModel

            composable(
                "editAssetScreen/{symbol}",
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol")
                EditAssetScreen(navController = navController, assetSymbol = symbol, viewModel = portfolioViewModel)  // Pass ViewModel
            }
        }
    }
}

