# KMP WebView Project

A complete Kotlin Multiplatform (KMP) project that demonstrates WebView integration and REST API calls across both iOS and Android platforms.

## Features

- **Shared Kotlin Code**: Common business logic and API client shared between iOS and Android
- **WebView Integration**: Loads https://sgcarmart.com on both platforms
- **REST API**: Fetches data from https://jsonplaceholder.typicode.com/todos/1
- **Platform-specific Notifications**: Toast messages on Android, Alerts on iOS
- **Modern Architecture**: Uses Ktor for networking, Jetpack Compose for Android UI, SwiftUI for iOS

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

## Technologies Used

### Shared Module
- **Kotlin Multiplatform**: 1.9.22
- **Ktor Client**: 2.3.7 (HTTP networking)
- **kotlinx.serialization**: 1.6.2 (JSON parsing)
- **kotlinx.coroutines**: 1.7.3 (Async operations)

### Android
- **Jetpack Compose**: 1.5.4 (UI framework)
- **Android WebView**: Built-in
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34

### iOS
- **SwiftUI**: For declarative UI
- **WKWebView**: For web content display
- **Minimum iOS**: 14.0

## Prerequisites

### For Android Development
- JDK 8 or higher
- Android Studio (Arctic Fox or later recommended)
- Android SDK with API level 34

### For iOS Development
- macOS (required for iOS development)
- Xcode 14.0 or later
- CocoaPods installed (`sudo gem install cocoapods`)

## Building the Project

### Clone the Repository

```bash
git clone https://github.com/banchamhnue/kmp-webview-project.git
cd kmp-webview-project
```

### Build Shared Module

```bash
./gradlew :shared:build
```

### Android App

#### Build APK
```bash
./gradlew :androidApp:assembleDebug
```

#### Run on Emulator/Device
```bash
./gradlew :androidApp:installDebug
```

Or open the project in Android Studio and run from there.

### iOS App

#### Build Shared Framework
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

#### Setup Xcode Project
1. Generate the framework:
   ```bash
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```

2. Open the iOS project:
   ```bash
   cd iosApp
   pod install  # If using CocoaPods
   open iosApp.xcworkspace  # Or double-click the file
   ```

3. In Xcode:
   - Select your target device/simulator
   - Click Run (⌘R)

## How It Works

### On App Launch

1. **API Call**: The app immediately calls `https://jsonplaceholder.typicode.com/todos/1`
2. **Parse Response**: Extracts the "title" field from the JSON response
3. **Show Message**: Displays the title in:
   - **Android**: Toast message (bottom of screen)
   - **iOS**: Alert dialog
4. **Load WebView**: Simultaneously loads https://sgcarmart.com in the WebView

### Code Flow

```
App Launch
    ↓
Initialize ApiClient (shared)
    ↓
fetchTodo() - Async API call (shared)
    ↓
Parse JSON response (shared)
    ↓
showMessage(title) - Platform-specific
    ↓          ↓
Android Toast   iOS Alert
    ↓          ↓
WebView loads sgcarmart.com
```

## API Response Model

```kotlin
@Serializable
data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)
```

Example response from API:
```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

## Error Handling

- Network errors are caught and displayed in Toast/Alert
- WebView loads independently of API call
- If API fails, a default error message is shown
- All coroutines are properly scoped to prevent leaks

## Development

### Adding Dependencies

Edit `shared/build.gradle.kts` for shared dependencies:
```kotlin
commonMain.dependencies {
    implementation("...")
}
```

### Platform-Specific Code

Use `expect`/`actual` pattern:
1. Declare `expect` in `commonMain`
2. Implement `actual` in `androidMain` and `iosMain`

### Testing

Run tests for shared module:
```bash
./gradlew :shared:test
```

## Troubleshooting

### Android Issues

**WebView not loading:**
- Check internet permission in AndroidManifest.xml
- Ensure device/emulator has internet connection

**Toast not showing:**
- Verify context is properly set with `setToastContext()`

### iOS Issues

**Framework not found:**
```bash
./gradlew :shared:clean
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

**Alert not showing:**
- Check that you're running on main thread
- Verify UIWindow has rootViewController

### Build Issues

**Gradle sync failed:**
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is open source and available under the MIT License.

## Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Ktor Client](https://ktor.io/docs/client.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [SwiftUI](https://developer.apple.com/xcode/swiftui/)

## Acknowledgments

- JSONPlaceholder for the free REST API
- sgcarmart.com as the demo website
- Kotlin team for KMP
- JetBrains for Ktor
