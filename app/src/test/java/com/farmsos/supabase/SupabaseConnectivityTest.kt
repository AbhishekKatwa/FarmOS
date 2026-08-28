package com.farmsos.supabase

import com.farmsos.core.config.AppConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class SupabaseConnectivityTest {

    @Test
    fun testSupabaseConnection() = runBlocking {
        val url = "https://hglpsbgimyckalhzmhrh.supabase.co"
        val anonKey = "sb_publishable__DZVyDKqZQIpyjTK6dBQOQ_FU2kBEpE"

        println("Testing connection to: $url")
        
        val client = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Postgrest)
        }

        val result = runCatching {
            // Attempt to fetch something public or check if the endpoint responds
            // Note: Since RLS is likely enabled, a direct select might return empty or fail if not auth
            // But we can check if the client can at least talk to the server
            client.postgrest["profiles"].select()
        }

        if (result.isFailure) {
            val error = result.exceptionOrNull()
            println("Connection check failed: ${error?.message}")
            // If it's a 401/403, it means the keys are recognized but we aren't auth'd (which is expected for profiles)
            // If it's a network error or 404, then the URL/Key is likely wrong
            assertTrue("Expected a valid Supabase response, even if empty/unauthorized", 
                error?.message?.contains("401") == true || 
                error?.message?.contains("403") == true || 
                result.isSuccess
            )
        } else {
            println("Successfully connected to Supabase Postgrest endpoint.")
        }
    }
}
