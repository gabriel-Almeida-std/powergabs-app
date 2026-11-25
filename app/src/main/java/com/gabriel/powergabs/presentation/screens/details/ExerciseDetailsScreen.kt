package com.gabriel.powergabs.presentation.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Equipment
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.ExerciseDetails
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.Muscle
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.presentation.components.BackTopBar
import com.gabriel.powergabs.presentation.components.EmptyState
import com.gabriel.powergabs.presentation.components.ExerciseImage
import com.gabriel.powergabs.presentation.components.LoadingState
import com.gabriel.powergabs.presentation.components.PrescriptionBadge
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import com.gabriel.powergabs.presentation.util.Formatters
import com.gabriel.powergabs.presentation.util.labelRes
import com.gabriel.powergabs.presentation.util.labelResOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExerciseDetailsRoute(
    exerciseId: Int,
    goal: Goal,
    onBack: () -> Unit,
    viewModel: ExerciseDetailsViewModel = koinViewModel { parametersOf(exerciseId, goal) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(context.getString(it)) }
    }
    ExerciseDetailsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAddToWorkout = viewModel::onAddToWorkout,
    )
}

@Composable
fun ExerciseDetailsScreen(
    state: ExerciseDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onAddToWorkout: () -> Unit,
) {
    val content = state.content
    Scaffold(
        topBar = { BackTopBar(onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (content is DetailsContent.Success) {
                AddToWorkoutButton(content.details.isInWorkout, state.isAdding, onAddToWorkout)
            }
        },
    ) { padding ->
        when (content) {
            DetailsContent.Loading -> LoadingState(Modifier.padding(padding))
            DetailsContent.NotFound -> EmptyState(
                message = stringResource(R.string.details_not_found),
                modifier = Modifier.padding(padding),
                actionLabel = stringResource(R.string.action_back),
                onAction = onBack,
            )
            is DetailsContent.Success -> DetailsBody(content.details, state, Modifier.padding(padding))
        }
    }
}

@Composable
private fun AddToWorkoutButton(isInWorkout: Boolean, isAdding: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isInWorkout && !isAdding,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.ItemSpacing),
    ) {
        Icon(if (isInWorkout) Icons.Rounded.Check else Icons.Rounded.Add, contentDescription = null)
        Text(
            stringResource(if (isInWorkout) R.string.already_in_workout else R.string.action_add_to_workout),
            modifier = Modifier.padding(start = Dimens.SmallSpacing),
        )
    }
}

@Composable
private fun DetailsBody(details: ExerciseDetails, state: ExerciseDetailsUiState, modifier: Modifier) {
    val exercise = details.exercise
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
    ) {
        ExerciseImage(
            url = exercise.imageUrl,
            modifier = Modifier.fillMaxWidth().height(Dimens.DetailImageHeight),
            contentScale = ContentScale.Fit,
        )

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
            Text(exercise.name, style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(exercise.muscleGroup.labelRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Section(title = stringResource(R.string.details_prescription)) {
            PrescriptionBadge(details.prescription)
            Text(
                stringResource(
                    R.string.sets_reps_rest,
                    Formatters.range(details.prescription.sets),
                    Formatters.range(details.prescription.reps),
                    Formatters.range(details.prescription.restSeconds),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Section(title = stringResource(R.string.details_how_to)) {
            Text(
                exercise.description.ifBlank { stringResource(R.string.details_no_description) },
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (exercise.muscles.isNotEmpty()) {
            Section(title = stringResource(R.string.details_muscles)) { InfoChips(exercise.muscles.map { muscleName(it) }) }
        }
        if (exercise.secondaryMuscles.isNotEmpty()) {
            Section(title = stringResource(R.string.details_secondary_muscles)) {
                InfoChips(exercise.secondaryMuscles.map { muscleName(it) })
            }
        }
        if (exercise.equipment.isNotEmpty()) {
            Section(title = stringResource(R.string.details_equipment)) {
                InfoChips(exercise.equipment.map { equipmentName(it) })
            }
        }

        Section(title = stringResource(R.string.details_video)) {
            ExerciseVideoSection(videoUrl = exercise.videoUrl, isOnline = state.isOnline)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

/** Chips de músculo e equipamento: `surfaceContainerHigh` / `onSurfaceVariant`. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoChips(labels: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
    ) {
        labels.distinct().forEach { label ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun muscleName(muscle: Muscle): String = muscle.labelResOrNull()?.let { stringResource(it) } ?: muscle.name

@Composable
private fun equipmentName(equipment: Equipment): String =
    equipment.labelResOrNull()?.let { stringResource(it) } ?: equipment.name

@PreviewLightDark
@Composable
private fun ExerciseDetailsScreenPreview() {
    val details = ExerciseDetails(
        exercise = Exercise(
            id = 73, muscleGroup = MuscleGroup.CHEST, name = "Supino reto",
            description = "Deite no banco, desça a barra até o peito e empurre de volta.",
            imageUrl = null, thumbnailUrl = null, videoUrl = null,
            muscles = listOf(Muscle(4, "Chest")), secondaryMuscles = listOf(Muscle(5, "Triceps")),
            equipment = listOf(Equipment(1, "Barbell"), Equipment(8, "Bench")),
        ),
        prescription = PrescriptionRules.forGoal(Goal.HYPERTROPHY),
        isInWorkout = false,
    )
    PowerGabsTheme {
        ExerciseDetailsScreen(
            state = ExerciseDetailsUiState(goal = Goal.HYPERTROPHY, content = DetailsContent.Success(details)),
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onAddToWorkout = {},
        )
    }
}
