package com.gabriel.powergabs.presentation.screens.musclegroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.presentation.components.BackTopBar
import com.gabriel.powergabs.presentation.components.RestWarningCard
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import com.gabriel.powergabs.presentation.util.labelRes
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MuscleGroupRoute(
    goal: Goal,
    onBack: () -> Unit,
    onBuildWorkout: (Goal, MuscleGroup) -> Unit,
    viewModel: MuscleGroupViewModel = koinViewModel { parametersOf(goal) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MuscleGroupScreen(
        state = state,
        onBack = onBack,
        onGroupSelected = viewModel::onGroupSelected,
        onBuildWorkout = { group -> onBuildWorkout(state.goal, group) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleGroupScreen(
    state: MuscleGroupUiState,
    onBack: () -> Unit,
    onGroupSelected: (MuscleGroup) -> Unit,
    onBuildWorkout: (MuscleGroup) -> Unit,
) {
    Scaffold(
        topBar = { BackTopBar(onBack = onBack) },
        bottomBar = {
            Button(
                onClick = { state.selectedGroup?.let(onBuildWorkout) },
                enabled = state.selectedGroup != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.ItemSpacing),
            ) {
                Text(stringResource(R.string.action_build_workout))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        ) {
            Text(stringResource(R.string.muscle_group_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.muscle_group_subtitle, stringResource(state.goal.labelRes())),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
                modifier = Modifier.padding(top = Dimens.SmallSpacing),
            ) {
                state.groups.forEach { group ->
                    FilterChip(
                        selected = group == state.selectedGroup,
                        onClick = { onGroupSelected(group) },
                        label = { Text(stringResource(group.labelRes()), style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }
            val selected = state.selectedGroup
            if (state.showRestWarning && selected != null) {
                RestWarningCard(groupName = stringResource(selected.labelRes()))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MuscleGroupScreenPreview() {
    PowerGabsTheme {
        MuscleGroupScreen(
            state = MuscleGroupUiState(goal = Goal.HYPERTROPHY, selectedGroup = MuscleGroup.LEGS, showRestWarning = true),
            onBack = {},
            onGroupSelected = {},
            onBuildWorkout = {},
        )
    }
}
