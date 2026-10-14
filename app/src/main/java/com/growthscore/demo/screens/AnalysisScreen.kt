package com.growthscore.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.components.LineChart
import com.growthscore.demo.components.RadarChart
import com.growthscore.demo.model.levelFor
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.Green

@Composable
fun AnalysisScreen(vm: DemoViewModel) {
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("分析中心", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("综合考试分析") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("单科成长分析") })
        }
        if (tab == 0) OverallAnalysis(vm) else SingleSubjectAnalysis(vm)
    }
}

@Composable
private fun OverallAnalysis(vm: DemoViewModel) {
    val latest = vm.latest
    var selected by remember { mutableStateOf(vm.exams.lastIndex) }
    Column(Modifier.fillMaxSize().padding(top = 18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Card { Column(Modifier.padding(20.dp)) { Text("当前综合得分率"); Text("%.1f%%".format(latest.rate), fontSize = 42.sp); Text(levelFor(latest.rate), color = Green) } }
            Card(Modifier.fillMaxWidth().padding(top = 16.dp)) { Column(Modifier.padding(20.dp)) { Text("综合成长曲线（横轴：考试 · 纵轴：得分率 %）"); LineChart(vm.exams.map { it.rate },labels=vm.exams.map{it.name},onPointClick={selected=it}); val exam=vm.exams[selected]; Text("已选：${exam.name} · ${exam.date} · %.1f%%".format(exam.rate),color=Green,fontWeight=FontWeight.Bold) } }
        }
        Column(Modifier.fillMaxWidth()) {
            Card { Column(Modifier.padding(20.dp)) { Text("学科能力雷达图（学科 · 得分率）"); RadarChart(latest.subjectScores.map { it.subject to it.rate }) } }
            Card(Modifier.fillMaxWidth().padding(top = 16.dp)) { Column(Modifier.padding(20.dp)) { Text("本次报告"); Text("数学保持优势；英语为主要提升方向，建议加强词汇与阅读训练。") } }
        }
    }
}

@Composable
private fun SingleSubjectAnalysis(vm: DemoViewModel) {
    var subject by remember(vm.subjects) { mutableStateOf(vm.subjects.firstOrNull() ?: "") }
    val values = vm.exams.mapNotNull { it.subjectScores.find { score -> score.subject == subject }?.rate }
    Column(Modifier.padding(top = 18.dp)) {
        vm.subjects.chunked(4).forEach { row -> Row { row.forEach { item -> FilterChip(selected = item == subject, onClick = { subject = item }, label = { Text(item) }, modifier = Modifier.padding(end = 8.dp)) } } }
        Card(Modifier.fillMaxWidth().padding(top = 18.dp)) { Column(Modifier.padding(20.dp)) { Text("${subject}成长曲线（纵轴：得分率 %）"); LineChart(values,labels=vm.exams.map{it.name}); Text(if(values.isEmpty()) "暂无该学科记录" else "平均得分率：%.1f%%  最高：%.1f%%".format(values.average(), values.max())) } }
    }
}
