package com.growthscore.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.growthscore.demo.screens.*
import com.growthscore.demo.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { GrowthTheme { GrowthApp() } } }
}
private enum class RootTab(val label:String){ HOME("首页"), ENTRY("成绩录入"), ANALYSIS("分析"), PROFILE("我的") }
@Composable private fun GrowthApp(vm:DemoViewModel=viewModel()){
    var tab by rememberSaveable{mutableStateOf(RootTab.HOME)}
    Scaffold(bottomBar={NavigationBar{RootTab.entries.forEach{item->NavigationBarItem(selected=tab==item,onClick={tab=item},icon={Icon(when(item){RootTab.HOME->Icons.Default.Home;RootTab.ENTRY->Icons.Default.AddCircle;RootTab.ANALYSIS->Icons.Default.Analytics;RootTab.PROFILE->Icons.Default.Person},contentDescription=item.label)},label={Text(item.label)})}}}){padding->Box(Modifier.padding(padding)){when(tab){RootTab.HOME->DashboardScreen(vm);RootTab.ENTRY->EntryScreen(vm);RootTab.ANALYSIS->AnalysisScreen(vm);RootTab.PROFILE->ProfileScreen(vm)}}}
}
