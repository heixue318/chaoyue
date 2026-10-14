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
import java.time.LocalDate

@Composable fun EntryScreen(vm: DemoViewModel, onSaved: () -> Unit) {
    var history by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Column { Text("新增综合考试",fontSize=28.sp,fontWeight=FontWeight.Bold); Text("只填本次得分；保存后参与分析",style=MaterialTheme.typography.bodySmall) }
            TextButton({ history=true }) { Text("历史成绩 ${vm.exams.size}/300") }
        }
        ComprehensiveForm(vm,onSaved)
    }
    if(history) HistoryDialog(vm) { history=false }
}

@Composable private fun ComprehensiveForm(vm: DemoViewModel, onSaved: () -> Unit) {
    val context=LocalContext.current; var name by remember { mutableStateOf("月考") }; var date by remember { mutableStateOf(LocalDate.now()) }
    val scores=remember(vm.subjects) { vm.subjects.associateWith { mutableStateOf("90") } }
    Column(Modifier.padding(top=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        ScoreField("考试名称",name,{name=it})
        OutlinedButton(onClick={ DatePickerDialog(context,{_,year,month,day->date=LocalDate.of(year,month+1,day)},date.year,date.monthValue-1,date.dayOfMonth).show() },modifier=Modifier.fillMaxWidth()) { Text("考试日期：$date  ·  选择年月日") }
        Text("科目成绩",fontSize=20.sp,fontWeight=FontWeight.Bold)
        vm.subjects.forEach { subject ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text(subject,fontWeight=FontWeight.Bold)
                OutlinedTextField(scores.getValue(subject).value,{scores.getValue(subject).value=it},label={Text("本次成绩（满分 ${vm.fullScore(subject).toInt()}）")},singleLine=true,modifier=Modifier.fillMaxWidth())
            } }
        }
        val sum=scores.values.sumOf { it.value.toDoubleOrNull()?:0.0 }; val total=vm.subjects.sumOf { vm.fullScore(it) }; Text("总分：${sum.toInt()} / ${total.toInt()}    得分率：%.1f%%".format(if(total==0.0)0.0 else sum/total*100),fontWeight=FontWeight.Bold)
        Button(onClick={ vm.saveExam(Exam("new-${System.currentTimeMillis()}",name.ifBlank{"未命名考试"},date.toString(),ExamKind.COMPREHENSIVE,scores.map { SubjectScore(it.key,it.value.value.toDoubleOrNull()?:0.0,vm.fullScore(it.key)) })); onSaved() }) { Text("保存并返回首页") }
    }
}

@Composable private fun HistoryDialog(vm: DemoViewModel, dismiss:()->Unit) {
    var pending by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest=dismiss,title={Text("历史成绩 · ${vm.exams.size}/300")},text={Column(Modifier.heightIn(max=460.dp).verticalScroll(rememberScrollState())) { vm.exams.asReversed().forEach { exam -> Row(Modifier.fillMaxWidth().padding(vertical=6.dp),horizontalArrangement=Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text(exam.name,fontWeight=FontWeight.Bold); Text("${exam.date} · %.1f%%".format(exam.rate),style=MaterialTheme.typography.bodySmall) }; TextButton({pending=exam.id}) { Text("删除") } } } }},confirmButton={TextButton(dismiss){Text("关闭")}})
    pending?.let { id -> AlertDialog(onDismissRequest={pending=null},title={Text("删除这场成绩？")},text={Text("删除后无法恢复。")},confirmButton={TextButton({vm.deleteExam(id);pending=null}){Text("删除")}},dismissButton={TextButton({pending=null}){Text("取消")}}) }
}

@Composable private fun ScoreField(label:String,value:String,onChange:(String)->Unit)=OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth())
