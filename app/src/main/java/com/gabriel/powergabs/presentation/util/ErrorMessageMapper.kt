package com.gabriel.powergabs.presentation.util

import androidx.annotation.StringRes
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError

/**
 * Converte um [AppError] no ID do texto em `strings.xml` (RNF06).
 * O ViewModel guarda só o ID (um `Int`), sem precisar de `Context`; a tela resolve o texto.
 */
object ErrorMessageMapper {

    @StringRes
    fun messageFor(error: AppError): Int = when (error) {
        AppError.Network -> R.string.error_network
        is AppError.Http -> R.string.error_http
        AppError.Serialization -> R.string.error_serialization
        AppError.Database -> R.string.error_database
        AppError.Unknown -> R.string.error_unknown
    }
}
