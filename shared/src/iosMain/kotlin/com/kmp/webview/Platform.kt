package com.kmp.webview

import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIApplication

actual fun showMessage(message: String) {
    val alert = UIAlertController.alertControllerWithTitle(
        title = "API Response",
        message = message,
        preferredStyle = UIAlertControllerStyleAlert
    )
    
    alert.addAction(
        UIAlertAction.actionWithTitle(
            title = "OK",
            style = UIAlertActionStyleDefault,
            handler = null
        )
    )
    
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        alert,
        animated = true,
        completion = null
    )
}
