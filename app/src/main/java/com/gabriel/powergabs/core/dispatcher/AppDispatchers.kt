package com.gabriel.powergabs.core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatchers injetados via Koin, em vez de usar `Dispatchers.IO` direto no código.
 * Assim, nos testes, todos podem ser trocados por um `StandardTestDispatcher`
 * e o tempo das coroutines fica sob controle do teste.
 */
data class AppDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)
