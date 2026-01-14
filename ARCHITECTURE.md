# Clean Architecture Implementation

## Layers Overview

### Domain Layer (innermost - Pure Kotlin)
- **Location**: `shared/src/commonMain/kotlin/com/kmp/webview/domain`
- **Dependencies**: None (Pure Kotlin)
- **Responsibility**: Business logic, entities, and rules

**Components:**
1. **Models** (`domain/model/`)
   - `Todo.kt` - Pure domain entity

2. **Repository Interfaces** (`domain/repository/`)
   - `TodoRepository.kt` - Contract for data operations

3. **Use Cases** (`domain/usecase/`)
   - `GetTodoUseCase.kt` - Encapsulates business logic for fetching todo

### Data Layer (middle - Implements Domain)
- **Location**: `shared/src/commonMain/kotlin/com/kmp/webview/data`
- **Dependencies**: Domain layer interfaces, Ktor, Kotlinx Serialization
- **Responsibility**: Data access, external API communication

**Components:**
1. **DTOs** (`data/remote/dto/`)
   - `TodoDto.kt` - Serializable API response model

2. **API Services** (`data/remote/api/`)
   - `TodoApiService.kt` - HTTP client wrapper
   - `HttpClientFactory.kt` - Creates configured Ktor client

3. **Repository Implementations** (`data/repository/`)
   - `TodoRepositoryImpl.kt` - Implements `TodoRepository` interface

4. **Mappers** (`data/mapper/`)
   - `TodoMapper.kt` - Converts DTO to Domain model

### Presentation Layer (outermost - Platform specific)
- **Location**: `shared/src/androidMain` and `shared/src/iosMain`
- **Dependencies**: Domain layer (use cases), Platform frameworks
- **Responsibility**: UI logic, user interactions

**Android Components:**
- `MainViewModel.kt` - State management with StateFlow
- `MainActivity.kt` - Jetpack Compose UI
- `Platform.kt` - Toast implementation

**iOS Components:**
- `ContentView.swift` - SwiftUI UI with ViewModel
- `Platform.kt` - UIAlert implementation

### Dependency Injection Layer
- **Location**: `shared/src/commonMain/kotlin/com/kmp/webview/di`
- **Responsibility**: Object creation and dependency management

**Components:**
- `CommonModule.kt` - Manual DI container with singleton instances

## Dependency Flow

```
┌──────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                      │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Android                        iOS                │  │
│  │  - MainActivity                 - ContentView      │  │
│  │  - MainViewModel                - TodoViewModel    │  │
│  │  - Platform.kt (Toast)          - Platform.kt      │  │
│  └─────────────────┬───────────────────┬──────────────┘  │
│                    │                   │                  │
└────────────────────┼───────────────────┼──────────────────┘
                     │                   │
                     ├───────┬───────────┤
                     ▼       ▼           ▼
              ┌────────────────────────────────┐
              │       DOMAIN LAYER             │
              │  (Pure Kotlin - No Framework)  │
              │  ┌──────────────────────────┐  │
              │  │  GetTodoUseCase          │  │
              │  └───────────┬──────────────┘  │
              │              │                  │
              │              ▼                  │
              │  ┌──────────────────────────┐  │
              │  │  TodoRepository          │  │
              │  │  (Interface)             │  │
              │  └───────────┬──────────────┘  │
              │              │                  │
              │              ▼                  │
              │  ┌──────────────────────────┐  │
              │  │  Todo (Model)            │  │
              │  └──────────────────────────┘  │
              └────────────────────────────────┘
                             ▲
                             │ implements
                             │
              ┌──────────────────────────────────┐
              │         DATA LAYER               │
              │  ┌──────────────────────────┐    │
              │  │  TodoRepositoryImpl      │    │
              │  └───────────┬──────────────┘    │
              │              │                    │
              │              ▼                    │
              │  ┌──────────────────────────┐    │
              │  │  TodoApiService          │    │
              │  └───────────┬──────────────┘    │
              │              │                    │
              │              ▼                    │
              │  ┌──────────────────────────┐    │
              │  │  TodoDto → TodoMapper    │    │
              │  │  (Serializable)          │    │
              │  └──────────────────────────┘    │
              └──────────────────────────────────┘
                             │
                             ▼
                    External API (REST)
```

## Data Flow Example: Fetching a Todo

```
1. User Opens App
         ↓
2. MainActivity/ContentView initializes
         ↓
3. MainViewModel created (with GetTodoUseCase injected)
         ↓
4. viewModel.fetchTodo() called
         ↓
5. GetTodoUseCase.invoke() executed
         ↓
6. TodoRepository.fetchTodo() called
         ↓
7. TodoRepositoryImpl.fetchTodo() executes
         ↓
8. TodoApiService.fetchTodo() makes HTTP request
         ↓
9. Ktor HttpClient → GET https://jsonplaceholder.typicode.com/todos/1
         ↓
10. API returns JSON response
         ↓
11. Ktor deserializes to TodoDto
         ↓
12. TodoMapper.toDomain() converts TodoDto → Todo
         ↓
13. Result<Todo> wrapped and returned
         ↓
14. TodoRepositoryImpl → GetTodoUseCase → ViewModel
         ↓
15. ViewModel updates StateFlow<TodoState>
         ↓
16. UI observes state change
         ↓
17. Show todo.title in Toast/Alert
         ↓
18. Load WebView with https://sgcarmart.com
```

## Clean Architecture Principles

### 1. Dependency Rule
- Outer layers depend on inner layers
- Inner layers NEVER depend on outer layers
- Domain layer is pure Kotlin with no external dependencies

### 2. Separation of Concerns
- **Domain**: What the app does (business rules)
- **Data**: How data is obtained (implementation details)
- **Presentation**: How information is shown to users

### 3. Testability
- Domain layer can be tested without any framework
- Use cases are pure functions that can be easily unit tested
- Repository interface allows mocking in tests

### 4. Independence
- UI can change without affecting business logic
- Database/API can be swapped without changing use cases
- Frameworks are just plugins to the core business logic

### 5. Single Responsibility
- Each class has one reason to change
- Models: Represent data structure
- Use Cases: Execute one business action
- Repositories: Manage data from one source
- ViewModels: Manage state for one screen

## Benefits of This Architecture

1. **Maintainability**: Changes are isolated to specific layers
2. **Testability**: Each layer can be tested independently
3. **Scalability**: Easy to add new features without breaking existing code
4. **Reusability**: Domain layer is shared across platforms
5. **Flexibility**: Easy to swap implementations (e.g., different data sources)
6. **Team Collaboration**: Different teams can work on different layers

## Comparison: Before vs After

### Before (Original Structure)
```
/shared/src/commonMain
  - Todo.kt (mixed concerns - serialization + domain)
  - ApiClient.kt (direct API calls from UI layer)
  - Platform.kt
```

**Issues:**
- ❌ Business logic mixed with data access
- ❌ UI layer directly calls API client
- ❌ Hard to test
- ❌ Tight coupling between layers

### After (Clean Architecture)
```
/shared/src/commonMain/kotlin/com/kmp/webview
  /domain (Pure business logic)
  /data (Data access implementation)
  /di (Dependency management)
```

**Improvements:**
- ✅ Clear separation of concerns
- ✅ Dependency inversion (UI → Use Case → Repository Interface)
- ✅ Easy to test each layer
- ✅ Loose coupling through interfaces
- ✅ Scalable and maintainable
