// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}

val composeVersion = "1.5.10"
val hiltVersion = "2.51.1"
val roomVersion = "2.8.4"
val lifecycleVersion = "2.8.7"
val workVersion = "2.11.2"
val supabaseVersion = "3.1.1"

ext {
    set("composeVersion", composeVersion)
    set("hiltVersion", hiltVersion)
    set("roomVersion", roomVersion)
    set("lifecycleVersion", lifecycleVersion)
    set("workVersion", workVersion)
    set("supabaseVersion", supabaseVersion)
}