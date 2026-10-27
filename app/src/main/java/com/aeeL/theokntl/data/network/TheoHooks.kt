package com.aeeL.theokntl.data.network

data class ActionInfo(
    val name: String,
    val args: String,
)

data class ResponseInfo(
    val method: String,
    val path: String,
    val status: Int?,
    val ok: Boolean,
    val outcome: String,
    val detail: String,
    val elapsedMs: Long,
)

data class CallFailureInfo(
    val method: String,
    val path: String,
    val technical: String,
    val status: Int?,
)

class TheoHooks(
    val onAction: (ActionInfo) -> Unit = {},
    val onResponse: (ResponseInfo) -> Unit = {},
    val onFailure: (CallFailureInfo) -> Unit = {},
)
