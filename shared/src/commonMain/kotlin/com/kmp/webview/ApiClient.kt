package com.kmp.webview

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchTodo(): Todo {
        return try {
            client.get("https://jsonplaceholder.typicode.com/todos/1").body()
        } catch (e: Exception) {
            // Return a default todo in case of error
            Todo(
                userId = 0,
                id = 0,
                title = "Error fetching todo: ${e.message}",
                completed = false
            )
        }
    }
}
