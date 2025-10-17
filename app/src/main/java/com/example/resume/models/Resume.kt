package com.example.resume

data class Resume(
    val name: String = "",
    val title: String = "",
    val summary: String = "",
    val skills: List<Skill> = emptyList()
)

