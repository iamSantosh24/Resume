package com.example.resume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ResumeViewModel : ViewModel() {
    var resume by mutableStateOf<Resume?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchResume() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val r = ResumeRepository.getResume()
                resume = r
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }
}

