package com.growthscore.demo.ui

import androidx.compose.runtime.*
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.growthscore.demo.data.*
import com.growthscore.demo.model.*
import com.growthscore.demo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.content.ContentValues
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.BitmapFactory
import android.graphics.RectF
import com.growthscore.demo.model.formatRate
import com.growthscore.demo.plan.PlanEngine
import kotlinx.coroutines.delay
import java.time.LocalDate

class DemoViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalStore(application)
    private var data by mutableStateOf(store.load())
    val student get() = data.student
    val subjects get() = data.subjects
    val exams get() = data.exams
    val goals get() = data.goals
    val fullScores get() = data.fullScores
    val studyPlan get() = data.plan
    val latest get() = exams.last()
    val currentVersion = BuildConfig.VERSION_NAME
    var updateStatus by mutableStateOf<UpdateStatus>(UpdateStatus.Idle); private set
    var downloadStatus by mutableStateOf<DownloadStatus>(DownloadStatus.Idle); private set
    init { checkUpdate() }
    private fun persist(next: AppData) { data = next; store.save(next) }
    fun saveExam(exam: Exam) { persist(data.copy(exams = (data.exams + exam).takeLast(300))) }
    fun createStudyPlan(name:String,type:StudyPlanType,start:String,end:String,availability:Map<Int,Int>,subjects:List<String>):Result<StudyPlan> = runCatching {
        val plan=PlanEngine.generate(name,type,LocalDate.parse(start),LocalDate.parse(end),availability,subjects,data.exams,data.goals)
        persist(data.copy(plan=plan)); plan
    }
    fun togglePlanTask(taskId:String) {
        val plan=data.plan?:return
        val tasks=plan.tasks.map { task -> if(task.id!=taskId) task else if(task.status==PlanTaskStatus.COMPLETED) task.copy(status=PlanTaskStatus.PENDING,completedAt=null) else task.copy(status=PlanTaskStatus.COMPLETED,completedAt=System.currentTimeMillis()) }
        persist(data.copy(plan=plan.copy(tasks=tasks)))
    }
    fun pausePlan() { data.plan?.let { persist(data.copy(plan=it.copy(status=if(it.status==StudyPlanStatus.PAUSED)StudyPlanStatus.ACTIVE else StudyPlanStatus.PAUSED))) } }
    fun endPlan() { data.plan?.let { persist(data.copy(plan=it.copy(status=StudyPlanStatus.ENDED))) } }
    fun todayPlanTasks(date:String=LocalDate.now().toString())=data.plan?.takeIf { it.status==StudyPlanStatus.ACTIVE }?.tasks?.filter { it.plannedDate==date }.orEmpty()
    fun deleteExam(id: String) { if (data.exams.size > 1) persist(data.copy(exams = data.exams.filterNot { it.id == id })) }
    fun deleteExams(ids: Set<String>) { val remaining=data.exams.filterNot { it.id in ids }; if(remaining.isNotEmpty()) persist(data.copy(exams = remaining)) }
    fun exportBackup(): Result<String> = runCatching {
        val context=getApplication<Application>(); val values=ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME,"chaoyue-backup-${System.currentTimeMillis()}.csv"); put(MediaStore.Downloads.MIME_TYPE,"text/csv"); put(MediaStore.Downloads.IS_PENDING,1) }
        val uri=context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values) ?: error("无法创建下载文件")
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(store.exportCsv(data)) } ?: error("无法写入下载文件")
        values.clear(); values.put(MediaStore.Downloads.IS_PENDING,0); context.contentResolver.update(uri,values,null,null); "已导出到设备的“下载”文件夹"
    }
    fun restoreBackup(uri: Uri): Result<String> = runCatching { val raw=getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("无法读取文件"); val restored=store.importCsv(raw); persist(restored); "已恢复 ${restored.exams.size} 场考试记录" }
    fun exportReportImage(range:String, records:List<Exam>): Result<String> = runCatching {
        require(records.isNotEmpty()) { "没有可导出的综合考试数据" }
        val width=1080; val height=1500; val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888); val canvas=Canvas(bitmap); canvas.drawColor(Color.WHITE)
        val title=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(20,38,77); textSize=54f; isFakeBoldText=true }; val body=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(45,58,85); textSize=34f }; val subtle=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(102,112,128); textSize=26f }; val blue=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(76,111,235); strokeWidth=6f; style=Paint.Style.STROKE }
        val context=getApplication<Application>(); if(data.student.avatarUri.isNotBlank()){runCatching{context.contentResolver.openInputStream(Uri.parse(data.student.avatarUri))?.use{BitmapFactory.decodeStream(it)}}.getOrNull()?.let{avatar->canvas.save();canvas.clipPath(android.graphics.Path().apply{addCircle(950f,95f,58f,android.graphics.Path.Direction.CW)});canvas.drawBitmap(avatar,null,RectF(892f,37f,1008f,153f),null);canvas.restore()}}
        canvas.drawText("我的阶段成长报告",64f,105f,title);canvas.drawText("${data.student.name} · ${data.student.schoolYear} ${data.student.term} · $range",64f,150f,subtle); val avg=records.map{it.rate}.average(); val delta=records.last().rate-records.first().rate; canvas.drawText("综合平均得分率  ${formatRate(avg)}",64f,235f,title);canvas.drawText("首末变化  ${if(delta>=0)"+" else ""}${"%.1f".format(delta)} 个百分点",64f,290f,body);canvas.drawText("综合成绩变化",64f,380f,title)
        val left=100f; val top=440f; val right=980f; val bottom=900f; listOf(70,80,90,100).forEach { tick->val y=bottom-(tick-70)/30f*(bottom-top);canvas.drawLine(left,y,right,y,Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(228,234,244)});canvas.drawText("$tick%",20f,y+8f,subtle) }; val points=records.mapIndexed { index,exam->android.graphics.PointF(left+(right-left)*index/(records.size-1).coerceAtLeast(1),bottom-((exam.rate-70)/30).toFloat().coerceIn(0f,1f)*(bottom-top)) }; points.zipWithNext().forEach{(a,b)->canvas.drawLine(a.x,a.y,b.x,b.y,blue)};points.forEach{canvas.drawCircle(it.x,it.y,8f,Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(76,111,235)})}
        canvas.drawText("成长总结",64f,1020f,title);canvas.drawText(if(records.size<2)"仅有 1 次综合考试，暂不生成趋势判断。" else "本阶段首末综合得分率变化 ${if(delta>=0)"提高" else "下降"} ${"%.1f".format(kotlin.math.abs(delta))} 个百分点。",64f,1080f,body);canvas.drawText("不同试卷难度会影响结果，不直接据此判断学习能力变化。",64f,1140f,subtle);canvas.drawText("统计口径：综合考试按日期排序，平均值为各场得分率算术平均。",64f,1190f,subtle)
        val values=ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME,"chaoyue-phase-report-${System.currentTimeMillis()}.png");put(MediaStore.Downloads.MIME_TYPE,"image/png");put(MediaStore.Downloads.IS_PENDING,1) };val uri=context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values)?:error("无法创建图片文件");context.contentResolver.openOutputStream(uri)?.use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}?:error("无法写入图片");values.clear();values.put(MediaStore.Downloads.IS_PENDING,0);context.contentResolver.update(uri,values,null,null);"报告图片已导出到设备的“下载”文件夹"
    }
    fun updateGoals(values: Map<String, Double>, term: String = "当前学期", enabled: Boolean = true) {
        persist(data.copy(goals = data.subjects.map { subject -> SubjectGoal(subject, (values[subject] ?: 90.0).coerceIn(0.0, 100.0), term, enabled) }))
    }
    fun updateStudent(name: String, grade: String, term: String, year: String, avatarUri:String=data.student.avatarUri) { persist(data.copy(student = Student(name.trim().ifBlank { "未命名同学" }, grade.trim().ifBlank { "未设置" }, term.trim().ifBlank { "未设置" }, year.trim().ifBlank { "未设置" },avatarUri))) }
    fun updateSubjects(items: List<String>) {
        val clean = items.map(String::trim).filter(String::isNotBlank).distinct()
        if (clean.isNotEmpty()) persist(data.copy(subjects = clean, goals = clean.map { subject -> SubjectGoal(subject, data.goals.find { it.subject == subject }?.target ?: 90.0) }, fullScores=clean.associateWith { data.fullScores[it] ?: 100.0 }))
    }
    fun fullScore(subject:String)=data.fullScores[subject]?:100.0
    fun updateFullScores(values:Map<String,Double>) { persist(data.copy(fullScores=data.fullScores + values.filterValues { it > 0 })) }
    fun checkUpdate() {
        updateStatus = UpdateStatus.Checking
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val connection = (URL("https://api.github.com/repos/heixue318/chaoyue/releases/latest").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000; readTimeout = 8_000; setRequestProperty("Accept", "application/vnd.github+json")
                }
                connection.inputStream.bufferedReader().use { reader -> JSONObject(reader.readText()) }.let { json ->
                    val assets = json.optJSONArray("assets"); var apk = ""
                    if (assets != null) for (i in 0 until assets.length()) { val asset=assets.getJSONObject(i); if(asset.optString("name").endsWith(".apk")) { apk=asset.optString("browser_download_url"); break } }
                    ReleaseInfo(json.optString("tag_name").ifBlank { json.optString("name") }, json.optString("html_url"), apk)
                }
            }
            withContext(Dispatchers.Main) { updateStatus = result.fold({ UpdateStatus.Found(it) }, { UpdateStatus.Failed("暂时无法检查更新，请稍后重试。") }) }
        }
    }
    fun downloadUpdate(url:String) { if(downloadStatus is DownloadStatus.Downloading) return; val manager=getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager; val id=manager.enqueue(DownloadManager.Request(Uri.parse(url)).setTitle("超越更新包").setDescription("正在下载更新").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalFilesDir(getApplication(),Environment.DIRECTORY_DOWNLOADS,"chaoyue-update.apk")); downloadStatus=DownloadStatus.Downloading(0); viewModelScope.launch(Dispatchers.IO) { while(true){val cursor=manager.query(DownloadManager.Query().setFilterById(id));cursor.use{if(it.moveToFirst()){val total=it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));val soFar=it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));val state=it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));withContext(Dispatchers.Main){downloadStatus=if(state==DownloadManager.STATUS_SUCCESSFUL)DownloadStatus.Ready(manager.getUriForDownloadedFile(id)) else DownloadStatus.Downloading(if(total>0)((soFar*100)/total).toInt() else 0)};if(state==DownloadManager.STATUS_SUCCESSFUL||state==DownloadManager.STATUS_FAILED)return@launch}};delay(350)}} }
}
data class ReleaseInfo(val version: String, val releaseUrl: String, val apkUrl: String)
sealed interface UpdateStatus { data object Idle:UpdateStatus; data object Checking:UpdateStatus; data class Found(val release:ReleaseInfo):UpdateStatus; data class Failed(val message:String):UpdateStatus }
sealed interface DownloadStatus { data object Idle:DownloadStatus; data class Downloading(val percent:Int):DownloadStatus; data class Ready(val uri:Uri):DownloadStatus }
