# Quick Reference Guide

## File Locations Cheat Sheet

### Adding New Features

#### 1. Define Business Entity
📁 `shared/src/commonMain/kotlin/com/kmp/webview/domain/model/`
```kotlin
data class YourEntity(
    val id: Int,
    val name: String
)
```

#### 2. Create Repository Interface
📁 `shared/src/commonMain/kotlin/com/kmp/webview/domain/repository/`
```kotlin
interface YourRepository {
    suspend fun getEntity(): Result<YourEntity>
}
```

#### 3. Create Use Case
📁 `shared/src/commonMain/kotlin/com/kmp/webview/domain/usecase/`
```kotlin
class GetYourEntityUseCase(
    private val repository: YourRepository
) {
    suspend operator fun invoke(): Result<YourEntity> {
        return repository.getEntity()
    }
}
```

#### 4. Create DTO
📁 `shared/src/commonMain/kotlin/com/kmp/webview/data/remote/dto/`
```kotlin
@Serializable
data class YourEntityDto(
    val id: Int,
    val name: String
)
```

#### 5. Create Mapper
📁 `shared/src/commonMain/kotlin/com/kmp/webview/data/mapper/`
```kotlin
fun YourEntityDto.toDomain(): YourEntity {
    return YourEntity(id = id, name = name)
}
```

#### 6. Extend API Service
📁 `shared/src/commonMain/kotlin/com/kmp/webview/data/remote/api/`
```kotlin
class YourApiService(private val client: HttpClient) {
    suspend fun getEntity(): YourEntityDto {
        return client.get("/your-endpoint").body()
    }
}
```

#### 7. Implement Repository
📁 `shared/src/commonMain/kotlin/com/kmp/webview/data/repository/`
```kotlin
class YourRepositoryImpl(
    private val apiService: YourApiService
) : YourRepository {
    override suspend fun getEntity(): Result<YourEntity> {
        return try {
            val dto = apiService.getEntity()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 8. Register in DI
📁 `shared/src/commonMain/kotlin/com/kmp/webview/di/CommonModule.kt`
```kotlin
fun provideYourRepository(): YourRepository {
    return YourRepositoryImpl(provideYourApiService())
}

fun provideGetYourEntityUseCase(): GetYourEntityUseCase {
    return GetYourEntityUseCase(provideYourRepository())
}
```

#### 9. Use in ViewModel (Android)
📁 `shared/src/androidMain/kotlin/com/kmp/webview/presentation/`
```kotlin
class YourViewModel(
    private val getEntityUseCase: GetYourEntityUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<EntityState>(EntityState.Loading)
    val state: StateFlow<EntityState> = _state.asStateFlow()
    
    fun loadEntity() {
        viewModelScope.launch {
            getEntityUseCase().fold(
                onSuccess = { _state.value = EntityState.Success(it) },
                onFailure = { _state.value = EntityState.Error(it.message ?: "") }
            )
        }
    }
}

sealed class EntityState {
    object Loading : EntityState()
    data class Success(val entity: YourEntity) : EntityState()
    data class Error(val message: String) : EntityState()
}
```

## Common Commands

### Build
```bash
# Build shared module
./gradlew :shared:build

# Build Android app
./gradlew :androidApp:build

# Clean and rebuild
./gradlew clean build
```

### Run
```bash
# Run Android app
./gradlew :androidApp:installDebug

# Run tests
./gradlew test
```

### Dependencies
```bash
# View dependency tree
./gradlew :shared:dependencies

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.5
```

## Layer Responsibilities

| Layer | Responsibility | Dependencies |
|-------|---------------|--------------|
| **Domain** | Business logic | None |
| **Data** | Data access | Domain interfaces |
| **Presentation** | UI logic | Domain use cases |
| **DI** | Wiring | All layers |

## Import Rules

### Domain Layer ✅
```kotlin
import kotlin.* // OK
import kotlinx.coroutines.* // OK (for suspend)
// NO framework imports!
```

### Data Layer ✅
```kotlin
import com.kmp.webview.domain.* // OK
import io.ktor.* // OK
import kotlinx.serialization.* // OK
```

### Presentation Layer ✅
```kotlin
import com.kmp.webview.domain.* // OK
import androidx.* // OK (Android)
import platform.UIKit.* // OK (iOS)
// NO data layer imports!
```

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Domain Model | Noun | `Todo`, `User`, `Post` |
| DTO | Model + Dto | `TodoDto`, `UserDto` |
| Repository Interface | Noun + Repository | `TodoRepository` |
| Repository Impl | Interface + Impl | `TodoRepositoryImpl` |
| Use Case | Verb + Noun + UseCase | `GetTodoUseCase` |
| Mapper | Extension function | `TodoDto.toDomain()` |
| API Service | Noun + ApiService | `TodoApiService` |
| ViewModel | Screen + ViewModel | `MainViewModel` |

## Testing Template

### Use Case Test
```kotlin
class GetTodoUseCaseTest {
    @Test
    fun `invoke should return success when repository succeeds`() = runTest {
        // Given
        val mockRepo = mockk<TodoRepository>()
        coEvery { mockRepo.fetchTodo() } returns Result.success(testTodo)
        val useCase = GetTodoUseCase(mockRepo)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(testTodo, result.getOrNull())
    }
}
```

### Repository Test
```kotlin
class TodoRepositoryImplTest {
    @Test
    fun `fetchTodo should map DTO correctly`() = runTest {
        // Given
        val mockApi = mockk<TodoApiService>()
        coEvery { mockApi.fetchTodo() } returns testDto
        val repository = TodoRepositoryImpl(mockApi)
        
        // When
        val result = repository.fetchTodo()
        
        // Then
        assertEquals(testDto.id, result.getOrNull()?.id)
    }
}
```

## Common Patterns

### Result Handling in ViewModel
```kotlin
viewModelScope.launch {
    useCase().fold(
        onSuccess = { data -> /* handle success */ },
        onFailure = { error -> /* handle error */ }
    )
}
```

### Mapper Pattern
```kotlin
fun DtoType.toDomain(): DomainType {
    return DomainType(/* map fields */)
}
```

### Repository Pattern
```kotlin
override suspend fun operation(): Result<DomainType> {
    return try {
        val dto = apiService.operation()
        Result.success(dto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Troubleshooting

### Build Issues
1. Clean build: `./gradlew clean`
2. Invalidate caches (Android Studio)
3. Delete `.gradle` and rebuild

### Dependency Issues
1. Check `build.gradle.kts` versions match
2. Run `./gradlew :shared:dependencies`
3. Ensure repository order in `settings.gradle.kts`

### Runtime Issues
1. Check DI wiring in `CommonModule`
2. Verify API endpoint URLs
3. Check network permissions in AndroidManifest

## Best Practices

✅ **DO**
- Keep domain layer pure Kotlin
- Use interfaces for repositories
- Return `Result<T>` from repositories
- Map DTOs to domain models
- Write tests for use cases
- Use sealed classes for UI states

❌ **DON'T**
- Put framework code in domain layer
- Return DTOs from repositories
- Throw exceptions from repositories
- Mix UI logic with business logic
- Expose data layer details to UI
- Skip dependency injection

## File Template

```kotlin
// Domain Model
package com.kmp.webview.domain.model
data class Entity(val id: Int, val name: String)

// Repository Interface
package com.kmp.webview.domain.repository
interface EntityRepository {
    suspend fun get(): Result<Entity>
}

// Use Case
package com.kmp.webview.domain.usecase
class GetEntityUseCase(private val repository: EntityRepository) {
    suspend operator fun invoke(): Result<Entity> = repository.get()
}

// DTO
package com.kmp.webview.data.remote.dto
@Serializable
data class EntityDto(val id: Int, val name: String)

// Mapper
package com.kmp.webview.data.mapper
fun EntityDto.toDomain() = Entity(id = id, name = name)

// Repository Impl
package com.kmp.webview.data.repository
class EntityRepositoryImpl(
    private val api: EntityApiService
) : EntityRepository {
    override suspend fun get(): Result<Entity> {
        return try {
            Result.success(api.get().toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Documentation Files

- 📖 **README.md** - Project overview
- 🏗️ **ARCHITECTURE.md** - Architecture details
- 🚀 **SETUP.md** - Setup and development guide
- 📁 **FILE_STRUCTURE.md** - Directory layout
- 📚 **CLEAN_ARCHITECTURE_PRINCIPLES.md** - Principles explained
- ⚡ **QUICK_REFERENCE.md** - This file
