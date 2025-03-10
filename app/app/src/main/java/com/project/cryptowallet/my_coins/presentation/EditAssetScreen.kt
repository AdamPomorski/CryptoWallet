package com.project.cryptowallet.my_coins.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.cryptowallet.R
import com.project.cryptowallet.login.presentation.TextField
import com.project.cryptowallet.portfolio.presentation.PortfolioViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditAssetScreen(
    modifier: Modifier = Modifier,
    assetSymbol: String?,
    viewModel: PortfolioViewModel = koinViewModel(),
    navController: NavHostController
) {

    val contentColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }

    val portfolioState by viewModel.state.collectAsState()


    val asset = portfolioState.portfolioItems.find { it.symbol == assetSymbol }

    var amount by remember { mutableStateOf(asset?.amount?.value.toString() ?: "") }

    // Store the initial amount when the screen is built
    val initialAmount = remember { asset?.amount?.value ?: 0.0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    id = asset?.iconRes ?: R.drawable.question_sign
                ),
                contentDescription = "logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(85.dp)
            )

            Text(
                text = assetSymbol?.uppercase() ?: "Unknown Asset",
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Amount",
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = amount,
            label = initialAmount.toString(),
            placeholder = "Enter your amount of coins",
            onValueChange = { amount = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = {
                assetSymbol?.let {
                    if (amount.toDoubleOrNull() != null) {
                        viewModel.editAsset(symbol = it,initialAmount = initialAmount, newAmount = amount.toDouble(), )
                        navController.popBackStack()
                    }
                }
            },
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
                imageVector = Icons.Default.Check,
                contentDescription = "Save",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }


    }
}

@Preview
@Composable
private fun EditAssetScreenPreview() {
    EditAssetScreen(assetSymbol = "btc", navController = rememberNavController())

}


