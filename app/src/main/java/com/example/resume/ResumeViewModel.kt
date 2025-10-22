package com.example.resume

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.resume.navigation.Screen

private const val KEY_SELECTED_TAB = "selected_tab"

class ResumeViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var resume by mutableStateOf<Resume?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Persist selected tab route across process death using SavedStateHandle.
    // Use a private mutableState backing property so we can implement a custom setter
    // that also writes into SavedStateHandle.
    private var _selectedTab by mutableStateOf(savedStateHandle.get<String>(KEY_SELECTED_TAB) ?: Screen.Home.route)

    var selectedTab: String
        get() = _selectedTab
        private set(value) {
            _selectedTab = value
            savedStateHandle[KEY_SELECTED_TAB] = value
        }

    fun selectTab(route: String) {
        selectedTab = route
    }

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
