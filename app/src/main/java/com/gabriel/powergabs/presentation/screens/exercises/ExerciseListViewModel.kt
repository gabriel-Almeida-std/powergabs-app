package com.gabriel.powergabs.presentation.screens.exercises

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.SuggestedExercise
import com.gabriel.powergabs.domain.model.SuggestedExercises
import com.gabriel.powergabs.domain.usecase.ObserveRestWarningUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSuggestedExercisesUseCase
import com.gabriel.powergabs.domain.usecase.RefreshExercisesUseCase
import com.gabriel.powergabs.presentation.util.ErrorMessageMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

/** Conteúdo principal da lista: os quatro estados de tela (RNF07). */
sealed interface ExerciseListContent {
    data object Loading : ExerciseListContent
    data class Success(val items: List<SuggestedExercise>) : ExerciseListContent
    data object Empty : ExerciseListContent
    data class Error(@StringRes val message: Int) : ExerciseListContent
}

data class ExerciseListUiState(
    val goal: Goal,
    val group: MuscleGroup,
    val content: ExerciseListContent = ExerciseListContent.Loading,
    val lastUpdated: Instant? = null,
    val isRefreshing: Boolean = false,
    val showRestWarning: Boolean = false,
)

/** Situação da última atualização pela rede. Só decide a tela quando o cache está vazio. */
private sealed interface RefreshStatus {
    data object AwaitingData : RefreshStatus
    data object NoResults : RefreshStatus
    data class Failed(val error: AppError) : RefreshStatus
}

/**
 * Lista de exercícios sugeridos (RF07–RF09), seguindo o Offline First (seção 6 do planejamento):
 * 1. a tela observa o cache (Room) e mostra na hora o que houver;
 * 2. ao abrir, pede uma atualização (só baixa se o cache expirou);
 * 3. falha COM cache → Snackbar, os dados continuam; falha SEM cache → estado de erro.
 */
class ExerciseListViewModel(
    private val goal: Goal,
    private val group: MuscleGroup,
    observeSuggestedExercises: ObserveSuggestedExercisesUseCase,
    private val refreshExercises: RefreshExercisesUseCase,
    observeRestWarning: ObserveRestWarningUseCase,
) : ViewModel() {

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.AwaitingData)
    private val isRefreshing = MutableStateFlow(false)

    /** Eventos únicos (Snackbar): um `Channel` entrega cada mensagem uma vez só, mesmo após rotação. */
    private val _messages = Channel<Int>(Channel.BUFFERED)
    val messages: Flow<Int> = _messages.receiveAsFlow()

    private val suggestions: Flow<SuggestedExercises> = observeSuggestedExercises(goal, group)

    val uiState: StateFlow<ExerciseListUiState> = combine(
        suggestions,
        refreshStatus,
        isRefreshing,
        observeRestWarning(group).catch { emit(false) },
    ) { data, status, refreshing, warn ->
        ExerciseListUiState(
            goal = goal,
            group = group,
            content = contentFor(data, status),
            lastUpdated = data.lastUpdated,
            isRefreshing = refreshing,
            showRestWarning = warn,
        )
    }
        .catch { emit(ExerciseListUiState(goal, group, ExerciseListContent.Error(R.string.error_database))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExerciseListUiState(goal, group))

    init {
        refresh(force = false)
    }

    /** Pull-to-refresh (RF08): força a busca, ignorando a validade de 24 h. */
    fun onRefresh() {
        isRefreshing.value = true
        refresh(force = true, onFinished = { isRefreshing.value = false })
    }

    fun onRetry() = refresh(force = false)

    private fun refresh(force: Boolean, onFinished: () -> Unit = {}) {
        refreshStatus.value = RefreshStatus.AwaitingData
        viewModelScope.launch {
            when (val result = refreshExercises(goal, group, force)) {
                is AppResult.Success -> {
                    // O Room emite os dados novos de forma assíncrona; só marca "sem resultados"
                    // se o banco continuar vazio, para não piscar "vazio" antes da lista.
                    if (isCacheEmpty()) refreshStatus.value = RefreshStatus.NoResults
                }
                is AppResult.Failure -> {
                    refreshStatus.value = RefreshStatus.Failed(result.error)
                    if (hasCachedItems()) _messages.send(snackbarMessageFor(result.error))
                }
            }
            onFinished()
        }
    }

    @StringRes
    private fun snackbarMessageFor(error: AppError): Int =
        if (error == AppError.Network) R.string.error_refresh_offline else ErrorMessageMapper.messageFor(error)

    private suspend fun hasCachedItems(): Boolean = current()?.items?.isNotEmpty() ?: false

    private suspend fun isCacheEmpty(): Boolean = current()?.let { it.items.isEmpty() && it.lastUpdated == null } ?: true

    /** Leitura pontual do cache; nunca engole `CancellationException`. */
    private suspend fun current(): SuggestedExercises? =
        try {
            suggestions.first()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private fun contentFor(data: SuggestedExercises, status: RefreshStatus): ExerciseListContent = when {
        data.items.isNotEmpty() -> ExerciseListContent.Success(data.items)
        data.lastUpdated != null -> ExerciseListContent.Empty // o grupo foi baixado, mas nada combina com o objetivo
        else -> when (status) {
            RefreshStatus.AwaitingData -> ExerciseListContent.Loading
            RefreshStatus.NoResults -> ExerciseListContent.Empty
            is RefreshStatus.Failed -> ExerciseListContent.Error(ErrorMessageMapper.messageFor(status.error))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
