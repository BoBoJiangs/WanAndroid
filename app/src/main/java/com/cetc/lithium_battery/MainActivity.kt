package com.cetc.lithium_battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cetc.lithium_battery.ui.app.WanAndroidForumApp
import com.cetc.lithium_battery.ui.theme.WanAndroidForumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as WanAndroidForumApplication)
            .container
            .forumRepository

        enableEdgeToEdge()

        setContent {
            WanAndroidForumTheme {
                WanAndroidForumApp(repository = repository)
            }
        }
    }
}
