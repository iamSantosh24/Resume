package com.example.resume

data class Resume(
    val name: String = "",
    val title: String = "",
    val summary: String = "",
    val skills: List<Skill> = emptyList(),
    val personalInfo: PersonalInfo? = null,
    val experience: List<ExperienceEntry> = emptyList(),
    val education: List<EducationEntry> = emptyList(),
    val projects: List<Project> = emptyList()
)

// Skill already exists in Skill.kt but we keep the simple shape here as well

data class PersonalInfo(
    val name: String = "",
    val email: String? = null,
    val phone: String? = null,
    val github: String? = null,
    val linkedin: String? = null,
    val summary: String? = null,
    val profilePicture: String? = null
)

data class ExperienceEntry(
    val company: String = "",
    val title: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val location: String? = null,
    val description: List<String> = emptyList()
)

data class EducationEntry(
    val university: String = "",
    val degree: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val gpa: String? = null,
    val location: String? = null
)

data class Project(
    val name: String = "",
    val description: String? = null,
    val githubUrl: String? = null,
    val technologies: List<String> = emptyList()
)
