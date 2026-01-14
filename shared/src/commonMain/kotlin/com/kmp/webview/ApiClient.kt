package com.kmp.webview

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.*

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
        } catch (e: ClientRequestException) {
            // HTTP error (4xx, 5xx)
            Todo(
                userId = 0,
                id = 0,
                title = "HTTP Error: ${e.response.status}",
                completed = false
            )
        } catch (e: HttpRequestTimeoutException) {
            // Timeout
            Todo(
                userId = 0,
                id = 0,
                title = "Request timeout",
                completed = false
            )
        } catch (e: Exception) {
            // Other errors (network, parsing, etc.)
            Todo(
                userId = 0,
                id = 0,
                title = "Error: ${e.message ?: "Unknown error"}",
                completed = false
            )
        }
    }
}
