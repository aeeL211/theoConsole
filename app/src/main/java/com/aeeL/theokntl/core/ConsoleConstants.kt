package com.aeeL.theokntl.core

/**
 * Public wire constants for the TheoTown 1.12.95a client.
 */
object ConsoleConstants {
    const val LIMIT = 100_000L
    const val INT32_MAX = 2_147_483_647L

    const val EVENT_EARNED = "CgkIl_L2yOgLEAIQDg"
    const val EVENT_SPEND = "CgkIl_L2yOgLEAIQDw"

    const val PURCHASE_SETTLE_MS = 1_400L
    const val PURCHASE_SETTLE_LONG_MS = 4_200L
    val PURCHASE_WATCH_MS = listOf(4_000L, 11_000L, 30_000L, 60_000L)
    const val PURCHASE_REWRITE_MS = 12L * 60L * 60L * 1_000L

    const val USERNAME_KEY = "theotown.console.username.v1"
    const val PURCHASE_STATE_KEY = "theotown.console.purchases.v1"

    const val CLIENT_ID = "1.12.95a"
    const val CLIENT_BUILD = "11295"
    const val CLIENT_VI = "98c128bb40b2d8a4aa99b0a160461880"
    const val CLIENT_MODEL = "Android"

    const val SESSION_SERVER = "57E1184BA92698EE"
    const val SESSION_CLIENT = "78C66A2EDF364241"
    const val SESSION_IV = "0fb16580ea386f2c614decbfc79a5cd4"
    const val REQUEST_SALT_HEX = "CBEF5C85D3C6B342"
    const val RESPONSE_SALT_HEX = "7B70BB55032D797A"

    const val DEFAULT_API = "https://api.theotown.com"
    const val API_HOST = "api.theotown.com"
    const val REQUEST_TIMEOUT_MS = 20_000L

    const val API_VERIFY = "/user/verify_fb.php"
    const val API_EVENTS = "/user/events.php"
    const val API_PURCHASES = "/user/purchases.php"

    const val DEVELOPER_NAME = "AeeL"
    const val DEVELOPER_TELEGRAM_URL = "https://t.me/aeeL2"
    const val DEVELOPER_TELEGRAM_LABEL = "t.me/aeeL2"

    const val FORUM_RSA_E = 65537L

    val FORUM_RSA_N_HEX: String = buildString {
        append("c9bea300efade53892bde3af4fdd5b484d385e1c339f3854f3c0a0055502a8be")
        append("8e655c23697ecae5e40765cab0fb75455cc3a0c7d1d297b4ba8aa57d31311ae1")
        append("96716ad9ba3aa630b005e84df4de008c3706f157748d265ade2b93223287636f")
        append("ba98f5a69d5a9ab8e01fb6670959be18fe44385ea1bcece13f508e108b6832e5")
        append("c774c76975c77719c6828c61e7681cf171094e76041e02a27cc2a3ee47dfa234")
        append("34b9ea888cbed6f95b309b132cd075d4482706f0d2705daf91c8741dc5a90fe1")
        append("7d03299709574382e3d11b3daa84b7218fab39d906eb2ab27aa7b03084073fd3")
        append("c763a2d28f777ac51732ab1d65e7cb70d8fd785517302ff6c8aa7016216827c1")
    }
}
