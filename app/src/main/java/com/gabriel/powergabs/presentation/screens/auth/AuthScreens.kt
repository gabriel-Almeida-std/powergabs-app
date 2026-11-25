package com.gabriel.powergabs.presentation.screens.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.powergabs.R
import com.gabriel.powergabs.presentation.components.ErrorMessage
import com.gabriel.powergabs.presentation.components.PowerGabsLogo
import com.gabriel.powergabs.presentation.theme.Dimens
import com.gabriel.powergabs.presentation.theme.PowerGabsTheme
import org.koin.androidx.compose.koinViewModel

// ---------- Login (RF02) ----------

/** "Route": obtém o ViewModel e coleta o estado. A "Screen" abaixo não conhece o ViewModel. */
@Composable
fun LoginRoute(onGoToRegister: () -> Unit, viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        onGoToRegister = onGoToRegister,
    )
}

@Composable
fun LoginScreen(
    state: AuthFormUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoToRegister: () -> Unit,
) {
    AuthScaffold(title = R.string.login_title, subtitle = R.string.login_subtitle) {
        EmailField(state.email, state.emailError, onEmailChange)
        PasswordField(
            value = state.password,
            label = R.string.field_password,
            error = state.passwordError,
            onValueChange = onPasswordChange,
            imeAction = ImeAction.Done,
            onDone = onSubmit,
        )
        state.generalError?.let { ErrorMessage(stringResource(it)) }
        SubmitButton(R.string.action_login, state.isSubmitting, onSubmit)
        OutlinedButton(onClick = onGoToRegister, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_go_to_register))
        }
    }
}

// ---------- Cadastro (RF01) ----------

@Composable
fun RegisterRoute(onGoToLogin: () -> Unit, viewModel: RegisterViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmationChange = viewModel::onConfirmationChange,
        onSubmit = viewModel::onSubmit,
        onGoToLogin = onGoToLogin,
    )
}

@Composable
fun RegisterScreen(
    state: AuthFormUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoToLogin: () -> Unit,
) {
    AuthScaffold(title = R.string.register_title, subtitle = R.string.register_subtitle) {
        EmailField(state.email, state.emailError, onEmailChange)
        PasswordField(state.password, R.string.field_password, state.passwordError, onPasswordChange)
        PasswordField(
            value = state.confirmation,
            label = R.string.field_password_confirmation,
            error = state.confirmationError,
            onValueChange = onConfirmationChange,
            imeAction = ImeAction.Done,
            onDone = onSubmit,
        )
        state.generalError?.let { ErrorMessage(stringResource(it)) }
        SubmitButton(R.string.action_register, state.isSubmitting, onSubmit)
        OutlinedButton(onClick = onGoToLogin, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_go_to_login))
        }
    }
}

// ---------- Partes compartilhadas ----------

@Composable
private fun AuthScaffold(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    content: @Composable () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding() // o teclado não cobre os campos
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        ) {
            Spacer(Modifier.height(Dimens.SectionSpacing))
            PowerGabsLogo()
            Spacer(Modifier.height(Dimens.SectionSpacing))
            Text(stringResource(title), style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.SmallSpacing))
            content()
        }
    }
}

@Composable
private fun EmailField(value: String, @StringRes error: Int?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.field_email)) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(
    value: String,
    @StringRes label: Int,
    @StringRes error: Int?,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onDone()
        }),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SubmitButton(@StringRes label: Int, isSubmitting: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(label))
        }
    }
}

// ---------- Previews ----------

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    PowerGabsTheme {
        LoginScreen(
            state = AuthFormUiState(email = "gabriel@mail.com", generalError = R.string.error_invalid_credentials),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onGoToRegister = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RegisterScreenPreview() {
    PowerGabsTheme {
        RegisterScreen(
            state = AuthFormUiState(passwordError = R.string.error_password_too_short),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmationChange = {},
            onSubmit = {},
            onGoToLogin = {},
        )
    }
}
