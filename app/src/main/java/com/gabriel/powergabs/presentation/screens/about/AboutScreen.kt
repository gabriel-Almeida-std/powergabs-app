package com.gabriel.powergabs.presentation.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.gabriel.powergabs.BuildConfig
import com.gabriel.powergabs.R
import com.gabriel.powergabs.presentation.components.BackTopBar
import com.gabriel.powergabs.presentation.components.PowerGabsLogo
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme

private const val WGER_URL = "https://wger.de"

/**
 * Tela "Sobre" (RF18, CA25): atribuição obrigatória à wger com a licença CC-BY-SA 4.0,
 * aviso de saúde e crédito das fontes. É estática, por isso não tem ViewModel.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Scaffold(topBar = { BackTopBar(onBack = onBack, title = stringResource(R.string.about_title)) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
        ) {
            PowerGabsLogo()
            Text(stringResource(R.string.about_description), style = MaterialTheme.typography.bodyLarge)

            // Aviso de saúde em destaque (escopo, item 10).
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.ItemSpacing),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null)
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing / 2)) {
                        Text(stringResource(R.string.about_health_title), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.about_health_warning), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AboutSection(R.string.about_data_title, R.string.about_data_credit) {
                OutlinedButton(onClick = { uriHandler.openUri(WGER_URL) }) {
                    Text(stringResource(R.string.about_open_wger))
                }
            }
            AboutSection(R.string.about_fonts_title, R.string.about_fonts_credit)

            Text(
                stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.SectionSpacing),
            )
        }
    }
}

@Composable
private fun AboutSection(title: Int, body: Int, action: @Composable () -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
        action()
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    PowerGabsTheme { AboutScreen(onBack = {}) }
}
