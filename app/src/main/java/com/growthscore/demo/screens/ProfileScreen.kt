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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.UpdateStatus

@Composable fun ProfileScreen(vm: DemoViewModel) {
    var help by remember { mutableStateOf(false) }; var studentEdit by remember { mutableStateOf(false) }; var subjectEdit by remember { mutableStateOf(false) }; var update by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text("我的",fontSize=28.sp,fontWeight=FontWeight.Bold)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if(maxWidth>=760.dp) Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { InfoCards(vm,{studentEdit=true},{subjectEdit=true}) }
            else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) { InfoCards(vm,{studentEdit=true},{subjectEdit=true}) }
        }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("目标管理",fontSize=19.sp,fontWeight=FontWeight.Bold); vm.goals.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){row.forEach{Goal(it.subject,it.target){v->vm.updateGoal(it.subject,v)}}} } } }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { OutlinedButton({help=true}){Text("帮助中心")}; OutlinedButton({update=true}){Text("检查更新")} }
    }
    if(studentEdit) StudentDialog(vm){studentEdit=false}; if(subjectEdit) SubjectsDialog(vm){subjectEdit=false}; if(help) HelpDialog{help=false}; if(update) UpdateDialog(vm){update=false}
}
@Composable private fun RowScope.InfoCards(vm:DemoViewModel,editStudent:()->Unit,editSubjects:()->Unit) { InfoCardsBody(vm,editStudent,editSubjects,Modifier.weight(1f),Modifier.weight(1f),Modifier.weight(1f)) }
@Composable private fun ColumnScope.InfoCards(vm:DemoViewModel,editStudent:()->Unit,editSubjects:()->Unit) { InfoCardsBody(vm,editStudent,editSubjects,Modifier.fillMaxWidth(),Modifier.fillMaxWidth(),Modifier.fillMaxWidth()) }
@Composable private fun InfoCardsBody(vm:DemoViewModel,editStudent:()->Unit,editSubjects:()->Unit,studentModifier:Modifier,subjectsModifier:Modifier,dataModifier:Modifier) {
    Card(studentModifier) { Column(Modifier.padding(16.dp)) { Text("学生信息",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("姓名：${vm.student.name}"); Text("${vm.student.grade} · ${vm.student.term}"); Text(vm.student.schoolYear); TextButton(editStudent){Text("编辑资料")} } }
    Card(subjectsModifier) { Column(Modifier.padding(16.dp)) { Text("学科设置",fontSize=19.sp,fontWeight=FontWeight.Bold); Text(vm.subjects.joinToString("、")); TextButton(editSubjects){Text("管理学科")} } }
    Card(dataModifier) { Column(Modifier.padding(16.dp)) { Text("本地数据",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("资料、学科、目标和成绩仅保存在本机。") } }
}
@Composable private fun StudentDialog(vm:DemoViewModel,dismiss:()->Unit) { var name by remember{mutableStateOf(vm.student.name)};var grade by remember{mutableStateOf(vm.student.grade)};var term by remember{mutableStateOf(vm.student.term)};var year by remember{mutableStateOf(vm.student.schoolYear)};AlertDialog(onDismissRequest=dismiss,title={Text("编辑学生信息")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text("保存后仅写入本机，不会上传。",style=MaterialTheme.typography.bodySmall);EditField("姓名",name){name=it};EditField("年级",grade){grade=it};EditField("学期",term){term=it};EditField("学年",year){year=it}}},confirmButton={TextButton({vm.updateStudent(name,grade,term,year);dismiss()}){Text("保存")}},dismissButton={TextButton(dismiss){Text("取消")}}) }
@Composable private fun SubjectsDialog(vm:DemoViewModel,dismiss:()->Unit) { val defaults=listOf("语文","数学","英语","物理","化学","历史","地理","生物","道德与法治");var selected by remember{mutableStateOf(vm.subjects.toSet())};var custom by remember{mutableStateOf("")};AlertDialog(onDismissRequest=dismiss,title={Text("学科设置")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("可勾选默认科目，也可添加自定义名称。",style=MaterialTheme.typography.bodySmall);defaults.chunked(3).forEach{row->Row{row.forEach{s->FilterChip(selected=s in selected,onClick={selected=if(s in selected)selected-s else selected+s},label={Text(s)},modifier=Modifier.padding(end=5.dp))}}};EditField("自定义学科",custom){custom=it};Text("当前：${selected.joinToString("、")}",style=MaterialTheme.typography.bodySmall)}},confirmButton={TextButton({vm.updateSubjects((selected+custom.split("，",","," ")).toList());dismiss()}){Text("保存")}},dismissButton={TextButton(dismiss){Text("取消")}}) }
@Composable private fun HelpDialog(dismiss:()->Unit)=AlertDialog(onDismissRequest=dismiss,title={Text("帮助中心")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("产品初心",fontWeight=FontWeight.Bold);Text("超越希望把一次次成绩变成可理解的成长轨迹：看见优势，也看见下一步可以努力的方向。所有记录默认留在你的设备上。");Text("使用方法",fontWeight=FontWeight.Bold);Text("1. 在“我的”填写学生信息并设置学习科目。\n2. 在“成绩录入”选择考试类型并保存。\n3. 在首页和分析中心查看趋势、学科表现和目标差距。\n4. 可随时修改目标或学科；后续新增记录会采用最新学科。");Text("更新与隐私",fontWeight=FontWeight.Bold);Text("检查更新会访问 GitHub Releases 获取版本信息。下载 APK 后，系统会要求确认安装；请只安装官方发布页的包。")}},confirmButton={TextButton(dismiss){Text("知道了")}})
@Composable private fun UpdateDialog(vm:DemoViewModel,dismiss:()->Unit){val context=LocalContext.current;AlertDialog(onDismissRequest=dismiss,title={Text("检查更新")},text={when(val status=vm.updateStatus){UpdateStatus.Idle->Text("当前版本：${vm.currentVersion}\n\n将访问 GitHub Releases 检查最新版本。") ;UpdateStatus.Checking->Text("正在检查 GitHub Releases…");is UpdateStatus.Failed->Text(status.message);is UpdateStatus.Found->{val r=status.release;Column{Text("当前版本：${vm.currentVersion}\n最新发布：${r.version}");Text("下载和安装均需你在系统中确认。",style=MaterialTheme.typography.bodySmall);if(r.apkUrl.isBlank())Text("该发布未附带 APK，请前往发布页查看。",style=MaterialTheme.typography.bodySmall)}}}},confirmButton={when(val status=vm.updateStatus){UpdateStatus.Idle,is UpdateStatus.Failed->TextButton({vm.checkUpdate()}){Text("检查")};UpdateStatus.Checking->{};is UpdateStatus.Found->TextButton({val target=status.release.apkUrl.ifBlank{status.release.releaseUrl};context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)));dismiss()}){Text("打开官方下载")}}},dismissButton={TextButton(dismiss){Text("关闭")}})}
@Composable private fun Goal(subject:String,target:Double,onSave:(Double)->Unit){var input by remember(target){mutableStateOf(target.toInt().toString())};Column(Modifier.fillMaxWidth().padding(top=10.dp)){Text("${subject}目标",fontWeight=FontWeight.Bold);OutlinedTextField(input,{input=it},suffix={Text("%")},singleLine=true,modifier=Modifier.fillMaxWidth());Button({onSave(input.toDoubleOrNull()?:target)}){Text("保存目标")}}}
@Composable private fun EditField(label:String,value:String,onChange:(String)->Unit)=OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth())
