package com.farmsos.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.farmsos.core.logging.AppLogger
import com.farmsos.domain.repository.AuthRepository
import com.farmsos.ui.navigation.FarmOSNavigation
import com.farmsos.ui.theme.FarmOSTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var logger: AppLogger

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.i("MainActivity onCreate")
        authRepository.handleAuthIntent(intent)

        setContent {
            FarmOSTheme {
                FarmOSNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        authRepository.handleAuthIntent(intent)
    }
}
