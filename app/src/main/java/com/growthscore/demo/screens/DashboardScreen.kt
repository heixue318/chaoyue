package com.growthscore.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.components.LineChart
import com.growthscore.demo.model.levelFor
import com.growthscore.demo.ui.*

@Composable fun DashboardScreen(vm: DemoViewModel) {
    val latest = vm.latest; val previous = vm.exams[vm.exams.lastIndex - 1]; val delta = latest.rate - previous.rate
    Row(Modifier.fillMaxSize().padding(28.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column { Text(vm.student.name, fontSize=28.sp, fontWeight=FontWeight.Bold); Text("${vm.student.grade}${vm.student.term} · ${vm.student.schoolYear}", color=Color.Gray) }
            Card(colors=CardDefaults.cardColors(containerColor=PaleBlue), modifier=Modifier.fillMaxWidth().weight(1f)) { Column(Modifier.padding(25.dp)) {
                Text("最近一次综合考试", color=Color.Gray); Text(latest.name, fontSize=22.sp, fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(12.dp)); Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("综合得分率",color=Color.Gray); Text("%.1f%%".format(latest.rate), fontSize=52.sp, fontWeight=FontWeight.Bold); Text("★★★★★  ${levelFor(latest.rate)}",color=Green,fontWeight=FontWeight.Bold) }; Text("↑ +%.1f%%".format(delta),color=Green,fontSize=19.sp,fontWeight=FontWeight.Bold) }
            } }
            Card(Modifier.fillMaxWidth().weight(1f)) { Column(Modifier.padding(20.dp)) { Text("我的综合成长曲线",fontSize=20.sp,fontWeight=FontWeight.Bold); LineChart(vm.exams.map { it.rate },Modifier.padding(top=8.dp)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ vm.exams.takeLast(4).forEach{Text(it.name,fontSize=11.sp,color=Color.Gray)} } } }
        }
        Column(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Card(Modifier.fillMaxWidth().weight(1f)) { Column(Modifier.padding(20.dp)) { Text("优势学科 TOP 3",fontSize=20.sp,fontWeight=FontWeight.Bold); latest.subjectScores.sortedByDescending { it.rate }.take(3).forEach { s -> SubjectLine(s.subject,s.rate,"保持优势") } } }
            Card(Modifier.fillMaxWidth().weight(1f)) { Column(Modifier.padding(20.dp)) { Text("提升空间",fontSize=20.sp,fontWeight=FontWeight.Bold); latest.subjectScores.sortedBy { it.rate }.take(3).forEach { s -> SubjectLine(s.subject,s.rate, if(s.subject=="英语") "加强词汇和阅读训练" else "梳理失分原因") } } }
        }
    }
}
@Composable private fun SubjectLine(subject:String,rate:Double,hint:String){ Row(Modifier.fillMaxWidth().padding(top=14.dp),verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(PaleBlue),contentAlignment=Alignment.Center){Text(subject.take(1),color=Blue,fontWeight=FontWeight.Bold)}; Column(Modifier.padding(start=12.dp).weight(1f)){Text(subject,fontWeight=FontWeight.Bold);Text(hint,fontSize=12.sp,color=Color.Gray)}; Text("%.0f%%".format(rate),fontWeight=FontWeight.Bold,color=if(rate>=90)Green else Orange) } }
