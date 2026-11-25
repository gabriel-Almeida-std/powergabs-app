package com.gabriel.powergabs.di

import com.gabriel.powergabs.BuildConfig
import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.network.AndroidNetworkMonitor
import com.gabriel.powergabs.core.network.NetworkMonitor
import com.gabriel.powergabs.core.time.SystemTimeProvider
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.data.auth.AuthRepositoryImpl
import com.gabriel.powergabs.data.auth.PasswordHasher
import com.gabriel.powergabs.data.local.AppDatabase
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.data.remote.NetworkFactory
import com.gabriel.powergabs.data.repository.ExerciseRepositoryImpl
import com.gabriel.powergabs.data.repository.WorkoutRepositoryImpl
import com.gabriel.powergabs.domain.repository.ExerciseRepository
import com.gabriel.powergabs.domain.repository.WorkoutRepository
import com.gabriel.powergabs.domain.usecase.AddToWorkoutUseCase
import com.gabriel.powergabs.domain.usecase.LoginUseCase
import com.gabriel.powergabs.domain.usecase.LogoutUseCase
import com.gabriel.powergabs.domain.usecase.MarkExerciseDoneUseCase
import com.gabriel.powergabs.domain.usecase.ObserveConnectivityUseCase
import com.gabriel.powergabs.domain.usecase.ObserveExerciseDetailsUseCase
import com.gabriel.powergabs.domain.usecase.ObserveRestWarningUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSessionUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSuggestedExercisesUseCase
import com.gabriel.powergabs.domain.usecase.ObserveWorkoutUseCase
import com.gabriel.powergabs.domain.usecase.RateEffortUseCase
import com.gabriel.powergabs.domain.usecase.RefreshExercisesUseCase
import com.gabriel.powergabs.domain.usecase.RegisterUseCase
import com.gabriel.powergabs.presentation.navigation.MainViewModel
import com.gabriel.powergabs.presentation.screens.auth.LoginViewModel
import com.gabriel.powergabs.presentation.screens.auth.RegisterViewModel
import com.gabriel.powergabs.presentation.screens.details.ExerciseDetailsViewModel
import com.gabriel.powergabs.presentation.screens.exercises.ExerciseListViewModel
import com.gabriel.powergabs.presentation.screens.goal.GoalViewModel
import com.gabriel.powergabs.presentation.screens.musclegroup.MuscleGroupViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Módulos Koin. O pacote `di` é o único que conhece todas as camadas:
 * é aqui que cada interface do domain é ligada à sua implementação do data.
 *
 * - `single`: uma instância para o app inteiro (banco, cliente HTTP, repositórios).
 * - `factory`: uma instância nova a cada pedido (casos de uso, que não guardam estado).
 * - `viewModel`: instância presa ao ciclo de vida da tela.
 */
val coreModule = module {
    single { AppDispatchers(io = Dispatchers.IO, default = Dispatchers.Default) }
    single<TimeProvider> { SystemTimeProvider() }
    single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
}

val dataModule = module {
    // Rede: toda a configuração fica aqui (timeouts e log só em depuração).
    single { NetworkFactory.json }
    single { NetworkFactory.okHttpClient(enableLogging = BuildConfig.DEBUG) }
    single { NetworkFactory.wgerApi(client = get(), json = get()) }

    // Banco local (fonte da verdade) e DAOs.
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().exerciseDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().workoutDao() }

    // Repositórios: a interface (domain) é ligada à implementação (data).
    single<ExerciseRepository> { ExerciseRepositoryImpl(api = get(), dao = get(), timeProvider = get(), dispatchers = get()) }
    single<WorkoutRepository> { WorkoutRepositoryImpl(dao = get(), dispatchers = get()) }

    // Autenticação local.
    single { PasswordHasher() }
    single<AuthRepository> { AuthRepositoryImpl(userDao = get(), hasher = get(), timeProvider = get(), dispatchers = get()) }
}

/** Casos de uso não guardam estado: `factory` cria um novo a cada injeção. */
val domainModule = module {
    factoryOf(::RegisterUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ObserveSessionUseCase)
    factoryOf(::ObserveSuggestedExercisesUseCase)
    factoryOf(::RefreshExercisesUseCase)
    factoryOf(::ObserveExerciseDetailsUseCase)
    factoryOf(::ObserveConnectivityUseCase)
    factoryOf(::AddToWorkoutUseCase)
    factoryOf(::ObserveWorkoutUseCase)
    factoryOf(::MarkExerciseDoneUseCase)
    factoryOf(::RateEffortUseCase)
    factoryOf(::ObserveRestWarningUseCase)
}

/** ViewModels presos ao ciclo de vida da tela. Argumentos de rota chegam por `parametersOf`. */
val presentationModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::GoalViewModel)
    viewModel { params -> MuscleGroupViewModel(goal = params.get(), observeRestWarning = get()) }
    viewModel { params ->
        ExerciseListViewModel(
            goal = params.get(),
            group = params.get(),
            observeSuggestedExercises = get(),
            refreshExercises = get(),
            observeRestWarning = get(),
        )
    }
    viewModel { params ->
        ExerciseDetailsViewModel(
            exerciseId = params.get(),
            goal = params.get(),
            observeDetails = get(),
            addToWorkout = get(),
            observeConnectivity = get(),
        )
    }
}

/** Todos os módulos, na ordem em que são carregados pelo [com.gabriel.powergabs.PowerGabsApp]. */
val appModules = listOf(coreModule, dataModule, domainModule, presentationModule)
