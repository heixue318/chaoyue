package com.growthscore.demo.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.growthscore.demo.data.DemoRepository
import com.growthscore.demo.model.*

class DemoViewModel : ViewModel() {
    val student = DemoRepository.student
    var exams by mutableStateOf(DemoRepository.exams); private set
    var goals by mutableStateOf(DemoRepository.initialGoals); private set
    val latest get() = exams.last()
    fun saveExam(exam: Exam) { exams = exams + exam }
    fun updateGoal(subject: String, target: Double) { goals = goals.map { if (it.subject == subject) it.copy(target = target) else it } }
}
