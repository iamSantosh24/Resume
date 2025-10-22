package com.example.resume

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Keep only the lazy-aware extension so callers can add Experience/Education items into a LazyColumn.
// Previously there was a non-lazy column composable here — it has been removed in favor of the lazy extension
// and a dedicated route screen (moved to the navigation package).

// LazyListScope extension to make the Experience/Education sections lazy-aware.
// Use this inside a LazyColumn { ... } scope to add the items directly.
fun LazyListScope.experienceEducationItems(
    experience: List<ExperienceEntry>,
    education: List<EducationEntry>,
    spacing: Dp = 8.dp
) {
    if (experience.isNotEmpty()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Experience", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemsIndexed(experience) { _, exp ->
            ExperienceCard(exp)
            Spacer(Modifier.height(spacing))
        }
    }

    if (education.isNotEmpty()) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Education", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemsIndexed(education) { _, edu ->
            EducationCard(edu)
            Spacer(Modifier.height(spacing))
        }
    }
}

// Preview via a small LazyColumn wrapper that exercises the lazy extension.
@Preview(showBackground = true)
@Composable
fun ExperienceEducationLazyPreview() {
    val sampleExperience = listOf(
        ExperienceEntry(
            company = "Example Co",
            title = "Android Developer",
            startDate = "2021-01-01",
            endDate = "Present",
            location = "City, Country",
            description = listOf("Built apps", "Wrote tests"),
            more = listOf("Led migration to Compose")
        )
    )

    val sampleEducation = listOf(
        EducationEntry(
            university = "Tech University",
            degree = "BSc Computer Science",
            startDate = "2016-09-01",
            endDate = "2020-05-31",
            gpa = "3.8/4.0",
            location = "City, Country"
        )
    )

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        experienceEducationItems(experience = sampleExperience, education = sampleEducation)
    }
}
