package com.gabriel.powergabs.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Prescription
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import com.gabriel.powergabs.presentation.util.Formatters

/**
 * Imagem do exercício com o Coil (cache em memória e em disco).
 * Sem URL, sem cache ou com erro de carga, mostra o espaço reservado da identidade:
 * `surfaceContainer` com borda `outline` e ícone de haltere em `outline`.
 */
@Composable
fun ExerciseImage(
    url: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val imageModifier = modifier.clip(shape)
    if (url == null) {
        ImagePlaceholder(imageModifier, shape)
        return
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = stringResource(R.string.exercise_image),
        contentScale = contentScale,
        modifier = imageModifier,
        loading = { ImagePlaceholder(Modifier.fillMaxSize(), shape) },
        error = { ImagePlaceholder(Modifier.fillMaxSize(), shape) },
    )
}

@Composable
private fun ImagePlaceholder(modifier: Modifier, shape: Shape) {
    Box(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape),
        contentAlignment = Alignment.Center,
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxSize()) {}
        Icon(
            painter = painterResource(R.drawable.ic_dumbbell),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

/** Badge de prescrição (ex.: "4 × 8–12 · 90 s"): `secondaryContainer` com `labelMedium`. */
@Composable
fun PrescriptionBadge(prescription: Prescription, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = Formatters.prescriptionBadge(prescription),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun ExerciseVisualsPreview() {
    PowerGabsTheme {
        Surface {
            androidx.compose.foundation.layout.Row {
                ExerciseImage(url = null, modifier = Modifier.padding(8.dp).then(Modifier.fillMaxSize(0.2f)))
                PrescriptionBadge(Prescription(3..4, 8..12, 60..90), Modifier.padding(8.dp))
            }
        }
    }
}
