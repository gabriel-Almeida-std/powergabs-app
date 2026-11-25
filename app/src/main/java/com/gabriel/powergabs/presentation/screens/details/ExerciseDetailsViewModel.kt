package com.gabriel.powergabs.presentation.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.ExerciseDetails
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.usecase.AddToWorkoutResult
import com.gabriel.powergabs.domain.usecase.AddToWorkoutUseCase
import com.gabriel.powergabs.domain.usecase.ObserveConnectivityUseCase
import com.gabriel.powergabs.domain.usecase.ObserveExerciseDetailsUseCase
import com.gabriel.powergabs.presentation.util.ErrorMessageMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailsContent {
    data object Loading : DetailsContent
    data class Success(val details: ExerciseDetails) : DetailsContent

    /** O exercício não está no cache (ex.: o cache do grupo foi trocado e ele saiu da wger). */
    data object NotFound : DetailsContent
}

data class ExerciseDetailsUiState(
    val goal: Goal,
    val content: DetailsContent = DetailsContent.Loading,
    /** O vídeo exige internet (RF11); offline, a área do vídeo mostra um aviso. */
    val isOnline: Boolean = true,
    val isAdding: Boolean = false,
)

/** Detalhes do exercício (RF10), vídeo (RF11) e "Adicionar ao treino" (RF12). */
class ExerciseDetailsViewModel(
    exerciseId: Int,
    private val goal: Goal,
    observeDetails: ObserveExerciseDetailsUseCase,
    private val addToWorkout: AddToWorkoutUseCase,
    observeConnectivity: ObserveConnectivityUseCase,
) : ViewModel() {

    private val isAdding = MutableStateFlow(false)

    private val _messages = Channel<Int>(Channel.BUFFERED)
    val messages: Flow<Int> = _messages.receiveAsFlow()

    private val content: Flow<DetailsContent> = observeDetails(exerciseId, goal)
        .map { details -> if (details == null) DetailsContent.NotFound else DetailsContent.Success(details) }
        .catch { emit(DetailsContent.NotFound) }

    val uiState: StateFlow<ExerciseDetailsUiState> =
        combine(content, observeConnectivity(), isAdding) { content, online, adding ->
            ExerciseDetailsUiState(goal = goal, content = content, isOnline = online, isAdding = adding)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExerciseDetailsUiState(goal))

    fun onAddToWorkout() {
        val details = (uiState.value.content as? DetailsContent.Success)?.details ?: return
        if (isAdding.value || details.isInWorkout) return
        isAdding.value = true
        viewModelScope.launch {
            val message = when (val result = addToWorkout(details.exercise, goal)) {
                AddToWorkoutResult.Added -> R.string.added_to_workout
                AddToWorkoutResult.AlreadyInWorkout -> R.string.already_in_workout
                AddToWorkoutResult.NotLoggedIn -> R.string.error_unknown
                is AddToWorkoutResult.Failure -> ErrorMessageMapper.messageFor(result.error)
            }
            isAdding.value = false
            _messages.send(message)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
