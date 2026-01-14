# Project File Structure

## Complete Clean Architecture Directory Layout

```
kmp-webview-project/
├── README.md                           # Main documentation
├── ARCHITECTURE.md                     # Architecture details
├── SETUP.md                           # Setup guide
├── build.gradle.kts                   # Root build configuration
├── settings.gradle.kts                # Gradle settings
├── gradle.properties                  # Gradle properties
│
├── shared/                            # Shared Kotlin Multiplatform module
│   ├── build.gradle.kts              # Shared module build config
│   └── src/
│       ├── commonMain/kotlin/com/kmp/webview/
│       │   ├── Platform.kt           # expect declarations
│       │   │
│       │   ├── domain/               # 🟢 DOMAIN LAYER (Pure Kotlin)
│       │   │   ├── model/
│       │   │   │   └── Todo.kt       # Domain entity (no annotations)
│       │   │   ├── repository/
│       │   │   │   └── TodoRepository.kt  # Repository interface
│       │   │   └── usecase/
│       │   │       └── GetTodoUseCase.kt  # Business logic
│       │   │
│       │   ├── data/                 # 🔵 DATA LAYER
│       │   │   ├── remote/
│       │   │   │   ├── dto/
│       │   │   │   │   └── TodoDto.kt     # @Serializable DTO
│       │   │   │   └── api/
│       │   │   │       ├── TodoApiService.kt     # HTTP client
│       │   │   │       └── HttpClientFactory.kt  # Ktor setup
│       │   │   ├── repository/
│       │   │   │   └── TodoRepositoryImpl.kt  # Implements interface
│       │   │   └── mapper/
│       │   │       └── TodoMapper.kt      # DTO → Domain
│       │   │
│       │   └── di/                   # 🟡 DEPENDENCY INJECTION
│       │       └── CommonModule.kt   # DI container
│       │
│       ├── androidMain/kotlin/com/kmp/webview/
│       │   ├── Platform.kt           # Android Toast implementation
│       │   └── presentation/         # 🟣 PRESENTATION (Android)
│       │       └── MainViewModel.kt  # ViewModel with StateFlow
│       │
│       └── iosMain/kotlin/com/kmp/webview/
│           └── Platform.kt           # iOS UIAlert implementation
│
├── androidApp/                        # 📱 Android Application
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/kmp/webview/android/
│           └── MainActivity.kt        # Jetpack Compose UI
│
└── iosApp/                           # 🍎 iOS Application
    ├── Podfile
    └── iosApp/
        ├── iOSApp.swift              # SwiftUI app entry
        └── ContentView.swift         # SwiftUI view + ViewModel
```

## Layer Breakdown

### 🟢 Domain Layer (Core Business Logic)
**Location**: `shared/src/commonMain/kotlin/com/kmp/webview/domain/`
**Files**: 3 files
- `model/Todo.kt` - Pure domain entity
- `repository/TodoRepository.kt` - Repository contract
- `usecase/GetTodoUseCase.kt` - Business logic use case

**Dependencies**: None (Pure Kotlin)
**Principles**: 
- No framework dependencies
- No annotations (except standard Kotlin)
- Represents business rules

### 🔵 Data Layer (External Data Access)
**Location**: `shared/src/commonMain/kotlin/com/kmp/webview/data/`
**Files**: 5 files
- `remote/dto/TodoDto.kt` - Serializable API model
- `remote/api/TodoApiService.kt` - Ktor client wrapper
- `remote/api/HttpClientFactory.kt` - HTTP client factory
- `repository/TodoRepositoryImpl.kt` - Repository implementation
- `mapper/TodoMapper.kt` - DTO to Domain mapper

**Dependencies**: Ktor, Kotlinx Serialization
**Principles**:
- Implements domain interfaces
- Handles external data sources
- Contains serialization logic

### 🟡 Dependency Injection
**Location**: `shared/src/commonMain/kotlin/com/kmp/webview/di/`
**Files**: 1 file
- `CommonModule.kt` - Manual DI container

**Principles**:
- Provides instances to all layers
- Manages object lifecycle
- Singleton pattern for shared instances

### 🟣 Presentation Layer (UI & Platform-specific)
**Android**: `shared/src/androidMain/kotlin/com/kmp/webview/presentation/`
- `MainViewModel.kt` - State management with StateFlow
- `Platform.kt` - Toast implementation

**iOS**: `iosApp/iosApp/`
- `ContentView.swift` - SwiftUI view with ViewModel
- (Platform.kt in iosMain for alerts)

**Android App**: `androidApp/src/main/java/com/kmp/webview/android/`
- `MainActivity.kt` - Jetpack Compose UI + WebView

**Dependencies**: Android/iOS frameworks, Domain layer
**Principles**:
- Platform-specific implementations
- Observes domain state
- Triggers use cases

## File Count Summary

| Layer | Common | Android | iOS | Total |
|-------|--------|---------|-----|-------|
| Domain | 3 | 0 | 0 | 3 |
| Data | 5 | 0 | 0 | 5 |
| DI | 1 | 0 | 0 | 1 |
| Presentation | 1 | 2 | 1 | 4 |
| **Total** | **10** | **2** | **1** | **13** |

## Dependency Graph

```
MainActivity.kt (Android)          ContentView.swift (iOS)
        ↓                                    ↓
    MainViewModel.kt                   TodoViewModel
        ↓                                    ↓
        └────────────┬───────────────────────┘
                     ↓
              CommonModule (DI)
                     ↓
              GetTodoUseCase ←─── domain/usecase/
                     ↓
              TodoRepository ←─── domain/repository/ (interface)
                     ↑
                     │ implements
                     │
            TodoRepositoryImpl ←─── data/repository/
                     ↓
              TodoApiService ←─── data/remote/api/
                     ↓
                  Ktor HTTP
                     ↓
              API Response (JSON)
                     ↓
                 TodoDto ←─── data/remote/dto/
                     ↓
               TodoMapper ←─── data/mapper/
                     ↓
                  Todo ←─── domain/model/
```

## Key Files Description

### Core Business Logic
1. **Todo.kt** - Domain model representing a todo item
2. **TodoRepository.kt** - Interface defining data operations
3. **GetTodoUseCase.kt** - Use case for fetching todo

### Data Access
4. **TodoDto.kt** - Data transfer object for API
5. **TodoApiService.kt** - HTTP client for API calls
6. **HttpClientFactory.kt** - Creates configured Ktor client
7. **TodoRepositoryImpl.kt** - Implements repository interface
8. **TodoMapper.kt** - Maps DTO to domain model

### Dependency Management
9. **CommonModule.kt** - Provides dependency instances

### Platform Implementations
10. **MainViewModel.kt** (Android) - State management
11. **MainActivity.kt** (Android) - UI with Compose + WebView
12. **ContentView.swift** (iOS) - SwiftUI UI + ViewModel
13. **Platform.kt** (expect/actual) - Platform-specific messages

## Clean Architecture Benefits Demonstrated

✅ **Separation of Concerns**
- Each file has a single, well-defined responsibility
- Easy to locate and modify specific functionality

✅ **Testability**
- Domain layer can be unit tested without mocks
- Each layer can be tested independently

✅ **Maintainability**
- Changes isolated to specific layers
- Clear structure makes onboarding easy

✅ **Scalability**
- Easy to add new features by extending existing patterns
- Layers can be developed in parallel

✅ **Reusability**
- Domain and data layers shared across platforms
- Business logic written once, used everywhere
