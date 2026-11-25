package com.gabriel.powergabs.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Estado da conexão com a internet (RF16).
 *
 * É uma interface em Kotlin puro para o domain poder usá-la sem conhecer o Android;
 * a implementação real é a [AndroidNetworkMonitor].
 */
interface NetworkMonitor {
    /** Emite `true` quando há internet e `false` quando não há. */
    val isOnline: Flow<Boolean>
}
