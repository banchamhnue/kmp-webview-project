# Clean Architecture Principles - Implementation Guide

This document explains how Clean Architecture principles are implemented in this Kotlin Multiplatform project.

## The Dependency Rule

**Principle**: Source code dependencies must point only inward, toward higher-level policies.

### Implementation

```
Presentation Layer (Outer)
        ↓ depends on
    Domain Layer (Inner - Core)
        ↑ implemented by
    Data Layer (Outer)
```

**How it's enforced:**
- Domain layer has ZERO imports from Data or Presentation layers
- Data layer imports Domain interfaces but not Presentation
- Presentation layer imports Domain but not Data (uses DI to get implementations)

**Example:**
```kotlin
// ✅ CORRECT: Data layer depends on Domain
class TodoRepositoryImpl(
    private val apiService: TodoApiService
) : TodoRepository {  // ← Implements domain interface
    override suspend fun fetchTodo(): Result<Todo> { // ← Returns domain model
        val dto = apiService.fetchTodo()
        return Result.success(dto.toDomain())
    }
}

// ❌ WRONG: Domain depending on Data
interface TodoRepository {
    suspend fun fetchTodo(): Result<TodoDto> // ← Never return DTOs from domain!
}
```

## Single Responsibility Principle (SRP)

**Principle**: A class should have only one reason to change.

### Implementation

Each class in our architecture has ONE clear responsibility:

| Class | Single Responsibility |
|-------|----------------------|
| `Todo.kt` | Represent a todo entity |
| `TodoRepository.kt` | Define todo data operations |
| `GetTodoUseCase.kt` | Execute "get todo" business logic |
| `TodoDto.kt` | Deserialize API response |
| `TodoApiService.kt` | Make HTTP requests |
| `TodoRepositoryImpl.kt` | Implement todo data operations |
| `TodoMapper.kt` | Convert DTO to Domain model |
| `MainViewModel.kt` | Manage UI state for main screen |
| `CommonModule.kt` | Provide dependency instances |

**Example:**
```kotlin
// ✅ CORRECT: Single responsibility - HTTP communication
class TodoApiService(private val client: HttpClient) {
    suspend fun fetchTodo(): TodoDto {
        return client.get("https://jsonplaceholder.typicode.com/todos/1").body()
    }
}

// ✅ CORRECT: Single responsibility - Data transformation
fun TodoDto.toDomain(): Todo {
    return Todo(
        userId = userId,
        id = id,
        title = title,
        completed = completed
    )
}

// ❌ WRONG: Multiple responsibilities in one class
class TodoService(private val client: HttpClient) {
    suspend fun fetchTodo(): Todo { // ← HTTP + Mapping in one place
        val response = client.get("...").body<TodoDto>()
        return Todo(response.userId, response.id, response.title, response.completed)
    }
}
```

## Dependency Inversion Principle (DIP)

**Principle**: High-level modules should not depend on low-level modules. Both should depend on abstractions.

### Implementation

**High-level module** (Use Case) depends on **abstraction** (Repository Interface):

```kotlin
// Domain Layer - Abstraction
interface TodoRepository {
    suspend fun fetchTodo(): Result<Todo>
}

// Domain Layer - High-level module
class GetTodoUseCase(
    private val repository: TodoRepository // ← Depends on abstraction
) {
    suspend operator fun invoke(): Result<Todo> {
        return repository.fetchTodo()
    }
}

// Data Layer - Low-level module
class TodoRepositoryImpl(
    private val apiService: TodoApiService
) : TodoRepository { // ← Implements abstraction
    override suspend fun fetchTodo(): Result<Todo> {
        // Implementation details
    }
}
```

**Benefits:**
- Use case doesn't know or care about HTTP, databases, or APIs
- Easy to swap implementations (mock for testing, different API, etc.)
- Changes to data layer don't affect business logic

## Interface Segregation Principle (ISP)

**Principle**: Clients should not be forced to depend on interfaces they don't use.

### Implementation

Repositories are focused and specific:

```kotlin
// ✅ CORRECT: Focused interface
interface TodoRepository {
    suspend fun fetchTodo(): Result<Todo>
}

// ✅ If we need more operations, create specific interfaces
interface TodoCacheRepository {
    suspend fun cacheTodo(todo: Todo)
    suspend fun getCachedTodo(): Todo?
}

// ❌ WRONG: Fat interface forcing clients to depend on unused methods
interface TodoRepository {
    suspend fun fetchTodo(): Result<Todo>
    suspend fun fetchAllTodos(): Result<List<Todo>>
    suspend fun createTodo(todo: Todo): Result<Todo>
    suspend fun updateTodo(id: Int, todo: Todo): Result<Todo>
    suspend fun deleteTodo(id: Int): Result<Unit>
    suspend fun cacheTodo(todo: Todo)
    suspend fun clearCache()
}
// GetTodoUseCase only needs fetchTodo but forced to know about all methods
```

## Open/Closed Principle (OCP)

**Principle**: Software entities should be open for extension but closed for modification.

### Implementation

**Extensibility through interfaces:**

```kotlin
// Base interface - closed for modification
interface TodoRepository {
    suspend fun fetchTodo(): Result<Todo>
}

// Open for extension - add new implementations
class ApiTodoRepository(
    private val apiService: TodoApiService
) : TodoRepository {
    override suspend fun fetchTodo(): Result<Todo> {
        // API implementation
    }
}

class CachedTodoRepository(
    private val apiRepository: TodoRepository,
    private val cache: Cache
) : TodoRepository {
    override suspend fun fetchTodo(): Result<Todo> {
        // Cached implementation
    }
}

class MockTodoRepository : TodoRepository {
    override suspend fun fetchTodo(): Result<Todo> {
        // Mock implementation for testing
    }
}
```

## Separation of Concerns

**Principle**: Different concerns should be separated into distinct sections or layers.

### Implementation

Our project has clear separation:

#### Concern: Business Rules
**Where**: Domain Layer
```kotlin
// Only business logic, no framework code
class GetTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(): Result<Todo> {
        return repository.fetchTodo()
    }
}
```

#### Concern: Data Access
**Where**: Data Layer
```kotlin
// HTTP, serialization, data transformation
class TodoApiService(private val client: HttpClient) {
    suspend fun fetchTodo(): TodoDto { /* ... */ }
}
```

#### Concern: User Interface
**Where**: Presentation Layer
```kotlin
// UI state management, Android-specific code
class MainViewModel(
    private val getTodoUseCase: GetTodoUseCase
) : ViewModel() {
    private val _todoState = MutableStateFlow<TodoState>(TodoState.Loading)
    // ...
}
```

## Pure Kotlin Domain

**Principle**: The domain layer should be pure Kotlin with no framework dependencies.

### Implementation

Domain layer files have NO imports from:
- `androidx.*`
- `io.ktor.*`
- `kotlinx.serialization.*` (except Result and coroutines)
- `android.*`
- `platform.UIKit.*`

```kotlin
// ✅ CORRECT: Pure Kotlin domain model
package com.kmp.webview.domain.model

data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

// ❌ WRONG: Framework dependency in domain
package com.kmp.webview.domain.model

import kotlinx.serialization.Serializable

@Serializable // ← Framework annotation in domain layer!
data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)
```

## Testability

**Principle**: Architecture should make testing easy.

### Implementation

**Unit testing use cases** (no mocks needed for domain logic):

```kotlin
class GetTodoUseCaseTest {
    @Test
    fun `should return todo when repository succeeds`() = runTest {
        // Given
        val mockRepository = object : TodoRepository {
            override suspend fun fetchTodo(): Result<Todo> {
                return Result.success(Todo(1, 1, "Test", false))
            }
        }
        val useCase = GetTodoUseCase(mockRepository)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.title)
    }
}
```

**Integration testing** (test repository with mock API):

```kotlin
class TodoRepositoryImplTest {
    @Test
    fun `should map DTO to domain model correctly`() = runTest {
        // Given
        val mockApi = object : TodoApiService {
            override suspend fun fetchTodo(): TodoDto {
                return TodoDto(1, 1, "Test", false)
            }
        }
        val repository = TodoRepositoryImpl(mockApi)
        
        // When
        val result = repository.fetchTodo()
        
        // Then
        val todo = result.getOrNull()
        assertNotNull(todo)
        assertEquals("Test", todo?.title)
    }
}
```

## Data Mapping

**Principle**: Don't expose data layer models to domain layer.

### Implementation

```kotlin
// Data Layer - API Response
@Serializable
data class TodoDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

// Domain Layer - Business Entity
data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

// Mapper - Converts between layers
fun TodoDto.toDomain(): Todo {
    return Todo(
        userId = userId,
        id = id,
        title = title,
        completed = completed
    )
}
```

**Why separate?**
1. API can change without affecting domain
2. Domain represents business concepts, not API structure
3. Can add validation/transformation in mapper
4. Makes testing easier

**Example with transformation:**
```kotlin
fun TodoDto.toDomain(): Todo {
    return Todo(
        userId = userId,
        id = id,
        title = title.trim().ifEmpty { "Untitled" }, // ← Business rule in mapper
        completed = completed
    )
}
```

## Error Handling

**Principle**: Use domain-appropriate error handling, not framework-specific exceptions.

### Implementation

```kotlin
// ✅ CORRECT: Return Result type (domain-friendly)
interface TodoRepository {
    suspend fun fetchTodo(): Result<Todo>
}

class TodoRepositoryImpl(
    private val apiService: TodoApiService
) : TodoRepository {
    override suspend fun fetchTodo(): Result<Todo> {
        return try {
            val dto = apiService.fetchTodo()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e) // Catch all exceptions, return Result
        }
    }
}

// ❌ WRONG: Expose framework exceptions
interface TodoRepository {
    @Throws(IOException::class, SerializationException::class)
    suspend fun fetchTodo(): Todo // Domain knows about IO and Serialization!
}
```

## Summary Checklist

When adding new features, verify:

- [ ] Domain layer has no framework imports
- [ ] Each class has single responsibility
- [ ] Use cases depend on repository interfaces, not implementations
- [ ] DTOs are separate from domain models
- [ ] Mappers handle data transformation
- [ ] Repositories return `Result<T>` not throw exceptions
- [ ] Dependency injection provides implementations
- [ ] Tests can be written without real API calls
- [ ] Changes to one layer don't require changes to others

## References

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
