package com.expenser.app

import android.app.Application
import com.expenser.app.di.AppContainer

class ExpenserApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
