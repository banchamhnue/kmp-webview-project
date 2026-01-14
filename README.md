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

