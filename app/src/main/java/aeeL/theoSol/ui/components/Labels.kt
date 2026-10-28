package aeeL.theoSol.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import aeeL.theoSol.R
import aeeL.theoSol.domain.model.AccountField
import aeeL.theoSol.domain.model.BusyOperation
import aeeL.theoSol.domain.model.FeatureUnlocks
import aeeL.theoSol.domain.model.SessionStatus

@Composable
fun featureLabel(id: String): String = when (id) {
    FeatureUnlocks.ALL[0].id -> stringResource(R.string.feature_free_fast_speed)
    FeatureUnlocks.ALL[1].id -> stringResource(R.string.feature_free_tunnels)
    else -> id
}

@StringRes
fun accountFieldLabel(field: AccountField): Int = when (field) {
    AccountField.LOGIN -> R.string.field_login
    AccountField.PLAYER_NAME -> R.string.field_player_name
    AccountField.FORUM_NAME -> R.string.field_forum_name
    AccountField.EMAIL -> R.string.field_email
    AccountField.USER_ID -> R.string.field_user_id
    AccountField.FORUM_ID -> R.string.field_forum_id
}

@StringRes
fun busyLabel(operation: BusyOperation?): Int = when (operation) {
    BusyOperation.LOGIN -> R.string.busy_login
    BusyOperation.REFRESH -> R.string.busy_refresh
    BusyOperation.TOPUP -> R.string.busy_topup
    BusyOperation.PURCHASES -> R.string.busy_purchases
    BusyOperation.SYNC -> R.string.busy_sync
    null -> R.string.busy_unknown
}

@StringRes
fun sessionStatusLabel(status: SessionStatus): Int = when (status) {
    SessionStatus.SIGNED_OUT -> R.string.session_signed_out
    SessionStatus.READY -> R.string.session_ready
    SessionStatus.BUSY -> R.string.session_busy
}

@StringRes
fun protocolLabel(key: String): Int = when (key) {
    "client" -> R.string.protocol_client
    "cipher" -> R.string.protocol_cipher
    "signature" -> R.string.protocol_signature
    "compression" -> R.string.protocol_compression
    "limit" -> R.string.protocol_limit
    "vi" -> R.string.protocol_vi
    "earned" -> R.string.protocol_earned
    "spend" -> R.string.protocol_spend
    "request salt" -> R.string.protocol_request_salt
    "response salt" -> R.string.protocol_response_salt
    "session iv" -> R.string.protocol_session_iv
    else -> R.string.protocol_unknown
}
