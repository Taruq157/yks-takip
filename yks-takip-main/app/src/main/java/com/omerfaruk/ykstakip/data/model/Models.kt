package com.omerfaruk.ykstakip.data.model

data class Subject(
    val id: Int,
    val name: String,
    val topics: List<Topic>
) {
    val progress: Float
        get() = if (topics.isEmpty()) 0f else topics.count { it.isCompleted }.toFloat() / topics.size
}

data class Topic(
    val id: Int,
    val subjectId: Int,
    val title: String,
    val isCompleted: Boolean = false
)
