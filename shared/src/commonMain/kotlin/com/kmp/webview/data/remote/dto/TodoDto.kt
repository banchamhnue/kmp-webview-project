package com.kmp.webview.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TodoDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)
