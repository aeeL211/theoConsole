package com.aeeL.theokntl.domain.model

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.core.FailureInfo

data class FeatureUnlock(
    val id: String,
    val label: String,
    val draft: String,
    val sku: String,
)

object FeatureUnlocks {
    val ALL: List<FeatureUnlock> = listOf(
        FeatureUnlock(
            id = "CgkIl_L2yOgLEAIQVA",
            label = "Free fast speed",
            draft = "\$feature_freefastspeed00",
            sku = "free_fast_speed00",
        ),
        FeatureUnlock(
            id = "CgkIl_L2yOgLEAIQOg",
            label = "Free tunnels",
            draft = "\$feature_freetunnels00",
            sku = "free_tunnels00",
        ),
    )
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class ThemePalette {
    TEAL,
    TANGERINE,
    GRAPE,
    OCEAN,
    ROSE,
}

enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    INDONESIAN,
}

data class AppSettings(
    val username: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.TEAL,
    val dynamicColor: Boolean = false,
    val debugDetails: Boolean = false,
)

data class AccountInfo(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val forumName: String = "",
    val forumId: Long? = null,
    val forumColor: String = "",
)

data class SessionData(
    val userId: Long,
    val sessionId: String,
    val encKey: String,
    val cake: String,
    val account: AccountInfo,
)

data class ActiveSession(
    val data: SessionData,
    val login: String,
    val sealedPassword: String,
    val keyFingerprint: String,
)

data class EventTotals(
    val earned: Long,
    val spend: Long,
) {
    val balance: Long get() = earned - spend
}

data class PurchaseItem(
    val id: String,
    val amount: Long,
)

data class FeatureStatus(
    val id: String,
    val label: String,
    val unlocked: Boolean,
)

data class FeatureReport(
    val items: List<FeatureStatus>,
    val error: FailureInfo? = null,
    val probe: Boolean? = null,
) {
    val verified: Boolean
        get() = error == null && items.isNotEmpty() && items.all { it.unlocked }
}

data class StatsView(
    val signedIn: Boolean,
    val balance: Long?,
    val earned: Long?,
    val spend: Long?,
    val room: Long?,
    val limit: Long,
    val progress: Float,
) {
    companion object {
        fun signedOut(): StatsView = StatsView(
            signedIn = false,
            balance = null,
            earned = null,
            spend = null,
            room = null,
            limit = ConsoleConstants.LIMIT,
            progress = 0f,
        )
    }
}

enum class AccountField {
    LOGIN,
    PLAYER_NAME,
    FORUM_NAME,
    EMAIL,
    USER_ID,
    FORUM_ID,
}

data class AccountRow(
    val field: AccountField,
    val value: String,
)

data class AccountView(
    val label: String,
    val login: String,
    val userId: Long,
    val name: String,
    val rows: List<AccountRow>,
    val since: String,
)

enum class SessionStatus {
    SIGNED_OUT,
    READY,
    BUSY,
}

data class SessionView(
    val signedIn: Boolean,
    val status: SessionStatus,
    val userId: Long?,
    val sessionBound: Boolean,
    val keyFingerprint: String?,
    val endpoint: String?,
    val lastAction: String?,
) {
    companion object {
        fun signedOut(): SessionView = SessionView(
            signedIn = false,
            status = SessionStatus.SIGNED_OUT,
            userId = null,
            sessionBound = false,
            keyFingerprint = null,
            endpoint = null,
            lastAction = null,
        )
    }
}

enum class BusyOperation {
    LOGIN,
    REFRESH,
    TOPUP,
    PURCHASES,
    SYNC,
}

data class ConsoleState(
    val signedIn: Boolean = false,
    val busy: Boolean = false,
    val busyOperation: BusyOperation? = null,
    val account: AccountView? = null,
    val stats: StatsView = StatsView.signedOut(),
    val session: SessionView = SessionView.signedOut(),
    val features: FeatureReport? = null,
    val lastTopUp: TopUpOutcome? = null,
    val loginTime: String? = null,
    val lastAction: String? = null,
)

data class LoginResult(
    val account: AccountView,
    val stats: StatsView,
    val ceiling: Boolean,
)

data class TopUpReview(
    val before: Long,
    val amount: Long,
    val earned: Long,
    val target: Long,
    val after: Long,
    val unlocks: List<String>,
    val accountLabel: String,
    val accountUserId: Long,
)

data class TopUpOutcome(
    val before: Long,
    val amount: Long,
    val after: Long,
    val target: Long,
    val capped: Boolean,
    val features: FeatureReport,
)

data class ProtocolRow(
    val labelKey: String,
    val value: String,
)

data class ProtocolInfo(
    val rows: List<ProtocolRow>,
    val raw: List<ProtocolRow>,
    val endpointHost: String,
)

data class DeveloperView(
    val name: String,
    val telegramUrl: String,
    val telegramLabel: String,
)

fun protocolInfo(): ProtocolInfo = ProtocolInfo(
    rows = listOf(
        ProtocolRow("client", "${ConsoleConstants.CLIENT_ID} / ${ConsoleConstants.CLIENT_BUILD}"),
        ProtocolRow("cipher", "AES-128-CBC"),
        ProtocolRow("signature", "MD5 + salt"),
        ProtocolRow("compression", "gzip"),
        ProtocolRow("limit", "100,000"),
    ),
    raw = listOf(
        ProtocolRow("vi", ConsoleConstants.CLIENT_VI),
        ProtocolRow("earned", ConsoleConstants.EVENT_EARNED),
        ProtocolRow("spend", ConsoleConstants.EVENT_SPEND),
        ProtocolRow("request salt", ConsoleConstants.REQUEST_SALT_HEX),
        ProtocolRow("response salt", ConsoleConstants.RESPONSE_SALT_HEX),
        ProtocolRow("session iv", ConsoleConstants.SESSION_IV),
    ),
    endpointHost = ConsoleConstants.API_HOST,
)

fun developerView(): DeveloperView = DeveloperView(
    name = ConsoleConstants.DEVELOPER_NAME,
    telegramUrl = ConsoleConstants.DEVELOPER_TELEGRAM_URL,
    telegramLabel = ConsoleConstants.DEVELOPER_TELEGRAM_LABEL,
)
