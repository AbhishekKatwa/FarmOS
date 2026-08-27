// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.dagger.hilt.android") version "2.53" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}

val composeVersion = "1.7.5"
val hiltVersion = "2.53"
val roomVersion = "2.6.1"
val lifecycleVersion = "2.8.7"
val workVersion = "2.10.0"
val supabaseVersion = "3.0.3"

ext {
    set("hiltVersion", hiltVersion)
    set("roomVersion", roomVersion)
    set("lifecycleVersion", lifecycleVersion)
    set("workVersion", workVersion)
    set("supabaseVersion", supabaseVersion)
}