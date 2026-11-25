package com.gabriel.powergabs.presentation.screens.musclegroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.usecase.ObserveRestWarningUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MuscleGroupUiState(
    val goal: Goal,
    val groups: List<MuscleGroup> = MuscleGroup.entries.toList(),
    val selectedGroup: MuscleGroup? = null,
    /** RF09: o grupo selecionado foi treinado ontem. Só informa; não bloqueia o botão. */
    val showRestWarning: Boolean = false,
)

/** Seleção do grupo muscular do dia (RF06) com aviso de descanso (RF09, RN10). */
class MuscleGroupViewModel(
    private val goal: Goal,
    private val observeRestWarning: ObserveRestWarningUseCase,
) : ViewModel() {

    private val selectedGroup = MutableStateFlow<MuscleGroup?>(null)

    /**
     * `flatMapLatest`: a cada novo grupo escolhido, cancela a observação do aviso do grupo anterior
     * e passa a observar o do novo. Assim o aviso sempre corresponde ao chip selecionado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MuscleGroupUiState> = selectedGroup
        .flatMapLatest { group ->
            if (group == null) {
                flowOf(MuscleGroupUiState(goal))
            } else {
                observeRestWarning(group)
                    .catch { emit(false) } // falha ao ler o histórico não impede a escolha
                    .map { warn -> MuscleGroupUiState(goal, selectedGroup = group, showRestWarning = warn) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MuscleGroupUiState(goal))

    fun onGroupSelected(group: MuscleGroup) {
        selectedGroup.value = group
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
