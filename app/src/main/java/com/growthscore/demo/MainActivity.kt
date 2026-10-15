package com.growthscore.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.content.res.Configuration
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.growthscore.demo.screens.*
import com.growthscore.demo.ui.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() { override fun onCreate(state: Bundle?) { super.onCreate(state); setContent { GrowthTheme { App() } } } }
private enum class RootTab(val label:String){ HOME("首页"), ENTRY("成绩录入"), ANALYSIS("分析"), EXPLORE("探索"), PROFILE("我的") }
@Composable private fun App(){ var splash by remember{mutableStateOf(true)}; LaunchedEffect(Unit){delay(3000);splash=false}; if(splash) Image(painterResource(R.drawable.chaoyue_splash),"超越启动页",Modifier.fillMaxSize(),contentScale=ContentScale.Crop) else GrowthApp() }
@Composable private fun GrowthApp(vm:DemoViewModel=viewModel()){
    val compact=LocalConfiguration.current.orientation==Configuration.ORIENTATION_LANDSCAPE
    var tab by rememberSaveable{mutableStateOf(RootTab.HOME)}
    var analysisImmersive by rememberSaveable { mutableStateOf(false) }
    var updatePrompt by remember{mutableStateOf(false)}
    val context=LocalContext.current
    val view=LocalView.current
    DisposableEffect(tab,analysisImmersive){
        val activity=context as? android.app.Activity
        val controller=activity?.let { WindowCompat.getInsetsController(it.window,view) }
        controller?.systemBarsBehavior=androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if(tab==RootTab.ANALYSIS) controller?.hide(WindowInsetsCompat.Type.statusBars()) else controller?.show(WindowInsetsCompat.Type.statusBars())
        if(tab==RootTab.ANALYSIS && analysisImmersive) controller?.hide(WindowInsetsCompat.Type.navigationBars()) else controller?.show(WindowInsetsCompat.Type.navigationBars())
        onDispose { }
    }
    LaunchedEffect(vm.updateStatus){val status=vm.updateStatus;if(status is UpdateStatus.Found && status.release.version.removePrefix("v")!=vm.currentVersion)updatePrompt=true}
    CompositionLocalProvider(LocalCompactLayout provides compact){
        Scaffold(bottomBar={if(!(tab==RootTab.ANALYSIS && analysisImmersive)) CompactNavigation(tab,{ tab=it; analysisImmersive=false })}){padding->
            Box(Modifier.padding(if(tab==RootTab.ANALYSIS && analysisImmersive) PaddingValues(0.dp) else padding)){
                when(tab){
                    RootTab.HOME->DashboardScreen(vm)
                    RootTab.ENTRY->EntryScreen(vm){}
                    RootTab.ANALYSIS->AnalysisScreen(vm,analysisImmersive,{analysisImmersive=it})
                    RootTab.EXPLORE->ExploreScreen()
                    RootTab.PROFILE->ProfileScreen(vm)
                }
            }
        }
    }
    if(updatePrompt) UpdatePrompt(vm,{updatePrompt=false})
}
@Composable private fun CompactNavigation(selected:RootTab,onSelect:(RootTab)->Unit)=Surface(shadowElevation=3.dp){
    Row(Modifier.fillMaxWidth().height(48.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
        RootTab.entries.forEach{item->
            IconButton(onClick={onSelect(item)},modifier=Modifier.size(48.dp)){
                Surface(shape=MaterialTheme.shapes.large,color=if(item==selected)MaterialTheme.colorScheme.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent){
                    Icon(TabIcon(item),item.label,modifier=Modifier.padding(10.dp).size(25.dp),tint=if(item==selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
private fun TabIcon(item:RootTab)=when(item){RootTab.HOME->Icons.Default.Home;RootTab.ENTRY->Icons.Default.AddCircle;RootTab.ANALYSIS->Icons.Default.Analytics;RootTab.EXPLORE->Icons.Default.Search;RootTab.PROFILE->Icons.Default.Person}
@Composable private fun UpdatePrompt(vm:DemoViewModel,dismiss:()->Unit){val context=androidx.compose.ui.platform.LocalContext.current;val status=vm.updateStatus as? UpdateStatus.Found ?: return;val download=vm.downloadStatus;AlertDialog(onDismissRequest=dismiss,title={Text("发现新版本 ${status.release.version}")},text={when(download){is DownloadStatus.Downloading->Column{Text("正在下载：${download.percent}%");LinearProgressIndicator(progress={download.percent/100f},modifier=Modifier.fillMaxWidth().padding(top=10.dp))};is DownloadStatus.Ready->Text("下载完成，请按系统提示确认安装。");DownloadStatus.Idle->Text("已为你准备更新包。下载过程会显示进度，安装仍需通过 Android 系统确认。")}},confirmButton={when(download){is DownloadStatus.Ready->TextButton({context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).setDataAndType(download.uri,"application/vnd.android.package-archive").addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION))}){Text("安装")};is DownloadStatus.Downloading->{};DownloadStatus.Idle->TextButton({vm.downloadUpdate(status.release.apkUrl)}){Text("立即更新")}}},dismissButton={TextButton(dismiss){Text("稍后")}})}
