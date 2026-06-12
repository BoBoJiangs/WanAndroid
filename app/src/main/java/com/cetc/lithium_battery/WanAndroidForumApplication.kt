package com.cetc.lithium_battery

import android.app.Application
import com.cetc.lithium_battery.core.AppContainer

class WanAndroidForumApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
