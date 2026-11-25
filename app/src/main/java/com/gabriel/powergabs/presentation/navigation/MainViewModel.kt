package com.gabriel.powergabs.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.usecase.ObserveConnectivityUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da sessão que decide qual grafo de navegação mostrar (RF03). */
sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

/**
 * ViewModel da raiz do app: sessão e conectividade.
 *
 * `stateIn(WhileSubscribed(5_000))` transforma o Flow em StateFlow e mantém a coleta viva por
 * 5 s depois que a tela some (ex.: rotação), evitando reiniciar tudo a cada mudança de configuração.
 */
class MainViewModel(
    observeSession: ObserveSessionUseCase,
    observeConnectivity: ObserveConnectivityUseCase,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = observeSession()
        .map { user -> if (user == null) SessionState.LoggedOut else SessionState.LoggedIn(user) }
        .catch { emit(SessionState.LoggedOut) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SessionState.Loading)

    /** Começa como `true` para não piscar a faixa "sem internet" antes da primeira leitura. */
    val isOnline: StateFlow<Boolean> = observeConnectivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), true)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
