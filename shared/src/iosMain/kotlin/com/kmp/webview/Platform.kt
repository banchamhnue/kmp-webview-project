package com.kmp.webview

import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.Foundation.NSSet

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
    
    // Use modern API for iOS 13+ with type-safe scene lookup
    val scenes = UIApplication.sharedApplication.connectedScenes as NSSet
    val windowScene = scenes.allObjects.firstOrNull { it is UIWindowScene } as? UIWindowScene
    val rootViewController = windowScene?.windows?.firstOrNull()?.rootViewController
    
    rootViewController?.presentViewController(
        alert,
        animated = true,
        completion = null
    )
}
