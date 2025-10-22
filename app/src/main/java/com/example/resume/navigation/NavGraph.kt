package com.example.resume.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.resume.ResumeViewModel
import com.example.resume.PersonalInfo
import com.example.resume.PersonalInfoScreen
import com.example.resume.ExperienceList
import com.example.resume.EducationList

@Composable
fun AppNavGraph(viewModel: ResumeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val routeNow = navBackStackEntry?.destination?.route ?: viewModel.selectedTab

            // Determine top-level tab by route prefix
            val currentRoute = when {
                routeNow.startsWith("home") -> Screen.Home.route
                routeNow.startsWith("employment") -> Screen.Employment.route
                routeNow.startsWith("education") -> Screen.Education.route
                else -> viewModel.selectedTab
            }

            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        if (currentRoute != Screen.Home.route) {
                            navController.navigate("home/main") { launchSingleTop = true }
                            viewModel.selectTab(Screen.Home.route)
                        }
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Work, contentDescription = "Employment") },
                    label = { Text("Employment") },
                    selected = currentRoute == Screen.Employment.route,
                    onClick = {
                        if (currentRoute != Screen.Employment.route) {
                            navController.navigate("employment/main") { launchSingleTop = true }
                            viewModel.selectTab(Screen.Employment.route)
                        }
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Education") },
                    label = { Text("Education") },
                    selected = currentRoute == Screen.Education.route,
                    onClick = {
                        if (currentRoute != Screen.Education.route) {
                            navController.navigate("education/main") { launchSingleTop = true }
                            viewModel.selectTab(Screen.Education.route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // Flat composable routes for each tab and their sub-routes
        NavHost(
            navController = navController,
            startDestination = when (viewModel.selectedTab) {
                Screen.Employment.route -> "employment/main"
                Screen.Education.route -> "education/main"
                else -> "home/main"
            },
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            // Home routes
            composable("home/main") {
                PersonalInfoScreen(
                    personal = viewModel.resume?.personalInfo ?: PersonalInfo(name = viewModel.resume?.name ?: ""),
                    displayName = viewModel.resume?.name,
                    title = viewModel.resume?.title
                )
            }
            composable("home/detail/{id}") {
                // Placeholder detail screen for home
                PersonalInfoScreen(personal = viewModel.resume?.personalInfo, displayName = viewModel.resume?.name)
            }

            // Employment routes
            composable("employment/main") {
                ExperienceList(viewModel.resume?.experience ?: emptyList())
            }
            composable("employment/detail/{index}") {
                // Placeholder detail; still renders list for now
                ExperienceList(viewModel.resume?.experience ?: emptyList())
            }

            // Education routes
            composable("education/main") {
                EducationList(viewModel.resume?.education ?: emptyList())
            }
            composable("education/detail/{index}") {
                EducationList(viewModel.resume?.education ?: emptyList())
            }
        }

        // Keep the ViewModel's selectedTab in sync when navigation changes
        val backStackEntry by navController.currentBackStackEntryAsState()
        val newRoute = backStackEntry?.destination?.route
        LaunchedEffect(newRoute) {
            if (!newRoute.isNullOrEmpty()) {
                val top = when {
                    newRoute.startsWith("home") -> Screen.Home.route
                    newRoute.startsWith("employment") -> Screen.Employment.route
                    newRoute.startsWith("education") -> Screen.Education.route
                    else -> viewModel.selectedTab
                }
                if (top != viewModel.selectedTab) viewModel.selectTab(top)
            }
        }
    }
}
