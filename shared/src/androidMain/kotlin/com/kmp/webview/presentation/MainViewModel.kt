package com.kmp.webview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmp.webview.domain.model.Todo
import com.kmp.webview.domain.usecase.GetTodoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val getTodoUseCase: GetTodoUseCase
) : ViewModel() {
    
    private val _todoState = MutableStateFlow<TodoState>(TodoState.Loading)
    val todoState: StateFlow<TodoState> = _todoState.asStateFlow()
    
    init {
        fetchTodo()
    }
    
    private fun fetchTodo() {
        viewModelScope.launch {
            _todoState.value = TodoState.Loading
            getTodoUseCase().fold(
                onSuccess = { todo ->
                    _todoState.value = TodoState.Success(todo)
                },
                onFailure = { error ->
                    _todoState.value = TodoState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

sealed class TodoState {
    object Loading : TodoState()
    data class Success(val todo: Todo) : TodoState()
    data class Error(val message: String) : TodoState()
}
