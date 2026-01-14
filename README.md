# KMP WebView Project

A complete Kotlin Multiplatform (KMP) project that demonstrates WebView integration and REST API calls across both iOS and Android platforms.

## Project Structure

```
/shared                  # Shared Kotlin Multiplatform module
  /src
    /commonMain         # Shared code (API client, models)
      - Todo.kt         # Data model
      - ApiClient.kt    # REST API client using Ktor
      - Platform.kt     # expect declarations
    /androidMain        # Android-specific implementations
      - Platform.kt     # actual implementation (Toast)
    /iosMain           # iOS-specific implementations
      - Platform.kt     # actual implementation (UIAlert)
      
/androidApp             # Android application
  /src/main
    - AndroidManifest.xml
    - MainActivity.kt   # Compose UI with WebView
    
/iosApp                 # iOS application
  /iosApp
    - iOSApp.swift      # SwiftUI App entry point
    - ContentView.swift # SwiftUI UI with WKWebView
  - Podfile            # CocoaPods dependencies
```
