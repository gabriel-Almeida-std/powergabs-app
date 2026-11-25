package com.gabriel.powergabs.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.gabriel.powergabs.R
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme

/**
 * Faixa "sem internet" (RF16): `inverseSurface` com ícone de nuvem cortada.
 * Fica no topo do app, por isso ocupa a área da barra de status.
 */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SmallSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
        ) {
            Icon(painterResource(R.drawable.ic_cloud_off), contentDescription = null)
            Text(stringResource(R.string.offline_banner), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Aviso de descanso (RF09): `tertiaryContainer` com ícone de informação. Não bloqueia a escolha. */
@Composable
fun RestWarningCard(groupName: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
        ) {
            Icon(Icons.Rounded.Info, contentDescription = null)
            Text(stringResource(R.string.rest_warning, groupName), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@PreviewLightDark
@Composable
private fun BannersPreview() {
    PowerGabsTheme {
        Surface {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)) {
                OfflineBanner()
                RestWarningCard(groupName = "Pernas", modifier = Modifier.padding(Dimens.ScreenPadding))
            }
        }
    }
}
