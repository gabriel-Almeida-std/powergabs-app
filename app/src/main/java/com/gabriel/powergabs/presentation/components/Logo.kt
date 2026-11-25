package com.gabriel.powergabs.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.gabriel.powergabs.R
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme

/**
 * Logo completo montado em Compose (e não como imagem), conforme a identidade visual:
 * acompanha o tema claro/escuro e é lido por leitores de tela como "PowerGabs".
 */
@Composable
fun PowerGabsLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_logo_symbol),
            contentDescription = null, // o texto ao lado já nomeia o app
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.LogoSymbolSize),
        )
        Spacer(Modifier.width(Dimens.ItemSpacing))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) { append("Power") }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Gabs") }
            },
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

@PreviewLightDark
@Composable
private fun PowerGabsLogoPreview() {
    PowerGabsTheme {
        Surface { PowerGabsLogo() }
    }
}
