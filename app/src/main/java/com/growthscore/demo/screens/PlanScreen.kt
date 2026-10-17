package com.growthscore.demo.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.model.*
import com.growthscore.demo.ui.DemoViewModel
import java.time.LocalDate

@Composable
fun PlanScreen(vm:DemoViewModel) {
    var creating by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    when {
        creating -> PlanCreator(vm,onCreated={creating=false},onCancel={creating=false})
        selectedId!=null -> vm.studyPlans.firstOrNull { it.id==selectedId }?.let { PlanDetail(vm,it,onBack={selectedId=null}) } ?: PlanHistory(vm,{creating=true},{selectedId=it})
        else -> PlanHistory(vm,{creating=true},{selectedId=it})
    }
}

@Composable private fun PlanHistory(vm:DemoViewModel,onCreate:()->Unit,onSelect:(String)->Unit) {
    val plans=vm.studyPlans.sortedByDescending { it.createdAt }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("学习计划",fontSize=21.sp,fontWeight=FontWeight.Bold); Button(onCreate){Text("新建计划")} }
        Text("可同时执行学期计划和假日／自定义计划；首页会合并显示当天的任务。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        if(plans.isEmpty()) Card { Column(Modifier.padding(18.dp)) { Text("还没有学习计划",fontWeight=FontWeight.Bold); Text("点击右上角“新建计划”，从可用时间和学科开始安排。",color=Color.Gray) } }
        plans.forEach { plan ->
            val status=when(plan.status){StudyPlanStatus.ACTIVE->"执行中";StudyPlanStatus.PAUSED->"已暂停";StudyPlanStatus.ENDED->"已结束"}
            val done=plan.tasks.count { it.status==PlanTaskStatus.COMPLETED }
            Card(colors=CardDefaults.cardColors(containerColor=if(plan.status==StudyPlanStatus.ACTIVE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(plan.name,fontWeight=FontWeight.Bold,fontSize=18.sp); Text(status,color=if(plan.status==StudyPlanStatus.ACTIVE) MaterialTheme.colorScheme.primary else Color.Gray) }
                Text("${plan.startDate} 至 ${plan.endDate} · ${planTypeLabel(plan.type)}")
                Text("已完成 $done / ${plan.tasks.size} 项",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
                Row { TextButton({onSelect(plan.id)}){Text("查看任务")}; if(plan.status!=StudyPlanStatus.ENDED) { TextButton({vm.pausePlan(plan.id)}){Text(if(plan.status==StudyPlanStatus.PAUSED)"恢复" else "暂停")}; TextButton({vm.endPlan(plan.id)}){Text("结束")} } }
            } }
        }
    }
}

@Composable private fun PlanCreator(vm:DemoViewModel,onCreated:()->Unit,onCancel:()->Unit) {
    var typeName by rememberSaveable { mutableStateOf("学期") }
    var name by rememberSaveable { mutableStateOf("我的学习计划") }
    var start by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var end by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(27)) }
    var weekday by rememberSaveable { mutableStateOf("40") }
    var weekend by rememberSaveable { mutableStateOf("60") }
    var enabled by remember { mutableStateOf(vm.subjects.toSet()) }
    var message by remember { mutableStateOf<String?>(null) }
    val type=when(typeName){"寒假"->StudyPlanType.WINTER_BREAK;"暑假"->StudyPlanType.SUMMER_BREAK;"自定义"->StudyPlanType.CUSTOM;else->StudyPlanType.TERM}; val context=LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("新建学习计划",fontSize=21.sp,fontWeight=FontWeight.Bold); TextButton(onCancel){Text("取消")} }
        Text("新计划不会替换已有计划。假日或自定义计划可以与学期计划叠加执行。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        Card { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("1. 计划类型与日期",fontWeight=FontWeight.Bold)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("学期","寒假","暑假","自定义").forEach{FilterChip(typeName==it,{typeName=it},label={Text(it)})}}
            OutlinedTextField(name,{name=it},label={Text("计划名称")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){DateButton("开始日期",start,Modifier.weight(1f)){start=it};DateButton("结束日期",end,Modifier.weight(1f)){end=it}}
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
            vm.createStudyPlan(name,type,start.toString(),end.toString(),mapOf(1 to week,2 to week,3 to week,4 to week,5 to week,6 to rest,7 to rest),enabled.toList()).onSuccess { onCreated() }.onFailure { message=it.message ?: "生成计划失败" }
        },modifier=Modifier.fillMaxWidth(),enabled=enabled.isNotEmpty()){Text("生成并启用学习计划")}
        message?.let{Text(it,color=MaterialTheme.colorScheme.error)}
    }
}

@Composable private fun PlanDetail(vm:DemoViewModel,plan:StudyPlan,onBack:()->Unit) {
    val today=LocalDate.now().toString(); val todayTasks=plan.tasks.filter { it.plannedDate==today }; val done=todayTasks.count{it.status==PlanTaskStatus.COMPLETED}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(plan.name,fontSize=21.sp,fontWeight=FontWeight.Bold); TextButton(onBack){Text("返回列表")} }
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("${plan.startDate} 至 ${plan.endDate} · ${planTypeLabel(plan.type)}")
            Text("今日完成 $done / ${todayTasks.size} 项",fontSize=18.sp,fontWeight=FontWeight.Medium)
            LinearProgressIndicator(progress={if(todayTasks.isEmpty())0f else done.toFloat()/todayTasks.size},modifier=Modifier.fillMaxWidth())
            if(plan.status!=StudyPlanStatus.ENDED) Row{TextButton({vm.pausePlan(plan.id)}){Text(if(plan.status==StudyPlanStatus.PAUSED)"恢复计划" else "暂停计划")};TextButton({vm.endPlan(plan.id)}){Text("结束计划")}}
        } }
        Text("每周学科安排",fontWeight=FontWeight.Bold); plan.weeklyMinutes.filterValues { it>0 }.forEach { (subject,minutes)->Text("$subject：$minutes 分钟") }
        Text("未来 14 天任务",fontWeight=FontWeight.Bold)
        plan.tasks.take(14).forEach { task->PlanTaskLine(task,enabled=plan.status==StudyPlanStatus.ACTIVE){vm.togglePlanTask(task.id)} }
    }
}

private fun planTypeLabel(type:StudyPlanType)=when(type){StudyPlanType.TERM->"学期计划";StudyPlanType.WINTER_BREAK->"寒假计划";StudyPlanType.SUMMER_BREAK->"暑假计划";StudyPlanType.CUSTOM->"自定义计划"}

@Composable private fun DateButton(label:String,date:LocalDate,modifier:Modifier=Modifier,onChange:(LocalDate)->Unit) { val context=LocalContext.current; OutlinedButton({DatePickerDialog(context,{_,year,month,day->onChange(LocalDate.of(year,month+1,day))},date.year,date.monthValue-1,date.dayOfMonth).show()},modifier){Text("$label：$date")} }

@Composable fun PlanTaskLine(task:PlanTask,enabled:Boolean=true,onToggle:()->Unit) = Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
    Checkbox(task.status==PlanTaskStatus.COMPLETED,onCheckedChange={onToggle()},enabled=enabled)
    Column(Modifier.padding(start=8.dp).weight(1f)) { Text("${task.plannedDate} · ${task.subject} · ${task.title}",fontWeight=FontWeight.Medium);Text(task.description,style=MaterialTheme.typography.bodySmall,color=Color.Gray);Text("建议 ${task.minutes} 分钟",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary) }
}
