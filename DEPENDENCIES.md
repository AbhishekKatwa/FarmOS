# FarmOS Android Dependency Foundation

This document defines the frozen technology stack for the FarmOS Android application.

## Core Stack

| Component | Version | Why |
| :--- | :--- | :--- |
| **Kotlin** | 1.9.22 | Primary language, stable version for current Compose/Hilt stack. |
| **Android Gradle Plugin** | 8.6.0 | Build system integration. |
| **Java** | 17 | JVM target for modern Android development. |
| **Compile SDK** | 35 | Target latest Android APIs. |
| **Target SDK** | 35 | Compliance with latest Android platform requirements. |
| **Min SDK** | 24 | Supports Android 7.0 and above. |

## UI & Navigation

| Component | Version | Why |
| :--- | :--- | :--- |
| **Jetpack Compose BOM** | 2024.02.00 | Manages Compose versions for stability and compatibility. |
| **Compose Compiler** | 1.5.8 | Required for Kotlin 1.9.22 compatibility. |
| **Material 3** | (BOM) | Modern Android design system. |
| **Navigation Compose** | 2.7.7 | Declarative navigation for Compose. |

## Architecture & Data

| Component | Version | Why |
| :--- | :--- | :--- |
| **Dagger Hilt** | 2.51.1 | Dependency Injection foundation. |
| **Room** | 2.6.1 | Local SQLite persistence for offline-first capability. |
| **WorkManager** | 2.9.0 | Background synchronization and task scheduling. |
| **Coroutines / Flow** | 1.7.3 | Structured concurrency and reactive data streams. |

## Cloud & Networking

| Component | Version | Why |
| :--- | :--- | :--- |
| **Supabase SDK** | 3.1.1 | Primary backend (Auth, Postgrest, Storage, Realtime). |
| **Ktor Client** | 3.0.0 | Networking layer used by Supabase and custom API client. |
| **Kotlinx Serialization** | 1.9.22 (Plugin) | JSON parsing and data serialization. |

## Utilities & Media

| Component | Version | Why |
| :--- | :--- | :--- |
| **Vico** | 1.15.0 | Compose-compatible charting library for farm analytics. |
| **Coil** | 2.6.0 | Image loading for receipts, profiles, and documents. |

## Testing

| Component | Version | Why |
| :--- | :--- | :--- |
| **JUnit** | 4.13.2 | Unit testing foundation. |
| **AndroidX Test** | 1.1.5 | Instrumentation tests. |

---

## Architecture Rules

1. **No Mixed UI**: Do not use XML layouts unless absolutely required for platform interop.
2. **KAPT**: Using KAPT for Hilt and Room for stability with Kotlin 1.9.22.
3. **No OkHttp**: Using Ktor (via Supabase) as the primary networking stack to minimize overhead.
4. **Offline First**: All critical operational data must have a Room DAO/Entity definition.
5. **Freeze**: Do not modify these versions during feature development without documented architectural review.
