package com.gabriel.powergabs.core.result

/**
 * Resultado de uma operação que pode falhar (rede, banco, leitura de dados).
 *
 * Repositórios devolvem AppResult em vez de lançar exceções: o erro vira um valor,
 * e o `when` sobre a interface selada obriga quem chama a tratar sucesso e falha (RNF06).
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/** Transforma o valor de sucesso mantendo a falha como está. */
inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}
