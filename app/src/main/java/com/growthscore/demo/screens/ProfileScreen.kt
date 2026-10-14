package com.growthscore.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.data.DemoRepository
import com.growthscore.demo.ui.DemoViewModel
import com.growthscore.demo.ui.Blue

@Composable fun ProfileScreen(vm: DemoViewModel) {
  var help by remember { mutableStateOf(false) }
  var update by remember { mutableStateOf(false) }
  Column(Modifier.fillMaxSize().padding(28.dp)) {
    Text("我的",fontSize=28.sp,fontWeight=FontWeight.Bold)
    Row(Modifier.fillMaxWidth().padding(top=18.dp),horizontalArrangement=Arrangement.spacedBy(18.dp)) {
      Card(Modifier.weight(1f)) { Column(Modifier.padding(20.dp)) { Text("学生信息",fontSize=20.sp); Text("姓名：${vm.student.name}"); Text("年级：${vm.student.grade}"); Text("学期：${vm.student.term}") } }
      Card(Modifier.weight(1f)) { Column(Modifier.padding(20.dp)) { Text("学科设置",fontSize=20.sp); Text(DemoRepository.subjects.joinToString("、")) } }
      Card(Modifier.weight(1f)) { Column(Modifier.padding(20.dp)) { Text("数据管理",fontSize=20.sp); listOf("导入数据","导出数据","清除数据").forEach { OutlinedButton({},Modifier.padding(top=8.dp)){Text(it)} } } }
    }
    Card(Modifier.fillMaxWidth().padding(top=18.dp)) { Column(Modifier.padding(20.dp)) { Text("目标管理",fontSize=20.sp); Row { vm.goals.forEach { Goal(it.subject,it.target){value->vm.updateGoal(it.subject,value)} } } } }
    Row(Modifier.padding(top=18.dp), horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      OutlinedButton(onClick = { help = true }) { Text("帮助中心") }
      OutlinedButton(onClick = { update = true }) { Text("检查更新") }
    }
  }
  if(help) AlertDialog(onDismissRequest={help=false},confirmButton={TextButton({help=false}){Text("知道了")}},title={Text("帮助中心")},text={Text("超越帮助你记录每一次进步。可在成绩录入中新增考试，在分析中心查看成长趋势与学习建议。")})
  if(update) AlertDialog(onDismissRequest={update=false},confirmButton={TextButton({update=false}){Text("确定")}},title={Text("检查更新")},text={Text("当前已是最新版本：v1.1.0-demo\n更新包会发布在 GitHub Releases。")})
}
@Composable private fun Goal(subject:String,target:Double,onSave:(Double)->Unit) {
  var input by remember(target){mutableStateOf(target.toInt().toString())}; val current=DemoRepository.exams.last().subjectScores.find{it.subject==subject}?.rate?:0.0
  Column(Modifier.width(180.dp).padding(end=12.dp)) { Text("${subject}目标",fontWeight=FontWeight.Bold); OutlinedTextField(input,{input=it},suffix={Text("%")},singleLine=true); Text("当前差距：%.1f%%".format(target-current),fontSize=12.sp); Button({onSave(input.toDoubleOrNull()?:target)}){Text("保存目标")} }
}
