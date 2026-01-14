package com.kmp.webview.data.mapper

import com.kmp.webview.data.remote.dto.TodoDto
import com.kmp.webview.domain.model.Todo

fun TodoDto.toDomain(): Todo {
    return Todo(
        userId = userId,
        id = id,
        title = title,
        completed = completed
    )
}
