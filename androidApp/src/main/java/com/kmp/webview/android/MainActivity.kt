package com.kmp.webview.android

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kmp.webview.di.CommonModule
import com.kmp.webview.presentation.MainViewModel
import com.kmp.webview.presentation.TodoState
import com.kmp.webview.setToastContext
import com.kmp.webview.showMessage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setToastContext(this)
        
        setContent {
            WebViewScreen()
        }
    }
}

@Composable
fun WebViewScreen() {
    val viewModel: MainViewModel = viewModel {
        MainViewModel(CommonModule.provideGetTodoUseCase())
    }
    
    val todoState by viewModel.todoState.collectAsState()
    
    LaunchedEffect(todoState) {
        when (val state = todoState) {
            is TodoState.Success -> {
                showMessage(state.todo.title)
            }
            is TodoState.Error -> {
                showMessage("Error: ${state.message}")
            }
            is TodoState.Loading -> {
                // Loading state, do nothing
            }
        }
    }
    
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl("https://sgcarmart.com")
            }
        }
    )
}
