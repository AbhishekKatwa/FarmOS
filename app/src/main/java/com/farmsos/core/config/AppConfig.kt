package com.farmsos.core.config

import com.farmsos.BuildConfig

/**
 * Application configuration constants.
 *
 * Supabase credentials are injected at build time from local.properties / Gradle
 * properties. The service-role key must never appear here or in BuildConfig.
 */
object AppConfig {

    const val APP_NAME = "FarmOS"
    const val APP_VERSION = "1.0.0"

    const val DATABASE_NAME = "farmos_database"
    const val DATABASE_VERSION = 1

    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    const val SYNC_INTERVAL_MINUTES = 15L
    const val SYNC_RETRY_COUNT = 3

    const val SESSION_TIMEOUT_MINUTES = 60L
    const val MAX_LOGIN_ATTEMPTS = 5

    const val ENABLE_OFFLINE_MODE = true
    const val ENABLE_BACKGROUND_SYNC = true
    const val ENABLE_PUSH_NOTIFICATIONS = true

    const val AUTH_DEEP_LINK_SCHEME = "farmsos"
    const val AUTH_DEEP_LINK_HOST = "auth-callback"

    val supabase: SupabaseEnvironment = SupabaseEnvironment(
        url = BuildConfig.SUPABASE_URL,
        anonKey = BuildConfig.SUPABASE_ANON_KEY
    )
}

data class SupabaseEnvironment(
    val url: String,
    val anonKey: String
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()

    fun requireConfigured() {
        require(url.isNotBlank()) {
            "Missing supabase.url. Copy local.properties.example to local.properties."
        }
        require(anonKey.isNotBlank()) {
            "Missing supabase.anon_key. Copy local.properties.example to local.properties."
        }
        require(!anonKey.contains("service_role")) {
            "The Supabase service-role key must never be used in the Android application."
        }
    }
}
