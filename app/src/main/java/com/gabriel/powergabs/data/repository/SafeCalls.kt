package com.gabriel.powergabs.data.repository

import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Executa uma chamada de rede e converte exceções previsíveis em [AppError] (RNF06),
 * para que nenhuma delas chegue à UI sem tratamento.
 *
 * `CancellationException` é relançada: cancelamento (ex.: o usuário saiu da tela) não é erro,
 * e engoli-la quebraria o cancelamento estruturado das coroutines.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network) // sem conexão, timeout, DNS
    } catch (e: HttpException) {
        AppResult.Failure(AppError.Http(e.code()))
    } catch (e: SerializationException) {
        AppResult.Failure(AppError.Serialization)
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown)
    }

/** Executa uma operação no banco e converte qualquer falha em [AppError.Database]. */
suspend fun <T> safeDbCall(call: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(AppError.Database)
    }
