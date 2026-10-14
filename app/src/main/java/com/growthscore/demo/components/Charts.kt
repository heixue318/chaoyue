package com.growthscore.demo.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.growthscore.demo.ui.Blue
import kotlin.math.*

@Composable fun LineChart(values: List<Double>, modifier: Modifier = Modifier, color: Color = Blue, labels: List<String> = emptyList(), onPointClick: ((Int) -> Unit)? = null, chartHeight: Dp = 180.dp) {
    val min=70f; val max=100f
    Canvas(modifier.fillMaxWidth().height(chartHeight).pointerInput(values) { detectTapGestures { tap -> if(values.size>1 && onPointClick!=null) { val left=48f; val step=(size.width-left)/(values.size-1); onPointClick((((tap.x-left)/step).roundToInt()).coerceIn(0,values.lastIndex)) } } }) {
        if(values.size<2) return@Canvas
        val left=48f; val top=12f; val bottom=size.height-28f; val step=(size.width-left)/(values.size-1)
        val paint=android.graphics.Paint().apply { this.color=android.graphics.Color.rgb(102,112,128); textSize=22f; textAlign=android.graphics.Paint.Align.RIGHT }
        listOf(100,95,90,85,80,75,70).forEach { tick -> val y=bottom-(tick-min)/(max-min)*(bottom-top); drawLine(Color(0xFFE4EAF4),Offset(left,y),Offset(size.width,y),1f); drawContext.canvas.nativeCanvas.drawText(tick.toString(),left-8f,y+7f,paint) }
        val points=values.mapIndexed { index,value -> Offset(left+index*step,bottom-((value.toFloat()-min)/(max-min)*(bottom-top)).coerceIn(0f,bottom-top)) }
        val path=Path().apply { moveTo(points.first().x,points.first().y); points.drop(1).forEach { lineTo(it.x,it.y) } }
        drawPath(path,color,style=Stroke(4f,cap=StrokeCap.Round)); points.forEachIndexed { index,point -> drawCircle(color,7f,point); drawCircle(Color.White,3f,point); if(labels.isNotEmpty()&&(index==0||index==points.lastIndex||values.size<=5)){paint.textAlign=android.graphics.Paint.Align.CENTER;drawContext.canvas.nativeCanvas.drawText(labels.getOrElse(index){""}.take(6),point.x,bottom+23f,paint)} }
    }
}
@Composable fun RadarChart(values: List<Pair<String, Double>>, modifier: Modifier = Modifier.size(270.dp)) {
    Canvas(modifier) {
        if(values.size < 3) return@Canvas
        val center=Offset(size.width/2,size.height/2); val radius=size.minDimension*.33f; val count=values.size
        fun point(index:Int,scale:Float):Offset { val angle=-Math.PI.toFloat()/2+2*Math.PI.toFloat()*index/count; return Offset(center.x+cos(angle)*radius*scale,center.y+sin(angle)*radius*scale) }
        listOf(.2f,.4f,.6f,.8f,1f).forEach { scale ->
            val polygon=Path().apply { moveTo(point(0,scale).x,point(0,scale).y); for(index in 1 until count) lineTo(point(index,scale).x,point(index,scale).y); close() }
            drawPath(polygon,Color(0xFFB8DDF3),style=Stroke(2f))
        }
        values.indices.forEach { drawLine(Color(0xFFB8DDF3),center,point(it,1f),2f) }
        val data=Path().apply { moveTo(point(0,(values[0].second/100).toFloat()).x,point(0,(values[0].second/100).toFloat()).y); for(index in 1 until count) lineTo(point(index,(values[index].second/100).toFloat()).x,point(index,(values[index].second/100).toFloat()).y); close() }
        drawPath(data,Blue.copy(alpha=.25f)); drawPath(data,Blue,style=Stroke(3f))
        val paint=android.graphics.Paint().apply { this.color=android.graphics.Color.DKGRAY; textSize=(size.minDimension*.055f).coerceAtMost(28f); textAlign=android.graphics.Paint.Align.CENTER }
        values.forEachIndexed { index,pair -> val anchor=point(index,1.22f); drawContext.canvas.nativeCanvas.drawText(pair.first,anchor.x,anchor.y-8f,paint); drawContext.canvas.nativeCanvas.drawText(pair.second.toInt().toString(),anchor.x,anchor.y+20f,paint) }
    }
}
