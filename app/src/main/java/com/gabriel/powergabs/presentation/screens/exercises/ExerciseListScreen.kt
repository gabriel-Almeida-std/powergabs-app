package com.gabriel.powergabs.presentation.screens.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Equipment
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.Muscle
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.SuggestedExercise
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.presentation.components.BackTopBar
import com.gabriel.powergabs.presentation.components.EmptyState
import com.gabriel.powergabs.presentation.components.ErrorState
import com.gabriel.powergabs.presentation.components.ExerciseImage
import com.gabriel.powergabs.presentation.components.LoadingState
import com.gabriel.powergabs.presentation.components.PrescriptionBadge
import com.gabriel.powergabs.presentation.components.RestWarningCard
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import com.gabriel.powergabs.presentation.util.Formatters
import com.gabriel.powergabs.presentation.util.Formatters.ElapsedUnit
import com.gabriel.powergabs.presentation.util.labelRes
import com.gabriel.powergabs.presentation.util.labelResOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.Instant

@Composable
fun ExerciseListRoute(
    goal: Goal,
    group: MuscleGroup,
    onBack: () -> Unit,
    onOpenExercise: (exerciseId: Int, goal: Goal) -> Unit,
    viewModel: ExerciseListViewModel = koinViewModel { parametersOf(goal, group) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Eventos únicos: cada mensagem do Channel vira um Snackbar não bloqueante (CA13, CA15).
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { messageRes -> snackbarHostState.showSnackbar(context.getString(messageRes)) }
    }

    ExerciseListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onOpenExercise = { id -> onOpenExercise(id, state.goal) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    state: ExerciseListUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenExercise: (Int) -> Unit,
) {
    Scaffold(
        topBar = { BackTopBar(onBack = onBack, title = stringResource(state.group.labelRes())) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when (val content = state.content) {
                ExerciseListContent.Loading -> LoadingState()
                is ExerciseListContent.Error -> ScrollableBox { ErrorState(stringResource(content.message), onRetry) }
                ExerciseListContent.Empty -> ScrollableBox {
                    EmptyState(
                        message = stringResource(R.string.exercise_list_empty),
                        actionLabel = stringResource(R.string.action_choose_other_group),
                        onAction = onBack,
                    )
                }
                is ExerciseListContent.Success -> ExerciseList(state, content.items, onOpenExercise)
            }
        }
    }
}

/** O pull-to-refresh só funciona sobre conteúdo rolável; este Box torna os estados vazios/erro roláveis. */
@Composable
private fun ScrollableBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { content() }
}

@Composable
private fun ExerciseList(
    state: ExerciseListUiState,
    items: List<SuggestedExercise>,
    onOpenExercise: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = Dimens.SmallSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
    ) {
        item(key = "header") {
            ListHeader(state, items.size)
        }
        // `key` estável (ID do exercício): ao atualizar a lista, o Compose reaproveita os itens que não mudaram.
        items(items, key = { it.exercise.id }) { item ->
            ExerciseListItem(item, onClick = { onOpenExercise(item.exercise.id) })
        }
    }
}

@Composable
private fun ListHeader(state: ExerciseListUiState, count: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
        Text(
            stringResource(R.string.exercise_list_subtitle, stringResource(state.goal.labelRes()), count),
            style = MaterialTheme.typography.bodyMedium,
        )
        state.lastUpdated?.let { UpdatedAgo(it) }
        if (state.showRestWarning) RestWarningCard(groupName = stringResource(state.group.labelRes()))
    }
}

/** "Atualizado há…" (RF08). O cálculo é Kotlin puro em [Formatters.elapsedSince], testado à parte. */
@Composable
private fun UpdatedAgo(lastUpdated: Instant) {
    val elapsed = Formatters.elapsedSince(lastUpdated, Instant.now())
    val value = elapsed.value.toInt()
    val text = when (elapsed.unit) {
        ElapsedUnit.JUST_NOW -> stringResource(R.string.updated_just_now)
        ElapsedUnit.MINUTES -> pluralStringResource(R.plurals.updated_minutes_ago, value, value)
        ElapsedUnit.HOURS -> pluralStringResource(R.plurals.updated_hours_ago, value, value)
        ElapsedUnit.DAYS -> pluralStringResource(R.plurals.updated_days_ago, value, value)
    }
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ExerciseListItem(item: SuggestedExercise, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.ItemSpacing),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        ) {
            ExerciseImage(url = item.exercise.thumbnailUrl, modifier = Modifier.size(Dimens.ListImageSize))
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing / 2)) {
                Text(
                    item.exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    equipmentText(item.exercise.equipment),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrescriptionBadge(item.prescription)
            }
        }
    }
}

/** Nomes dos equipamentos em português, separados por vírgula; sem equipamento → "Sem equipamento". */
@Composable
fun equipmentText(equipment: List<Equipment>): String {
    if (equipment.isEmpty()) return stringResource(R.string.equipment_none)
    return equipment.map { eq -> eq.labelResOrNull()?.let { stringResource(it) } ?: eq.name }.joinToString(", ")
}

// ---------- Preview ----------

private fun previewItem(id: Int, name: String) = SuggestedExercise(
    exercise = Exercise(
        id = id, muscleGroup = MuscleGroup.CHEST, name = name, description = "", imageUrl = null,
        thumbnailUrl = null, videoUrl = null, muscles = listOf(Muscle(4, "Chest")), secondaryMuscles = emptyList(),
        equipment = listOf(Equipment(1, "Barbell"), Equipment(8, "Bench")),
    ),
    prescription = PrescriptionRules.forGoal(Goal.STRENGTH),
)

@PreviewLightDark
@Composable
private fun ExerciseListScreenPreview() {
    PowerGabsTheme {
        ExerciseListScreen(
            state = ExerciseListUiState(
                goal = Goal.STRENGTH,
                group = MuscleGroup.CHEST,
                content = ExerciseListContent.Success(listOf(previewItem(1, "Supino reto"), previewItem(2, "Supino inclinado"))),
                lastUpdated = Instant.now().minusSeconds(3_600),
                showRestWarning = true,
            ),
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onRefresh = {},
            onRetry = {},
            onOpenExercise = {},
        )
    }
}
