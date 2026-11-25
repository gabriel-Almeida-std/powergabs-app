package com.gabriel.powergabs.presentation.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.presentation.components.LoadingState
import com.gabriel.powergabs.presentation.components.OfflineBanner
import org.koin.androidx.compose.koinViewModel

/**
 * Raiz da interface: faixa "sem internet" no topo (RF16) e o grafo de navegação abaixo.
 *
 * A sessão decide o grafo (RF03): sem usuário, começa no login; com usuário, no objetivo.
 * O `key(...)` recria o NavHost quando a sessão muda, o que limpa a pilha de telas no login
 * e no logout (não dá para "voltar" para uma tela do usuário anterior).
 */
@Composable
fun PowerGabsRoot(viewModel: MainViewModel = koinViewModel()) {
    val session by viewModel.sessionState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    StatusBarIconsFor(isOnline)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AnimatedVisibility(visible = !isOnline) { OfflineBanner() }

        // Com a faixa visível, ela já ocupa a barra de status: as telas abaixo não repetem esse espaço.
        val contentModifier = if (isOnline) Modifier else Modifier.consumeWindowInsets(WindowInsets.statusBars)
        Box(contentModifier.weight(1f)) {
            when (val state = session) {
                SessionState.Loading -> LoadingState()
                SessionState.LoggedOut -> key(LOGGED_OUT_KEY) {
                    PowerGabsNavHost(startDestination = LoginRoute)
                }
                is SessionState.LoggedIn -> key(state.user.id) {
                    PowerGabsNavHost(startDestination = GoalRoute)
                }
            }
        }
    }
}

/**
 * Cor dos ícones da barra de status. Com a faixa "sem internet" (fundo `inverseSurface`) atrás deles,
 * os ícones invertem para continuar legíveis: claros no tema claro e escuros no tema escuro.
 */
@Composable
private fun StatusBarIconsFor(isOnline: Boolean) {
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
            if (isOnline) !darkTheme else darkTheme
    }
}

private const val LOGGED_OUT_KEY = -1L
