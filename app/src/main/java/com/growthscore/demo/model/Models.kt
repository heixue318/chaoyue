package com.growthscore.demo.model

enum class ExamKind { COMPREHENSIVE, SUBJECT_TEST }
data class Student(val name: String, val grade: String, val term: String, val schoolYear: String, val avatarUri: String = "")
data class LossRecord(val reason: String, val points: Double)
data class SubjectScore(val subject: String, val score: Double, val fullScore: Double, val lossReasons: List<String> = emptyList(), val losses: List<LossRecord> = emptyList()) {
    val rate: Double get() = score / fullScore * 100
}
data class Exam(val id: String, val name: String, val date: String, val kind: ExamKind, val subjectScores: List<SubjectScore>, val schoolYear: String = "未标注", val term: String = "未标注", val note: String = "") {
    val totalScore: Double get() = subjectScores.sumOf { it.score }
    val totalFullScore: Double get() = subjectScores.sumOf { it.fullScore }
    val rate: Double get() = totalScore / totalFullScore * 100
}
data class SubjectGoal(val subject: String, val target: Double, val term: String = "当前学期", val enabled: Boolean = true)
enum class StudyPlanType { TERM, WINTER_BREAK, SUMMER_BREAK, CUSTOM }
enum class StudyPlanStatus { ACTIVE, PAUSED, ENDED }
enum class PlanTaskStatus { PENDING, COMPLETED, SKIPPED }
data class PlanTask(val id:String,val subject:String,val title:String,val description:String,val plannedDate:String,val minutes:Int,val status:PlanTaskStatus=PlanTaskStatus.PENDING,val completedAt:Long?=null)
data class StudyPlan(
    val id:String,
    val name:String,
    val type:StudyPlanType,
    val startDate:String,
    val endDate:String,
    val status:StudyPlanStatus,
    val weeklyAvailability:Map<Int,Int>,
    val weeklyMinutes:Map<String,Int>,
    val focusSubjects:Set<String>,
    val tasks:List<PlanTask>,
    val createdAt:Long=System.currentTimeMillis()
)

fun formatScore(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
fun formatRate(value: Double): String = "${formatScore(value)}%"

fun levelFor(rate: Double): String = when {
    rate >= 95 -> "卓越突破"
    rate >= 90 -> "优秀稳定"
    rate >= 85 -> "良好提升"
    rate >= 80 -> "稳步成长"
    rate >= 70 -> "重点提升"
    rate >= 60 -> "基础加强"
    else -> "重新夯实"
}
