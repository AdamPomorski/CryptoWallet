package com.project.cryptowallet.portfolio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.cryptowallet.R
import com.project.cryptowallet.ui.theme.CryptoTrackerTheme

@Composable
fun PortfolioTableCell(
    textContent: String,
    modifier: Modifier = Modifier,
    textAlignment: TextAlign = TextAlign.Center,
    containsImg: Boolean = false,
    imgRes: Int? = null,
    fontSize: Int = 16

) {

    val contentColor = if(isSystemInDarkTheme()){
        Color.White
    }else{
        Color.Black
    }
    if(containsImg&&imgRes!=null){
        Row(
            modifier = modifier
                .padding(4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center

        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = imgRes),
                contentDescription = textContent,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp)
            )

                Text(
                    text = textContent,
                    textAlign = textAlignment,
                    color = contentColor,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = fontSize.sp
                )

        }


    }else {
        Row(
            modifier = modifier
                .padding(4.dp)
                .height(25.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center

        ) {
            Text(
                text = textContent,
                textAlign = textAlignment,
                color = contentColor,
                fontSize = fontSize.sp
            )
        }
    }



}

@PreviewLightDark
@Composable
private fun PortfolioTableCellPreview() {
    CryptoTrackerTheme{
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {


            PortfolioTableCell(
                textContent = "Coin",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .width(200.dp)
            )
            PortfolioTableCell(
                textContent = "BTC",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .width(200.dp),
                containsImg = true,
                imgRes = R.drawable.btc
            )
        }
    }

}

