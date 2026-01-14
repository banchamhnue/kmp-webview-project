# KMP WebView Project - Clean Architecture

A Kotlin Multiplatform (KMP) project implementing Clean Architecture principles with WebView integration and REST API calls across both iOS and Android platforms.

## Clean Architecture Overview

This project follows Clean Architecture principles with proper separation of concerns and dependency inversion:

```
┌─────────────────────────────────────────────────────┐
│                 Presentation Layer                   │
│  (UI, ViewModels, Platform-specific implementations)│
└────────────────┬────────────────────────────────────┘
                 │ depends on
                 ▼
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│        (Business Logic, Entities, Use Cases)         │
│              Pure Kotlin - No Dependencies           │
└────────────────┬────────────────────────────────────┘
                 │ implemented by
                 ▼
┌─────────────────────────────────────────────────────┐
│                   Data Layer                         │
│   (Repository Implementations, API, DTOs, Mappers)  │
└─────────────────────────────────────────────────────┘
```

## Project Structure

```
/shared                     # Shared Kotlin Multiplatform module
  /src
    /commonMain/kotlin/com/kmp/webview
      /domain              # Domain Layer (Pure Kotlin)
        /model
          - Todo.kt        # Pure domain model (no annotations)
        /repository
          - TodoRepository.kt    # Repository interface
        /usecase
          - GetTodoUseCase.kt   # Business logic use case
          
      /data                # Data Layer
        /remote
          /dto
            - TodoDto.kt         # Data Transfer Object (@Serializable)
          /api
            - TodoApiService.kt  # Ktor API client
            - HttpClientFactory.kt
        /repository
          - TodoRepositoryImpl.kt # Repository implementation
        /mapper
          - TodoMapper.kt        # DTO to Domain mapping
          
      /di                  # Dependency Injection
        - CommonModule.kt        # Shared DI container
        
      - Platform.kt        # expect/actual declarations
      
    /androidMain/kotlin/com/kmp/webview
      /presentation
        - MainViewModel.kt       # Android ViewModel with StateFlow
      - Platform.kt              # Android implementation (Toast)
      
    /iosMain/kotlin/com/kmp/webview
      - Platform.kt              # iOS implementation (UIAlert)
      
/androidApp                # Android application
  /src/main/java/com/kmp/webview/android
    - MainActivity.kt            # Compose UI with WebView
    
/iosApp                    # iOS application
  /iosApp
    - iOSApp.swift              # SwiftUI App entry point
    - ContentView.swift         # SwiftUI UI with WKWebView
```

## Architecture Layers

### 1. Domain Layer (Pure Kotlin)

The domain layer contains the business logic and is completely independent of frameworks and external dependencies.

**Key Components:**
- **Models**: Pure data classes representing business entities
  ```kotlin
  data class Todo(
      val userId: Int,
      val id: Int,
      val title: String,
      val completed: Boolean
  )
  ```

- **Repository Interfaces**: Define contracts for data operations
  ```kotlin
  interface TodoRepository {
      suspend fun fetchTodo(): Result<Todo>
  }
  ```

- **Use Cases**: Encapsulate business logic
  ```kotlin
  class GetTodoUseCase(private val repository: TodoRepository) {
      suspend operator fun invoke(): Result<Todo> {
          return repository.fetchTodo()
      }
  }
  ```

### 2. Data Layer

The data layer implements repository interfaces and handles external data sources.

**Key Components:**
- **DTOs**: Serializable data transfer objects for API responses
- **API Service**: Ktor HTTP client for network requests
- **Repository Implementation**: Implements domain repository interfaces
- **Mappers**: Convert DTOs to domain models

**Data Flow:**
```
API Response → TodoDto → Mapper → Todo (Domain Model)
```

### 3. Presentation Layer

Platform-specific UI implementations using modern frameworks.

**Android:**
- ViewModel with StateFlow for state management
- Jetpack Compose for UI
- WebView integration

**iOS:**
- SwiftUI for UI
- WKWebView integration
- ObservableObject for state management

### 4. Dependency Injection

Manual dependency injection through `CommonModule`:

```kotlin
object CommonModule {
    fun provideGetTodoUseCase(): GetTodoUseCase
    fun provideTodoRepository(): TodoRepository
    fun provideTodoApiService(): TodoApiService
}
```

## Data Flow

```
User Action
    ↓
UI (MainActivity/ContentView)
    ↓
ViewModel/Presenter
    ↓
Use Case (GetTodoUseCase)
    ↓
Repository Interface (TodoRepository)
    ↓
Repository Implementation (TodoRepositoryImpl)
    ↓
API Service (TodoApiService)
    ↓
HTTP Request → API
    ↓
TodoDto Response
    ↓
Mapper (TodoDto → Todo)
    ↓
Result<Todo>
    ↓
ViewModel → UI Update
```

## Features

1. **WebView Integration**: Loads https://sgcarmart.com in a WebView
2. **REST API Call**: Fetches todo data from JSONPlaceholder API
3. **Cross-Platform**: Shared business logic between Android and iOS
4. **Clean Architecture**: Proper separation of concerns
5. **Modern UI**: Jetpack Compose (Android) and SwiftUI (iOS)
6. **Error Handling**: Proper Result type usage for error handling

## Dependencies

### Shared Module (commonMain)
```kotlin
implementation("io.ktor:ktor-client-core:2.3.7")
implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
```

### Android-Specific
```kotlin
implementation("io.ktor:ktor-client-android:2.3.7")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
```

### iOS-Specific
```kotlin
implementation("io.ktor:ktor-client-darwin:2.3.7")
```

## Building the Project

### Android
```bash
./gradlew :androidApp:build
```

### iOS
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Build and run the project

## Clean Architecture Principles Applied

✅ **Dependency Rule**: Dependencies point inward (Domain ← Data ← Presentation)  
✅ **Separation of Concerns**: Each layer has a single responsibility  
✅ **Testability**: Domain layer is pure Kotlin and easily testable  
✅ **Independence**: Domain layer has no framework dependencies  
✅ **Single Responsibility**: Each class has one reason to change  

## Expected Behavior

1. User launches the app
2. ViewModel/Presenter calls GetTodoUseCase
3. Use case invokes repository
4. Repository calls API service
5. API returns TodoDto
6. Mapper converts DTO to Domain model
7. Result flows back through layers
8. WebView loads https://sgcarmart.com
9. Toast/Alert displays the todo title from API

## Development Guidelines

### Adding New Features

1. **Start with Domain**: Define models, repository interfaces, and use cases
2. **Implement Data Layer**: Create DTOs, API services, and repository implementations
3. **Update Presentation**: Add UI and ViewModel/Presenter logic
4. **Register Dependencies**: Update CommonModule for DI

### Testing Strategy

- **Unit Tests**: Test use cases and domain logic (pure Kotlin)
- **Integration Tests**: Test repository implementations
- **UI Tests**: Test platform-specific UI components

## License

This project is for educational purposes demonstrating Clean Architecture in Kotlin Multiplatform.
