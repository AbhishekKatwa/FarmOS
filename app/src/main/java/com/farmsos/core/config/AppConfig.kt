package com.farmsos.core.config

/**
 * Application configuration constants
 * This object holds all configuration values for the FarmOS application
 */
object AppConfig {

    // Application info
    const val APP_NAME = "FarmOS"
    const val APP_VERSION = "1.0.0"

    // Database configuration
    const val DATABASE_NAME = "farmos_database"
    const val DATABASE_VERSION = 1

    // Network configuration
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // Sync configuration
    const val SYNC_INTERVAL_MINUTES = 15L
    const val SYNC_RETRY_COUNT = 3

    // Authentication configuration
    const val SESSION_TIMEOUT_MINUTES = 60L
    const val MAX_LOGIN_ATTEMPTS = 5

    // Feature flags
    const val ENABLE_OFFLINE_MODE = true
    const val ENABLE_BACKGROUND_SYNC = true
    const val ENABLE_PUSH_NOTIFICATIONS = true

    // Supabase Configuration
    // These should ideally be provided via BuildConfig or a secure properties file
    const val SUPABASE_URL = "https://your-project-id.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key"
}
