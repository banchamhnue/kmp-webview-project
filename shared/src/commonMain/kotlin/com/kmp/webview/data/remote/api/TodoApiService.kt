package com.kmp.webview.data.remote.api

import com.kmp.webview.data.remote.dto.TodoDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TodoApiService(private val client: HttpClient) {
    suspend fun fetchTodo(): TodoDto {
        return client.get("https://jsonplaceholder.typicode.com/todos/1").body()
    }
    
    fun close() {
        client.close()
    }
}
