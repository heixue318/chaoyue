package com.growthscore.demo.data

import com.growthscore.demo.model.*

object DemoRepository {
    val student = Student("张同学", "初二", "上学期", "2026-2027学年")
    val subjects = listOf("语文", "数学", "英语", "物理", "历史", "地理", "生物", "道德与法治")
    val exams = listOf(
        exam("月考1", "2026-09-12", listOf(83,86,79,88,82,80,84,83)),
        exam("月考2", "2026-10-10", listOf(85,88,81,90,84,82,86,85)),
        exam("期中考试", "2026-11-06", listOf(88,91,84,92,87,85,89,88)),
        exam("月考3", "2026-12-04", listOf(89,93,86,93,88,87,90,89)),
        exam("期末考试", "2027-01-15", listOf(91,95,88,94,90,89,92,90)),
        exam("模拟考试1", "2027-02-20", listOf(92,96,87,95,91,90,93,91)),
        exam("月考4", "2027-03-18", listOf(93,97,84,96,92,91,94,92)),
        exam("模拟考试2", "2027-04-12", listOf(93,98,82,95,92,90,93,91))
    )
    val subjectTests = listOf(
        test("数学周测1", "2026-10-02", "数学", 45, 50), test("英语词汇", "2026-10-16", "英语", 42, 50),
        test("数学周测2", "2026-11-14", "数学", 47, 50), test("英语阅读", "2026-11-21", "英语", 40, 50),
        test("物理测验", "2026-12-12", "物理", 46, 50), test("数学周测3", "2026-12-26", "数学", 48, 50),
        test("英语语法", "2027-01-09", "英语", 43, 50), test("数学周测4", "2027-02-13", "数学", 49, 50),
        test("英语阅读2", "2027-03-06", "英语", 41, 50), test("数学周测5", "2027-04-05", "数学", 49, 50)
    )
    val initialGoals = listOf(SubjectGoal("语文",85.0), SubjectGoal("数学",95.0), SubjectGoal("英语",90.0), SubjectGoal("物理",90.0), SubjectGoal("历史",90.0), SubjectGoal("地理",90.0), SubjectGoal("生物",90.0), SubjectGoal("道德与法治",90.0))

    private fun exam(name: String, date: String, rates: List<Int>): Exam {
        val scores=subjects.zip(rates).map { (subject, rate) ->
            SubjectScore(subject,rate.toDouble(),100.0,emptyList(),if(subject=="英语") listOf(LossRecord("记忆不牢",4.0),LossRecord("审题错误",2.0)) else listOf(LossRecord("计算错误",2.0)))
        }
        return Exam(name,name,date,ExamKind.COMPREHENSIVE,scores,"2026-2027学年",if(date<"2027-02-01")"上学期" else "下学期")
    }
    private fun test(name: String, date: String, subject: String, score: Int, full: Int) = Exam(name, name, date, ExamKind.SUBJECT_TEST, listOf(SubjectScore(subject, score.toDouble(), full.toDouble())), "2026-2027学年", if(date<"2027-02-01")"上学期" else "下学期")
}
