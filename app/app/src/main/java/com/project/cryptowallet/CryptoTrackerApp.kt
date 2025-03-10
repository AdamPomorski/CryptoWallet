package com.project.cryptowallet

import android.app.Application
import com.project.cryptowallet.di.authModule
import com.project.cryptowallet.di.coinListModule
import com.project.cryptowallet.di.dataModule
import com.project.cryptowallet.di.portfolioModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CryptoTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CryptoTrackerApp)
            androidLogger()
            modules(dataModule, authModule, coinListModule, portfolioModule)
        }
    }
}