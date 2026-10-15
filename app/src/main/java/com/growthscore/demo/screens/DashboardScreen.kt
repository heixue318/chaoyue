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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.components.LineChart
import com.growthscore.demo.model.PlanTaskStatus
import com.growthscore.demo.model.StudyPlanStatus
import com.growthscore.demo.model.levelFor
import com.growthscore.demo.ui.*

@Composable fun DashboardScreen(vm:DemoViewModel){val compact=LocalCompactLayout.current;val gap=if(compact)8.dp else 14.dp;val pad=if(compact)8.dp else 16.dp;val latest=vm.latest;val previous=vm.exams.getOrNull(vm.exams.lastIndex-1);val delta=latest.rate-(previous?.rate?:latest.rate);val change="%+.1f%%".format(delta);Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(gap)){Text(vm.student.name,fontSize=if(compact)22.sp else 28.sp,fontWeight=FontWeight.Bold);Text("${vm.student.grade}${vm.student.term} · ${vm.student.schoolYear}",color=Color.Gray);TodayPlanCard(vm,compact);Card(colors=CardDefaults.cardColors(containerColor=PaleBlue),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(if(compact)12.dp else 18.dp)){Text("最近一次综合考试",color=Color.Gray);Text(latest.name,fontSize=if(compact)18.sp else 22.sp,fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("综合得分率",color=Color.Gray);Text("%.1f%%".format(latest.rate),fontSize=if(compact)36.sp else 48.sp,fontWeight=FontWeight.Bold);Text("★★★★★  ${levelFor(latest.rate)}",color=Green,fontWeight=FontWeight.Bold)};Text("↑ $change",color=Green,fontWeight=FontWeight.Bold)}}};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(if(compact)12.dp else 16.dp)){Text("我的综合成长曲线",fontSize=19.sp,fontWeight=FontWeight.Bold);LineChart(vm.exams.map{it.rate},Modifier.padding(top=4.dp),chartHeight=if(compact)118.dp else 180.dp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){vm.exams.takeLast(4).forEach{Text(it.name,fontSize=11.sp,color=Color.Gray)}}}};BoxWithConstraints(Modifier.fillMaxWidth()){if(maxWidth>=700.dp)Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){SubjectCards(vm,Modifier.weight(1f),Modifier.weight(1f))}else Column(verticalArrangement=Arrangement.spacedBy(12.dp)){SubjectCards(vm,Modifier.fillMaxWidth(),Modifier.fillMaxWidth())}}}}

@Composable private fun TodayPlanCard(vm:DemoViewModel,compact:Boolean) {
    val plans=vm.activeStudyPlans
    if(plans.isEmpty()) return
    val tasks=vm.todayPlanTasks(); val done=tasks.count { it.status==PlanTaskStatus.COMPLETED }
    Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(if(compact)12.dp else 16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text("今日学习计划",fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("${plans.size} 个执行中计划 · 已完成 $done / ${tasks.size} 项",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        if(tasks.isEmpty()) Text("今天安排休息或暂无任务。",color=Color.Gray) else plans.forEach { plan ->
            val planTasks=plan.tasks.filter { it.plannedDate==java.time.LocalDate.now().toString() }
            if(planTasks.isNotEmpty()) { Text(plan.name,fontWeight=FontWeight.Medium,color=Blue); planTasks.forEach { task -> PlanTaskLine(task) { vm.togglePlanTask(task.id) } } }
        }
    } }
}
@Composable private fun RowScope.SubjectCards(vm:DemoViewModel,a:Modifier,b:Modifier){SubjectCardsBody(vm,a,b)}
@Composable private fun ColumnScope.SubjectCards(vm:DemoViewModel,a:Modifier,b:Modifier){SubjectCardsBody(vm,a,b)}
@Composable private fun SubjectCardsBody(vm:DemoViewModel,a:Modifier,b:Modifier){val latest=vm.latest;Card(a){Column(Modifier.padding(16.dp)){Text("优势学科 TOP 3",fontSize=19.sp,fontWeight=FontWeight.Bold);latest.subjectScores.sortedByDescending{it.rate}.take(3).forEach{s->SubjectLine(s.subject,s.rate,"保持优势")}}};Card(b){Column(Modifier.padding(16.dp)){Text("提升空间",fontSize=19.sp,fontWeight=FontWeight.Bold);latest.subjectScores.sortedBy{it.rate}.take(3).forEach{s->SubjectLine(s.subject,s.rate,"梳理失分原因")}}}}
@Composable private fun SubjectLine(subject:String,rate:Double,hint:String){Row(Modifier.fillMaxWidth().padding(top=12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PaleBlue),contentAlignment=Alignment.Center){Text(subject.take(1),color=Blue,fontWeight=FontWeight.Bold)};Column(Modifier.padding(start=10.dp).weight(1f)){Text(subject,fontWeight=FontWeight.Bold);Text(hint,fontSize=12.sp,color=Color.Gray)};Text("%.0f%%".format(rate),fontWeight=FontWeight.Bold,color=if(rate>=90)Green else Orange)}}
