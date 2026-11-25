package com.gabriel.powergabs.presentation.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.model.WorkoutStatus
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.domain.rule.WorkoutRules
import com.gabriel.powergabs.presentation.components.BackTopBar
import com.gabriel.powergabs.presentation.components.EmptyState
import com.gabriel.powergabs.presentation.components.ErrorMessage
import com.gabriel.powergabs.presentation.components.ExerciseImage
import com.gabriel.powergabs.presentation.components.LoadingState
import com.gabriel.powergabs.presentation.components.PrescriptionBadge
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import com.gabriel.powergabs.presentation.util.labelRes
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@Composable
fun WorkoutRoute(
    onBack: () -> Unit,
    onChooseGoal: () -> Unit,
    viewModel: WorkoutViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(context.getString(it)) }
    }
    WorkoutScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onChooseGoal = onChooseGoal,
        onFilterSelected = viewModel::onFilterSelected,
        onMarkDone = viewModel::onMarkDone,
        onRateEffort = viewModel::onRateEffort,
    )
}

@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onChooseGoal: () -> Unit,
    onFilterSelected: (WorkoutFilter) -> Unit,
    onMarkDone: (Long) -> Unit,
    onRateEffort: (Long, Int) -> Unit,
) {
    Scaffold(
        topBar = { BackTopBar(onBack = onBack, title = stringResource(R.string.workout_title)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FilterRow(state.filter, onFilterSelected)
            when (val content = state.content) {
                WorkoutContent.Loading -> LoadingState()
                WorkoutContent.Error -> ErrorMessage(
                    stringResource(R.string.error_database),
                    Modifier.padding(Dimens.ScreenPadding),
                )
                WorkoutContent.Empty -> EmptyWorkout(state.filter, onChooseGoal)
                is WorkoutContent.Success -> LazyColumn(
                    contentPadding = PaddingValues(Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
                ) {
                    items(content.items, key = { it.id }) { item ->
                        WorkoutItemCard(item, onMarkDone = { onMarkDone(item.id) }, onRateEffort = { onRateEffort(item.id, it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: WorkoutFilter, onFilterSelected: (WorkoutFilter) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
    ) {
        WorkoutFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onFilterSelected(filter) },
                label = { Text(stringResource(filter.labelRes()), style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun EmptyWorkout(filter: WorkoutFilter, onChooseGoal: () -> Unit) {
    when (filter) {
        WorkoutFilter.ALL -> EmptyState(
            message = stringResource(R.string.workout_empty),
            actionLabel = stringResource(R.string.action_choose_goal),
            onAction = onChooseGoal,
        )
        WorkoutFilter.PLANNED -> EmptyState(stringResource(R.string.workout_empty_planned))
        WorkoutFilter.DONE -> EmptyState(stringResource(R.string.workout_empty_done))
    }
}

/** Item do treino: `surfaceContainer`; concluído: `surfaceContainerLow` com check em `primary`. */
@Composable
private fun WorkoutItemCard(item: PlannedExercise, onMarkDone: () -> Unit, onRateEffort: (Int) -> Unit) {
    val isDone = item.status == WorkoutStatus.DONE
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isDone) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(Dimens.ItemSpacing), verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)) {
                ExerciseImage(url = item.thumbnailUrl, modifier = Modifier.size(Dimens.ListImageSize))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing / 2)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(R.string.workout_goal, stringResource(item.goal.labelRes()), stringResource(item.muscleGroup.labelRes())),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PrescriptionBadge(item.prescription)
                }
                if (isDone) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.status_done),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (isDone) {
                EffortSelector(effort = item.effort, onRateEffort = onRateEffort)
            } else {
                OutlinedButton(onClick = onMarkDone, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_mark_done))
                }
            }
        }
    }
}

/**
 * Esforço percebido de 1 a 5 (RF15). Só aparece para exercícios feitos (RN14).
 * Nível preenchido em `tertiary`, vazio em `outline` (identidade visual).
 */
@Composable
private fun EffortSelector(effort: Int?, onRateEffort: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.effort_label),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
        )
        WorkoutRules.EFFORT_RANGE.forEach { level ->
            IconButton(onClick = { onRateEffort(level) }) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = stringResource(R.string.effort_value, level),
                    tint = if (effort != null && level <= effort) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

private fun WorkoutFilter.labelRes(): Int = when (this) {
    WorkoutFilter.ALL -> R.string.filter_all
    WorkoutFilter.PLANNED -> R.string.filter_planned
    WorkoutFilter.DONE -> R.string.filter_done
}

@PreviewLightDark
@Composable
private fun WorkoutScreenPreview() {
    fun item(id: Long, status: WorkoutStatus, effort: Int?) = PlannedExercise(
        id = id, exerciseId = id.toInt(), name = if (id == 1L) "Supino reto" else "Agachamento",
        muscleGroup = if (id == 1L) MuscleGroup.CHEST else MuscleGroup.LEGS, thumbnailUrl = null,
        goal = Goal.HYPERTROPHY, prescription = PrescriptionRules.forGoal(Goal.HYPERTROPHY), status = status,
        addedAt = Instant.now(), doneAt = if (status == WorkoutStatus.DONE) Instant.now() else null, effort = effort,
    )
    PowerGabsTheme {
        WorkoutScreen(
            state = WorkoutUiState(
                content = WorkoutContent.Success(listOf(item(1, WorkoutStatus.PLANNED, null), item(2, WorkoutStatus.DONE, 4))),
            ),
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onChooseGoal = {},
            onFilterSelected = {},
            onMarkDone = {},
            onRateEffort = { _, _ -> },
        )
    }
}
