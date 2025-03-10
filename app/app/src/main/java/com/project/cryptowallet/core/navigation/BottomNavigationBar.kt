package com.project.cryptowallet.core.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.material3.Icon as Icon

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {


    NavigationBar(

    ) {

        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Portfolio") },
            label = { Text("Portfolio") },
            selected = currentRoute == "portfolio",
            onClick = {
                if (currentRoute != "portfolio") {
                    navController.navigate("portfolio") {
                        popUpTo("portfolio") { inclusive = true }
                    }

                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Coins") },
            label = { Text("Coins") },
            selected = currentRoute == "coin_list",
            onClick = {
                if (currentRoute != "coin_list") {
                    navController.navigate("coin_list") {
                        popUpTo("coin_list") { inclusive = true }
                    }

                }
            }
        )

    }
}

