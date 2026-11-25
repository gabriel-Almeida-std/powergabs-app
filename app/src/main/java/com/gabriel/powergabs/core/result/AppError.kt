package com.gabriel.powergabs.core.result

/**
 * Falhas técnicas previsíveis (RNF06).
 *
 * Recusas de regra de negócio (ex.: "exercício já está no treino") NÃO entram aqui:
 * os casos de uso têm tipos de resultado próprios para elas.
 */
sealed interface AppError {
    /** Sem conexão, timeout ou falha de DNS. */
    data object Network : AppError

    /** O servidor respondeu com código 4xx/5xx. */
    data class Http(val code: Int) : AppError

    /** A resposta chegou, mas não pôde ser convertida (JSON inválido). */
    data object Serialization : AppError

    /** Falha ao ler ou gravar no banco local. */
    data object Database : AppError

    /** Qualquer outra falha inesperada. */
    data object Unknown : AppError
}
