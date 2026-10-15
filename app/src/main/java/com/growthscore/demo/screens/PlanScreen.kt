package com.growthscore.demo.screens

import androidx.compose.foundation.horizontalScroll
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
import com.growthscore.demo.model.*
import com.growthscore.demo.ui.DemoViewModel
import java.time.LocalDate

@Composable
fun PlanScreen(vm:DemoViewModel) {
    val plan=vm.studyPlan
    if(plan!=null && plan.status!=StudyPlanStatus.ENDED) ActivePlan(vm,plan) else PlanCreator(vm)
}

@Composable private fun PlanCreator(vm:DemoViewModel) {
    var typeName by rememberSaveable { mutableStateOf("学期") }
    var name by rememberSaveable { mutableStateOf("我的学习计划") }
    var start by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var end by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(27).toString()) }
    var weekday by rememberSaveable { mutableStateOf("40") }
    var weekend by rememberSaveable { mutableStateOf("60") }
    var enabled by remember { mutableStateOf(vm.subjects.toSet()) }
    var message by remember { mutableStateOf<String?>(null) }
    val type=when(typeName){"寒假"->StudyPlanType.WINTER_BREAK;"暑假"->StudyPlanType.SUMMER_BREAK;"自定义"->StudyPlanType.CUSTOM;else->StudyPlanType.TERM}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("学习计划",fontSize=21.sp,fontWeight=FontWeight.Bold)
        Text("根据现有成绩给出可修改的学习安排；任务仅使用本地数据，不会覆盖历史成绩。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        Card { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("1. 计划类型与日期",fontWeight=FontWeight.Bold)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("学期","寒假","暑假","自定义").forEach{FilterChip(typeName==it,{typeName=it},label={Text(it)})}}
            OutlinedTextField(name,{name=it},label={Text("计划名称")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(start,{start=it},label={Text("开始日期")},placeholder={Text("2026-10-15")},modifier=Modifier.weight(1f),singleLine=true);OutlinedTextField(end,{end=it},label={Text("结束日期")},modifier=Modifier.weight(1f),singleLine=true)}
        } }
        Card { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("2. 可用自主学习时间",fontWeight=FontWeight.Bold)
            Text("请填写扣除学校课程、必要作业和固定活动后的时间。0 分钟代表休息。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(weekday,{weekday=it},label={Text("周一至周五 / 天")},suffix={Text("分")},modifier=Modifier.weight(1f),singleLine=true);OutlinedTextField(weekend,{weekend=it},label={Text("周末 / 天")},suffix={Text("分")},modifier=Modifier.weight(1f),singleLine=true)}
            Text("每周约 ${(weekday.toIntOrNull()?:0)*5+(weekend.toIntOrNull()?:0)*2} 分钟",color=MaterialTheme.colorScheme.primary)
        } }
        Card { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("3. 参与学科",fontWeight=FontWeight.Bold)
            Text("默认根据目标差距、趋势和稳定性分配时间；没有足够成绩时采用均衡权重。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
            vm.subjects.chunked(4).forEach { row->Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){row.forEach { subject->FilterChip(subject in enabled,{enabled=if(subject in enabled)enabled-subject else enabled+subject},label={Text(subject)})}} }
        } }
        Button(onClick={
            val week=(weekday.toIntOrNull()?:0).coerceIn(0,600); val rest=(weekend.toIntOrNull()?:0).coerceIn(0,600)
            vm.createStudyPlan(name,type,start,end,mapOf(1 to week,2 to week,3 to week,4 to week,5 to week,6 to rest,7 to rest),enabled.toList()).onFailure { message=it.message ?: "生成计划失败" }
        },modifier=Modifier.fillMaxWidth(),enabled=enabled.isNotEmpty()){Text("生成并启用学习计划")}
        message?.let{Text(it,color=MaterialTheme.colorScheme.error)}
    }
}

@Composable private fun ActivePlan(vm:DemoViewModel,plan:StudyPlan) {
    val today=LocalDate.now().toString(); val todayTasks=vm.todayPlanTasks(today); val done=todayTasks.count{it.status==PlanTaskStatus.COMPLETED}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("学习计划",fontSize=21.sp,fontWeight=FontWeight.Bold)
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(plan.name,fontSize=20.sp,fontWeight=FontWeight.Bold);Text("${plan.startDate} 至 ${plan.endDate} · ${if(plan.status==StudyPlanStatus.PAUSED)"已暂停" else "执行中"}")
            Text("今日完成 $done / ${todayTasks.size} 项",fontSize=18.sp,fontWeight=FontWeight.Medium)
            LinearProgressIndicator(progress={if(todayTasks.isEmpty())0f else done.toFloat()/todayTasks.size},modifier=Modifier.fillMaxWidth())
            Row{TextButton({vm.pausePlan()}){Text(if(plan.status==StudyPlanStatus.PAUSED)"恢复计划" else "暂停计划")};TextButton({vm.endPlan()}){Text("结束计划")}}
        } }
        Text("每周学科安排",fontWeight=FontWeight.Bold); plan.weeklyMinutes.filterValues { it>0 }.forEach { (subject,minutes)->Text("$subject：$minutes 分钟") }
        Text("未来 14 天任务",fontWeight=FontWeight.Bold)
        plan.tasks.take(14).forEach { task->PlanTaskLine(task,enabled=plan.status==StudyPlanStatus.ACTIVE){vm.togglePlanTask(task.id)} }
    }
}

@Composable fun PlanTaskLine(task:PlanTask,enabled:Boolean=true,onToggle:()->Unit) = Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
    Checkbox(task.status==PlanTaskStatus.COMPLETED,onCheckedChange={onToggle()},enabled=enabled)
    Column(Modifier.padding(start=8.dp).weight(1f)) { Text("${task.plannedDate} · ${task.subject} · ${task.title}",fontWeight=FontWeight.Medium);Text(task.description,style=MaterialTheme.typography.bodySmall,color=Color.Gray);Text("建议 ${task.minutes} 分钟",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary) }
}
