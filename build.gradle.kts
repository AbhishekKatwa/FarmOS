// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

val composeVersion = "1.5.10"
val hiltVersion = "2.51.1"
val roomVersion = "2.6.1"
val lifecycleVersion = "2.7.0"
val workVersion = "2.9.0"
val supabaseVersion = "2.0.0"

ext {
    set("composeVersion", composeVersion)
    set("hiltVersion", hiltVersion)
    set("roomVersion", roomVersion)
    set("lifecycleVersion", lifecycleVersion)
    set("workVersion", workVersion)
    set("supabaseVersion", supabaseVersion)
}