package com.gabriel.powergabs.presentation.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.usecase.MarkDoneOutcome
import com.gabriel.powergabs.domain.usecase.MarkExerciseDoneUseCase
import com.gabriel.powergabs.domain.usecase.ObserveWorkoutUseCase
import com.gabriel.powergabs.domain.usecase.RateEffortOutcome
import com.gabriel.powergabs.domain.usecase.RateEffortUseCase
import com.gabriel.powergabs.presentation.util.ErrorMessageMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WorkoutContent {
    data object Loading : WorkoutContent
    data class Success(val items: List<PlannedExercise>) : WorkoutContent
    data object Empty : WorkoutContent
    data object Error : WorkoutContent
}

data class WorkoutUiState(
    val filter: WorkoutFilter = WorkoutFilter.ALL,
    val content: WorkoutContent = WorkoutContent.Loading,
)

/** "Meu treino" (RF13–RF15). Totalmente local: funciona sem internet. */
class WorkoutViewModel(
    private val observeWorkout: ObserveWorkoutUseCase,
    private val markExerciseDone: MarkExerciseDoneUseCase,
    private val rateEffort: RateEffortUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(WorkoutFilter.ALL)

    private val _messages = Channel<Int>(Channel.BUFFERED)
    val messages: Flow<Int> = _messages.receiveAsFlow()

    /** Ao trocar o filtro, `flatMapLatest` troca a consulta observada. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WorkoutUiState> = filter
        .flatMapLatest { current ->
            observeWorkout(current)
                .map { items ->
                    WorkoutUiState(
                        filter = current,
                        content = if (items.isEmpty()) WorkoutContent.Empty else WorkoutContent.Success(items),
                    )
                }
                .catch { emit(WorkoutUiState(current, WorkoutContent.Error)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), WorkoutUiState())

    fun onFilterSelected(selected: WorkoutFilter) {
        filter.value = selected
    }

    fun onMarkDone(itemId: Long) {
        viewModelScope.launch {
            when (val outcome = markExerciseDone(itemId)) {
                MarkDoneOutcome.Done, MarkDoneOutcome.AlreadyDone -> Unit // a lista se atualiza sozinha (Room)
                MarkDoneOutcome.NotFound -> _messages.send(R.string.error_unknown)
                is MarkDoneOutcome.Failure -> _messages.send(ErrorMessageMapper.messageFor(outcome.error))
            }
        }
    }

    fun onRateEffort(itemId: Long, effort: Int) {
        viewModelScope.launch {
            when (val outcome = rateEffort(itemId, effort)) {
                RateEffortOutcome.Saved -> Unit
                RateEffortOutcome.NotDone, RateEffortOutcome.OutOfRange, RateEffortOutcome.NotFound ->
                    _messages.send(R.string.error_unknown)
                is RateEffortOutcome.Failure -> _messages.send(ErrorMessageMapper.messageFor(outcome.error))
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
