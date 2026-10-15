package com.growthscore.demo.plan

import com.growthscore.demo.model.*
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt

/** Deterministic, offline plan generation. Scores are read-only inputs. */
object PlanEngine {
    fun generate(
        name:String,
        type:StudyPlanType,
        start:LocalDate,
        end:LocalDate,
        availability:Map<Int,Int>,
        enabledSubjects:List<String>,
        exams:List<Exam>,
        goals:List<SubjectGoal>
    ): StudyPlan {
        require(!end.isBefore(start)) { "结束日期不能早于开始日期" }
        require(enabledSubjects.isNotEmpty()) { "请至少选择一门学科" }
        require(availability.values.sum()>0) { "每周可用学习时间不能全部为 0" }
        val weights=enabledSubjects.associateWith { subject -> priority(subject,exams,goals) }
        val weekly=allocate(availability.values.sum(),weights)
        val tasks=generateTasks(start,end,availability,weekly,exams)
        require(tasks.isNotEmpty()) { "可用时间内没有可安排的日期，请调整日期或学习时间" }
        return StudyPlan(UUID.randomUUID().toString(),name.ifBlank { "我的学习计划" },type,start.toString(),end.toString(),StudyPlanStatus.ACTIVE,availability,weekly,enabledSubjects.toSet(),tasks)
    }

    private fun priority(subject:String, exams:List<Exam>, goals:List<SubjectGoal>):Double {
        val values=exams.filter { it.kind==ExamKind.COMPREHENSIVE }.mapNotNull { exam -> exam.subjectScores.firstOrNull { it.subject==subject }?.rate }.takeLast(5)
        val current=values.lastOrNull() ?: 75.0
        val target=goals.firstOrNull { it.subject==subject && it.enabled }?.target ?: 90.0
        val gap=((target-current).coerceAtLeast(0.0)/25.0*100).coerceAtMost(100.0)
        val delta=if(values.size>=2) values.last()-values[values.lastIndex-1] else 0.0
        val trend=when { delta < -2 -> 80.0; delta < 0 -> 65.0; delta > 2 -> 20.0; else -> 50.0 }
        val stability=if(values.size<3)50.0 else { val mean=values.average(); val sd=kotlin.math.sqrt(values.sumOf{(it-mean)*(it-mean)}/values.size); when { sd<=3 -> 30.0; sd<=6 -> 60.0; else -> 90.0 } }
        return .40*gap+.25*trend+.20*stability+.15*50.0
    }

    private fun allocate(total:Int,weights:Map<String,Double>):Map<String,Int> {
        val base=if(total>=weights.size*15) 15 else 0
        val remaining=(total-base*weights.size).coerceAtLeast(0)
        val sum=weights.values.sum().takeIf { it>0 } ?: 1.0
        val raw=weights.mapValues { remaining*it.value/sum }
        val result=raw.mapValues { base+(it.value/5).roundToInt()*5 }.toMutableMap()
        while(result.values.sum()>total) { result.maxByOrNull { it.value }?.key?.let { result[it]=(result[it]!!-5).coerceAtLeast(0) } }
        return result
    }

    private fun generateTasks(start:LocalDate,end:LocalDate,availability:Map<Int,Int>,weekly:Map<String,Int>,exams:List<Exam>):List<PlanTask> {
        val remaining=weekly.toMutableMap(); val ordered=weekly.keys.sortedByDescending { weekly[it]?:0 }; val output=mutableListOf<PlanTask>(); var date=start; var cursor=0
        val finalDate=minOf(end,start.plusDays(13))
        while(!date.isAfter(finalDate)) {
            var minutes=availability[date.dayOfWeek.value]?:0
            var guard=0
            while(minutes>=15 && remaining.values.any { it>=15 } && guard<ordered.size*2) {
                val subject=ordered[cursor%ordered.size]; cursor++; guard++
                if((remaining[subject]?:0)<15) continue
                val duration=minOf(30,minutes,remaining[subject]?:0).let { if(it>=25)30 else 15 }
                val rate=exams.lastOrNull { it.kind==ExamKind.COMPREHENSIVE }?.subjectScores?.firstOrNull { it.subject==subject }?.rate ?: 75.0
                val task=template(subject,rate,duration)
                output += PlanTask(UUID.randomUUID().toString(),subject,task.first,task.second,date.toString(),duration)
                minutes-=duration; remaining[subject]=(remaining[subject]?:0)-duration
            }
            date=date.plusDays(1)
            if(date.dayOfWeek.value==1) weekly.forEach { (subject,value)->remaining[subject]=(remaining[subject]?:0)+value }
        }
        return output
    }

    private fun template(subject:String,rate:Double,minutes:Int):Pair<String,String> {
        val weak=rate<80
        return when(subject) {
            "语文" -> if(weak) "字词与古诗文复习" to "复习近期字词、古诗文并完成简短自测。" else "阅读理解" to "完成一篇适合当前阶段的阅读练习，核对答案并记录理解问题。"
            "数学" -> if(weak) "基础概念巩固" to "选择近期薄弱知识，复习概念与典型例题。" else "错题复盘" to "独立重做 2–3 道典型错题，记录错误原因。"
            "英语" -> if(weak) "词汇复习" to "复习近期单词，完成拼写、理解或使用练习。" else "阅读理解" to "完成一篇阅读练习并分析理解错误。"
            "物理" -> "公式与典型例题" to "整理公式适用条件，分析一道典型例题。"
            "化学" -> "反应与概念复习" to "复习核心概念、常见反应或实验现象。"
            "历史" -> "时间线与因果整理" to "梳理重点事件、时间线和因果关系。"
            "地理" -> "地图与图表练习" to "完成地图或图表练习，整理区域知识。"
            "生物" -> "概念与过程复习" to "整理概念、过程和图示，并做简短自测。"
            else -> "重点知识整理" to "复习近期课程内容，整理重点并完成基础练习。"
        }.let { it.first to "建议 $minutes 分钟：${it.second}" }
    }
}
