package com.example.tools

import android.content.Context
import com.example.data.repository.MahiRepository

data class ToolParameter(
    val name: String,
    val description: String,
    val isRequired: Boolean = true,
    val defaultValue: String? = null
)

data class ToolResult(
    val isSuccess: Boolean,
    val spokenResponse: String,
    val displayDetail: String,
    val data: Map<String, String> = emptyMap()
)

interface MahiTool {
    val name: String
    val description: String
    val parameters: List<ToolParameter>
    val requiredPermission: String? get() = null
    val requiresConfirmation: Boolean get() = false

    suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult
}
