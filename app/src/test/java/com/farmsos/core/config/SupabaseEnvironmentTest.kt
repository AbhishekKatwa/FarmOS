package com.farmsos.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseEnvironmentTest {

    @Test
    fun rejectsBlankConfiguration() {
        val env = SupabaseEnvironment(url = "", anonKey = "")
        assertFalse(env.isConfigured)
        val error = runCatching { env.requireConfigured() }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun rejectsServiceRoleKey() {
        val env = SupabaseEnvironment(
            url = "https://example.supabase.co",
            anonKey = "header.payload-service_role-signature"
        )
        val error = runCatching { env.requireConfigured() }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("service-role"))
    }

    @Test
    fun acceptsAnonConfiguration() {
        val env = SupabaseEnvironment(
            url = "https://example.supabase.co",
            anonKey = "anon-public-key"
        )
        env.requireConfigured()
        assertTrue(env.isConfigured)
        assertEquals("anon-public-key", env.anonKey)
    }
}
