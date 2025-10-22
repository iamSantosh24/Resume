package com.example.resume

import java.util.Locale

fun parseResumeFromMap(raw: Map<*, *>): Resume {
    // Helper to safely read nested map values
    fun Map<*, *>.getString(key: String): String? = this[key]?.toString()

    val personal = raw["personalInfo"] as? Map<*, *>
    val name = raw.getString("name") ?: personal?.getString("name") ?: ""
    val title = raw.getString("title") ?: personal?.getString("title") ?: ""
    val summary = raw.getString("summary") ?: personal?.getString("summary") ?: ""

    fun parseSkillsNode(node: Any?): List<Skill> {
        if (node == null) return emptyList()
        when (node) {
            is List<*> -> {
                return node.mapNotNull { item ->
                    when (item) {
                        is String -> Skill(name = item)
                        is Map<*, *> -> {
                            val n = item["name"]?.toString() ?: item["title"]?.toString()
                            val lvl = item["level"]?.toString()
                            if (n != null) Skill(name = n, level = lvl) else null
                        }
                        else -> null
                    }
                }
            }
            is Map<*, *> -> {
                val out = mutableListOf<Skill>()
                node.forEach { (_, v) ->
                    when (v) {
                        is List<*> -> v.forEach { it ->
                            when (it) {
                                is String -> out.add(Skill(name = it))
                                is Map<*, *> -> {
                                    val n = it["name"]?.toString() ?: it["title"]?.toString()
                                    val lvl = it["level"]?.toString()
                                    if (n != null) out.add(Skill(name = n, level = lvl))
                                }
                            }
                        }
                        is Map<*, *> -> {
                            v.values.forEach { entry ->
                                when (entry) {
                                    is String -> out.add(Skill(name = entry))
                                    is Map<*, *> -> {
                                        val n = entry["name"]?.toString() ?: entry["title"]?.toString()
                                        val lvl = entry["level"]?.toString()
                                        if (n != null) out.add(Skill(name = n, level = lvl))
                                    }
                                }
                            }
                        }
                        is String -> out.add(Skill(name = v))
                    }
                }
                return out
            }
            else -> return emptyList()
        }
    }

    fun parseExperience(node: Any?): List<ExperienceEntry> {
        if (node == null) return emptyList()
        val out = mutableListOf<ExperienceEntry>()
        when (node) {
            is List<*> -> node.forEach { item ->
                if (item is Map<*, *>) {
                    val company = item["company"]?.toString() ?: ""
                    val titleE = item["title"]?.toString() ?: ""
                    val start = item["startDate"]?.toString()
                    val end = item["endDate"]?.toString()
                    val loc = item["location"]?.toString()
                    val desc = (item["description"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val more = (item["more"] as? List<*>)?.mapNotNull { it?.toString() }?: emptyList()
                    out.add(ExperienceEntry(company = company, title = titleE, startDate = start, endDate = end, location = loc, description = desc, more = more))
                }
            }
            is Map<*, *> -> {
                node.forEach { _, v ->
                    val entry = v as? Map<*, *> ?: return@forEach
                    val company = entry["company"]?.toString() ?: ""
                    val titleE = entry["title"]?.toString() ?: entry["role"]?.toString() ?: ""
                    val start = entry["startDate"]?.toString() ?: entry["start"]?.toString()
                    val end = entry["endDate"]?.toString() ?: entry["end"]?.toString()
                    val loc = entry["location"]?.toString()
                    val desc = (entry["description"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val more = (entry["more"] as? List<*>)?.mapNotNull { it?.toString() }?: emptyList()
                    out.add(ExperienceEntry(company = company, title = titleE, startDate = start, endDate = end, location = loc, description = desc, more = more))
                }
            }
        }
        return out
    }

    fun parseEducation(node: Any?): List<EducationEntry> {
        if (node == null) return emptyList()
        val out = mutableListOf<EducationEntry>()
        when (node) {
            is List<*> -> node.forEach { item ->
                if (item is Map<*, *>) {
                    val uni = item["university"]?.toString() ?: ""
                    val degree = item["degree"]?.toString() ?: ""
                    val start = item["startDate"]?.toString()
                    val end = item["endDate"]?.toString()
                    val gpa = item["gpa"]?.toString()
                    val loc = item["location"]?.toString()
                    out.add(EducationEntry(university = uni, degree = degree, startDate = start, endDate = end, gpa = gpa, location = loc))
                }
            }
            is Map<*, *> -> {
                node.forEach { _, v ->
                    val entry = v as? Map<*, *> ?: return@forEach
                    val uni = entry["university"]?.toString() ?: entry["school"]?.toString() ?: ""
                    val degree = entry["degree"]?.toString() ?: entry["program"]?.toString() ?: ""
                    val start = entry["startDate"]?.toString() ?: entry["start"]?.toString()
                    val end = entry["endDate"]?.toString() ?: entry["end"]?.toString()
                    val gpa = entry["gpa"]?.toString()
                    val loc = entry["location"]?.toString()
                    out.add(EducationEntry(university = uni, degree = degree, startDate = start, endDate = end, gpa = gpa, location = loc))
                }
            }
        }
        return out
    }

    fun parseProjects(node: Any?): List<Project> {
        if (node == null) return emptyList()
        val out = mutableListOf<Project>()
        when (node) {
            is List<*> -> node.forEach { item ->
                if (item is Map<*, *>) {
                    val projName = item["name"]?.toString() ?: ""
                    val desc = item["description"]?.toString()
                    val github = item["githubUrl"]?.toString() ?: item["github"]?.toString()
                    val tech = (item["technologies"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    out.add(Project(name = projName, description = desc, githubUrl = github, technologies = tech))
                }
            }
            is Map<*, *> -> {
                node.forEach { _, v ->
                    val entry = v as? Map<*, *> ?: return@forEach
                    val projName = entry["name"]?.toString() ?: entry["title"]?.toString() ?: ""
                    val desc = entry["description"]?.toString()
                    val github = entry["githubUrl"]?.toString() ?: entry["github"]?.toString()
                    val tech = (entry["technologies"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    out.add(Project(name = projName, description = desc, githubUrl = github, technologies = tech))
                }
            }
        }
        return out
    }

    val skillsNode = raw["skills"]
    val skills = parseSkillsNode(skillsNode)
    val exp = parseExperience(raw["experience"])
    val edu = parseEducation(raw["education"])
    val projects = parseProjects(raw["projects"])

    // Helper to convert a date-like string into a comparable Long.
    // We'll produce an integer-like YYYYMMDD value so we don't need Java 8 time APIs (avoids API level issues).
    // 'Present'/'current'/'now' -> Long.MAX_VALUE to ensure they sort first.
    fun dateStringToComparable(s: String?): Long {
        if (s.isNullOrBlank()) return Long.MIN_VALUE
        val lowered = s.trim().lowercase(Locale.getDefault())
        if (lowered.contains("present") || lowered.contains("now") || lowered.contains("current")) return Long.MAX_VALUE

        // Try to extract YYYY, MM, DD pieces with regexes. Default missing month/day to 12/31 (end of year) or to 1 for start-of-month if needed.
        // Try full ISO-ish form first: YYYY[-/]MM[-/]DD or YYYY[-/]MM
        val isoLike = Regex("(\\d{4})(?:[-/](\\d{1,2}))?(?:[-/](\\d{1,2}))?")
        val m = isoLike.find(s)
        if (m != null) {
            val y = m.groupValues[1].toIntOrNull() ?: return Long.MIN_VALUE
            val mo = m.groupValues.getOrNull(2)?.toIntOrNull() ?: 12
            val d = m.groupValues.getOrNull(3)?.toIntOrNull() ?: 28
            val mm = mo.coerceIn(1, 12)
            val dd = d.coerceIn(1, 31)
            return (y.toLong() * 10000L) + (mm.toLong() * 100L) + dd.toLong()
        }

        // Try 'Jun 2022' style
        val monthNames = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
            "jul" to 7, "aug" to 8, "sep" to 9, "sept" to 9, "oct" to 10, "nov" to 11, "dec" to 12
        )
        val m2 = Regex("([A-Za-z]+)\\s+(\\d{4})").find(s)
        if (m2 != null) {
            val monStr = m2.groupValues[1].substring(0,3).lowercase(Locale.getDefault())
            val mo = monthNames[monStr] ?: 12
            val y = m2.groupValues[2].toIntOrNull() ?: return Long.MIN_VALUE
            return (y.toLong() * 10000L) + (mo.toLong() * 100L) + 1L
        }

        // Try to find any 4-digit year
        val yearOnly = Regex("(\\d{4})").find(s)
        if (yearOnly != null) {
            val y = yearOnly.groupValues[1].toIntOrNull() ?: return Long.MIN_VALUE
            return (y.toLong() * 10000L) + 1231L
        }

        return Long.MIN_VALUE
    }

    // Sort experience so newest entries appear first. Use endDate (fallback to startDate). 'Present' becomes top.
    val expSorted = exp.sortedWith(compareByDescending<ExperienceEntry> {
        val key = it.endDate ?: it.startDate
        dateStringToComparable(key)
    })

    val personalInfo = personal?.let { p ->

        PersonalInfo(
            name = p.getString("name") ?: "",
            email = p.getString("email"),
            phone = p.getString("phone"),
            github = p.getString("github"),
            linkedin = p.getString("linkedin"),
            summary = p.getString("summary"),
            profilePicture = p.getString("profilePicture"),
        )
    }

    return Resume(name = name, title = title, summary = summary, skills = skills, personalInfo = personalInfo, experience = expSorted, education = edu, projects = projects)
}
