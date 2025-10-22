package com.example.resume

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Reusable card-based UI components for the resume

@Composable
fun ProjectCard(project: Project) {
    InfoCard(
        elevation = 4.dp,
        title = { Text(project.name, fontWeight = FontWeight.Bold) },
        content = {
            project.description?.let { Text(it) }
            if (project.technologies.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Technologies: ${project.technologies.joinToString(", ")}")
            }
        }
    )
}

@Composable
fun ExperienceCard(exp: ExperienceEntry) {
    var expanded by remember { mutableStateOf(false) }

    InfoCard(
        elevation = 2.dp,
        title = { Text("${exp.title} — ${exp.company}", fontWeight = FontWeight.Bold) },
        subtitle = {
            Row { Text(exp.startDate ?: ""); Spacer(Modifier.width(8.dp)); Text(exp.endDate ?: "") }
            exp.location?.let { Text(it) }
        },
        trailing = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Less" else "More",
                    tint = MaterialTheme.colors.primary
                )
            }
        },
        content = {
            if (exp.description.isNotEmpty()) {
                Column { exp.description.forEach { d -> Text("• $d") } }
            }

            if (expanded) {
                Spacer(Modifier.height(4.dp))
                Column { exp.more.forEach { m -> Text("• $m") } }
            }
        }
    )
}

@Composable
fun EducationCard(edu: EducationEntry) {
    InfoCard(
        elevation = 2.dp,
        title = { Text("${edu.degree} — ${edu.university}", fontWeight = FontWeight.Bold) },
        subtitle = { Row { Text(edu.startDate ?: ""); Spacer(Modifier.width(8.dp)); Text(edu.endDate ?: "") } },
        content = {
            edu.gpa?.let { Text("GPA: $it") }
            edu.location?.let { Text(it) }
        }
    )
}

@Composable
fun SkillChip(skill: Skill) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEEEEEE),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(skill.name)
            if (!skill.level.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text("(${skill.level})", style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardPreview() {
    val p = Project(
        name = "Recipe Finder App",
        description = "Search, save and share recipes.",
        githubUrl = "github.com/janedoe/recipe-finder",
        technologies = listOf("Kotlin", "Compose")
    )
    ProjectCard(p)
}

@Preview(showBackground = true)
@Composable
fun ExperienceCardPreview() {
    val e = ExperienceEntry(
        company = "App Innovators Inc.",
        title = "Senior Android Developer",
        startDate = "2022-06-01",
        endDate = "Present",
        location = "City, State",
        description = listOf("Led development."),
        more = listOf("Architected feature X", "Reduced load time by 40%")
    )
    ExperienceCard(e)
}

@Preview(showBackground = true)
@Composable
fun EducationCardPreview() {
    val ed = EducationEntry(
        university = "University of Technology",
        degree = "MSc Computer Science",
        startDate = "2018-09-01",
        endDate = "2020-05-31",
        gpa = "3.9/4.0",
        location = "City, State"
    )
    EducationCard(ed)
}

@Preview(showBackground = true)
@Composable
fun SkillChipPreview() {
    SkillChip(Skill(name = "Kotlin", level = "programming_languages"))
}
