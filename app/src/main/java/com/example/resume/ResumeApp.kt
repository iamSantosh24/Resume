package com.example.resume

import android.util.Log
import java.net.URLEncoder
import java.util.Locale

import com.example.resume.navigation.AppNavGraph

import coil.compose.AsyncImage

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun ResumeApp(viewModel: ResumeViewModel) {
    // Use the app navigation graph which will host the tabbed UI as the Home destination.
    MaterialTheme { AppNavGraph(viewModel = viewModel) }
}

@Composable
fun AvatarImage(photoUrl: String?, name: String?, modifier: Modifier = Modifier) {
    val initials =
        name?.split(" ")?.mapNotNull { it.firstOrNull()?.toString()?.uppercase() }?.take(2)
            ?.joinToString("") ?: ""

    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val available = this.maxWidth
        val preferred = available * 1.0f
        val size = if (preferred < 375.dp) preferred else 375.dp

        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl.trim(),
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), CircleShape),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                color = Color(0xFFDDDDDD)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = initials.ifBlank { "" },
                        style = MaterialTheme.typography.h4,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
    // Apply a subtle vertical gradient behind the whole screen. Keep modifier usage minimal so caller's sizing is preserved.
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50), // green (Material Green 500)
                        Color(0xFFFFEB3B)  // yellow (Material Yellow 500)
                    )
                )
            )
            .padding(16.dp)
    ) {
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
                        // Header: large centered avatar, Name / Title
                        AvatarImage(
                            photoUrl = resume.personalInfo?.profilePicture,
                            name = resume.personalInfo?.name ?: resume.name
                        )
                        Spacer(Modifier.height(12.dp))
                        val displayName = resume.personalInfo?.name ?: resume.name
                        Text(
                            displayName,
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold
                        )
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

                    // Experience & Education (moved to a dedicated lazy-aware composable extension)
                    experienceEducationItems(experience = resume.experience, education = resume.education)

                    item { Spacer(Modifier.height(48.dp)) }
                }
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
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

    fun encode(v: String): String = try {
        URLEncoder.encode(v, "UTF-8")
    } catch (t: Exception) {
        ""
    }

    fun normalizePhone(raw: String): String {
        val trimmed = raw.trim()
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val region = Locale.getDefault().country.ifBlank { "US" }
            val parsed = phoneUtil.parse(trimmed, region)
            if (phoneUtil.isValidNumber(parsed)) phoneUtil.format(
                parsed,
                PhoneNumberFormat.E164
            ) else trimmed.filter { it.isDigit() || it == '+' }
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
            if (phoneUtil.isValidNumber(parsed)) phoneUtil.format(
                parsed,
                PhoneNumberFormat.NATIONAL
            ) else trimmed
        } catch (e: NumberParseException) {
            trimmed
        }
    }

    fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        val cleaned = if (trimmed.startsWith("@")) trimmed.substring(1) else trimmed
        if (cleaned.contains("github.com") || cleaned.contains("linkedin.com")) return if (trimmed.startsWith(
                "http"
            )
        ) trimmed else "https://$trimmed"
        if (!cleaned.contains('.') && !cleaned.startsWith("http")) return "https://github.com/${
            cleaned.trimStart(
                '/'
            )
        }"
        return if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            info.email?.takeIf { it.trim().isNotBlank() }?.let { email ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(indication = LocalIndication.current, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                        try {
                            val subject = encode("Inquiry from Resume App")
                            val body =
                                encode("Hello ${info.name},\n\nI found your resume and would like to get in touch.\n\nBest,\n")
                            uriHandler.openUri("mailto:${email.trim()}?subject=$subject&body=$body")
                        } catch (t: Exception) {
                            Log.w("ResumeApp", "Failed to open email URI", t)
                        }
                    }
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = "email",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = email,
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }

            info.phone?.takeIf { it.trim().isNotBlank() }?.let { phone ->
                val telTarget = normalizePhone(phone)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(indication = LocalIndication.current, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                        try {
                            if (telTarget.isNotBlank()) uriHandler.openUri("tel:$telTarget")
                        } catch (t: Exception) {
                            Log.w("ResumeApp", "Failed to open phone URI", t)
                        }
                    }
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Phone, contentDescription = "phone")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatPhoneDisplay(phone),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }

            info.github?.takeIf { it.trim().isNotBlank() }?.let { github ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(indication = LocalIndication.current, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                        try {
                            uriHandler.openUri(normalizeUrl(github))
                        } catch (t: Exception) {
                            Log.w("ResumeApp", "Failed to open github URI", t)
                        }
                    }
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Code,
                        contentDescription = "github",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = github,
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }

            info.linkedin?.takeIf { it.trim().isNotBlank() }?.let { linkedin ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(indication = LocalIndication.current, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                        try {
                            uriHandler.openUri(normalizeUrl(linkedin))
                        } catch (t: Exception) {
                            Log.w("ResumeApp", "Failed to open linkedin URI", t)
                        }
                    }
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Business,
                        contentDescription = "linkedin",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = linkedin,
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.body2.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }
        }
    }
}

// NOTE: The following UI components were intentionally moved to their own files to keep this file focused on the
// main resume screen and reduce file length. See:
// - app/src/main/java/com/example/resume/InfoCard.kt  (the generic InfoCard composable)
// - app/src/main/java/com/example/resume/Cards.kt     (ProjectCard, ExperienceCard, EducationCard, SkillChip and previews)

// Project/Experience/Education/Skill card composables were moved to `Cards.kt`.
// See: app/src/main/java/com/example/resume/Cards.kt

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
            description = listOf(
                "Led development of core features.",
                "Mentored junior developers."
            ),
            more = listOf("Introduced CI pipeline", "Improved app startup time")
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
        summary = "Highly motivated Android Developer with experience in Kotlin and Compose.",
        profilePicture = "https://via.placeholder.com/375"
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

// Previews for the card components live in `Cards.kt` to keep this file focused.
// See: app/src/main/java/com/example/resume/Cards.kt

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
            summary = "Sample summary",
            profilePicture = "https://via.placeholder.com/375"
        )
    )
}

@Composable
fun TabbedResumeAppContent(
    sampleResume: Resume?,
    isLoading: Boolean = false,
    error: String? = null,
    onRefresh: () -> Unit = {}
) {
    // A simple saveable tab state so selection survives configuration changes
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Home) }

    val titleText = sampleResume?.personalInfo?.name ?: sampleResume?.name ?: "My Resume"

    Scaffold(
        topBar = { TopAppBar(title = { Text(titleText) }) },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == BottomTab.Home,
                    onClick = { selectedTab = BottomTab.Home }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Work, contentDescription = "Employment") },
                    label = { Text("Employment") },
                    selected = selectedTab == BottomTab.Employment,
                    onClick = { selectedTab = BottomTab.Employment }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.School, contentDescription = "Education") },
                    label = { Text("Education") },
                    selected = selectedTab == BottomTab.Education,
                    onClick = { selectedTab = BottomTab.Education }
                )
            }
        }
    ) { innerPadding ->
        // Content area: show different composables depending on selected tab
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                BottomTab.Home -> {
                    // show personal info summary - reuse ContactRow and header pieces
                    when {
                        isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        error != null -> Column(modifier = Modifier.align(Alignment.Center)) {
                            Text("Error: $error")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onRefresh) { Text("Retry") }
                        }
                        sampleResume != null -> PersonalInfoScreen(personal = sampleResume.personalInfo ?: PersonalInfo(name = sampleResume.name), displayName = sampleResume.name, title = sampleResume.title)
                        else -> PersonalInfoPreviewContent()
                    }
                }
                BottomTab.Employment -> {
                    if (!isLoading && sampleResume?.experience != null) ExperienceList(sampleResume.experience)
                    else ExperienceListPreviewContent()
                }
                BottomTab.Education -> {
                    if (!isLoading && sampleResume?.education != null) EducationList(sampleResume.education)
                    else EducationListPreviewContent()
                }
            }
        }
    }
}

@Composable
fun TabbedResumeApp(viewModel: ResumeViewModel) {
    TabbedResumeAppContent(sampleResume = viewModel.resume, isLoading = viewModel.isLoading, error = viewModel.errorMessage, onRefresh = { viewModel.fetchResume() })
}

private enum class BottomTab { Home, Employment, Education }

@Composable
fun ExperienceList(items: List<ExperienceEntry>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No employment history")
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        items(items) { exp ->
            ExperienceCard(exp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun EducationList(items: List<EducationEntry>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No education records")
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        items(items) { ed ->
            EducationCard(ed)
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Minimal preview content adapters (reuse data from previews defined earlier)
@Composable
private fun ResumeScreenPreviewContent() {
    // Recreate the sampleResume from ResumeScreenPreview since sample data is local to that function
    ResumeScreenPreview()
}

@Composable
private fun ExperienceListPreviewContent() {
    val sample = listOf(
        ExperienceEntry(
            company = "App Innovators Inc.",
            title = "Senior Android Developer",
            startDate = "2022-06-01",
            endDate = "Present",
            location = "City, State",
            description = listOf("Led development of core features.")
        )
    )
    ExperienceList(sample)
}

@Composable
private fun EducationListPreviewContent() {
    val sample = listOf(
        EducationEntry(
            university = "University of Technology",
            degree = "MSc Computer Science",
            startDate = "2018-09-01",
            endDate = "2020-05-31",
            gpa = "3.9/4.0",
            location = "City, State"
        )
    )
    EducationList(sample)
}

// Update Preview to show TabbedResumeApp with a sample resume
@Preview(showBackground = true)
@Composable
fun ResumeAppPreview() {
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
            description = listOf("Led development of core features."),
            more = listOf("Spearheaded migration to Compose", "Improved test coverage")
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
        summary = "Highly motivated Android Developer with experience in Kotlin and Compose.",
        profilePicture = "https://via.placeholder.com/375"
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
        TabbedResumeAppContent(sampleResume)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonalInfoScreen(personal: PersonalInfo?, displayName: String? = null, title: String? = null) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Pinned header containing avatar, name and title
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarImage(photoUrl = personal?.profilePicture, name = personal?.name ?: displayName)
                Spacer(Modifier.height(12.dp))
                val nameToShow = personal?.name ?: displayName
                if (!nameToShow.isNullOrBlank()) Text(nameToShow, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                if (!title.isNullOrBlank()) Text(title, style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(8.dp))
            }
        }

        // Body: summary
        item {
            personal?.summary?.let { s ->
                if (s.isNotBlank()) {
                    Text(s, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Contact/details area
        item {
            personal?.let { ContactRow(it) }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PersonalInfoPreviewContent() {
    val personal = PersonalInfo(
        name = "Jane Doe",
        email = "jane.doe@example.com",
        phone = "(123) 456-7890",
        github = "github.com/janedoe",
        linkedin = "linkedin.com/in/janedoe",
        summary = "Highly motivated Android Developer with experience in Kotlin and Compose.",
        profilePicture = "https://via.placeholder.com/375"
    )
    PersonalInfoScreen(personal = personal, displayName = personal.name, title = "Senior Android Developer")
}
