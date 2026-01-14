# Setup and Development Guide

## Prerequisites

### For Android Development
- JDK 17 or later
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK with API level 34
- Gradle 8.5 or later

### For iOS Development
- macOS with Xcode 14 or later
- CocoaPods installed
- iOS SDK 14 or later

## Project Setup

### 1. Clone the Repository
```bash
git clone https://github.com/banchamhnue/kmp-webview-project.git
cd kmp-webview-project
```

### 2. Android Setup

#### Option A: Using Android Studio
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete
5. Run the `androidApp` configuration

#### Option B: Using Command Line
```bash
# Build the project
./gradlew :androidApp:build

# Install on connected device/emulator
./gradlew :androidApp:installDebug

# Run the app
adb shell am start -n com.kmp.webview.android/.MainActivity
```

### 3. iOS Setup

#### Build the Shared Framework
```bash
cd iosApp
pod install
```

#### Open in Xcode
```bash
open iosApp.xcworkspace
```

#### Run the App
1. Select a simulator or device
2. Press Cmd + R to build and run

## Project Structure Walkthrough

### Shared Module (`/shared`)

#### Domain Layer
Pure Kotlin with no external dependencies:
```
/domain
  /model
    - Todo.kt              # Domain entity
  /repository
    - TodoRepository.kt    # Repository contract
  /usecase
    - GetTodoUseCase.kt    # Business logic
```

#### Data Layer
Handles external data sources:
```
/data
  /remote
    /dto
      - TodoDto.kt         # API response model
    /api
      - TodoApiService.kt  # HTTP client
      - HttpClientFactory.kt
  /repository
    - TodoRepositoryImpl.kt  # Implements TodoRepository
  /mapper
    - TodoMapper.kt        # DTO to Domain conversion
```

#### DI Layer
```
/di
  - CommonModule.kt        # Dependency injection container
```

#### Platform Layer
```
/androidMain
  /presentation
    - MainViewModel.kt     # Android ViewModel
  - Platform.kt            # Toast implementation

/iosMain
  - Platform.kt            # UIAlert implementation
```

### Android App (`/androidApp`)
```
/src/main/java/com/kmp/webview/android
  - MainActivity.kt        # Jetpack Compose UI
```

### iOS App (`/iosApp`)
```
/iosApp
  - iOSApp.swift          # App entry point
  - ContentView.swift     # SwiftUI view with ViewModel
```

## Development Workflow

### Adding a New Feature

#### 1. Start with Domain Layer
Define your business entities and use cases:

```kotlin
// 1. Create domain model
data class User(val id: Int, val name: String)

// 2. Define repository interface
interface UserRepository {
    suspend fun getUser(id: Int): Result<User>
}

// 3. Create use case
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int): Result<User> {
        return repository.getUser(id)
    }
}
```

#### 2. Implement Data Layer
Create DTOs, API services, and repository implementations:

```kotlin
// 1. Create DTO
@Serializable
data class UserDto(val id: Int, val name: String)

// 2. Add API method
class UserApiService(private val client: HttpClient) {
    suspend fun getUser(id: Int): UserDto {
        return client.get("/users/$id").body()
    }
}

// 3. Implement repository
class UserRepositoryImpl(
    private val apiService: UserApiService
) : UserRepository {
    override suspend fun getUser(id: Int): Result<User> {
        return try {
            val dto = apiService.getUser(id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// 4. Create mapper
fun UserDto.toDomain() = User(id = id, name = name)
```

#### 3. Update DI
Register your new dependencies:

```kotlin
object CommonModule {
    fun provideUserRepository(): UserRepository {
        return UserRepositoryImpl(provideUserApiService())
    }
    
    fun provideGetUserUseCase(): GetUserUseCase {
        return GetUserUseCase(provideUserRepository())
    }
}
```

#### 4. Update Presentation Layer

**Android:**
```kotlin
class UserViewModel(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()
    
    fun loadUser(id: Int) {
        viewModelScope.launch {
            getUserUseCase(id).fold(
                onSuccess = { _userState.value = UserState.Success(it) },
                onFailure = { _userState.value = UserState.Error(it.message ?: "Error") }
            )
        }
    }
}
```

**iOS:**
```swift
class UserViewModel: ObservableObject {
    @Published var userState: UserViewState = .loading
    private let getUserUseCase: GetUserUseCase
    
    func loadUser(id: Int32) {
        Task {
            do {
                let result = try await getUserUseCase.invoke(id: id)
                // Handle result
            }
        }
    }
}
```

## Testing

### Unit Testing Domain Layer
```kotlin
class GetTodoUseCaseTest {
    @Test
    fun `invoke should return success when repository succeeds`() = runTest {
        // Given
        val mockRepository = mock<TodoRepository>()
        val todo = Todo(1, 1, "Test", false)
        whenever(mockRepository.fetchTodo()).thenReturn(Result.success(todo))
        
        val useCase = GetTodoUseCase(mockRepository)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(todo, result.getOrNull())
    }
}
```

### Integration Testing Repository
```kotlin
class TodoRepositoryImplTest {
    @Test
    fun `fetchTodo should map DTO to domain model`() = runTest {
        // Given
        val mockApiService = mock<TodoApiService>()
        val dto = TodoDto(1, 1, "Test", false)
        whenever(mockApiService.fetchTodo()).thenReturn(dto)
        
        val repository = TodoRepositoryImpl(mockApiService)
        
        // When
        val result = repository.fetchTodo()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.title)
    }
}
```

## Common Tasks

### Clean Build
```bash
./gradlew clean
./gradlew :shared:build
./gradlew :androidApp:build
```

### Run Tests
```bash
./gradlew test
```

### Generate Documentation
```bash
./gradlew dokkaHtml
```

### Update Dependencies
Check for updates in:
- `build.gradle.kts` (root)
- `shared/build.gradle.kts`
- `androidApp/build.gradle.kts`
- `iosApp/Podfile`

## Troubleshooting

### Gradle Build Issues
```bash
# Clear Gradle cache
./gradlew clean cleanBuildCache

# Invalidate caches (Android Studio)
File > Invalidate Caches / Restart
```

### iOS Build Issues
```bash
# Clean derived data
rm -rf ~/Library/Developer/Xcode/DerivedData

# Reinstall pods
cd iosApp
rm -rf Pods Podfile.lock
pod install
```

### Network/API Issues
- Check internet connection
- Verify API endpoint: https://jsonplaceholder.typicode.com/todos/1
- Check for proxy settings in `gradle.properties`

## Best Practices

### 1. Follow Clean Architecture Principles
- Keep domain layer pure (no framework dependencies)
- Use dependency inversion (depend on abstractions)
- Separate concerns clearly

### 2. Use Kotlin Conventions
- Use data classes for models
- Prefer `suspend` functions for async operations
- Use `Result` type for error handling

### 3. Write Tests
- Unit test use cases and domain logic
- Integration test repositories
- UI test critical user flows

### 4. Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Keep functions small and focused

### 5. Git Workflow
- Create feature branches
- Write descriptive commit messages
- Review code before merging

## Resources

### Documentation
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Ktor Client](https://ktor.io/docs/getting-started-ktor-client.html)

### Libraries Used
- Ktor 2.3.7 - HTTP client
- Kotlinx Serialization 1.6.2 - JSON serialization
- Kotlinx Coroutines 1.7.3 - Async programming
- AndroidX Lifecycle 2.6.2 - Android ViewModel

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes following Clean Architecture
4. Write tests for new features
5. Submit a pull request

## License

This project is for educational purposes demonstrating Clean Architecture in Kotlin Multiplatform.
