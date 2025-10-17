package com.example.resume

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ResumeApp(viewModel: ResumeViewModel) {
    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("My Resume") }) }
        ) { innerPadding ->
            ResumeScreen(
                resume = viewModel.resume,
                isLoading = viewModel.isLoading,
                error = viewModel.errorMessage,
                onRefresh = { viewModel.fetchResume() },
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        }
    }
}

@Composable
fun ResumeScreen(
    resume: Resume?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(16.dp)) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    Text("Error: $error")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text("Retry") }
                }
            }
            resume != null -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(resume.name, style = MaterialTheme.typography.h5)
                        Text(resume.title, style = MaterialTheme.typography.subtitle1)
                        Spacer(Modifier.height(8.dp))
                        Text(resume.summary)
                        Spacer(Modifier.height(12.dp))
                        Text("Skills", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(resume.skills) { skill ->
                        SkillChip(skill)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    Text("No resume loaded")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text("Load") }
                }
            }
        }
    }
}

@Composable
fun SkillChip(skill: Skill) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEEEEEE),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(skill.name)
            if (!skill.level.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text("(${skill.level})", style = MaterialTheme.typography.caption)
            }
        }
    }
}

