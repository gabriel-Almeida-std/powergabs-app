package com.gabriel.powergabs.presentation.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.usecase.LogoutUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela de objetivo (RF05). `goals` é fixo: os quatro objetivos, na ordem da grade. */
data class GoalUiState(
    val userEmail: String? = null,
    val selectedGoal: Goal? = null,
    val goals: List<Goal> = Goal.entries.toList(),
)

class GoalViewModel(
    observeSession: ObserveSessionUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val selectedGoal = MutableStateFlow<Goal?>(null)

    val uiState: StateFlow<GoalUiState> =
        combine(observeSession(), selectedGoal) { user, goal ->
            GoalUiState(userEmail = user?.email, selectedGoal = goal)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GoalUiState())

    fun onGoalSelected(goal: Goal) {
        selectedGoal.value = goal
    }

    /** Sai da conta (RF04). A raiz percebe a sessão vazia e volta ao login. */
    fun onLogout() {
        viewModelScope.launch { logout() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
