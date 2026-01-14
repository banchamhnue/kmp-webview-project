package com.kmp.webview.android

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kmp.webview.ApiClient
import com.kmp.webview.setToastContext
import com.kmp.webview.showMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the context for Toast
        setToastContext(this)
        
        setContent {
            WebViewScreen()
        }
    }
}

@Composable
fun WebViewScreen() {
    // Remember ApiClient across recompositions
    val apiClient = remember { ApiClient() }
    
    // Fetch API data on launch
    LaunchedEffect(Unit) {
        try {
            val todo = withContext(Dispatchers.IO) {
                apiClient.fetchTodo()
            }
            showMessage(todo.title)
        } catch (e: Exception) {
            showMessage("Error: ${e.message}")
        }
    }
    
    // WebView to load sgcarmart.com
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
