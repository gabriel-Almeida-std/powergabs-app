package com.gabriel.powergabs

import android.app.Application
import com.gabriel.powergabs.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Ponto de entrada do processo: roda antes de qualquer tela.
 * Inicia o Koin com o contexto da aplicação (nunca o de uma Activity, para não vazar memória).
 */
class PowerGabsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@PowerGabsApp)
            modules(appModules)
        }
    }
}
