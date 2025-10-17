package com.example.resume

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this).get(ResumeViewModel::class.java)
        setContent {
            ResumeApp(viewModel)
        }
        // Trigger initial load
        viewModel.fetchResume()
    }
}
