package com.guilherme.documentscanner

import android.app.Application
import com.guilherme.documentscanner.di.appModule
import com.guilherme.documentscanner.di.initKoin
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}