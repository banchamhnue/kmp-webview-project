package com.kmp.webview

import android.widget.Toast
import java.lang.ref.WeakReference

// Thread-safe context storage using WeakReference
// Note: setToastContext should be called from the main thread (e.g., in Activity.onCreate)
private var toastContextRef: WeakReference<android.content.Context>? = null

@Synchronized
fun setToastContext(context: android.content.Context) {
    toastContextRef = WeakReference(context)
}

actual fun showMessage(message: String) {
    toastContextRef?.get()?.let {
        Toast.makeText(it, message, Toast.LENGTH_LONG).show()
    }
}
