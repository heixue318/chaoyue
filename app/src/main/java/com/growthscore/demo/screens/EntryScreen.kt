package com.growthscore.demo.screens

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.model.*
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.LocalCompactLayout
import java.time.LocalDate

@Composable fun EntryScreen(vm:DemoViewModel,onSaved:()->Unit){
    val compact=LocalCompactLayout.current; val context=LocalContext.current; var showEditor by remember{mutableStateOf(false)}
    var selectedId by remember{mutableStateOf(vm.exams.lastOrNull()?.id)}; var deleteId by remember{mutableStateOf<String?>(null)}
    var recentLimit by remember{mutableStateOf(3)}; var yearFilter by remember{mutableStateOf("")}; var termFilter by remember{mutableStateOf("")}; var from by remember{mutableStateOf("")}; var to by remember{mutableStateOf("")}; var selecting by remember{mutableStateOf(false)}; val selectedIds=remember{mutableStateListOf<String>()}; var confirmBatch by remember{mutableStateOf(false)}
    val filtered=vm.exams.asReversed().filter { exam -> (yearFilter.isBlank()||exam.schoolYear.contains(yearFilter))&&(termFilter.isBlank()||exam.term.contains(termFilter))&&(from.isBlank()||exam.date>=from)&&(to.isBlank()||exam.date<=to) }.let { if(recentLimit==0)it else it.take(recentLimit) }
    val selected=vm.exams.find{it.id==selectedId}?:filtered.firstOrNull()
    Column(Modifier.fillMaxSize().padding(if(compact)8.dp else 16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(if(compact)6.dp else 12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Column{Text("成绩录入",style=if(compact)MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall);Text("历史成绩最多保存 300 场",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)};Button({showEditor=true},contentPadding=PaddingValues(horizontal=16.dp,vertical=10.dp)){Text("录入成绩")}}
        Card { Column(Modifier.padding(if(compact)12.dp else 16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) { Text("查看历史成绩",style=MaterialTheme.typography.titleMedium); Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(3,5,10).forEach { limit -> FilterChip(selected=recentLimit==limit,onClick={recentLimit=limit},label={Text("最近 $limit 次")}) }}; OutlinedTextField(yearFilter,{yearFilter=it},label={Text("学年筛选，如 2026-2027")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(termFilter,{termFilter=it},label={Text("学期筛选，如 上学期")},singleLine=true,modifier=Modifier.fillMaxWidth());Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({pickDate(context,from){from=it}},Modifier.weight(1f)){Text(if(from.isBlank())"开始日期" else "开始：$from")};OutlinedButton({pickDate(context,to){to=it}},Modifier.weight(1f)){Text(if(to.isBlank())"结束日期" else "结束：$to")}};Text("可组合使用学年、学期和日历日期范围筛选。",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text("历史成绩（${filtered.size} 场）",fontSize=20.sp,fontWeight=FontWeight.Bold);TextButton({selecting=!selecting;selectedIds.clear()}){Text(if(selecting)"取消批量" else "批量删除")}}
        if(selecting&&selectedIds.isNotEmpty()) Button({confirmBatch=true},colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error)){Text("删除已选 ${selectedIds.size} 场")}
        if(filtered.isEmpty())Text("没有匹配条件的成绩记录。")
        filtered.forEach{exam->HistoryCard(exam,exam.id==selected?.id,selecting,exam.id in selectedIds,{if(exam.id in selectedIds)selectedIds.remove(exam.id)else selectedIds.add(exam.id)},{selectedId=exam.id},{deleteId=exam.id})}
    }
    if(showEditor) ExamEditor(vm){showEditor=false;onSaved()}
    deleteId?.let{id->AlertDialog(onDismissRequest={deleteId=null},title={Text("删除这场成绩？")},text={Text("删除后无法恢复。")},confirmButton={TextButton({vm.deleteExam(id);deleteId=null}){Text("删除")}},dismissButton={TextButton({deleteId=null}){Text("取消")}})}
    if(confirmBatch) AlertDialog(onDismissRequest={confirmBatch=false},title={Text("删除已选成绩？")},text={Text("将删除 ${selectedIds.size} 场筛选出的历史记录，且无法恢复。")},confirmButton={TextButton({vm.deleteExams(selectedIds.toSet());selectedIds.clear();confirmBatch=false;selecting=false}){Text("确认删除")}},dismissButton={TextButton({confirmBatch=false}){Text("取消")}})
}
private fun pickDate(context:android.content.Context,initial:String,onPick:(String)->Unit){val date=runCatching{LocalDate.parse(initial)}.getOrDefault(LocalDate.now());DatePickerDialog(context,{_,year,month,day->onPick(LocalDate.of(year,month+1,day).toString())},date.year,date.monthValue-1,date.dayOfMonth).show()}
@Composable private fun HistoryCard(exam:Exam,expanded:Boolean,selecting:Boolean,checked:Boolean,onCheck:()->Unit,onExpand:()->Unit,onDelete:()->Unit)=Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=if(expanded)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)){Column(Modifier.padding(14.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Row{if(selecting)Checkbox(checked,onCheckedChange={onCheck()});Text(exam.name,fontWeight=FontWeight.Bold)};if(!selecting)TextButton(onDelete){Text("删除")}};Text("${exam.date} · ${exam.schoolYear} ${exam.term} · ${if(exam.kind==ExamKind.COMPREHENSIVE)"综合考试" else "单科测试"} · 得分率 ${formatRate(exam.rate)}");if(expanded){HorizontalDivider(Modifier.padding(vertical=8.dp));exam.subjectScores.forEach{score->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(score.subject);Text("${formatScore(score.score)} / ${formatScore(score.fullScore)} · ${formatRate(score.rate)}",fontWeight=FontWeight.Bold)}}}else if(!selecting)TextButton(onExpand){Text("查看各科成绩")}}}
@OptIn(ExperimentalLayoutApi::class)
@Composable private fun ExamEditor(vm:DemoViewModel,onSaved:()->Unit){
    val context=LocalContext.current; val isLandscape=LocalConfiguration.current.orientation==Configuration.ORIENTATION_LANDSCAPE; val contentMaxHeight=if(isLandscape)120.dp else 500.dp; var step by remember{mutableIntStateOf(0)}; var name by remember{mutableStateOf("月考")}; var date by remember{mutableStateOf(LocalDate.now())}; val scores=remember(vm.subjects){vm.subjects.associateWith{mutableStateOf("")}}; val reasons=listOf("知识点不会","审题错误","计算错误","记忆不牢","时间不足","其他"); var lossSubject by remember{mutableStateOf(vm.subjects.firstOrNull()?:"")}; val lossPoints=remember{mutableStateMapOf<String,String>()}
    val stepTitles=listOf("考试信息","分科成绩","失分复盘")
    fun save(){ val items=scores.map{entry->val losses=reasons.mapNotNull { reason->lossPoints["${entry.key}|$reason"]?.toDoubleOrNull()?.takeIf{it>0}?.let{LossRecord(reason,it)} }; SubjectScore(entry.key,entry.value.value.toDoubleOrNull()?:0.0,vm.fullScore(entry.key),losses.map{it.reason},losses)}; vm.saveExam(Exam("new-${System.currentTimeMillis()}",name.ifBlank{"未命名考试"},date.toString(),ExamKind.COMPREHENSIVE,items,vm.student.schoolYear,vm.student.term));onSaved() }
    AlertDialog(onDismissRequest=onSaved,title={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text("录入综合考试成绩");Text("${step + 1}/3 · ${stepTitles[step]}",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);LinearProgressIndicator(progress={(step + 1) / 3f},modifier=Modifier.fillMaxWidth())}},text={Column(Modifier.heightIn(max=contentMaxHeight).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
        when(step){
            0->{ if(!isLandscape)Text("先确认这次考试，之后可以逐科录入。",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant); OutlinedTextField(name,{name=it},label={Text("考试名称")},supportingText=if(isLandscape)null else { { Text("例如：十月月考") } },singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedButton({DatePickerDialog(context,{_,year,month,day->date=LocalDate.of(year,month+1,day)},date.year,date.monthValue-1,date.dayOfMonth).show()},Modifier.fillMaxWidth()){Text("考试日期：$date")} }
            1->{ Text("按学科填写实得分，未填写的科目会按 0 分保存。",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);vm.subjects.forEach{subject->OutlinedTextField(scores.getValue(subject).value,{scores.getValue(subject).value=it},label={Text("${subject}（满分 ${formatScore(vm.fullScore(subject))}）")},suffix={Text("分")},singleLine=true,modifier=Modifier.fillMaxWidth())} }
            else->{ Text("选填失分原因，帮助后续分析薄弱点。",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("选择学科",fontWeight=FontWeight.Bold);FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){vm.subjects.forEach { subject->FilterChip(selected=lossSubject==subject,onClick={lossSubject=subject},label={Text(subject)})}};Text("$lossSubject 的失分",style=MaterialTheme.typography.titleSmall);reasons.forEach { reason->OutlinedTextField(lossPoints["$lossSubject|$reason"]?:"",{lossPoints["$lossSubject|$reason"]=it},label={Text("$reason")},suffix={Text("分")},singleLine=true,modifier=Modifier.fillMaxWidth())} }
        }
    }},confirmButton={TextButton({if(step==2)save() else step++}){Text(if(step==2)"保存成绩" else "下一步")}},dismissButton={Row{if(step>0)TextButton({step--}){Text("上一步")};TextButton(onSaved){Text("取消")}}})
}
