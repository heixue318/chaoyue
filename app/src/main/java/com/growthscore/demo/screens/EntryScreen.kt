package com.growthscore.demo.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.model.*
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.LocalCompactLayout
import java.time.LocalDate

@Composable fun EntryScreen(vm:DemoViewModel,onSaved:()->Unit){
    val compact=LocalCompactLayout.current
    var showEditor by remember{mutableStateOf(false)};var filter by remember{mutableStateOf("")};var selectedId by remember{mutableStateOf(vm.exams.last().id)};var deleteId by remember{mutableStateOf<String?>(null)}
    val records=vm.exams.asReversed().filter{filter.isBlank()||it.date.contains(filter)};val selected=vm.exams.find{it.id==selectedId}?:records.firstOrNull()
    Column(Modifier.fillMaxSize().padding(if(compact)8.dp else 16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(if(compact)6.dp else 12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Column{Text("成绩录入",fontSize=if(compact)22.sp else 28.sp,fontWeight=FontWeight.Bold);Text("历史成绩最多保存 300 场",style=MaterialTheme.typography.bodySmall)};Button({showEditor=true}){Text("录入成绩")}}
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text("按日期筛选",fontWeight=FontWeight.Bold);OutlinedTextField(filter,{filter=it},label={Text("输入日期，如 2026-10 或 2026-10-14")},singleLine=true,modifier=Modifier.fillMaxWidth());if(filter.isNotBlank())TextButton({filter=""}){Text("清除筛选")}}}
        Text("历史成绩（${records.size} 场）",fontSize=20.sp,fontWeight=FontWeight.Bold)
        if(records.isEmpty())Text("没有匹配日期的成绩记录。")
        records.forEach{exam->Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=if(exam.id==selected?.id)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)){Column(Modifier.padding(14.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(exam.name,fontWeight=FontWeight.Bold);TextButton({deleteId=exam.id}){Text("删除")}};Text("${exam.date} · ${if(exam.kind==ExamKind.COMPREHENSIVE)"综合考试" else "单科测试"} · 得分率 %.1f%%".format(exam.rate));if(exam.id==selected?.id){HorizontalDivider(Modifier.padding(vertical=8.dp));exam.subjectScores.forEach{score->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(score.subject);Text("${score.score.toInt()} / ${score.fullScore.toInt()} · %.1f%%".format(score.rate),fontWeight=FontWeight.Bold)}}}else TextButton({selectedId=exam.id}){Text("查看各科成绩")}}}}
    }
    if(showEditor) ExamEditor(vm){showEditor=false;onSaved()};deleteId?.let{id->AlertDialog(onDismissRequest={deleteId=null},title={Text("删除这场成绩？")},text={Text("删除后无法恢复。")},confirmButton={TextButton({vm.deleteExam(id);deleteId=null}){Text("删除")}},dismissButton={TextButton({deleteId=null}){Text("取消")}})}
}

@Composable private fun ExamEditor(vm:DemoViewModel,onSaved:()->Unit){
    val context=LocalContext.current;var name by remember{mutableStateOf("月考")};var date by remember{mutableStateOf(LocalDate.now())};val scores=remember(vm.subjects){vm.subjects.associateWith{mutableStateOf("")}}
    AlertDialog(onDismissRequest=onSaved,title={Text("录入综合考试成绩")},text={Column(Modifier.heightIn(max=540.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
        OutlinedTextField(name,{name=it},label={Text("考试名称")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedButton({DatePickerDialog(context,{_,year,month,day->date=LocalDate.of(year,month+1,day)},date.year,date.monthValue-1,date.dayOfMonth).show()},Modifier.fillMaxWidth()){Text("考试日期：$date")}
        vm.subjects.forEach{subject->OutlinedTextField(scores.getValue(subject).value,{scores.getValue(subject).value=it},label={Text("${subject}（满分 ${vm.fullScore(subject).toInt()}）")},singleLine=true,modifier=Modifier.fillMaxWidth())}
    }},confirmButton={TextButton({vm.saveExam(Exam("new-${System.currentTimeMillis()}",name.ifBlank{"未命名考试"},date.toString(),ExamKind.COMPREHENSIVE,scores.map{SubjectScore(it.key,it.value.value.toDoubleOrNull()?:0.0,vm.fullScore(it.key))}));onSaved()}){Text("保存")}},dismissButton={TextButton(onSaved){Text("取消")}})
}
