package com.project.cryptowallet.portfolio.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.project.cryptowallet.R
import com.project.cryptowallet.crypto_list.presentation.coin_detail.ChartStyle
import com.project.cryptowallet.crypto_list.presentation.coin_detail.DataPoint
import com.project.cryptowallet.crypto_list.presentation.coin_detail.LineChart

import com.project.cryptowallet.portfolio.presentation.components.PortfolioTableCell
import com.project.cryptowallet.ui.theme.CryptoTrackerTheme
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun PortfolioScreen(
    modifier: Modifier = Modifier,
    viewModel: PortfolioViewModel = koinViewModel(),
    navController: NavHostController
) {

    val state by viewModel.state.collectAsState()

    val contentColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }
    val scrollState = rememberScrollState()

    // Recalculate portfolio value when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.calculatePortfolioValueOverTime()
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(state.isRefreshing),
        onRefresh = { viewModel.refreshPortfolio() } // Trigger refresh when swiping down
    ) {

        if (state.isLoading) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.portfolioValueMap.isNotEmpty()) {

            val portfolioValueMap = state.portfolioValueMap
            val portfolioItems = state.portfolioItems
            val pieChartData = state.pieChartData


            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // Enable vertical scrolling
                    .padding(bottom = 16.dp) // Prevent con
            ) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.crypto_wallet_icon),
                        contentDescription = "logo",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(85.dp)
                    )

                    Text(
                        text = "CryptoWallet",
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 24.sp
                    )

                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Portfolio",
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

//            //Pasted from Line Chart Preview
//            val coinHistoryRandomized = remember {
//                (0..20).map {
//                    CoinPrice(
//                        priceUsd = Random.nextFloat() * 1000.0,
//                        dateTime = ZonedDateTime.now().plusDays(it.toLong())
//                    )
//                }
//            }


                val dataPoints = remember {
                    portfolioValueMap.map {
                        DataPoint(
                            x = it.key.dayOfYear.toFloat(),
                            y = it.value.toFloat(),
                            xLabel = DateTimeFormatter
                                .ofPattern("d/M")
                                .format(it.key),
                        )
                    }

                }
                AnimatedVisibility(
                    visible = dataPoints.isNotEmpty()
                )
                {
                    var selectedDataPoint by remember {
                        mutableStateOf<DataPoint?>(null)
                    }

                    var labelWidth by remember {
                        mutableFloatStateOf(0f)
                    }
                    var totalChartWidth by remember {
                        mutableFloatStateOf(0f)
                    }
                    val amountOfVisibleDataPoints = if (labelWidth > 0) {
                        ((totalChartWidth - 2.5 * labelWidth) / labelWidth).toInt()
                    } else {
                        0
                    }
                    val startIndex = (dataPoints.lastIndex - amountOfVisibleDataPoints)
                        .coerceAtLeast(0)



                    LineChart(
                        dataPoints = dataPoints,
                        style = ChartStyle(
                            chartLineColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.secondary.copy(
                                alpha = 0.3f
                            ),
                            selectedColor = MaterialTheme.colorScheme.primary,
                            helpersLinesThicknessPx = 5f,
                            axisLinesThicknessPx = 5f,
                            labelFontSize = 14.sp,
                            minYLabelSpacing = 25.dp,
                            verticalPadding = 8.dp,
                            horizontalPadding = 8.dp,
                            xAxisLabelSpacing = 8.dp
                        ),
                        visibleDataPointsIndices = startIndex..dataPoints.size - 1,
                        unit = "$",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .onSizeChanged { totalChartWidth = it.width.toFloat() },
                        selectedDataPoint = selectedDataPoint,
                        onSelectedDataPoint = {
                            selectedDataPoint = it
                        },
                        onXLabelWidthChange = { labelWidth = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Assets",
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 16.dp),
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
                }


                // Table Rows
                for (portfolioItem in portfolioItems) { // Example rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("my_assets") },
                    modifier = Modifier
                        .width(120.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.height(16.dp))

                PieChart(pieChartData = pieChartData)
            }
        }
    }


}

@PreviewLightDark
@Composable
private fun PortfolioScreenPreview() {
    CryptoTrackerTheme {
        PortfolioScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
            navController = rememberNavController()
        )
    }
}