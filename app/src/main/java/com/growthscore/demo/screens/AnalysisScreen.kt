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
import com.growthscore.demo.model.*
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
            Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("阶段报告") })
            Tab(selected = tab == 4, onClick = { tab = 4 }, text = { Text("考试对比") })
        }
        Box(Modifier.weight(1f)) { when(tab) { 0->OverallAnalysis(vm); 1->SingleSubjectAnalysis(vm); 2->GoalGapAnalysis(vm); 3->PhaseReport(vm); else->ExamCompare(vm) } }
    }
}

@Composable private fun OverallAnalysis(vm: DemoViewModel) {
    val latest = vm.latest; var selected by remember { mutableStateOf(vm.exams.lastIndex) }
    Column(Modifier.fillMaxSize().padding(top = 18.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Card { Column(Modifier.padding(20.dp)) { Text("当前综合得分率"); Text(formatRate(latest.rate), fontSize = 42.sp); Text(levelFor(latest.rate), color = Green) } }
            Card(Modifier.fillMaxWidth().padding(top = 16.dp)) { Column(Modifier.padding(20.dp)) { Text("综合成长曲线（横轴：考试 · 纵轴：得分率 %）"); LineChart(vm.exams.map { it.rate },labels=vm.exams.map{it.name},onPointClick={selected=it}); val exam=vm.exams[selected]; Text("已选：${exam.name} · ${exam.date} · ${formatRate(exam.rate)}",color=Green,fontWeight=FontWeight.Bold) } }
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
    val goalBySubject=vm.goals.filter { it.enabled && (it.term=="当前学期" || it.term==latest.term) }.associate { it.subject to it.target }
    val rows=latest.subjectScores.map { score -> GoalGapRow(score, goalBySubject[score.subject] ?: 90.0) }
    val ordered=if(sortByGap) rows.sortedByDescending { it.missingPoints } else rows.sortedByDescending { it.lostPoints }
    val targetTotal=ceil(latest.totalFullScore*.95).toInt(); val missingTotal=(targetTotal-latest.totalScore).coerceAtLeast(0.0)
    Column(Modifier.fillMaxSize().padding(top=18.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("本次记录：${if(latest.kind==ExamKind.COMPREHENSIVE)"综合考试" else "单科测试"}",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        if(latest.kind==ExamKind.COMPREHENSIVE) Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp)) {
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
@Composable private fun GoalHeader()=Row(Modifier.fillMaxWidth()){Text("学科",Modifier.weight(1.0f),fontWeight=FontWeight.Bold);Text("当前 / 目标",Modifier.weight(1.35f),fontWeight=FontWeight.Bold);Text("距离目标",Modifier.weight(1.05f),fontWeight=FontWeight.Bold)}
@Composable private fun GoalGapItem(row:GoalGapRow)=Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text(row.score.subject,Modifier.weight(1.0f),fontWeight=FontWeight.Medium);Column(Modifier.weight(1.35f)){Text("${formatRate(row.score.rate)} / ${formatRate(row.target)}");Text("${formatScore(row.score.score)} / ${formatScore(row.score.fullScore)}分  →  ${formatScore(row.targetScore)} / ${formatScore(row.score.fullScore)}分",style=MaterialTheme.typography.bodySmall,color=Color.Gray)};Column(Modifier.weight(1.05f)){if(row.achieved)Text("已达标",color=Green,fontWeight=FontWeight.Bold) else {Text("还差 ${ceil(row.missingPoints).toInt()} 分",color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold);Text("差 ${formatScore(row.missingRate)} pp",style=MaterialTheme.typography.bodySmall,color=Color.Gray)}}}
@Composable private fun SingleSubjectAnalysis(vm: DemoViewModel) {
    var subject by remember(vm.subjects) { mutableStateOf(vm.subjects.firstOrNull() ?: "") }
    val values = vm.exams.mapNotNull { it.subjectScores.find { score -> score.subject == subject }?.rate }
    val latestKind=vm.exams.lastOrNull { exam -> exam.subjectScores.any { it.subject==subject } }?.kind
    val comparable=vm.exams.filter { it.kind==latestKind }.mapNotNull { exam -> exam.subjectScores.find { it.subject==subject }?.rate }.takeLast(5)
    val lossLabels=listOf("知识点不会","审题错误","计算错误","记忆不牢","时间不足","其他")
    val lossTotals=lossLabels.map { label -> label to vm.exams.flatMap { exam -> exam.subjectScores.find { it.subject==subject }?.losses.orEmpty() }.filter { it.reason==label }.sumOf { it.points } }; val largestLoss=lossTotals.maxOfOrNull { it.second }?:0.0; val lossRadar=lossTotals.map { it.first to if(largestLoss>0)it.second/largestLoss*100 else 0.0 }
    val stability=stabilityOf(comparable)
    Column(Modifier.padding(top = 18.dp).verticalScroll(rememberScrollState())) {
        vm.subjects.chunked(4).forEach { row -> Row { row.forEach { item -> FilterChip(selected = item == subject, onClick = { subject = item }, label = { Text(item) }, modifier = Modifier.padding(end = 8.dp)) } } }
        Card(Modifier.fillMaxWidth().padding(top = 18.dp)) { Column(Modifier.padding(20.dp)) { Text("${subject}成长曲线（纵轴：得分率 %）"); LineChart(values,labels=vm.exams.map{it.name}); Text(if(values.isEmpty()) "暂无该学科记录" else "平均得分率：${formatRate(values.average())}  最高：${formatRate(values.max())}") } }
        Card(Modifier.fillMaxWidth().padding(top=12.dp)) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) { Text("近期成绩稳定性",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("成绩水平：${if(values.isEmpty()) "暂无记录" else levelFor(values.last())}"); Text("发展趋势：${trendOf(comparable)}"); Text("稳定性：${stability.label}",color=if(stability.label=="波动较大") MaterialTheme.colorScheme.error else Green,fontWeight=FontWeight.Bold); Text(stability.detail,style=MaterialTheme.typography.bodySmall,color=Color.Gray); Text("稳定性只描述近期成绩波动，不代表学科水平高低。",style=MaterialTheme.typography.bodySmall,color=Color.Gray) } }
        if(lossRadar.any { it.second>0 }) Card(Modifier.fillMaxWidth().padding(top=12.dp)) { Column(Modifier.padding(20.dp)) { Text("${subject}失分原因雷达图",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("按已记录的失分分值累计，仅供复盘定位常见问题。",style=MaterialTheme.typography.bodySmall,color=Color.Gray); RadarChart(lossRadar,Modifier.fillMaxWidth().aspectRatio(1f)) } }
    }
}
private data class Stability(val label:String,val detail:String)
private fun stabilityOf(values:List<Double>):Stability { if(values.size<3)return Stability("数据不足","同类可比考试少于 3 次，暂不评价稳定性。"); val mean=values.average(); val sd=kotlin.math.sqrt(values.sumOf { (it-mean)*(it-mean) }/values.size); val label=when { sd<=3->"表现稳定";sd<=6->"存在一定波动";else->"波动较大" }; return Stability(label,"最近 ${values.size} 次同类考试得分率标准差 ${formatScore(sd)} 个百分点。") }
private fun trendOf(values:List<Double>)=when { values.size<2->"数据不足"; values.last()-values.first()>2->"近期上升"; values.first()-values.last()>2->"近期回落"; else->"近期基本持平" }

@Composable private fun PhaseReport(vm:DemoViewModel) {
    var range by rememberSaveable { mutableStateOf("当前学期") }; var from by rememberSaveable { mutableStateOf("") }; var to by rememberSaveable { mutableStateOf("") }; var exportMessage by remember { mutableStateOf<String?>(null) }
    val all=vm.exams.filter { it.kind==ExamKind.COMPREHENSIVE }; val records=all.filter { exam -> when(range) { "当前学期"->exam.term==vm.student.term; "当前学年"->exam.schoolYear==vm.student.schoolYear; "指定年级"->true; else->(from.isBlank()||exam.date>=from)&&(to.isBlank()||exam.date<=to) } }
    val average=records.map{it.rate}.average(); val delta=if(records.size>=2)records.last().rate-records.first().rate else null
    Column(Modifier.fillMaxSize().padding(top=14.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("阶段成长报告",fontSize=21.sp,fontWeight=FontWeight.Bold); Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("当前学期","当前学年","指定年级","自定义").forEach { FilterChip(selected=range==it,onClick={range=it},label={Text(it)}) }}
        if(range=="自定义") Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(from,{from=it},label={Text("开始日期")},modifier=Modifier.weight(1f));OutlinedTextField(to,{to=it},label={Text("结束日期")},modifier=Modifier.weight(1f))}
        if(records.isEmpty()) Text("该范围暂无综合考试记录。") else { Button({exportMessage=vm.exportReportImage(range,records).getOrElse{"导出失败：${it.message}"}}){Text("导出报告图片")}; Card { Column(Modifier.padding(18.dp)) { Text("综合表现",fontWeight=FontWeight.Bold);Text("综合平均得分率 ${formatRate(average)}",fontSize=26.sp,fontWeight=FontWeight.Bold);Text(if(delta==null)"仅 1 次综合考试，显示成绩但不判断增长趋势。" else "首末两次可比综合考试变化 ${if(delta>=0)"+" else ""}${formatScore(delta)} 个百分点",color=if((delta?:0.0)>=0)Green else MaterialTheme.colorScheme.error);LineChart(records.map{it.rate},labels=records.map{it.name}) } }
            val common=records.first().subjectScores.map{it.subject}.toSet().intersect(records.last().subjectScores.map{it.subject}.toSet()); Card { Column(Modifier.padding(18.dp)) { Text("学科变化",fontWeight=FontWeight.Bold); common.forEach { subject->val a=records.first().subjectScores.first{it.subject==subject}.rate;val b=records.last().subjectScores.first{it.subject==subject}.rate;Text("$subject：${if(b-a>=0)"+" else ""}${formatScore(b-a)} 个百分点") }; if(common.size!=records.first().subjectScores.size||common.size!=records.last().subjectScores.size)Text("学科组合不同，仅比较共同学科；综合成绩不直接比较。",style=MaterialTheme.typography.bodySmall,color=Color.Gray) } }
            Card { Column(Modifier.padding(18.dp)) { Text("成长总结",fontWeight=FontWeight.Bold);Text("${if(delta==null)"继续积累同类综合考试记录。" else if(delta>=0)"本阶段综合得分率较起点提高。" else "本阶段综合得分率较起点回落。"} 不同试卷难度会影响分数，不直接据此判断学习能力变化。") } }
        }
    }
    exportMessage?.let { message->AlertDialog(onDismissRequest={exportMessage=null},title={Text("阶段报告")},text={Text(message)},confirmButton={TextButton({exportMessage=null}){Text("知道了")}}) }
}
@Composable private fun ExamCompare(vm:DemoViewModel) {
    val exams=vm.exams.filter{it.kind==ExamKind.COMPREHENSIVE}; var aIndex by rememberSaveable{mutableStateOf(0)};var bIndex by rememberSaveable{mutableStateOf((exams.size-1).coerceAtLeast(0))};var menuA by remember{mutableStateOf(false)};var menuB by remember{mutableStateOf(false)}; if(exams.isEmpty())return
    val a=exams[aIndex.coerceIn(0,exams.lastIndex)];val b=exams[bIndex.coerceIn(0,exams.lastIndex)];val common=a.subjectScores.map{it.subject}.toSet().intersect(b.subjectScores.map{it.subject}.toSet()); val comparable=common.size==a.subjectScores.size&&common.size==b.subjectScores.size
    Column(Modifier.fillMaxSize().padding(top=14.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("任意两次考试对比",fontSize=21.sp,fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Box{OutlinedButton({menuA=true}){Text("考试 A：${a.name}")};DropdownMenu(menuA,{menuA=false}){exams.forEachIndexed{i,e->DropdownMenuItem({Text("${e.date} · ${e.name}")},{aIndex=i;menuA=false})}}};Box{OutlinedButton({menuB=true}){Text("考试 B：${b.name}")};DropdownMenu(menuB,{menuB=false}){exams.forEachIndexed{i,e->DropdownMenuItem({Text("${e.date} · ${e.name}")},{bIndex=i;menuB=false})}}}};Card { Column(Modifier.padding(18.dp)){Text("${a.name} ${formatRate(a.rate)}   →   ${b.name} ${formatRate(b.rate)}",fontWeight=FontWeight.Bold);if(comparable)Text("综合得分率变化 ${if(b.rate-a.rate>=0)"+" else ""}${formatScore(b.rate-a.rate)} 个百分点",color=Green) else Text("学科组合不同，仅比较共同学科，综合成绩不可直接比较。",color=MaterialTheme.colorScheme.error);common.forEach { subject->val x=a.subjectScores.first{it.subject==subject}.rate;val y=b.subjectScores.first{it.subject==subject}.rate;Text("$subject  ${formatRate(x)} → ${formatRate(y)}  ${if(y-x>=0)"+" else ""}${formatScore(y-x)} pp")};Text("不同试卷难度会影响结果，不直接判定学习能力上升或下降。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)}}
}}
