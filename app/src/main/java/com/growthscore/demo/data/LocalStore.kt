package com.growthscore.demo.data

import android.content.Context
import com.growthscore.demo.model.*
import org.json.JSONArray
import org.json.JSONObject
import android.util.Base64

/** Small, offline-first store. It deliberately keeps data on this device only. */
class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("chaoyue_local_data", Context.MODE_PRIVATE)

    fun load(): AppData = prefs.getString("data", null)?.let(::decode) ?: AppData(
        DemoRepository.student, DemoRepository.subjects, (DemoRepository.exams + DemoRepository.subjectTests).sortedBy { it.date }, DemoRepository.initialGoals
    )

    fun save(data: AppData) {
        prefs.edit().putString("data", encode(data)).apply()
    }
    fun exportCsv(data: AppData): String = "超越完整数据备份,1\nJSON_BASE64,${Base64.encodeToString(encode(data).toByteArray(Charsets.UTF_8), Base64.NO_WRAP)}\n"
    fun importCsv(raw: String): AppData {
        val lines=raw.lineSequence().filter { it.isNotBlank() }.toList()
        require(lines.firstOrNull()=="超越完整数据备份,1" && lines.getOrNull(1)?.startsWith("JSON_BASE64,")==true) { "不是有效的超越备份文件" }
        val decoded=String(Base64.decode(lines[1].substringAfter(','),Base64.NO_WRAP),Charsets.UTF_8)
        val data=decode(decoded)
        require(data.exams.size<=300 && data.exams.all { it.subjectScores.all { score -> score.fullScore>0 && score.score>=0 } }) { "备份数据校验失败" }
        return data
    }

    private fun encode(data: AppData) = JSONObject().apply {
        put("student", JSONObject().apply { put("name", data.student.name); put("grade", data.student.grade); put("term", data.student.term); put("year", data.student.schoolYear); put("avatarUri",data.student.avatarUri) })
        put("subjects", JSONArray(data.subjects))
        put("fullScores", JSONObject().apply { data.fullScores.forEach { (subject, score) -> put(subject, score) } })
        put("goals", JSONArray().apply { data.goals.forEach { put(JSONObject().apply { put("subject", it.subject); put("target", it.target); put("term", it.term); put("enabled", it.enabled) }) } })
        put("exams", JSONArray().apply { data.exams.forEach { exam -> put(JSONObject().apply {
            put("id", exam.id); put("name", exam.name); put("date", exam.date); put("kind", exam.kind.name); put("schoolYear", exam.schoolYear); put("term", exam.term); put("note", exam.note)
            put("scores", JSONArray().apply { exam.subjectScores.forEach { score -> put(JSONObject().apply {
                put("subject", score.subject); put("score", score.score); put("full", score.fullScore); put("reasons", JSONArray(score.lossReasons)); put("losses", JSONArray().apply { score.losses.forEach { loss -> put(JSONObject().apply { put("reason",loss.reason);put("points",loss.points) }) } })
            }) } })
        }) } })
        put("plans", JSONArray().apply { data.plans.forEach { plan -> put(encodePlan(plan)) } })
    }.toString()

    private fun decode(raw: String): AppData = JSONObject(raw).let { root ->
        val studentJson = root.getJSONObject("student")
        val student = Student(studentJson.getString("name"), studentJson.getString("grade"), studentJson.getString("term"), studentJson.getString("year"),studentJson.optString("avatarUri", ""))
        fun strings(array: JSONArray) = List(array.length()) { array.getString(it) }
        val subjects = strings(root.getJSONArray("subjects"))
        val fullScoresJson=root.optJSONObject("fullScores"); val fullScores=subjects.associateWith { fullScoresJson?.optDouble(it,100.0) ?: 100.0 }
        val goals = root.getJSONArray("goals").let { list -> List(list.length()) { i -> list.getJSONObject(i).let { SubjectGoal(it.getString("subject"), it.getDouble("target"), it.optString("term", "当前学期"), it.optBoolean("enabled", true)) } } }
        val exams = root.getJSONArray("exams").let { list -> List(list.length()) { i -> list.getJSONObject(i).let { item ->
            val scores = item.getJSONArray("scores").let { values -> List(values.length()) { j -> values.getJSONObject(j).let { score ->
                val reasons=strings(score.optJSONArray("reasons") ?: JSONArray()); val losses=score.optJSONArray("losses")?.let { lossArray -> List(lossArray.length()) { k -> lossArray.getJSONObject(k).let { LossRecord(it.getString("reason"),it.optDouble("points",0.0)) } } } ?: reasons.map { LossRecord(it,0.0) }
                SubjectScore(score.getString("subject"), score.getDouble("score"), score.getDouble("full"), reasons, losses)
            } } }
            Exam(item.getString("id"), item.getString("name"), item.getString("date"), ExamKind.valueOf(item.getString("kind")), scores, item.optString("schoolYear", "未标注"), item.optString("term", "未标注"), item.optString("note", ""))
        } } }
        val plans=root.optJSONArray("plans")?.let { values -> List(values.length()) { readPlan(values.getJSONObject(it)) } }
            ?: root.optJSONObject("plan")?.let { listOf(readPlan(it)) }
            ?: emptyList()
        AppData(student, subjects, exams, goals, fullScores, plans)
    }

    private fun encodePlan(plan:StudyPlan)=JSONObject().apply {
        put("id", plan.id); put("name", plan.name); put("type", plan.type.name)
        put("start", plan.startDate); put("end", plan.endDate); put("status", plan.status.name); put("createdAt", plan.createdAt)
        put("availability", JSONObject().apply { plan.weeklyAvailability.forEach { (day, minutes) -> put(day.toString(), minutes) } })
        put("weeklyMinutes", JSONObject().apply { plan.weeklyMinutes.forEach { (subject, minutes) -> put(subject, minutes) } })
        put("focus", JSONArray(plan.focusSubjects.toList()))
        put("tasks", JSONArray().apply { plan.tasks.forEach { task -> put(JSONObject().apply {
            put("id", task.id); put("subject", task.subject); put("title", task.title); put("description", task.description)
            put("date", task.plannedDate); put("minutes", task.minutes); put("status", task.status.name)
            put("completedAt", task.completedAt ?: JSONObject.NULL)
        }) } })
    }

    private fun readPlan(item:JSONObject):StudyPlan {
        fun intMap(key:String)=item.optJSONObject(key)?.let { map -> map.keys().asSequence().associate { it.toInt() to map.optInt(it,0) } } ?: emptyMap()
        fun subjectMinutes()=item.optJSONObject("weeklyMinutes")?.let { map -> map.keys().asSequence().associateWith { map.optInt(it,0) } } ?: emptyMap()
        val focus=List(item.optJSONArray("focus")?.length() ?: 0) { item.getJSONArray("focus").getString(it) }.toSet()
        val tasks=item.optJSONArray("tasks")?.let { values -> List(values.length()) { i -> values.getJSONObject(i).let { task -> PlanTask(task.getString("id"),task.getString("subject"),task.getString("title"),task.optString("description"),task.getString("date"),task.optInt("minutes",15),runCatching { PlanTaskStatus.valueOf(task.optString("status","PENDING")) }.getOrDefault(PlanTaskStatus.PENDING),if(task.isNull("completedAt"))null else task.optLong("completedAt")) } } } ?: emptyList()
        return StudyPlan(item.getString("id"),item.getString("name"),runCatching { StudyPlanType.valueOf(item.optString("type","CUSTOM")) }.getOrDefault(StudyPlanType.CUSTOM),item.getString("start"),item.getString("end"),runCatching { StudyPlanStatus.valueOf(item.optString("status","ACTIVE")) }.getOrDefault(StudyPlanStatus.ACTIVE),intMap("availability"),subjectMinutes(),focus,tasks,item.optLong("createdAt",System.currentTimeMillis()))
    }
}

data class AppData(val student: Student, val subjects: List<String>, val exams: List<Exam>, val goals: List<SubjectGoal>, val fullScores: Map<String, Double> = emptyMap(), val plans:List<StudyPlan> = emptyList())
