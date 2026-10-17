package com.growthscore.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.components.LineChart
import com.growthscore.demo.model.PlanTaskStatus
import com.growthscore.demo.model.levelFor
import com.growthscore.demo.ui.*

@Composable
fun DashboardScreen(vm:DemoViewModel) {
    val compact=LocalCompactLayout.current
    val latest=vm.latest
    val previous=vm.exams.getOrNull(vm.exams.lastIndex-1)
    val delta=latest.rate-(previous?.rate?:latest.rate)
    Column(Modifier.fillMaxSize().padding(if(compact)12.dp else 20.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(if(compact)10.dp else 16.dp)) {
        Column {
            Text("今天的成长",style=if(compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall)
            Text("${vm.student.name} · ${vm.student.grade}${vm.student.term}",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TodayActionCard(vm,compact)
        PerformanceCard(latest.name,latest.rate,delta,compact)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(if(compact)14.dp else 18.dp)) {
            Text("近期趋势",style=MaterialTheme.typography.titleLarge)
            Text("最近 ${vm.exams.takeLast(5).size} 次考试",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            LineChart(vm.exams.takeLast(5).map{it.rate},Modifier.padding(top=6.dp),labels=vm.exams.takeLast(5).map{it.name},chartHeight=if(compact)128.dp else 170.dp)
        } }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if(maxWidth>=700.dp) Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { SubjectCards(vm,Modifier.weight(1f),Modifier.weight(1f)) }
            else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) { SubjectCards(vm,Modifier.fillMaxWidth(),Modifier.fillMaxWidth()) }
        }
    }
}

@Composable private fun TodayActionCard(vm:DemoViewModel,compact:Boolean) {
    val tasks=vm.todayPlanTasks(); val done=tasks.count { it.status==PlanTaskStatus.COMPLETED }; val next=tasks.firstOrNull { it.status!=PlanTaskStatus.COMPLETED }
    Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(if(compact)14.dp else 18.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
        Text("今日最重要的一步",style=MaterialTheme.typography.titleLarge)
        if(next==null) { Text(if(tasks.isEmpty()) "今天暂时没有计划任务，先录入一次成绩或创建学习计划。" else "今日任务已全部完成，做得很好。",color=MaterialTheme.colorScheme.onPrimaryContainer); if(tasks.isNotEmpty()) LinearProgressIndicator(progress={1f},modifier=Modifier.fillMaxWidth()) }
        else { Text("${next.subject} · ${next.title}",style=MaterialTheme.typography.titleMedium); Text(next.description.ifBlank { "安排 ${next.minutes} 分钟专注完成。" },style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onPrimaryContainer)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) { Text("${next.minutes} 分钟 · 已完成 $done / ${tasks.size} 项",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick={vm.togglePlanTask(next.id)}) { Text("完成这一步") } }
            LinearProgressIndicator(progress={done.toFloat()/tasks.size},modifier=Modifier.fillMaxWidth())
        }
    } }
}

@Composable private fun PerformanceCard(name:String,rate:Double,delta:Double,compact:Boolean) = Card(colors=CardDefaults.cardColors(containerColor=PaleBlue),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(if(compact)14.dp else 18.dp)) {
    Text("最近一次考试 · $name",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("%.1f%%".format(rate),fontSize=if(compact)38.sp else 48.sp,fontWeight=FontWeight.Bold); Text(levelFor(rate),color=Green,fontWeight=FontWeight.Bold) }; Surface(color=if(delta>=0)MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,shape=MaterialTheme.shapes.medium) { Text("较上次 ${if(delta>=0)"+" else ""}${"%.1f".format(delta)} pp",modifier=Modifier.padding(horizontal=10.dp,vertical=8.dp),fontWeight=FontWeight.Bold,color=if(delta>=0)Green else MaterialTheme.colorScheme.error) } }
} }

@Composable private fun RowScope.SubjectCards(vm:DemoViewModel,a:Modifier,b:Modifier){SubjectCardsBody(vm,a,b)}
@Composable private fun ColumnScope.SubjectCards(vm:DemoViewModel,a:Modifier,b:Modifier){SubjectCardsBody(vm,a,b)}
@Composable private fun SubjectCardsBody(vm:DemoViewModel,a:Modifier,b:Modifier){ val latest=vm.latest; Card(a){Column(Modifier.padding(16.dp)){Text("保持优势",style=MaterialTheme.typography.titleMedium);latest.subjectScores.sortedByDescending{it.rate}.take(3).forEach{s->SubjectLine(s.subject,s.rate,"继续保持")}}}; Card(b){Column(Modifier.padding(16.dp)){Text("下一步优先",style=MaterialTheme.typography.titleMedium);latest.subjectScores.sortedBy{it.rate}.take(3).forEach{s->SubjectLine(s.subject,s.rate,"优先复盘")}}} }
@Composable private fun SubjectLine(subject:String,rate:Double,hint:String){Row(Modifier.fillMaxWidth().padding(top=12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PaleBlue),contentAlignment=Alignment.Center){Text(subject.take(1),color=Blue,fontWeight=FontWeight.Bold)};Column(Modifier.padding(start=10.dp).weight(1f)){Text(subject,fontWeight=FontWeight.Bold);Text(hint,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text("%.0f%%".format(rate),fontWeight=FontWeight.Bold,color=if(rate>=90)Green else Orange)}}
