package com.growthscore.demo.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.growthscore.demo.ui.Blue
import kotlin.math.*

@Composable fun LineChart(values: List<Double>, modifier: Modifier = Modifier, color: Color = Blue) {
    Canvas(modifier.height(126.dp).fillMaxWidth()) {
        if (values.size < 2) return@Canvas
        val min = 70f; val max = 100f; val step = size.width / (values.size - 1)
        for (i in 0..4) drawLine(Color(0xFFE7ECF7), Offset(0f, i * size.height / 4), Offset(size.width, i * size.height / 4), 1f)
        val pts = values.mapIndexed { i, v -> Offset(i * step, size.height - ((v.toFloat() - min) / (max - min) * size.height).coerceIn(0f, size.height)) }
        val path = Path().apply { moveTo(pts.first().x, pts.first().y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
        drawPath(path, color, style = Stroke(4f, cap = StrokeCap.Round)); pts.forEach { drawCircle(color, 6f, it); drawCircle(Color.White, 3f, it) }
    }
}

@Composable fun RadarChart(values: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    Canvas(modifier.size(205.dp)) {
        val center = Offset(size.width / 2, size.height / 2); val radius = size.minDimension * .39f; val n = values.size
        fun point(i: Int, scale: Float) = Offset(center.x + cos(-Math.PI.toFloat()/2 + 2*Math.PI.toFloat()*i/n) * radius * scale, center.y + sin(-Math.PI.toFloat()/2 + 2*Math.PI.toFloat()*i/n) * radius * scale)
        for (s in listOf(.25f,.5f,.75f,1f)) { val p = Path().apply { moveTo(point(0,s).x,point(0,s).y); for(i in 1 until n) lineTo(point(i,s).x,point(i,s).y); close() }; drawPath(p, Color(0xFFD9E3F7), style=Stroke(1f)) }
        val data = Path().apply { moveTo(point(0,(values[0].second/100).toFloat()).x,point(0,(values[0].second/100).toFloat()).y); for(i in 1 until n) lineTo(point(i,(values[i].second/100).toFloat()).x,point(i,(values[i].second/100).toFloat()).y); close() }
        drawPath(data, Blue.copy(alpha=.23f)); drawPath(data, Blue, style=Stroke(3f))
    }
}
