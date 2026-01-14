package com.kmp.webview

import android.widget.Toast
import java.lang.ref.WeakReference

private var toastContextRef: WeakReference<android.content.Context>? = null

fun setToastContext(context: android.content.Context) {
    toastContextRef = WeakReference(context)
}

actual fun showMessage(message: String) {
    toastContextRef?.get()?.let {
        Toast.makeText(it, message, Toast.LENGTH_LONG).show()
    }
}
