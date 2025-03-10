package com.project.cryptowallet.portfolio.presentation

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.project.cryptowallet.R
import com.project.cryptowallet.portfolio.presentation.models.PieChartData
import com.project.cryptowallet.ui.theme.SpaceMono

@Composable
fun PieChart(
    pieChartData: List<PieChartData>,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val colors = arrayListOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb(),
        MaterialTheme.colorScheme.surfaceVariant.toArgb()
    )
    val textStyle = MaterialTheme.typography.bodyMedium
    val fontFamily = SpaceMono

    val typeface = Typeface.create(
        context.resources.getFont(R.font.space_mono_regular),
        Typeface.NORMAL
    )


    val contentColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }


    // on below line we are creating a column
    // and specifying a modifier as max size.
    Column(modifier = modifier.fillMaxSize()) {
        // on below line we are again creating a column
        // with modifier and horizontal and vertical arrangement
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .size(320.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // method we have created in Pie chart data class.
                Crossfade(targetState = pieChartData) { pieChartData ->
                    AndroidView(factory = { context ->
                        PieChart(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            this.description.isEnabled = false
                            this.isDrawHoleEnabled = false
                            this.legend.isEnabled = true
                            this.legend.textSize = 14F
                            this.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        }
                    },
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(5.dp),
                        update = {
                            updatePieChartWithData(
                                chart = it,
                                data = pieChartData,
                                colors = colors,
                                textColor = contentColor.toArgb(),  // Themed text color
                                textSize = textStyle.fontSize.value,  // Themed text size
                                typeface = typeface
                            )
                        }
                    )
                }

            }
        }
    }
}

@Preview
@Composable
private fun PieChartPreview() {
    PieChart(
        listOf(
            PieChartData("Chrome", 60f),
            PieChartData("Safari", 210f),
            PieChartData("Firefox", 10f),
            PieChartData("Others", 10f),
        ), Modifier.fillMaxSize()
    )

}

// on below line we are creating a update pie
// chart function to update data in pie chart.

fun updatePieChartWithData(
    chart: PieChart,
    data: List<PieChartData>,
    colors: ArrayList<Int>,
    textColor: Int,   // Themed text color
    textSize: Float,  // Themed text size
    typeface: Typeface? // Themed typeface
) {
    val entries = ArrayList<PieEntry>()

    for (i in data.indices) {
        val item = data[i]
        entries.add(PieEntry(item.value ?: 0.toFloat(), item.browserName ?: ""))
    }

    // Set entry label typeface
    chart.setEntryLabelTypeface(typeface)  // Apply typeface to slice labels
    chart.setEntryLabelColor(textColor)    // Apply text color to slice labels
    chart.setEntryLabelTextSize(textSize)

    val ds = PieDataSet(entries, "")
    ds.colors = colors

    ds.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
    ds.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
    ds.sliceSpace = 2f
    ds.valueTextSize = textSize  // Apply font size from theme
    ds.valueTypeface = typeface  // Apply font typeface from theme
    ds.valueTextColor = textColor  // Apply themed text color

    chart.setExtraOffsets(10f, 10f, 10f, 10f)
    chart.setUsePercentValues(true)
    chart.rotationAngle = 270f
    chart.animateY(1000, Easing.EaseInOutQuad)

    ds.setDrawValues(true)
    ds.setDrawIcons(false)
    ds.setAutomaticallyDisableSliceSpacing(true)
    ds.valueLinePart1Length = 1f
    ds.valueLinePart2Length = -0.5f
    ds.valueLineColor = Color.Transparent.toArgb()
    ds.valueLineWidth = 0f

    chart.legend.textColor = textColor  // Apply themed legend color
    chart.legend.typeface = typeface
    chart.legend.formToTextSpace = 5f
    chart.legend.xEntrySpace = 15f
    ds.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "${"%.1f".format(value)}%"
        }
    }

    val d = PieData(ds)
    chart.setUsePercentValues(true)
    chart.data = d
    chart.invalidate()
}


