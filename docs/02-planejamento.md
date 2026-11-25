# 02 — Planejamento técnico do PowerGabs

> Fase 2 do SDD: **como** construir o que está em `01-especificacao.md`. Os códigos RF, RNF e RN são os de lá.

## 1. Ponto de partida

O projeto nasceu do template "Empty Activity" do Android Studio (pacote `com.gabriel.powergabs`, AGP 9.5, `compileSdk`/`targetSdk` 37, `minSdk` 24). Mudanças em relação ao template:

- O tema sai de `ui/theme/` e vai para `presentation/theme/`, com o código de `docs/identidade_visual.md`.
- Os testes de exemplo (`ExampleUnitTest`, `ExampleInstrumentedTest`) são removidos.
- Os ícones `.webp` do template foram removidos (duplicavam os `.png` fornecidos pelo responsável).
- `themes.xml` passa a herdar de um tema sem ActionBar (o Compose desenha a tela inteira) e a usar o carvão como fundo da janela, evitando o "flash" branco ao abrir.

## 2. Arquitetura

### 2.1 Camadas e regra de dependência

```
           ┌──────────────────────┐
           │     presentation     │  Compose, ViewModels, UiState, navegação
           └──────────┬───────────┘
                      │ chama casos de uso
           ┌──────────▼───────────┐
           │        domain        │  Kotlin puro: modelos, regras, casos de uso,
           │                      │  interfaces de repositório
           └──────────▲───────────┘
                      │ implementa as interfaces
           ┌──────────┴───────────┐
           │         data         │  Room, Retrofit, DTOs, entidades, mappers,
           │                      │  autenticação local, Worker
           └──────────────────────┘
  core/ → AppResult, AppError, AppDispatchers, NetworkMonitor, TimeProvider (usado por todos)
  di/   → módulos Koin: único pacote que conhece todas as camadas
```

- O **domain** não importa `android.*`, Retrofit, Room, Media3 nem Koin. Ele depende só de Kotlin, `kotlinx.coroutines` (Flow) e `java.time`.
- A **presentation** só fala com casos de uso; nunca com repositórios, DAOs ou DTOs.
- O **data** conhece o domain (implementa as interfaces e usa as regras de idioma); DTOs e entidades nunca saem dele.

### 2.2 Estrutura de pacotes

```
com.gabriel.powergabs/
├── PowerGabsApp.kt            # Application: Koin, canal de notificação, agenda do lembrete
├── MainActivity.kt            # enableEdgeToEdge + PowerGabsTheme + PowerGabsRoot
├── di/                        # AppModule.kt (dataModule, domainModule, presentationModule)
├── core/
│   ├── result/                # AppResult, AppError
│   ├── dispatcher/            # AppDispatchers
│   ├── network/               # NetworkMonitor (interface) + AndroidNetworkMonitor
│   └── time/                  # TimeProvider + SystemTimeProvider
├── domain/
│   ├── model/                 # Goal, MuscleGroup, Exercise, Muscle, Equipment, LocalizedText, Prescription,
│   │                          # SuggestedExercise, ExerciseDetails, PlannedExercise, WorkoutStatus, WorkoutFilter, User
│   ├── rule/                  # PrescriptionRules, ExerciseSelectionRules, ExerciseCatalogRules, CachePolicy,
│   │                          # RestWarningRules, WorkoutRules, CredentialsRules, ReminderRules
│   ├── repository/            # ExerciseRepository, WorkoutRepository, AuthRepository
│   └── usecase/               # casos de uso agrupados por assunto (Auth, Exercise, Workout)
├── data/
│   ├── remote/                # WgerApi, dto/
│   ├── local/                 # AppDatabase, Converters, dao/, entity/
│   ├── auth/                  # PasswordHasher (PBKDF2), AuthRepositoryImpl
│   ├── notification/          # DailyReminderWorker, ReminderScheduler, NotificationChannels
│   ├── mapper/                # ExerciseMapper, WorkoutMapper, HtmlText
│   └── repository/            # SafeCalls, ExerciseRepositoryImpl, WorkoutRepositoryImpl
└── presentation/
    ├── navigation/            # Routes, PowerGabsNavHost, PowerGabsRoot, MainViewModel
    ├── theme/                 # Color, Type, Theme, Dimens
    ├── components/            # Logo, StateViews, OfflineBanner, PrescriptionBadge, ExerciseImage, RestWarningCard...
    ├── util/                  # ErrorMessageMapper, LabelMappers (Goal/MuscleGroup/Muscle/Equipment → string)
    └── screens/               # auth/, goal/, musclegroup/, exercises/, details/, workout/, about/
```

### 2.3 Modelos de domínio

| Modelo | Campos |
|---|---|
| `Goal` (enum) | `HYPERTROPHY`, `STRENGTH`, `ENDURANCE`, `CONDITIONING` |
| `MuscleGroup` (enum) | `ABS(10)`, `ARMS(8)`, `BACK(12)`, `CALVES(14)`, `CARDIO(15)`, `CHEST(11)`, `LEGS(9)`, `SHOULDERS(13)`, com `categoryId` e `fromCategoryId()` |
| `Muscle` | `id`, `name` (nome vindo da API, usado se o app não tiver tradução) |
| `Equipment` | `id`, `name`; constantes `BARBELL = 1`, `DUMBBELL = 3`, `BODYWEIGHT = 7` |
| `LocalizedText` | `languageId`, `name`, `description`; constantes `PORTUGUESE = 7`, `ENGLISH = 2` |
| `Exercise` | `id`, `muscleGroup`, `name`, `description`, `imageUrl?`, `thumbnailUrl?`, `videoUrl?`, `muscles`, `secondaryMuscles`, `equipment` |
| `Prescription` | `sets: IntRange`, `reps: IntRange`, `restSeconds: IntRange` |
| `SuggestedExercise` | `exercise`, `prescription` |
| `SuggestedExercises` | `items: List<SuggestedExercise>`, `lastUpdated: Instant?` |
| `ExerciseDetails` | `exercise`, `prescription`, `isInWorkout` |
| `WorkoutStatus` (enum) | `PLANNED`, `DONE` |
| `WorkoutFilter` (enum) | `ALL`, `PLANNED`, `DONE` |
| `PlannedExercise` | `id`, `exerciseId`, `name`, `muscleGroup`, `thumbnailUrl?`, `goal`, `prescription`, `status`, `addedAt`, `doneAt?`, `effort?` |
| `User` | `id`, `email` |

### 2.4 Regras e casos de uso

| Classe | Requisitos | Descrição |
|---|---|---|
| `PrescriptionRules` | RN01 | `forGoal(goal)` devolve a `Prescription` da tabela. |
| `ExerciseSelectionRules` | RN02–RN05 | `groupsFor(goal, group)` (Condicionamento acrescenta Cardio) e `matches(exercise, goal)` (Força exige Barra/Halteres). |
| `ExerciseCatalogRules` | RN06–RN08 | `pickTranslation(texts)`, `hasValidName`, `normalizeName`, `prepare(list)` = descarta sem nome + ordena (imagem primeiro, depois nome) + remove duplicados. |
| `CachePolicy` | RN09 | `isExpired(lastUpdated, now)`; `VALIDITY = 24 h`. |
| `RestWarningRules` | RN10 | `shouldWarn(doneAt, now, zone)`: alguma data de conclusão cai no dia anterior. |
| `WorkoutRules` | RN11, RN13, RN14 | `markDone(item, now)` e `rateEffort(item, effort)` com resultados selados. |
| `CredentialsRules` | RN15 | `validateLogin`, `validateRegistration`, `normalizeEmail`. |
| `ReminderRules` | RN16 | `delayUntilNextReminder(now, zone)` até as 18:00. |
| `RegisterUseCase` / `LoginUseCase` / `LogoutUseCase` / `ObserveSessionUseCase` | RF01–RF04 | Validam e delegam ao `AuthRepository`. |
| `ObserveSuggestedExercisesUseCase` | RF07, RN01–RN08 | Combina o cache dos grupos necessários e aplica seleção, preparo e prescrição. |
| `RefreshExercisesUseCase` | RF08, RN09, RNF01 | Atualiza os grupos expirados (ou todos, se forçado) quando há conexão. |
| `ObserveExerciseDetailsUseCase` | RF10, RF11 | Exercício do cache + prescrição + "já está no treino". |
| `AddToWorkoutUseCase` | RF12, RN11, RN12 | Grava com a prescrição do objetivo atual; recusa duplicado. |
| `ObserveWorkoutUseCase` | RF13 | Treino do usuário filtrado por status. |
| `MarkExerciseDoneUseCase` | RF14, RN13 | `PLANNED → DONE` com data/hora. |
| `RateEffortUseCase` | RF15, RN14 | Valida e grava o esforço. |
| `ObserveRestWarningUseCase` | RF09, RN10 | `Flow<Boolean>` para um grupo. |
| `ObserveConnectivityUseCase` | RF16 | Repassa o `NetworkMonitor`. |

Recusas de regra de negócio (ex.: "já está no treino") **não** são `AppError`: cada caso de uso tem um resultado selado próprio, para a UI tratar cada caso com uma mensagem específica.

## 3. API

### 3.1 Por que a wger

- **Gratuita e sem chave** para leitura: não há segredo para proteger no APK.
- **Conteúdo certo para o problema:** exercícios com categoria (grupo muscular), músculos, equipamentos, imagens e vídeos.
- **Traduções em português** para parte dos exercícios (50 de 90 em Peito), com inglês como reserva.
- **Licença aberta** (CC-BY-SA 4.0), que permite uso acadêmico com atribuição.
- **Documentação** navegável (`https://wger.de/api/v2/`) e projeto open source mantido.

### 3.2 Autenticação

Nenhuma. O OkHttp é configurado com timeouts (15 s de conexão, 30 s de leitura) e, só em debug, `HttpLoggingInterceptor` no nível `BASIC`.

### 3.3 Endpoints

| Uso | Endpoint |
|---|---|
| Exercícios de um grupo | `GET exerciseinfo/?category={id}&limit=100&offset={n}` (repete até `next == null`) |

As categorias, os equipamentos e os idiomas foram consultados na especificação (seção 8) e viram constantes do domain; não há motivo para baixá-los a cada uso.

## 4. Stack e versões

| Biblioteca | Versão | Motivo |
|---|---|---|
| Android Gradle Plugin | 9.5.2 | Já usado pelo template; Kotlin embutido. |
| Kotlin (compose/serialization plugins) | 2.4.21 | Estável mais recente. |
| KSP | 2.3.14 | Processador do Room. |
| Compose BOM | 2026.09.00 | Versões alinhadas de Compose (1.12) e Material 3 (1.4). O BOM 2026.11.01 exige `compileSdk` 37.1, que não está instalado no ambiente. |
| material-icons-core | 1.7.8 | Ícones `Icons.Rounded` básicos (sem o pacote "extended"). |
| Activity Compose / Core KTX | 1.13.0 / 1.19.1 | Template. |
| Lifecycle (runtime-compose, viewmodel-compose) | 2.11.1 | `collectAsStateWithLifecycle`, `viewModelScope`. |
| Navigation Compose | 2.10.2 | Rotas type-safe com `@Serializable`. |
| Koin BOM | 4.2.2 | DI (`koin-android`, `koin-androidx-compose`, `koin-androidx-workmanager`). |
| Coroutines | 1.11.0 | Flow/StateFlow. |
| kotlinx.serialization | 1.13.0 | JSON da API, rotas e conversores do Room. |
| Retrofit / OkHttp | 3.0.0 / 5.5.1 | Rede (conversor kotlinx.serialization oficial do Retrofit). |
| Room | 2.8.5 | Banco local, fonte da verdade. |
| Coil | 3.6.3 | Imagens com cache em memória e disco (`coil-network-okhttp`). |
| Media3 | 1.11.1 | ExoPlayer + `PlayerView` (via `AndroidView`, sem XML). |
| WorkManager | 2.12.0 | Lembrete diário. |
| desugar_jdk_libs | 2.1.5 | `java.time` (`Instant`, `LocalDate`, `ZoneId`) no Android 7 (API 24–25). |
| JUnit 4 / MockK / coroutines-test / Turbine | 4.13.2 / 1.14.12 / 1.11.0 / 1.2.1 | Testes unitários JVM. |

**Bibliotecas além da tabela do `CLAUDE.md`:** `desugar_jdk_libs` (necessária para usar `java.time` com `minSdk 24`), `material-icons-core` (ícones do conjunto core, autorizado pela identidade visual) e `koin-androidx-workmanager` (injeção no Worker). As três são de suporte, não adicionam funcionalidade.

## 5. Modelo de dados (Room)

```
┌──────────────────────────┐      ┌──────────────────────────┐
│ exercises                │      │ category_sync            │
│ id (PK)                  │      │ categoryId (PK)          │
│ categoryId (índice)      │◄─────│ lastUpdated (epoch ms)   │
│ name, description        │      └──────────────────────────┘
│ imageUrl, thumbnailUrl   │
│ videoUrl                 │      ┌──────────────────────────┐
│ muscles (JSON)           │      │ users                    │
│ secondaryMuscles (JSON)  │      │ id (PK, auto)            │
│ equipment (JSON)         │      │ email (único)            │
└──────────────────────────┘      │ passwordHash, salt       │
                                  │ createdAt                │
┌──────────────────────────┐      └────────────▲─────────────┘
│ workout_items            │                   │
│ id (PK, auto)            │      ┌────────────┴─────────────┐
│ userId ──────────────────┼─────►│ session                  │
│ exerciseId               │      │ id = 0 (PK, linha única) │
│ (userId, exerciseId) único│     │ userId                   │
│ name, categoryId,        │      └──────────────────────────┘
│ thumbnailUrl, goal       │
│ setsMin..restMax         │
│ status, addedAt, doneAt  │
│ effort                   │
└──────────────────────────┘
```

Decisões:
- **Exercício pertence a uma categoria** na wger; por isso `exercises` tem `categoryId` e a troca do cache de um grupo é `DELETE WHERE categoryId = ? + INSERT` numa `@Transaction`, junto com a atualização de `category_sync`.
- **`workout_items` guarda uma cópia** de nome, grupo e miniatura: o "Meu treino" continua completo mesmo se o cache do grupo for trocado.
- **Índice único `(userId, exerciseId)`** reforça a RN11 no próprio banco.
- **Listas (músculos, equipamentos)** são gravadas como JSON por `TypeConverter` com kotlinx.serialization (sem Gson).
- **Sessão em linha única** (`id = 0`): login faz upsert; logout apaga a linha.

## 6. Estratégia Offline First e sincronização

1. A tela observa `ObserveSuggestedExercisesUseCase` (Flow do Room) e mostra na hora o que houver em cache.
2. Ao abrir, o ViewModel chama `RefreshExercisesUseCase(force = false)`: para cada grupo necessário, se `CachePolicy.isExpired` e há conexão, busca todas as páginas e grava em transação; o Flow do Room emite sozinho.
3. Falha com cache na tela → Snackbar (não bloqueia). Falha sem cache → estado de erro com "Tentar novamente".
4. Pull-to-refresh → `force = true`. Sem conexão, devolve `AppError.Network` sem tentar a rede e a tela mostra "Sem internet: não foi possível atualizar."
5. "Atualizado há…" vem do `category_sync.lastUpdated` do grupo escolhido.
6. Os detalhes vêm do mesmo cache (o `exerciseinfo` já traz tudo), então funcionam offline. Imagens: cache de disco do Coil; sem cache, espaço reservado com ícone de haltere.
7. Vídeo: só tenta reproduzir com conexão; offline, mostra o aviso.
8. Busca "tudo ou nada": só grava se todas as páginas chegarem; o cache nunca fica pela metade.

## 7. Tratamento de erros

### 7.1 Conversão de exceções (`safeApiCall` / `safeDbCall`)

| Exceção | `AppError` |
|---|---|
| `IOException` (sem rede, timeout, DNS) | `Network` |
| `HttpException` | `Http(code)` |
| `SerializationException` | `Serialization` |
| `SQLiteException` e afins (no `safeDbCall`) | `Database` |
| Outras | `Unknown` |
| `CancellationException` | relançada (cancelamento não é erro) |

### 7.2 Mensagens ao usuário (`ErrorMessageMapper` → `strings.xml`)

| Erro | Mensagem |
|---|---|
| `Network` | "Não foi possível carregar os exercícios. Verifique sua conexão." |
| `Http` | "O servidor da wger não respondeu como esperado. Tente novamente mais tarde." |
| `Serialization` | "Os dados recebidos vieram em um formato inesperado." |
| `Database` | "Não foi possível acessar os dados salvos no aparelho." |
| `Unknown` | "Algo deu errado. Tente novamente." |

## 8. Tarefas em segundo plano

- `ReminderScheduler.schedule()` (chamado no `PowerGabsApp.onCreate`): `PeriodicWorkRequest` de 24 h com `initialDelay = ReminderRules.delayUntilNextReminder(now, zone)` e `ExistingPeriodicWorkPolicy.KEEP` (não reagenda a cada abertura).
- `DailyReminderWorker` (CoroutineWorker): só monta e mostra a notificação; sem regra de negócio. Se a permissão não foi concedida (API 33+), termina com sucesso sem notificar.
- Canal `daily_reminder` criado no `Application` apenas na API 26+.
- `POST_NOTIFICATIONS` pedida uma vez na tela de objetivo, apenas na API 33+.
- A notificação abre a `MainActivity` por `PendingIntent` (`FLAG_IMMUTABLE`).

## 9. Interface e navegação

### 9.1 Rotas (type-safe, `@Serializable`)

| Rota | Argumentos |
|---|---|
| `LoginRoute`, `RegisterRoute` | — |
| `GoalRoute` | — |
| `MuscleGroupRoute` | `goal: Goal` |
| `ExerciseListRoute` | `goal: Goal`, `group: MuscleGroup` |
| `ExerciseDetailsRoute` | `exerciseId: Int`, `goal: Goal` |
| `WorkoutRoute`, `AboutRoute` | — |

`PowerGabsRoot` observa a sessão (`MainViewModel`): enquanto carrega, mostra o indicador; sem sessão, o grafo começa no login; com sessão, no objetivo. Ao sair, o grafo é recriado no login, limpando a pilha. A faixa "sem internet" fica no topo de todas as telas, acima do `NavHost`.

### 9.2 Telas e estados

| Tela | Estados |
|---|---|
| Login / Cadastro | formulário com erros por campo; enviando; erro geral (credenciais) |
| Objetivo | estático (4 cards em grade 2×2) + ações: Meu treino, Sobre, Sair |
| Grupo muscular | chips; aviso de descanso para o grupo selecionado |
| Lista de exercícios | carregando / sucesso (com "atualizado há…") / vazio / erro; refresh; aviso de descanso |
| Detalhes | carregando / sucesso / não encontrado; vídeo: disponível / indisponível / offline |
| Meu treino | carregando / sucesso / vazio (por filtro) |
| Sobre | estático |

### 9.3 Padrões de implementação

- `XRoute` (obtém o ViewModel com `koinViewModel { parametersOf(...) }` e coleta o estado) + `XScreen` sem estado (recebe `UiState` e lambdas), o que permite previews `@PreviewLightDark`.
- UiState `data class` imutável; listas como `List`; `key = { it.id }` em `LazyColumn`/`LazyVerticalGrid`.
- Eventos únicos (Snackbar) por `Channel` → `Flow`, coletados com `LaunchedEffect`.
- `ExoPlayer` criado em `remember` e liberado em `DisposableEffect.onDispose`.
- Cores, textos e formas apenas do tema; nenhum `Color(0x…)` fora de `Color.kt`.

## 10. Injeção de dependências (Koin)

| Módulo | Registra | Escopo |
|---|---|---|
| `coreModule` | `AppDispatchers`, `TimeProvider`, `NetworkMonitor` | `single` |
| `dataModule` | `Json`, `OkHttpClient`, `Retrofit`, `WgerApi`, `AppDatabase`, DAOs, `PasswordHasher`, repositórios, `ReminderScheduler`, `DailyReminderWorker` | `single` / `worker` |
| `domainModule` | casos de uso | `factory` |
| `presentationModule` | ViewModels (com `parametersOf` para argumentos de rota) | `viewModel` |

## 11. Estratégia de testes

| Camada | O que é testado | Como |
|---|---|---|
| domain/rule | todas as RN | testes puros, `TimeProvider` fixo |
| domain/usecase | orquestração e resultados selados | repositórios falsos com MockK |
| data/mapper | DTO → entidade → domain, HTML → texto | objetos montados à mão |
| data/repository | cache válido/expirado, falha com e sem cache, transação, duplicado, login/cadastro | API e DAO com MockK |
| data/auth | hash determinístico com o mesmo sal; sais diferentes geram hashes diferentes | JVM pura |
| presentation | ViewModels: estados, Snackbar, refresh | MockK + Turbine + `StandardTestDispatcher` (`MainDispatcherRule`) |

Nomes de teste em inglês com crases, padrão Arrange / Act / Assert. Comando: `./gradlew testDebugUnitTest`.

## 12. Riscos e limitações

| Risco | Mitigação |
|---|---|
| API fora do ar ou lenta | Cache de 24 h + mensagens não bloqueantes. |
| Payload grande (Pernas: 235 exercícios ≈ 2 MB) | Páginas de 100 e gravação em transação única. |
| Vídeos `.MOV` | O ExoPlayer reproduz H.264 em contêiner QuickTime; se falhar, mostra o erro no lugar do vídeo. |
| Senha esquecida | Sem recuperação (limitação aceita); conta é local. |

## 13. Decisões registradas

| Data | Decisão | Motivo |
|---|---|---|
| 24/11/2026 | Autenticação local (Room + PBKDF2, 120 000 iterações, sal de 16 bytes). | Firebase removido pelo responsável; PBKDF2 está no `javax.crypto`, sem biblioteca extra. |
| 24/11/2026 | Hash guardado como `algoritmo:iterações:hash`; `PBKDF2WithHmacSHA256` com reserva `PBKDF2WithHmacSHA1` no Android 7. | O SHA-256 no PBKDF2 só existe a partir da API 26; guardar o algoritmo garante que a verificação use os mesmos parâmetros do cadastro. |
| 24/11/2026 | `java.time` com desugaring. | Datas claras e testáveis com `minSdk 24`. |
| 24/11/2026 | Categorias, equipamentos e idiomas como constantes. | São fixos na API e já foram confirmados; evita requisições extras. |
| 24/11/2026 | Descrição HTML convertida para texto simples no mapper. | Compose `Text` não renderiza HTML; texto simples funciona offline e é testável. |
| 24/11/2026 | Compose BOM 2026.09.00 em vez de 2026.11.01. | O 2026.11.01 (Compose 1.13) exige a plataforma 37.1; o SDK instalado tem a 37.0. |
| 24/11/2026 | Um único arquivo `AppModule.kt` com os quatro módulos Koin. | Projeto pequeno; mantém a visão de toda a injeção num lugar só. |
