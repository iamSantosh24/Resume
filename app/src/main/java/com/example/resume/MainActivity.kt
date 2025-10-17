
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[ResumeViewModel::class.java]
        setContent {
            ResumeApp(viewModel)
        }
        // Trigger initial load
        viewModel.fetchResume()
    }
}
package com.example.resume

