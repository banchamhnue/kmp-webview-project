package com.kmp.webview.di

import com.kmp.webview.data.remote.api.HttpClientFactory
import com.kmp.webview.data.remote.api.TodoApiService
import com.kmp.webview.data.repository.TodoRepositoryImpl
import com.kmp.webview.domain.repository.TodoRepository
import com.kmp.webview.domain.usecase.GetTodoUseCase

object CommonModule {
    private var todoApiServiceInstance: TodoApiService? = null
    private var todoRepositoryInstance: TodoRepository? = null
    private var getTodoUseCaseInstance: GetTodoUseCase? = null
    
    fun provideTodoApiService(): TodoApiService {
        if (todoApiServiceInstance == null) {
            todoApiServiceInstance = TodoApiService(HttpClientFactory.create())
        }
        return todoApiServiceInstance!!
    }
    
    fun provideTodoRepository(): TodoRepository {
        if (todoRepositoryInstance == null) {
            todoRepositoryInstance = TodoRepositoryImpl(provideTodoApiService())
        }
        return todoRepositoryInstance!!
    }
    
    fun provideGetTodoUseCase(): GetTodoUseCase {
        if (getTodoUseCaseInstance == null) {
            getTodoUseCaseInstance = GetTodoUseCase(provideTodoRepository())
        }
        return getTodoUseCaseInstance!!
    }
    
    fun reset() {
        todoApiServiceInstance?.close()
        todoApiServiceInstance = null
        todoRepositoryInstance = null
        getTodoUseCaseInstance = null
    }
}
