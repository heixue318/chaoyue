package com.growthscore.demo.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.model.Student
import com.growthscore.demo.ui.*

@Composable fun ProfileScreen(vm: DemoViewModel) {
    val compact=LocalCompactLayout.current; var studentEdit by remember { mutableStateOf(false) }; var subjectEdit by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(false) }; var backupMessage by remember { mutableStateOf<String?>(null) }; var restoreUri by remember { mutableStateOf<Uri?>(null) }; var clearConfirm by remember { mutableStateOf(false) }
    val importBackup=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { restoreUri=it }
    Column(Modifier.fillMaxSize().padding(if(compact)8.dp else 16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(if(compact)8.dp else 14.dp)) {
        Column { Text("我的",style=if(compact)MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall); Text("资料、目标与数据管理",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
        Card(Modifier.fillMaxWidth()) { StudentInfo(vm,{studentEdit=true}) }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("学科设置",fontSize=19.sp,fontWeight=FontWeight.Bold); Text(vm.subjects.ifEmpty { listOf("尚未设置学科") }.joinToString("、")); Text("可设置满分、目标和自定义学科",style=MaterialTheme.typography.bodySmall); TextButton({subjectEdit=true}){Text("管理学科与目标")} } }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { OutlinedButton({update=true}){Text("检查更新")} }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) { Text("数据备份与恢复",fontSize=19.sp,fontWeight=FontWeight.Bold);Text("备份包含个人信息、头像引用、成绩与当次满分、学科设置、学习目标、学习计划及任务完成状态。恢复前会校验文件。",style=MaterialTheme.typography.bodySmall);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){TextButton({backupMessage=vm.exportBackup().getOrElse { "导出失败：${it.message}" }}){Text("导出 CSV")};TextButton({importBackup.launch(arrayOf("text/*","text/csv"))}){Text("导入备份")}};TextButton({clearConfirm=true},colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.error)){Text("清空所有本机数据")} } }
        Text("v${vm.currentVersion}  ·  作者：风乱吹  ·  仅供交流学习使用",modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if(studentEdit) StudentDialog(vm){studentEdit=false}; if(subjectEdit) SubjectsDialog(vm){subjectEdit=false}; if(update) UpdateDialog(vm){update=false}
    backupMessage?.let { AlertDialog(onDismissRequest={backupMessage=null},title={Text("数据备份")},text={Text(it)},confirmButton={TextButton({backupMessage=null}){Text("知道了")}}) }
    restoreUri?.let { uri -> AlertDialog(onDismissRequest={restoreUri=null},title={Text("恢复完整备份？")},text={Text("恢复会覆盖当前本机数据。文件会先校验，校验失败不会修改现有数据。")},confirmButton={TextButton({backupMessage=vm.restoreBackup(uri).getOrElse { "恢复失败：${it.message}" };restoreUri=null}){Text("确认恢复")}},dismissButton={TextButton({restoreUri=null}){Text("取消")}}) }
    if(clearConfirm) AlertDialog(onDismissRequest={clearConfirm=false},title={Text("清空所有数据？")},text={Text("将清空个人资料、成绩、学科、目标和学习计划，恢复为空白初始状态。已导出的备份文件和成长报告不会被删除。")},confirmButton={TextButton({vm.clearAllData();clearConfirm=false;backupMessage="已清空本机数据"}){Text("确认清空")}},dismissButton={TextButton({clearConfirm=false}){Text("取消")}})
}

@Composable private fun StudentInfo(vm:DemoViewModel,onEdit:()->Unit) { val s=vm.student; Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text("学生信息",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("姓名：${s.name}"); Text("${s.grade} · ${s.term}"); Text(s.schoolYear); HealthSummary(s) }; StudentAvatar(s.avatarUri,Modifier.size(72.dp)) }; TextButton(onEdit){Text("编辑资料")} } }

@Composable private fun HealthSummary(s:Student) { val age=s.age; val sex=s.sex; val height=s.heightCm; val weight=s.weightKg; if(age==null||height==null||weight==null) return
    val bmi=weight/(height/100.0)/(height/100.0)
    HorizontalDivider(Modifier.padding(vertical=8.dp)); Text("健康参考",fontWeight=FontWeight.Bold)
    if(sex !in listOf("男","女")) { Text("BMI ${format1(bmi)}。填写性别后，可显示同龄参考身高、体重差距。",style=MaterialTheme.typography.bodySmall); Text("仅作健康教育参考，不代替医生评估。",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant); return }
    val ref=referenceFor(age,sex); val heightDiff=height-ref.first; val weightDiff=weight-ref.second
    val heightLabel=when { heightDiff<=-7 -> "偏矮倾向"; heightDiff>=7 -> "偏高倾向"; else -> "身高参考范围内" }; val weightLabel=when { weightDiff<=-ref.second*.15 -> "偏瘦倾向"; weightDiff>=ref.second*.15 -> "偏重倾向"; else -> "体重参考范围内" }
    Text("$heightLabel：${format1(heightDiff)} cm；$weightLabel：${format1(weightDiff)} kg；BMI ${format1(bmi)}",style=MaterialTheme.typography.bodySmall); Text("同龄同性别中位参考值：${format1(ref.first)} cm / ${format1(ref.second)} kg。仅作健康教育参考，不代替医生评估。",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun referenceFor(age:Int,sex:String):Pair<Double,Double> { val a=age.coerceIn(12,18); val boys=listOf(151.0 to 40.0,159.0 to 46.0,165.0 to 52.0,169.0 to 57.0,172.0 to 61.0,173.0 to 64.0,174.0 to 66.0); val girls=listOf(153.0 to 41.0,157.0 to 46.0,159.0 to 50.0,160.0 to 52.0,160.0 to 54.0,160.0 to 55.0,160.0 to 56.0); return if(sex=="女")girls[a-12] else boys[a-12] }
private fun format1(value:Double)=if(value>=0)"+${"%.1f".format(value)}" else "%.1f".format(value)

@Composable private fun StudentDialog(vm:DemoViewModel,dismiss:()->Unit) {
    var name by remember{mutableStateOf(vm.student.name)}; var grade by remember{mutableStateOf(vm.student.grade.takeIf { it in grades } ?: "初一")}; var term by remember{mutableStateOf(vm.student.term.takeIf { it in terms } ?: "上学期")}; var startYear by remember{mutableStateOf(vm.student.schoolYear.take(4).toIntOrNull() ?: 2026)}
    var avatar by remember{mutableStateOf(vm.student.avatarUri)}; var sex by remember{mutableStateOf(vm.student.sex)}; var age by remember{mutableStateOf(vm.student.age?.toString() ?: "")}; var height by remember{mutableStateOf(vm.student.heightCm?.toString() ?: "")}; var weight by remember{mutableStateOf(vm.student.weightKg?.toString() ?: "")}
    val context=LocalContext.current; val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){runCatching{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)};avatar=uri.toString()}}
    val schoolYear=if(term=="上学期") "${startYear}-${startYear+1}学年" else "${startYear}-${startYear}学年"
    AlertDialog(onDismissRequest=dismiss,title={Text("编辑学生信息")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(verticalAlignment=Alignment.CenterVertically){StudentAvatar(avatar,Modifier.size(76.dp).clickable{picker.launch(arrayOf("image/*"))});TextButton({picker.launch(arrayOf("image/*"))}){Text("选择头像")}};EditField("姓名",name){name=it};SelectField("年级",grade,grades){grade=it};SelectField("学期",term,terms){term=it};SelectField("学年起始年份",startYear.toString(),(2024..2032).map(Int::toString)){startYear=it.toInt()};Text("自动生成：$schoolYear",color=MaterialTheme.colorScheme.primary);SelectField("性别（用于健康参考）",sex,listOf("未设置","男","女")){sex=it};EditField("年龄（周岁）",age){age=it};EditField("身高（cm）",height){height=it};EditField("体重（kg）",weight){weight=it}}},confirmButton={TextButton({vm.updateStudent(name,grade,term,schoolYear,avatar,sex,age.toIntOrNull()?.takeIf { it in 7..18 },height.toDoubleOrNull()?.takeIf { it in 60.0..230.0 },weight.toDoubleOrNull()?.takeIf { it in 15.0..200.0 });dismiss()}){Text("保存")}},dismissButton={TextButton(dismiss){Text("取消")}})
}

@Composable private fun SubjectsDialog(vm:DemoViewModel,dismiss:()->Unit) {
    val defaults=listOf("语文","数学","英语","物理","化学","历史","地理","生物","道德与法治")
    var selected by remember{mutableStateOf(vm.subjects.filter { it in defaults }.toSet())}
    val custom=remember{mutableStateListOf<String>().also { it.addAll(vm.subjects.filter { subject -> subject !in defaults }) }}
    var newCustom by remember{mutableStateOf("")}; var renameIndex by remember{mutableIntStateOf(-1)}
    val full=remember{mutableStateMapOf<String,String>().also { map->vm.subjects.forEach{map[it]=vm.fullScore(it).toString()} }}
    val goals=remember{mutableStateMapOf<String,String>().also { map->vm.subjects.forEach{map[it]=(vm.goals.find { g->g.subject==it }?.target?:90.0).toString()} }}
    var goalTerm by remember { mutableStateOf(vm.goals.firstOrNull()?.term ?: vm.student.term) }; var goalsEnabled by remember { mutableStateOf(vm.goals.firstOrNull()?.enabled ?: true) }
    val allSubjects=(selected.toList()+custom.toList()).distinct()
    AlertDialog(onDismissRequest=dismiss,title={Text("学科、满分与学习目标")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(7.dp)) {
        Text("自定义学科可重命名或删除；删除只移除当前设置，不会篡改历史成绩快照。",style=MaterialTheme.typography.bodySmall)
        SelectField("目标适用学期",goalTerm,listOf("当前学期",vm.student.term,"上学期","下学期").distinct()){goalTerm=it}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(goalsEnabled,{goalsEnabled=it});Text("启用学习目标")}
        defaults.chunked(3).forEach { row->Row { row.forEach { subject->FilterChip(subject in selected,{selected=if(subject in selected)selected-subject else selected+subject;full.putIfAbsent(subject,"100");goals.putIfAbsent(subject,"90")},label={Text(subject)},modifier=Modifier.padding(end=4.dp))} } }
        Row { OutlinedTextField(newCustom,{newCustom=it},label={Text("新增自定义学科")},singleLine=true,modifier=Modifier.weight(1f));TextButton({newCustom.trim().takeIf { it.isNotEmpty() }?.let { value->custom.add(value);full.putIfAbsent(value,"100");goals.putIfAbsent(value,"90");newCustom="" }}){Text("添加")} }
        custom.forEachIndexed { index,subject->Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(subject,Modifier.weight(1f));IconButton({renameIndex=index}){Icon(Icons.Default.Edit,"重命名")};IconButton({custom.removeAt(index)}){Icon(Icons.Default.Delete,"删除")}} }
        allSubjects.forEach { subject->OutlinedTextField(full[subject]?:"100",{full[subject]=it},label={Text("$subject 满分")},suffix={Text("分")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(goals[subject]?:"90",{goals[subject]=it},label={Text("$subject 学习目标")},suffix={Text("%")},singleLine=true,modifier=Modifier.fillMaxWidth())}
    }},confirmButton={TextButton({vm.updateSubjects(allSubjects);vm.updateFullScores(allSubjects.associateWith{full[it]?.toDoubleOrNull()?:100.0});vm.updateGoals(allSubjects.associateWith{goals[it]?.toDoubleOrNull()?:90.0},goalTerm,goalsEnabled);dismiss()}){Text("保存")}},dismissButton={TextButton(dismiss){Text("取消")}})
    if(renameIndex in custom.indices) { var value by remember(renameIndex){mutableStateOf(custom[renameIndex])}; AlertDialog(onDismissRequest={renameIndex=-1},title={Text("重命名自定义学科")},text={EditField("名称",value){value=it}},confirmButton={TextButton({value.trim().takeIf{it.isNotEmpty()}?.let{custom[renameIndex]=it};renameIndex=-1}){Text("保存")}},dismissButton={TextButton({renameIndex=-1}){Text("取消")}}) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SelectField(label:String,value:String,options:List<String>,onSelect:(String)->Unit) { var expanded by remember { mutableStateOf(false) }; ExposedDropdownMenuBox(expanded,{expanded=it}) { OutlinedTextField(value=value,onValueChange={},label={Text(label)},readOnly=true,trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.menuAnchor().fillMaxWidth());ExposedDropdownMenu(expanded,{expanded=false}){options.forEach{DropdownMenuItem(text={Text(it)},onClick={onSelect(it);expanded=false})}} } }
@Composable private fun StudentAvatar(uri:String,modifier:Modifier=Modifier){val context=LocalContext.current;val bitmap=remember(uri){runCatching{context.contentResolver.openInputStream(Uri.parse(uri))?.use{BitmapFactory.decodeStream(it)}}.getOrNull()};Box(modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),contentAlignment=Alignment.Center){if(bitmap!=null)Image(bitmap.asImageBitmap(),"学生头像",Modifier.fillMaxSize()) else Text("头像",style=MaterialTheme.typography.labelSmall)}}
@Composable private fun UpdateDialog(vm:DemoViewModel,dismiss:()->Unit){val context=LocalContext.current;AlertDialog(onDismissRequest=dismiss,title={Text("软件更新")},text={when(val download=vm.downloadStatus){is DownloadStatus.Downloading->Column{Text("正在下载更新包：${download.percent}%");LinearProgressIndicator(progress={download.percent/100f},modifier=Modifier.fillMaxWidth().padding(top=10.dp))};is DownloadStatus.Ready->Text("下载完成，点击安装后请按 Android 系统提示确认。");DownloadStatus.Idle->when(val status=vm.updateStatus){UpdateStatus.Idle->Text("当前版本：${vm.currentVersion}");UpdateStatus.Checking->Text("正在检查新版本…");is UpdateStatus.Failed->Text(status.message);is UpdateStatus.Found->Text("当前版本：${vm.currentVersion}\n最新版本：${status.release.version}")}}},confirmButton={when(val download=vm.downloadStatus){is DownloadStatus.Ready->TextButton({context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(download.uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}){Text("安装")};is DownloadStatus.Downloading->{};DownloadStatus.Idle->when(val status=vm.updateStatus){is UpdateStatus.Found->TextButton({if(status.release.apkUrl.isNotBlank())vm.downloadUpdate(status.release.apkUrl)}){Text("下载更新")};else->TextButton({vm.checkUpdate()}){Text("检查")}}}},dismissButton={TextButton(dismiss){Text("关闭")}})}
@Composable private fun EditField(label:String,value:String,onChange:(String)->Unit)=OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth())
private val grades=listOf("初一","初二","初三","高一","高二","高三")
private val terms=listOf("上学期","下学期")
