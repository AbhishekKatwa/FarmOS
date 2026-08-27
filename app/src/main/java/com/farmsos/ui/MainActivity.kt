package com.farmsos.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.farmsos.core.logging.AppLogger
import com.farmsos.ui.navigation.FarmOSNavigation
import com.farmsos.ui.theme.FarmOSTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var logger: AppLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logger.i("MainActivity onCreate")

        setContent {
            FarmOSTheme {
                FarmOSNavigation()
            }
        }
    }
}
