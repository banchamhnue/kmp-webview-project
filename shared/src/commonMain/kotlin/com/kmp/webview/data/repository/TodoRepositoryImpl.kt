package com.kmp.webview.data.repository

import com.kmp.webview.data.mapper.toDomain
import com.kmp.webview.data.remote.api.TodoApiService
import com.kmp.webview.domain.model.Todo
import com.kmp.webview.domain.repository.TodoRepository

class TodoRepositoryImpl(
    private val apiService: TodoApiService
) : TodoRepository {
    override suspend fun fetchTodo(): Result<Todo> {
        return try {
            val dto = apiService.fetchTodo()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
