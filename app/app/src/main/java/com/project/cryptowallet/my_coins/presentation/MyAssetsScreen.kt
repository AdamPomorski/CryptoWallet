package com.project.cryptowallet.my_coins.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.cryptowallet.portfolio.presentation.PortfolioViewModel
import com.project.cryptowallet.portfolio.presentation.components.PortfolioTableCell
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyAssetsScreen(
    modifier: Modifier = Modifier,
    viewModel: PortfolioViewModel = koinViewModel(),
    navController: NavHostController
) {

    val contentColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }
    val scrollState = rememberScrollState()

    val state by viewModel.state.collectAsState()
    val portfolioItems = state.portfolioItems

//    LaunchedEffect(Unit) {
//        viewModel.calculatePortfolioValueOverTime()
//    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Enable vertical scrolling
            .padding(bottom = 16.dp) // Prevent con
    ) {


        Text(
            text = "Assets",
            color = contentColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly


        ) {
            PortfolioTableCell("Coin", modifier = Modifier.width(80.dp))
            PortfolioTableCell("Price", modifier = Modifier.width(80.dp))
            PortfolioTableCell("Amount", modifier = Modifier.width(80.dp))
            PortfolioTableCell("Value", modifier = Modifier.width(80.dp))
            Box(modifier = Modifier.size(18.dp))
            Box(modifier = Modifier.size(18.dp))


        }


        // Table Rows
        for (portfolioItem in portfolioItems) { // Example rows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PortfolioTableCell(
                    portfolioItem.symbol.uppercase(),
                    modifier = Modifier.width(80.dp),
                    containsImg = true,
                    imgRes = portfolioItem.iconRes,
                    fontSize = 11
                )
                PortfolioTableCell(
                    portfolioItem.priceUsd.formatted + "$",
                    modifier = Modifier.width(80.dp),
                    fontSize = 11
                )
                PortfolioTableCell(
                    portfolioItem.amount.formatted,
                    modifier = Modifier.width(80.dp),
                    fontSize = 11
                )
                PortfolioTableCell(
                    portfolioItem.value.formatted + "$",
                    modifier = Modifier.width(80.dp),
                    fontSize = 11
                )

                // Clickable Icons
                IconButton(
                    onClick = {
                        navController.navigate("editAssetScreen/${portfolioItem.symbol}")
                    },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier.size(18.dp),
                        tint = contentColor
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteAsset(portfolioItem.symbol, portfolioItem.amount.value) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier.size(18.dp),
                        tint = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = { navController.navigate("addAssetScreen") },
            modifier = Modifier
                .size(36.dp) // Adjust the circle size
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) // Circular background
                .padding(4.dp) // Padding to avoid icon touching the edges
                .align(Alignment.CenterHorizontally) // Center the icon horizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }


    }

}

@PreviewLightDark
@Composable
private fun MyAssetsScreenPreview() {
    MyAssetsScreen(navController = rememberNavController())

}