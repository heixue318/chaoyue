package com.growthscore.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.components.LineChart
import com.growthscore.demo.components.RadarChart
import com.growthscore.demo.model.SubjectScore
import com.growthscore.demo.model.levelFor
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.Green
import com.growthscore.demo.ui.LocalCompactLayout
import kotlin.math.ceil

@Composable
fun AnalysisScreen(vm: DemoViewModel) {
    val compact=LocalCompactLayout.current
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().padding(if(compact)8.dp else 16.dp)) {
        Text("分析中心", fontSize = if(compact)22.sp else 28.sp, fontWeight = FontWeight.Bold)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("综合考试") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("单科成长") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("目标差距") })
        }
        Box(Modifier.weight(1f)) { when(tab) { 0->OverallAnalysis(vm); 1->SingleSubjectAnalysis(vm); else->GoalGapAnalysis(vm) } }
    }
}

@Composable private fun OverallAnalysis(vm: DemoViewModel) {
    val latest = vm.latest; var selected by remember { mutableStateOf(vm.exams.lastIndex) }
    Column(Modifier.fillMaxSize().padding(top = 18.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Card { Column(Modifier.padding(20.dp)) { Text("当前综合得分率"); Text("%.1f%%".format(latest.rate), fontSize = 42.sp); Text(levelFor(latest.rate), color = Green) } }
            Card(Modifier.fillMaxWidth().padding(top = 16.dp)) { Column(Modifier.padding(20.dp)) { Text("综合成长曲线（横轴：考试 · 纵轴：得分率 %）"); LineChart(vm.exams.map { it.rate },labels=vm.exams.map{it.name},onPointClick={selected=it}); val exam=vm.exams[selected]; Text("已选：${exam.name} · ${exam.date} · %.1f%%".format(exam.rate),color=Green,fontWeight=FontWeight.Bold) } }
        }
        Column(Modifier.fillMaxWidth()) {
            Card { Column(Modifier.padding(20.dp)) { Text("学科能力雷达图（学科 · 得分率）"); RadarChart(latest.subjectScores.map { it.subject to it.rate },Modifier.fillMaxWidth().aspectRatio(1f)) } }
            Card(Modifier.fillMaxWidth().padding(top = 16.dp)) { Column(Modifier.padding(20.dp)) { Text("本次报告"); Text("可在“目标差距”中按差距或失分数查看本次考试的学科情况。") } }
        }
    }
}

@Composable private fun GoalGapAnalysis(vm: DemoViewModel) {
    val latest=vm.latest
    var sortByGap by rememberSaveable { mutableStateOf(true) }
    val goalBySubject=vm.goals.associate { it.subject to it.target }
    val rows=latest.subjectScores.map { score -> GoalGapRow(score, goalBySubject[score.subject] ?: 90.0) }
    val ordered=if(sortByGap) rows.sortedByDescending { it.missingPoints } else rows.sortedByDescending { it.lostPoints }
    val targetTotal=ceil(latest.totalFullScore*.95).toInt(); val missingTotal=(targetTotal-latest.totalScore).coerceAtLeast(0.0)
    Column(Modifier.fillMaxSize().padding(top=18.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp)) {
            Text("综合目标：得分率 95%",fontWeight=FontWeight.Bold)
            Text("当前总分 ${formatScore(latest.totalScore)} / ${formatScore(latest.totalFullScore)}",fontSize=17.sp)
            if(missingTotal>0) Text("目标至少 $targetTotal 分，还差 ${ceil(missingTotal).toInt()} 分",fontSize=22.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
            else Text("已达到 95% 综合目标，超出 ${formatScore(latest.totalScore-targetTotal)} 分",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Green)
        } }
        Text("学科目标与差距",fontSize=20.sp,fontWeight=FontWeight.Bold)
        Text("目标满分与得分率由“我的 → 学科设置”维护。差距只反映当前目标，不代表提分难易。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(selected=sortByGap,onClick={sortByGap=true},label={Text("按目标差距")})
            FilterChip(selected=!sortByGap,onClick={sortByGap=false},label={Text("按本次失分")})
        }
        Card { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) { GoalHeader(); ordered.forEach { GoalGapItem(it) } } }
    }
}

private data class GoalGapRow(val score:SubjectScore,val target:Double) {
    val targetScore get()=ceil(score.fullScore*target/100.0)
    val missingPoints get()=(targetScore-score.score).coerceAtLeast(0.0)
    val missingRate get()=(target-score.rate).coerceAtLeast(0.0)
    val lostPoints get()=(score.fullScore-score.score).coerceAtLeast(0.0)
    val achieved get()=missingPoints<=0.0
}
@Composable private fun GoalHeader()=Row(Modifier.fillMaxWidth()){Text("学科",Modifier.weight(1.1f),fontWeight=FontWeight.Bold);Text("当前/目标",Modifier.weight(1.05f),fontWeight=FontWeight.Bold);Text("距离目标",Modifier.weight(1.25f),fontWeight=FontWeight.Bold)}
@Composable private fun GoalGapItem(row:GoalGapRow)=Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text("${row.score.subject}\n(${formatScore(row.score.fullScore)}分)",Modifier.weight(1.1f),fontWeight=FontWeight.Medium);Text("%.1f%% / %.0f%%".format(row.score.rate,row.target),Modifier.weight(1.05f));Column(Modifier.weight(1.25f)){if(row.achieved)Text("已达标",color=Green,fontWeight=FontWeight.Bold) else {Text("还差 ${ceil(row.missingPoints).toInt()} 分",color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold);Text("差 %.1f 个百分点".format(row.missingRate),style=MaterialTheme.typography.bodySmall,color=Color.Gray)}}}
private fun formatScore(value:Double)=if(value%1.0==0.0)value.toInt().toString() else "%.1f".format(value)

@Composable private fun SingleSubjectAnalysis(vm: DemoViewModel) {
    var subject by remember(vm.subjects) { mutableStateOf(vm.subjects.firstOrNull() ?: "") }
    val values = vm.exams.mapNotNull { it.subjectScores.find { score -> score.subject == subject }?.rate }
    Column(Modifier.padding(top = 18.dp)) {
        vm.subjects.chunked(4).forEach { row -> Row { row.forEach { item -> FilterChip(selected = item == subject, onClick = { subject = item }, label = { Text(item) }, modifier = Modifier.padding(end = 8.dp)) } } }
        Card(Modifier.fillMaxWidth().padding(top = 18.dp)) { Column(Modifier.padding(20.dp)) { Text("${subject}成长曲线（纵轴：得分率 %）"); LineChart(values,labels=vm.exams.map{it.name}); Text(if(values.isEmpty()) "暂无该学科记录" else "平均得分率：%.1f%%  最高：%.1f%%".format(values.average(), values.max())) } }
    }
}
