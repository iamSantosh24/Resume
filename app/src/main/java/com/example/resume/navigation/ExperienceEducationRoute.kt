package com.example.resume.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resume.EducationEntry
import com.example.resume.ExperienceEntry
import com.example.resume.experienceEducationItems

@Composable
fun ExperienceEducationRoute(
    experience: List<ExperienceEntry>,
    education: List<EducationEntry>,
    onBack: (() -> Unit)? = null
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Experience & Education") },
            navigationIcon = if (onBack != null) {
                {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            } else null
        )
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            experienceEducationItems(experience = experience, education = education)
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}
