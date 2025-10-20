package com.example.resume

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseResumeRepository {
    // Reads /users/myResumeProfile from the Realtime Database and maps it to the Resume data class.
    suspend fun getResume(): Resume = suspendCancellableCoroutine { cont ->
        val ref = Firebase.database.getReference("users/myResumeProfile")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Try direct mapping first, but guard against parsing exceptions (e.g. array vs object shape mismatches)
                    var direct: Resume? = null
                    try {
                        direct = snapshot.getValue(Resume::class.java)
                    } catch (e: Exception) {
                        // Can't map directly (likely due to shape mismatch) — log and fall back to manual parsing
                        Log.w("FirebaseResumeRepository", "Direct mapping failed, will parse manually: ${e.message}")
                    }
                    if (direct != null) {
                        cont.resume(direct)
                        return
                    }

                    // Fallback: build Resume manually to handle different JSON shapes
                    val raw = snapshot.value as? Map<*, *> ?: run {
                        cont.resumeWithException(Exception("No resume data found in Firebase"))
                        return
                    }

                    // Helper to safely read nested map values
                    fun Map<*, *>.getString(key: String): String? = this[key]?.toString()

                    // name/title/summary may be at top-level or under personalInfo
                    val personal = raw["personalInfo"] as? Map<*, *>
                    val name = raw.getString("name") ?: personal?.getString("name") ?: ""
                    val title = raw.getString("title") ?: personal?.getString("title") ?: ""
                    val summary = raw.getString("summary") ?: personal?.getString("summary") ?: ""

                    // Parse skills node which may be an array or an object with categories
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
                                    out.add(ExperienceEntry(company = company, title = titleE, startDate = start, endDate = end, location = loc, description = desc))
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
                                    out.add(ExperienceEntry(company = company, title = titleE, startDate = start, endDate = end, location = loc, description = desc))
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
                                    val name = item["name"]?.toString() ?: ""
                                    val desc = item["description"]?.toString()
                                    val github = item["githubUrl"]?.toString() ?: item["github"]?.toString()
                                    val tech = (item["technologies"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                    out.add(Project(name = name, description = desc, githubUrl = github, technologies = tech))
                                }
                            }
                            is Map<*, *> -> {
                                node.forEach { _, v ->
                                    val entry = v as? Map<*, *> ?: return@forEach
                                    val name = entry["name"]?.toString() ?: entry["title"]?.toString() ?: ""
                                    val desc = entry["description"]?.toString()
                                    val github = entry["githubUrl"]?.toString() ?: entry["github"]?.toString()
                                    val tech = (entry["technologies"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                    out.add(Project(name = name, description = desc, githubUrl = github, technologies = tech))
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

                    val personalInfo = personal?.let { p ->
                        PersonalInfo(
                            name = p.getString("name") ?: "",
                            email = p.getString("email"),
                            phone = p.getString("phone"),
                            github = p.getString("github"),
                            linkedin = p.getString("linkedin"),
                            summary = p.getString("summary")
                        )
                    }

                    val built = Resume(name = name, title = title, summary = summary, skills = skills, personalInfo = personalInfo, experience = exp, education = edu, projects = projects)
                     cont.resume(built)
                 } catch (t: Throwable) {
                     cont.resumeWithException(t)
                 }
             }

             override fun onCancelled(error: DatabaseError) {
                 cont.resumeWithException(Exception(error.message))
             }
         }

         ref.addListenerForSingleValueEvent(listener)

         cont.invokeOnCancellation {
             try {
                 ref.removeEventListener(listener)
             } catch (_: Exception) {
             }
         }
     }

    // Writes /users/myResumeProfile to the Realtime Database (overwrites existing node).
    suspend fun setResume(resume: Resume) = suspendCancellableCoroutine<Unit> { cont ->
        val ref = Firebase.database.getReference("users/myResumeProfile")
        val listener = com.google.firebase.database.DatabaseReference.CompletionListener { error, _ ->
            if (error != null) {
                cont.resumeWithException(Exception(error.message))
            } else {
                cont.resume(Unit)
            }
        }

        ref.setValue(resume, listener)

        cont.invokeOnCancellation {
            // No direct way to cancel setValue; nothing to do here.
        }
    }
}
