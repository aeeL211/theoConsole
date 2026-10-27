package com.aeeL.theokntl.ui

import android.content.Context
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.FailureInfo

fun failureText(context: Context, failure: FailureInfo, debugDetails: Boolean): String {
    val base = when (failure.kind) {
        ErrorKind.CONFIG -> context.getString(R.string.error_config)
        ErrorKind.USAGE -> context.getString(R.string.error_usage)
        ErrorKind.INPUT -> context.getString(R.string.error_input)
        ErrorKind.STATE -> context.getString(R.string.error_state)
        ErrorKind.BUSY -> context.getString(R.string.error_busy)
        ErrorKind.CANCELLED -> context.getString(R.string.error_cancelled)
        ErrorKind.NETWORK -> context.getString(R.string.error_network)
        ErrorKind.TIMEOUT -> context.getString(R.string.error_timeout)
        ErrorKind.AUTH -> context.getString(R.string.error_auth)
        ErrorKind.SESSION -> context.getString(R.string.error_session)
        ErrorKind.SIGNATURE -> context.getString(R.string.error_signature)
        ErrorKind.PROTOCOL -> context.getString(R.string.error_protocol)
        ErrorKind.OVERFLOW -> context.getString(R.string.error_overflow)
        ErrorKind.HTTP -> context.getString(R.string.error_http)
        ErrorKind.UNKNOWN -> context.getString(R.string.error_unknown)
    }
    if (!debugDetails) return base
    val details = buildString {
        append(base)
        if (failure.httpStatus != null) {
            append("\nHTTP ")
            append(failure.httpStatus)
        }
        if (failure.technical.isNotBlank()) {
            append('\n')
            append(failure.technical)
        }
    }
    return details
}
