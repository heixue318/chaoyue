package com.growthscore.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.model.*
import com.growthscore.demo.ui.DemoViewModel
import java.time.LocalDate

@Composable fun EntryScreen(vm: DemoViewModel) {
    var kind by remember { mutableStateOf(ExamKind.COMPREHENSIVE) }; var saved by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("新增成绩",fontSize=28.sp,fontWeight=FontWeight.Bold); Text("选择考试类型，录入后会立即参与分析",color=androidx.compose.ui.graphics.Color.Gray)
        Row(Modifier.padding(top=16.dp)) { FilterChip(selected=kind==ExamKind.COMPREHENSIVE,onClick={kind=ExamKind.COMPREHENSIVE},label={Text("综合考试")}); Spacer(Modifier.width(10.dp)); FilterChip(selected=kind==ExamKind.SUBJECT_TEST,onClick={kind=ExamKind.SUBJECT_TEST},label={Text("单科测试")}) }
        if(kind==ExamKind.COMPREHENSIVE) ComprehensiveForm(vm,{saved=true}) else SubjectTestForm(vm,{saved=true})
        if(saved) Text("已保存到本地 DEMO 数据，可前往分析页面查看。",color=com.growthscore.demo.ui.Green,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))
    }
}
@Composable private fun ComprehensiveForm(vm:DemoViewModel,onSaved:()->Unit){
    var name by remember { mutableStateOf("月考") }; var lossReasons by remember { mutableStateOf(setOf<String>()) }; val values=remember(vm.subjects){vm.subjects.associateWith { mutableStateOf("90") }}
    Column(Modifier.padding(top=18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) { Field("考试名称",name,{name=it},Modifier.fillMaxWidth()); Field("考试日期",LocalDate.now().toString(),{},Modifier.fillMaxWidth(),enabled=false); Field("年级",vm.student.grade,{},Modifier.fillMaxWidth(),enabled=false); Field("学期",vm.student.term,{},Modifier.fillMaxWidth(),enabled=false) }
    Text("科目成绩",fontSize=20.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=20.dp)); vm.subjects.chunked(2).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(12.dp),modifier=Modifier.fillMaxWidth()){row.forEach{s->Column(Modifier.weight(1f)){Text(s);OutlinedTextField(value=values.getValue(s).value,onValueChange={values.getValue(s).value=it},suffix={Text("/100")},singleLine=true,modifier=Modifier.fillMaxWidth());Text("得分率：${values.getValue(s).value}%",fontSize=12.sp)}}} }
    Text("主要失分原因（可多选）",fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=14.dp)); Row { listOf("知识点不会","计算错误","审题错误","时间不足","粗心","其他").forEach { reason -> FilterChip(selected=reason in lossReasons,onClick={lossReasons=if(reason in lossReasons) lossReasons-reason else lossReasons+reason},label={Text(reason)},modifier=Modifier.padding(end=6.dp)) } }
    val sum=values.values.sumOf{it.value.toDoubleOrNull()?:0.0}; Text("总分：${sum.toInt()} / ${values.size * 100}    综合得分率：%.1f%%".format(if(values.isEmpty())0.0 else sum/values.size),fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=18.dp)); Button(onClick={vm.saveExam(Exam("new-${System.currentTimeMillis()}",name,LocalDate.now().toString(),ExamKind.COMPREHENSIVE,values.map{SubjectScore(it.key,it.value.value.toDoubleOrNull()?:0.0,100.0,lossReasons.toList())}));onSaved()},modifier=Modifier.padding(top=12.dp)){Text("保存考试记录")}
}
@Composable private fun SubjectTestForm(vm:DemoViewModel,onSaved:()->Unit){ var name by remember{mutableStateOf("单科测试")};var subject by remember(vm.subjects){mutableStateOf(vm.subjects.firstOrNull()?:"")};var score by remember{mutableStateOf("45")};var full by remember{mutableStateOf("50")};Column(Modifier.padding(top=18.dp).widthIn(max=700.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Field("考试名称",name,{name=it});Text("选择科目",fontWeight=FontWeight.Bold); vm.subjects.chunked(4).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach{ item -> FilterChip(selected=subject==item,onClick={subject=item},label={Text(item)})}} };Field("自定义科目",subject,{subject=it});Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Field("成绩",score,{score=it},Modifier.weight(1f));Field("满分",full,{full=it},Modifier.weight(1f))};val rate=(score.toDoubleOrNull()?:0.0)/(full.toDoubleOrNull()?:1.0)*100;Text("得分率：%.1f%%".format(rate),fontWeight=FontWeight.Bold);Button(onClick={vm.saveExam(Exam("test-${System.currentTimeMillis()}",name,LocalDate.now().toString(),ExamKind.SUBJECT_TEST,listOf(SubjectScore(subject,score.toDoubleOrNull()?:0.0,full.toDoubleOrNull()?:100.0))));onSaved()}){Text("保存单科测试")}} }
@Composable private fun Field(label:String,value:String,onChange:(String)->Unit,modifier:Modifier=Modifier,enabled:Boolean=true)=OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},singleLine=true,enabled=enabled,modifier=modifier)
