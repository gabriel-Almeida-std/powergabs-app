package com.gabriel.powergabs.di

import com.gabriel.powergabs.BuildConfig
import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.network.AndroidNetworkMonitor
import com.gabriel.powergabs.core.network.NetworkMonitor
import com.gabriel.powergabs.core.time.SystemTimeProvider
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.data.remote.NetworkFactory
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Módulos Koin. O pacote `di` é o único que conhece todas as camadas:
 * é aqui que cada interface do domain é ligada à sua implementação do data.
 *
 * - `single`: uma instância para o app inteiro (banco, cliente HTTP, repositórios).
 * - `factory`: uma instância nova a cada pedido (casos de uso, que não guardam estado).
 * - `viewModel`: instância presa ao ciclo de vida da tela.
 */
val coreModule = module {
    single { AppDispatchers(io = Dispatchers.IO, default = Dispatchers.Default) }
    single<TimeProvider> { SystemTimeProvider() }
    single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
}

val dataModule = module {
    // Rede: toda a configuração fica aqui (timeouts e log só em depuração).
    single { NetworkFactory.json }
    single { NetworkFactory.okHttpClient(enableLogging = BuildConfig.DEBUG) }
    single { NetworkFactory.wgerApi(client = get(), json = get()) }
}

/** Todos os módulos, na ordem em que são carregados pelo [com.gabriel.powergabs.PowerGabsApp]. */
val appModules = listOf(coreModule, dataModule)
