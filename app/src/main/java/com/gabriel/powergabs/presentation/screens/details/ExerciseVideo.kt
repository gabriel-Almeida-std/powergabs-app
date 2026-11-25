package com.gabriel.powergabs.presentation.screens.details

import androidx.annotation.OptIn
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.ui.PlayerView
import com.gabriel.powergabs.R
import com.gabriel.powergabs.presentation.theme.Dimens

/**
 * Área do vídeo de execução (RF11, CA18):
 * - sem vídeo na wger → mensagem informativa (não é erro);
 * - sem internet → aviso de que o vídeo precisa de conexão;
 * - com vídeo e internet → player do Media3.
 */
@Composable
fun ExerciseVideoSection(videoUrl: String?, isOnline: Boolean, modifier: Modifier = Modifier) {
    when {
        videoUrl == null -> VideoMessage(R.string.video_unavailable, modifier, iconIsCloudOff = false)
        !isOnline -> VideoMessage(R.string.video_offline, modifier, iconIsCloudOff = true)
        else -> ExerciseVideoPlayer(videoUrl, modifier)
    }
}

/**
 * Player do ExoPlayer dentro do Compose (via `AndroidView`, sem layout XML).
 *
 * Cuidados de memória (RNF08): o player é criado com `remember` (um por URL) e liberado no
 * `onDispose` do `DisposableEffect`, quando a tela sai da composição. Ele também pausa quando o app
 * vai para segundo plano (ON_STOP), para não continuar tocando sem ninguém ver.
 */
@OptIn(UnstableApi::class)
@Composable
private fun ExerciseVideoPlayer(url: String, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasError by remember(url) { mutableStateOf(false) }

    val player = remember(url) {
        ExoPlayer.Builder(context, videoRenderersFactory(context)).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false
            prepare()
        }
    }

    DisposableEffect(player, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                hasError = true
            }
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        player.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            player.removeListener(listener)
            player.release() // libera decodificadores e memória do vídeo
        }
    }

    if (hasError) {
        VideoMessage(R.string.video_error, modifier, iconIsCloudOff = false)
        return
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    this.player = player
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(VIDEO_ASPECT_RATIO)
                .clip(MaterialTheme.shapes.medium),
        )
        Text(
            stringResource(R.string.video_source),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Os emuladores do Android Studio trazem decodificadores "goldfish" que falham com alguns vídeos
 * H.264 da wger (perfil High). Este seletor só muda a ORDEM de preferência: decodificadores goldfish
 * vão para o fim da lista, e o de software do Android é tentado primeiro. Em aparelhos reais
 * (sem goldfish) a ordem fica igual. `setEnableDecoderFallback` tenta o próximo se um falhar ao iniciar.
 */
@OptIn(UnstableApi::class)
private fun videoRenderersFactory(context: Context): DefaultRenderersFactory {
    val selector = MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
        MediaCodecSelector.DEFAULT
            .getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder)
            .sortedBy { it.name.contains(EMULATOR_DECODER_MARK) }
    }
    return DefaultRenderersFactory(context)
        .setMediaCodecSelector(selector)
        .setEnableDecoderFallback(true)
}

private const val EMULATOR_DECODER_MARK = "goldfish"

/** Área de vídeo indisponível/offline: `surfaceContainerHigh` com ícone e texto em `onSurfaceVariant`. */
@Composable
private fun VideoMessage(@StringRes message: Int, modifier: Modifier, iconIsCloudOff: Boolean) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(VIDEO_ASPECT_RATIO),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (iconIsCloudOff) {
                Icon(painterResource(R.drawable.ic_cloud_off), contentDescription = null)
            } else {
                Icon(Icons.Rounded.Info, contentDescription = null)
            }
            Text(stringResource(message), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

private const val VIDEO_ASPECT_RATIO = 16f / 9f
