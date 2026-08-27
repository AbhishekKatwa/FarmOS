package com.farmsos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farmsos.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var fieldError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("FarmOS") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to FarmOS",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to manage your farms",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            val errorText = fieldError ?: uiState.errorMessage
            val showError = errorText != null

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    fieldError = null
                    viewModel.clearMessages()
                },
                label = { Text("Email address") },
                singleLine = true,
                isError = showError,
                supportingText = when {
                    showError -> {
                        { Text(errorText!!) }
                    }
                    uiState.infoMessage != null -> {
                        { Text(uiState.infoMessage!!) }
                    }
                    else -> null
                },
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    fieldError = null
                    viewModel.clearMessages()
                },
                label = { Text("Password") },
                singleLine = true,
                isError = showError,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() || !email.contains("@") ->
                            fieldError = "Please enter a valid email"
                        password.isBlank() || password.length < 6 ->
                            fieldError = "Password must be at least 6 characters"
                        else -> viewModel.login(email, password)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    when {
                        email.isBlank() || !email.contains("@") ->
                            fieldError = "Please enter a valid email first"
                        else -> viewModel.resetPassword(email)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Forgot password")
            }

            TextButton(
                onClick = {
                    when {
                        email.isBlank() || !email.contains("@") ->
                            fieldError = "Please enter a valid email"
                        password.isBlank() || password.length < 6 ->
                            fieldError = "Password must be at least 6 characters"
                        else -> viewModel.signUp(
                            name = email.substringBefore("@").ifBlank { "Farmer" },
                            email = email,
                            password = password
                        )
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create account")
            }
        }
    }
}
