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
import kotlinx.coroutines.delay

class DemoViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalStore(application)
    private var data by mutableStateOf(store.load())
    val student get() = data.student
    val subjects get() = data.subjects
    val exams get() = data.exams
    val goals get() = data.goals
    val fullScores get() = data.fullScores
    val latest get() = exams.last()
    val currentVersion = BuildConfig.VERSION_NAME
    var updateStatus by mutableStateOf<UpdateStatus>(UpdateStatus.Idle); private set
    var downloadStatus by mutableStateOf<DownloadStatus>(DownloadStatus.Idle); private set
    init { checkUpdate() }
    private fun persist(next: AppData) { data = next; store.save(next) }
    fun saveExam(exam: Exam) { persist(data.copy(exams = (data.exams + exam).takeLast(300))) }
    fun deleteExam(id: String) { if (data.exams.size > 1) persist(data.copy(exams = data.exams.filterNot { it.id == id })) }
    fun updateGoal(subject: String, target: Double) { persist(data.copy(goals = data.goals.map { if (it.subject == subject) it.copy(target = target) else it })) }
    fun updateStudent(name: String, grade: String, term: String, year: String) { persist(data.copy(student = Student(name.trim().ifBlank { "未命名同学" }, grade.trim().ifBlank { "未设置" }, term.trim().ifBlank { "未设置" }, year.trim().ifBlank { "未设置" }))) }
    fun updateSubjects(items: List<String>) {
        val clean = items.map(String::trim).filter(String::isNotBlank).distinct()
        if (clean.isNotEmpty()) persist(data.copy(subjects = clean, goals = data.goals.filter { it.subject in clean }, fullScores=clean.associateWith { data.fullScores[it] ?: 100.0 }))
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
