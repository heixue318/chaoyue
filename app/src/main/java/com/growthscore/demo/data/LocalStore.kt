package com.growthscore.demo.data

import android.content.Context
import com.growthscore.demo.model.*
import org.json.JSONArray
import org.json.JSONObject

/** Small, offline-first store. It deliberately keeps data on this device only. */
class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("chaoyue_local_data", Context.MODE_PRIVATE)

    fun load(): AppData = prefs.getString("data", null)?.let(::decode) ?: AppData(
        DemoRepository.student, DemoRepository.subjects, DemoRepository.exams, DemoRepository.initialGoals
    )

    fun save(data: AppData) {
        prefs.edit().putString("data", encode(data)).apply()
    }

    private fun encode(data: AppData) = JSONObject().apply {
        put("student", JSONObject().apply { put("name", data.student.name); put("grade", data.student.grade); put("term", data.student.term); put("year", data.student.schoolYear) })
        put("subjects", JSONArray(data.subjects))
        put("goals", JSONArray().apply { data.goals.forEach { put(JSONObject().apply { put("subject", it.subject); put("target", it.target) }) } })
        put("exams", JSONArray().apply { data.exams.forEach { exam -> put(JSONObject().apply {
            put("id", exam.id); put("name", exam.name); put("date", exam.date); put("kind", exam.kind.name)
            put("scores", JSONArray().apply { exam.subjectScores.forEach { score -> put(JSONObject().apply {
                put("subject", score.subject); put("score", score.score); put("full", score.fullScore); put("reasons", JSONArray(score.lossReasons))
            }) } })
        }) } })
    }.toString()

    private fun decode(raw: String): AppData = JSONObject(raw).let { root ->
        val studentJson = root.getJSONObject("student")
        val student = Student(studentJson.getString("name"), studentJson.getString("grade"), studentJson.getString("term"), studentJson.getString("year"))
        fun strings(array: JSONArray) = List(array.length()) { array.getString(it) }
        val subjects = strings(root.getJSONArray("subjects"))
        val goals = root.getJSONArray("goals").let { list -> List(list.length()) { i -> list.getJSONObject(i).let { SubjectGoal(it.getString("subject"), it.getDouble("target")) } } }
        val exams = root.getJSONArray("exams").let { list -> List(list.length()) { i -> list.getJSONObject(i).let { item ->
            val scores = item.getJSONArray("scores").let { values -> List(values.length()) { j -> values.getJSONObject(j).let { score ->
                SubjectScore(score.getString("subject"), score.getDouble("score"), score.getDouble("full"), strings(score.optJSONArray("reasons") ?: JSONArray()))
            } } }
            Exam(item.getString("id"), item.getString("name"), item.getString("date"), ExamKind.valueOf(item.getString("kind")), scores)
        } } }
        AppData(student, subjects, exams, goals)
    }
}

data class AppData(val student: Student, val subjects: List<String>, val exams: List<Exam>, val goals: List<SubjectGoal>)
