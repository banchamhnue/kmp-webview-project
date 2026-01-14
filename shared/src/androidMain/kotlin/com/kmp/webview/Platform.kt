package com.kmp.webview

import android.widget.Toast

private var toastContext: android.content.Context? = null

fun setToastContext(context: android.content.Context) {
    toastContext = context
}

actual fun showMessage(message: String) {
    toastContext?.let {
        Toast.makeText(it, message, Toast.LENGTH_LONG).show()
    }
}
