package com.growthscore.demo.screens

import android.content.Intent
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
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.DownloadStatus
import com.growthscore.demo.ui.LocalCompactLayout
import com.growthscore.demo.ui.UpdateStatus

@Composable fun ProfileScreen(vm: DemoViewModel) {
    val compact=LocalCompactLayout.current
    var help by remember { mutableStateOf(false) }; var studentEdit by remember { mutableStateOf(false) }
    var subjectEdit by remember { mutableStateOf(false) }; var update by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(if(compact)8.dp else 16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(if(compact)8.dp else 14.dp)) {
        Text("我的",fontSize=if(compact)22.sp else 28.sp,fontWeight=FontWeight.Bold)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if(maxWidth>=760.dp) Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { InfoCards(vm,{studentEdit=true},{subjectEdit=true}) }
            else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) { InfoCards(vm,{studentEdit=true},{subjectEdit=true}) }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { OutlinedButton({help=true}){Text("帮助中心")}; OutlinedButton({update=true}){Text("检查更新")} }
    }
    if(studentEdit) StudentDialog(vm){studentEdit=false}
    if(subjectEdit) SubjectsDialog(vm){subjectEdit=false}
    if(help) HelpDialog{help=false}
    if(update) UpdateDialog(vm){update=false}
}

@Composable private fun RowScope.InfoCards(vm:DemoViewModel,editStudent:()->Unit,editSubjects:()->Unit) = InfoCardsBody(vm,editStudent,editSubjects,Modifier.weight(1f),Modifier.weight(1f),Modifier.weight(1f))
@Composable private fun ColumnScope.InfoCards(vm:DemoViewModel,editStudent:()->Unit,editSubjects:()->Unit) = InfoCardsBody(vm,editStudent,editSubjects,Modifier.fillMaxWidth(),Modifier.fillMaxWidth(),Modifier.fillMaxWidth())
@Composable private fun InfoCardsBody(vm:DemoViewModel,editStudent:()->Unit,editSubjects:()->Unit,studentModifier:Modifier,subjectsModifier:Modifier,dataModifier:Modifier) {
    Card(studentModifier) { Column(Modifier.padding(16.dp)) { Text("学生信息",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("姓名：${vm.student.name}"); Text("${vm.student.grade} · ${vm.student.term}"); Text(vm.student.schoolYear); TextButton(editStudent){Text("编辑资料")} } }
    Card(subjectsModifier) { Column(Modifier.padding(16.dp)) { Text("学科设置",fontSize=19.sp,fontWeight=FontWeight.Bold); Text(vm.subjects.joinToString("、")); Text("可设置满分与学习目标",style=MaterialTheme.typography.bodySmall); TextButton(editSubjects){Text("管理学科与目标")} } }
    Card(dataModifier) { Column(Modifier.padding(16.dp)) { Text("本地数据",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("资料、学科、目标和成绩仅保存在本机。") } }
}

@Composable private fun StudentDialog(vm:DemoViewModel,dismiss:()->Unit) {
    var name by remember{mutableStateOf(vm.student.name)}; var grade by remember{mutableStateOf(vm.student.grade)}
    var term by remember{mutableStateOf(vm.student.term)}; var year by remember{mutableStateOf(vm.student.schoolYear)}
    AlertDialog(onDismissRequest=dismiss,title={Text("编辑学生信息")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text("保存后仅写入本机，不会上传。",style=MaterialTheme.typography.bodySmall);EditField("姓名",name){name=it};EditField("年级",grade){grade=it};EditField("学期",term){term=it};EditField("学年",year){year=it}}},confirmButton={TextButton({vm.updateStudent(name,grade,term,year);dismiss()}){Text("保存")}},dismissButton={TextButton(dismiss){Text("取消")}})
}

@Composable private fun SubjectsDialog(vm:DemoViewModel,dismiss:()->Unit) {
    val defaults=listOf("语文","数学","英语","物理","化学","历史","地理","生物","道德与法治")
    var selected by remember{mutableStateOf(vm.subjects.toSet())}; var custom by remember{mutableStateOf("")}
    val full=remember{mutableStateMapOf<String,String>().also { map -> vm.subjects.forEach { subject -> map[subject]=vm.fullScore(subject).toInt().toString() } }}
    val goals=remember{mutableStateMapOf<String,String>().also { map -> vm.subjects.forEach { subject -> map[subject]=(vm.goals.find { it.subject==subject }?.target?:90.0).toInt().toString() } }}
    fun configuredSubjects()=(selected+custom.split("，",","," ")).map(String::trim).filter(String::isNotBlank).distinct()
    AlertDialog(onDismissRequest=dismiss,title={Text("学科、满分与学习目标")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(7.dp)) {
        Text("为每科设置满分和目标得分率；目标用于分析中心的差距计算。",style=MaterialTheme.typography.bodySmall)
        defaults.chunked(3).forEach { row -> Row { row.forEach { subject -> FilterChip(selected=subject in selected,onClick={selected=if(subject in selected)selected-subject else selected+subject; if(subject !in full)full[subject]="100";if(subject !in goals)goals[subject]="90"},label={Text(subject)},modifier=Modifier.padding(end=5.dp)) } } }
        EditField("自定义学科",custom){custom=it}
        configuredSubjects().forEach { subject ->
            if(subject !in full) full[subject]="100"; if(subject !in goals) goals[subject]="90"
            OutlinedTextField(value=full[subject] ?: "100",onValueChange={full[subject]=it},label={Text("${subject} 满分")},suffix={Text("分")},singleLine=true,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=goals[subject] ?: "90",onValueChange={goals[subject]=it},label={Text("${subject} 学习目标")},suffix={Text("%")},singleLine=true,modifier=Modifier.fillMaxWidth())
        }
    }},confirmButton={TextButton({val subjects=configuredSubjects();vm.updateSubjects(subjects);vm.updateFullScores(subjects.associateWith { full[it]?.toDoubleOrNull()?:100.0 });vm.updateGoals(subjects.associateWith { goals[it]?.toDoubleOrNull()?:90.0 });dismiss()}){Text("保存")}},dismissButton={TextButton(dismiss){Text("取消")}})
}

@Composable private fun HelpDialog(dismiss:()->Unit)=AlertDialog(onDismissRequest=dismiss,title={Text("帮助中心")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("产品初心",fontWeight=FontWeight.Bold);Text("超越希望把一次次成绩变成可理解的成长轨迹：看见优势，也看见下一步可以努力的方向。所有记录默认留在你的设备上。");Text("使用方法",fontWeight=FontWeight.Bold);Text("1. 在“我的”填写学生信息、设置学科满分和学习目标。\n2. 在“成绩录入”选择考试类型并保存。\n3. 在分析中心的“目标差距”查看每科距离目标还差多少分。\n4. 可随时修改目标或学科；后续新增记录会采用最新设置。")}},confirmButton={TextButton(dismiss){Text("知道了")}})
@Composable private fun UpdateDialog(vm:DemoViewModel,dismiss:()->Unit){val context=LocalContext.current;AlertDialog(onDismissRequest=dismiss,title={Text("软件更新")},text={when(val download=vm.downloadStatus){is DownloadStatus.Downloading->Column{Text("正在下载更新包：${download.percent}%");LinearProgressIndicator(progress={download.percent/100f},modifier=Modifier.fillMaxWidth().padding(top=10.dp))};is DownloadStatus.Ready->Text("下载完成，点击安装后请按 Android 系统提示确认。");DownloadStatus.Idle->when(val status=vm.updateStatus){UpdateStatus.Idle->Text("当前版本：${vm.currentVersion}");UpdateStatus.Checking->Text("正在检查新版本…");is UpdateStatus.Failed->Text(status.message);is UpdateStatus.Found->Text("当前版本：${vm.currentVersion}\n最新版本：${status.release.version}")}}},confirmButton={when(val download=vm.downloadStatus){is DownloadStatus.Ready->TextButton({context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(download.uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}){Text("安装")};is DownloadStatus.Downloading->{};DownloadStatus.Idle->when(val status=vm.updateStatus){is UpdateStatus.Found->TextButton({if(status.release.apkUrl.isNotBlank())vm.downloadUpdate(status.release.apkUrl)}){Text("下载更新")};else->TextButton({vm.checkUpdate()}){Text("检查")}}}},dismissButton={TextButton(dismiss){Text("关闭")}})}
@Composable private fun EditField(label:String,value:String,onChange:(String)->Unit)=OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth())
