package com.kmp.webview.domain.usecase

import com.kmp.webview.domain.model.Todo
import com.kmp.webview.domain.repository.TodoRepository

class GetTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(): Result<Todo> {
        return repository.fetchTodo()
    }
}
