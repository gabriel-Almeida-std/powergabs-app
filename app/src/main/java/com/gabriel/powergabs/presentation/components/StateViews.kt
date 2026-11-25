package com.gabriel.powergabs.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.gabriel.powergabs.R
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import androidx.compose.ui.res.stringResource

/** Carregando: indicador circular centralizado em `primary`. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/** Vazio: frase curta com o motivo e, opcionalmente, uma ação. */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction, modifier = Modifier.padding(top = Dimens.ItemSpacing)) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Mensagem de erro: `errorContainer` + ícone de alerta + texto explicativo.
 * O erro nunca depende só da cor (regra 7 da identidade visual).
 */
@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
        ) {
            Icon(Icons.Rounded.Warning, contentDescription = null)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Erro de tela inteira: o que aconteceu + botão "Tentar novamente". */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ErrorMessage(message)
        Button(onClick = onRetry, modifier = Modifier.padding(top = Dimens.ItemSpacing)) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    PowerGabsTheme {
        Surface { ErrorState(message = stringResource(R.string.error_network), onRetry = {}) }
    }
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    PowerGabsTheme {
        Surface {
            EmptyState(
                message = stringResource(R.string.workout_empty),
                actionLabel = stringResource(R.string.action_choose_goal),
                onAction = {},
            )
        }
    }
}
