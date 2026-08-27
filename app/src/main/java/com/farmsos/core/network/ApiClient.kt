package com.farmsos.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val networkManager: NetworkManager
) {
    val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            engine {
                connectTimeout = 15_000
                socketTimeout = 15_000
            }
        }
    }

    fun isNetworkAvailable(): Boolean {
        return networkManager.isNetworkAvailable()
    }
}
