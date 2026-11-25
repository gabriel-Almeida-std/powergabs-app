package com.gabriel.powergabs.presentation.screens.goal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import com.gabriel.powergabs.presentation.util.descriptionRes
import com.gabriel.powergabs.presentation.util.labelRes
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoalRoute(
    onContinue: (Goal) -> Unit,
    onOpenWorkout: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: GoalViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RequestNotificationPermissionOnce()
    GoalScreen(
        state = state,
        onGoalSelected = viewModel::onGoalSelected,
        onContinue = onContinue,
        onOpenWorkout = onOpenWorkout,
        onOpenAbout = onOpenAbout,
        onLogout = viewModel::onLogout,
    )
}

/**
 * Pede a permissão de notificação (RF17) apenas no Android 13+ (API 33), onde ela existe.
 * `rememberSaveable` garante um único pedido por abertura da tela, mesmo após rotação.
 */
@Composable
private fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    var alreadyAsked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted && !alreadyAsked) {
            alreadyAsked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    state: GoalUiState,
    onGoalSelected: (Goal) -> Unit,
    onContinue: (Goal) -> Unit,
    onOpenWorkout: () -> Unit,
    onOpenAbout: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onOpenWorkout) {
                        Icon(Icons.AutoMirrored.Rounded.List, contentDescription = stringResource(R.string.action_my_workout))
                    }
                    IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Rounded.Info, contentDescription = stringResource(R.string.action_about))
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = stringResource(R.string.action_logout))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Button(
                onClick = { state.selectedGoal?.let(onContinue) },
                enabled = state.selectedGoal != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // não fica atrás da barra de gestos
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.ItemSpacing),
            ) {
                Text(stringResource(R.string.action_continue))
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        ) {
            // O cabeçalho ocupa as duas colunas da grade.
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
                    Text(stringResource(R.string.goal_title), style = MaterialTheme.typography.displaySmall)
                    Text(
                        stringResource(R.string.goal_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    state.userEmail?.let {
                        Text(
                            stringResource(R.string.logged_as, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            // `key` estável: o Compose reconhece cada card mesmo se a lista mudar de ordem.
            items(state.goals, key = { it.name }) { goal ->
                GoalCard(goal = goal, selected = goal == state.selectedGoal, onClick = { onGoalSelected(goal) })
            }
        }
    }
}

/** Card de objetivo: selecionado = `primaryContainer` + borda `primary` 2dp; senão `surfaceContainerLowest` + `outlineVariant`. */
@Composable
private fun GoalCard(goal: Goal, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.GoalCardMinHeight)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLowest,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = if (selected) {
            BorderStroke(Dimens.SelectedBorderWidth, colors.primary)
        } else {
            BorderStroke(1.dp, colors.outlineVariant)
        },
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ItemSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
        ) {
            Text(stringResource(goal.labelRes()), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(goal.descriptionRes()),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun GoalScreenPreview() {
    PowerGabsTheme {
        GoalScreen(
            state = GoalUiState(userEmail = "gabriel@mail.com", selectedGoal = Goal.STRENGTH),
            onGoalSelected = {},
            onContinue = {},
            onOpenWorkout = {},
            onOpenAbout = {},
            onLogout = {},
        )
    }
}
