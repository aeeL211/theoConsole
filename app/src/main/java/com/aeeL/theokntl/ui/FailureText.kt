package com.aeeL.theokntl.ui

import android.content.res.Resources
import com.aeeL.theokntl.R
import com.aeeL.theokntl.core.ErrorKind
import com.aeeL.theokntl.core.FailureInfo

fun failureText(resources: Resources, failure: FailureInfo, debugDetails: Boolean): String {
    val base = when (failure.kind) {
        ErrorKind.CONFIG -> resources.getString(R.string.error_config)
        ErrorKind.USAGE -> resources.getString(R.string.error_usage)
        ErrorKind.INPUT -> resources.getString(R.string.error_input)
        ErrorKind.STATE -> resources.getString(R.string.error_state)
        ErrorKind.BUSY -> resources.getString(R.string.error_busy)
        ErrorKind.CANCELLED -> resources.getString(R.string.error_cancelled)
        ErrorKind.NETWORK -> resources.getString(R.string.error_network)
        ErrorKind.TIMEOUT -> resources.getString(R.string.error_timeout)
        ErrorKind.AUTH -> resources.getString(R.string.error_auth)
        ErrorKind.SESSION -> resources.getString(R.string.error_session)
        ErrorKind.SIGNATURE -> resources.getString(R.string.error_signature)
        ErrorKind.PROTOCOL -> resources.getString(R.string.error_protocol)
        ErrorKind.OVERFLOW -> resources.getString(R.string.error_overflow)
        ErrorKind.HTTP -> resources.getString(R.string.error_http)
        ErrorKind.UNKNOWN -> resources.getString(R.string.error_unknown)
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
