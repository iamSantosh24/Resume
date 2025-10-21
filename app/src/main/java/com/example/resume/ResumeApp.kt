package com.example.resume

import android.util.Log
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Business
import java.net.URLEncoder
import java.util.Locale

// libphonenumber imports
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.NumberParseException

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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text("Error: $error")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text("Retry") }
                }
            }
            resume != null -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        // Header: Name / Title
                        val displayName = resume.personalInfo?.name ?: resume.name
                        Text(displayName, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        if (resume.title.isNotBlank()) {
                            Text(resume.title, style = MaterialTheme.typography.subtitle1)
                        }
                        Spacer(Modifier.height(8.dp))

                        // Contact + summary
                        resume.personalInfo?.let { p ->
                            ContactRow(p)
                            Spacer(Modifier.height(8.dp))
                            p.summary?.let { s ->
                                if (s.isNotBlank()) Text(s)
                            }
                        } ?: run {
                            if (resume.summary.isNotBlank()) Text(resume.summary)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Skills
                        Text("Skills", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(8.dp))
                    }

                    // Skills list (as chips)
                    items(resume.skills) { skill ->
                        SkillChip(skill)
                        Spacer(Modifier.height(6.dp))
                    }

                    // Projects
                    if (resume.projects.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            Text("Projects", style = MaterialTheme.typography.h6)
                            Spacer(Modifier.height(8.dp))
                        }
                        items(resume.projects) { project ->
                            ProjectCard(project)
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Experience
                    if (resume.experience.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            Text("Experience", style = MaterialTheme.typography.h6)
                            Spacer(Modifier.height(8.dp))
                        }
                        itemsIndexed(resume.experience) { _, exp ->
                            ExperienceCard(exp)
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Education
                    if (resume.education.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            Text("Education", style = MaterialTheme.typography.h6)
                            Spacer(Modifier.height(8.dp))
                        }
                        itemsIndexed(resume.education) { _, edu ->
                            EducationCard(edu)
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item { Spacer(Modifier.height(48.dp)) }
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
fun ContactRow(info: PersonalInfo) {
    val uriHandler = LocalUriHandler.current

    fun encode(v: String): String = try { URLEncoder.encode(v, "UTF-8") } catch (t: Exception) { "" }

    fun normalizePhone(raw: String): String {
        val trimmed = raw.trim()
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val region = Locale.getDefault().country.ifBlank { "US" }
            val parsed = phoneUtil.parse(trimmed, region)
            if (phoneUtil.isValidNumber(parsed)) phoneUtil.format(parsed, PhoneNumberFormat.E164) else trimmed.filter { it.isDigit() || it == '+' }
        } catch (e: NumberParseException) {
            trimmed.filter { it.isDigit() || it == '+' }
        }
    }

    fun formatPhoneDisplay(raw: String): String {
        val trimmed = raw.trim()
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val region = Locale.getDefault().country.ifBlank { "US" }
            val parsed = phoneUtil.parse(trimmed, region)
            if (phoneUtil.isValidNumber(parsed)) phoneUtil.format(parsed, PhoneNumberFormat.NATIONAL) else trimmed
        } catch (e: NumberParseException) {
            trimmed
        }
    }

    fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        val cleaned = if (trimmed.startsWith("@")) trimmed.substring(1) else trimmed
        if (cleaned.contains("github.com") || cleaned.contains("linkedin.com")) return if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
        if (!cleaned.contains('.') && !cleaned.startsWith("http")) return "https://github.com/${cleaned.trimStart('/')}"
        return if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            info.email?.takeIf { it.trim().isNotBlank() }?.let { email ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val subject = encode("Inquiry from Resume App")
                            val body = encode("Hello ${info.name},\n\nI found your resume and would like to get in touch.\n\nBest,\n")
                            uriHandler.openUri("mailto:${email.trim()}?subject=$subject&body=$body")
                        } catch (t: Exception) { Log.w("ResumeApp", "Failed to open email URI", t) }
                    }
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "email", tint = MaterialTheme.colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = email, color = MaterialTheme.colors.primary, style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline))
                }
            }

            info.phone?.takeIf { it.trim().isNotBlank() }?.let { phone ->
                val telTarget = normalizePhone(phone)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { try { if (telTarget.isNotBlank()) uriHandler.openUri("tel:$telTarget") } catch (t: Exception) { Log.w("ResumeApp", "Failed to open phone URI", t) } }
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "phone")
                    Spacer(Modifier.width(8.dp))
                    Text(text = formatPhoneDisplay(phone), color = MaterialTheme.colors.primary, style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline))
                }
            }

            info.github?.takeIf { it.trim().isNotBlank() }?.let { github ->
                Row(modifier = Modifier.fillMaxWidth().clickable { try { uriHandler.openUri(normalizeUrl(github)) } catch (t: Exception) { Log.w("ResumeApp", "Failed to open github URI", t) } }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = "github", tint = MaterialTheme.colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = github, color = MaterialTheme.colors.primary, style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline))
                }
            }

            info.linkedin?.takeIf { it.trim().isNotBlank() }?.let { linkedin ->
                Row(modifier = Modifier.fillMaxWidth().clickable { try { uriHandler.openUri(normalizeUrl(linkedin)) } catch (t: Exception) { Log.w("ResumeApp", "Failed to open linkedin URI", t) } }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = "linkedin", tint = MaterialTheme.colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = linkedin, color = MaterialTheme.colors.primary, style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline))
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(project.name, fontWeight = FontWeight.Bold)
            project.description?.let { Text(it) }
            if (project.technologies.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Technologies: ${project.technologies.joinToString(", ")}")
            }
        }
    }
}

@Composable
fun ExperienceCard(exp: ExperienceEntry) {
    Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${exp.title} — ${exp.company}", fontWeight = FontWeight.Bold)
            Row { Text(exp.startDate ?: ""); Spacer(Modifier.width(8.dp)); Text(exp.endDate ?: "") }
            exp.location?.let { Text(it) }
            if (exp.description.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Column { exp.description.forEach { d -> Text("• $d") } }
            }
        }
    }
}

@Composable
fun EducationCard(edu: EducationEntry) {
    Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${edu.degree} — ${edu.university}", fontWeight = FontWeight.Bold)
            Row { Text(edu.startDate ?: ""); Spacer(Modifier.width(8.dp)); Text(edu.endDate ?: "") }
            edu.gpa?.let { Text("GPA: $it") }
            edu.location?.let { Text(it) }
        }
    }
}

@Composable
fun SkillChip(skill: Skill) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEEEEEE), modifier = Modifier.padding(4.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(skill.name)
            if (!skill.level.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text("(${skill.level})", style = MaterialTheme.typography.caption)
            }
        }
    }
}

fun displayGithubLabel(raw: String): String {
    val t = raw.trim()
    var s = t.removePrefix("http://").removePrefix("https://").removePrefix("www.")
    s = s.substringAfter("github.com/").trim().removePrefix("/")
    if (s.isEmpty()) return t
    return if (s.contains('/')) s else "@${s}"
}

fun displayLinkedInLabel(raw: String): String {
    val t = raw.trim()
    var s = t.removePrefix("http://").removePrefix("https://").removePrefix("www.")
    s = s.substringAfter("linkedin.com/").trim().removePrefix("/")
    if (s.isEmpty()) return t
    return if (s.startsWith("in/")) s else "in/${s}"
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreview() {
    val sampleSkills = listOf(
        Skill(name = "Kotlin", level = "programming_languages"),
        Skill(name = "Jetpack Compose", level = "frameworks_libraries"),
        Skill(name = "Firebase", level = "firebase_services")
    )

    val sampleProjects = listOf(
        Project(
            name = "Recipe Finder App",
            description = "An Android app to search and save recipes.",
            githubUrl = "github.com/janedoe/recipe-finder",
            technologies = listOf("Kotlin", "Jetpack Compose", "Retrofit")
        )
    )

    val sampleExperience = listOf(
        ExperienceEntry(
            company = "App Innovators Inc.",
            title = "Senior Android Developer",
            startDate = "2022-06-01",
            endDate = "Present",
            location = "City, State",
            description = listOf("Led development of core features.", "Mentored junior developers.")
        )
    )

    val sampleEducation = listOf(
        EducationEntry(
            university = "University of Technology",
            degree = "Master of Science in Computer Science",
            startDate = "2018-09-01",
            endDate = "2020-05-31",
            gpa = "3.9/4.0",
            location = "City, State"
        )
    )

    val personal = PersonalInfo(
        name = "Jane Doe",
        email = "jane.doe@example.com",
        phone = "(123) 456-7890",
        github = "github.com/janedoe",
        linkedin = "linkedin.com/in/janedoe",
        summary = "Highly motivated Android Developer with experience in Kotlin and Compose."
    )

    val sampleResume = Resume(
        name = "Jane Doe",
        title = "Senior Android Developer",
        summary = "Experienced mobile engineer.",
        skills = sampleSkills,
        personalInfo = personal,
        experience = sampleExperience,
        education = sampleEducation,
        projects = sampleProjects
    )

    ResumeScreen(
        resume = sampleResume,
        isLoading = false,
        error = null,
        onRefresh = {},
        modifier = Modifier.fillMaxSize()
    )
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
        description = listOf("Led development.")
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

@Preview(showBackground = true)
@Composable
fun ContactRowPreview() {
    ContactRow(
        PersonalInfo(
            name = "Jane Doe",
            email = "jane.doe@example.com",
            phone = "(123) 456-7890",
            github = "github.com/janedoe",
            linkedin = "linkedin.com/in/janedoe",
            summary = "Sample summary"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ResumeAppPreview() {
    // create a small sample resume similar to ResumeScreenPreview
    val sampleSkills = listOf(
        Skill(name = "Kotlin", level = "programming_languages"),
        Skill(name = "Jetpack Compose", level = "frameworks_libraries")
    )

    val sampleProjects = listOf(
        Project(
            name = "Recipe Finder App",
            description = "An Android app to search and save recipes.",
            githubUrl = "github.com/janedoe/recipe-finder",
            technologies = listOf("Kotlin", "Jetpack Compose")
        )
    )

    val sampleExperience = listOf(
        ExperienceEntry(
            company = "App Innovators Inc.",
            title = "Senior Android Developer",
            startDate = "2022-06-01",
            endDate = "Present",
            location = "City, State",
            description = listOf("Led development of core features.")
        )
    )

    val sampleEducation = listOf(
        EducationEntry(
            university = "University of Technology",
            degree = "MSc Computer Science",
            startDate = "2018-09-01",
            endDate = "2020-05-31",
            gpa = "3.9/4.0",
            location = "City, State"
        )
    )

    val personal = PersonalInfo(
        name = "Jane Doe",
        email = "jane.doe@example.com",
        phone = "(123) 456-7890",
        github = "github.com/janedoe",
        linkedin = "linkedin.com/in/janedoe",
        summary = "Highly motivated Android Developer with experience in Kotlin and Compose."
    )

    val sampleResume = Resume(
        name = "Jane Doe",
        title = "Senior Android Developer",
        summary = "Experienced mobile engineer.",
        skills = sampleSkills,
        personalInfo = personal,
        experience = sampleExperience,
        education = sampleEducation,
        projects = sampleProjects
    )

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("My Resume") }) }) { innerPadding ->
            ResumeScreen(
                resume = sampleResume,
                isLoading = false,
                error = null,
                onRefresh = {},
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenLoadingPreview() {
    ResumeScreen(resume = null, isLoading = true, error = null, onRefresh = {})
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenErrorPreview() {
    ResumeScreen(resume = null, isLoading = false, error = "Network error", onRefresh = {})
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenEmptyPreview() {
    ResumeScreen(resume = null, isLoading = false, error = null, onRefresh = {})
}
