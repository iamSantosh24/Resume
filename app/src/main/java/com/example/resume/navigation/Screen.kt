package com.example.resume.navigation

// Central place for typed navigation destinations
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Employment : Screen("employment")
    object Education : Screen("education")
    object ExperienceEducation : Screen("experience_education")
}
