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

                    // Delegate to shared parser to produce a Resume instance (handles photoUrl normalization)
                    try {
                        val parsed = parseResumeFromMap(raw)
                        cont.resume(parsed)
                    } catch (t: Throwable) {
                        cont.resumeWithException(t)
                    }
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
