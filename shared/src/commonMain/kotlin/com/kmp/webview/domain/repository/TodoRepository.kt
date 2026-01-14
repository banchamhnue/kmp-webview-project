package com.kmp.webview.domain.repository

import com.kmp.webview.domain.model.Todo

interface TodoRepository {
    suspend fun fetchTodo(): Result<Todo>
}
