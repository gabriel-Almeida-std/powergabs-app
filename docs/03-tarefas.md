# 03 — Tarefas do PowerGabs

> Decomposição de `02-planejamento.md`. Uma tarefa por vez, nesta ordem, um commit por tarefa.

## Definição de pronto (vale para todas)

- `./gradlew assembleDebug` compila.
- `./gradlew testDebugUnitTest` passa (e o resultado é registrado no `TEST_RESULTS.md` quando a tarefa tem testes).
- Regra de dependência respeitada (domain sem `android.*`, Retrofit, Room, Media3 ou Koin).
- Telas conforme `docs/identidade_visual.md`, com `@PreviewLightDark`.
- Checkbox marcado aqui e README atualizado só com o que existe.

## Configuração

### [x] T01 — Repositório Git e documentos do SDD
- **Descrição:** `git init`, `.gitignore` (sem `local.properties`), remoção dos `.webp` duplicados, `docs/01`, `02` e `03`.
- **Pronto quando:** os três documentos estão commitados antes de qualquer código de produção.
- **Relacionado:** todas · Seções 1 e 13
- **Commit:** `docs: adiciona planejamento e decomposicao em tarefas`

### [x] T02 — Build, dependências e README inicial
- **Descrição:** atualizar `libs.versions.toml` e `app/build.gradle.kts` com a stack da seção 4 (plugins serialization e KSP, desugaring, `buildConfig`), `.gitattributes` para `gradlew` com LF, remover os testes de exemplo e criar o README inicial.
- **Pronto quando:** `assembleDebug` e `testDebugUnitTest` rodam com sucesso.
- **Relacionado:** RNF04, RNF05 · Seção 4
- **Commit:** `chore: configura dependencias da stack do projeto`

### [x] T03 — Tema da identidade visual
- **Descrição:** mover o tema para `presentation/theme/` (Color, Type, Theme, Dimens) conforme a identidade; `themes.xml` sem ActionBar; `MainActivity` com `enableEdgeToEdge`.
- **Pronto quando:** o app abre com o tema PowerGabs, claro e escuro.
- **Relacionado:** RNF09 · Seção 9.3
- **Commit:** `feat: aplica tema da identidade visual`

### [x] T04 — Pacote core
- **Descrição:** `AppResult`, `AppError`, `AppDispatchers`, `NetworkMonitor` + `AndroidNetworkMonitor`, `TimeProvider` + `SystemTimeProvider`.
- **Pronto quando:** compila; nenhuma dependência de camadas superiores.
- **Relacionado:** RNF06, RNF08, RN17 · Seções 2.2 e 7
- **Commit:** `feat: adiciona pacote core com resultado, erros, dispatchers e tempo`

### [x] T05 — Application, Koin e manifesto
- **Descrição:** `PowerGabsApp` (Koin), `di/AppModule.kt` com `coreModule`, permissões `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`.
- **Pronto quando:** o app abre com o Koin iniciado.
- **Relacionado:** RNF02 · Seção 10
- **Commit:** `feat: configura Application e injecao de dependencias com Koin`

## Domain

### [x] T06 — Modelos de domínio
- **Descrição:** modelos da seção 2.3.
- **Pronto quando:** compila sem imports Android.
- **Relacionado:** RNF02 · Seção 2.3
- **Commit:** `feat: adiciona modelos de dominio`

### [x] T07 — Regras de negócio + testes
- **Descrição:** `PrescriptionRules`, `ExerciseSelectionRules`, `ExerciseCatalogRules`, `CachePolicy`, `RestWarningRules`, `WorkoutRules`, `CredentialsRules`, `ReminderRules` e testes de cada RN.
- **Pronto quando:** testes de RN01–RN16 passam.
- **Relacionado:** RN01–RN17 · Seção 2.4
- **Commit:** `feat: implementa regras de negocio do dominio com testes`

### [x] T08 — Interfaces de repositório e casos de uso + testes
- **Descrição:** `ExerciseRepository`, `WorkoutRepository`, `AuthRepository` e os casos de uso da seção 2.4, com testes (MockK).
- **Pronto quando:** testes dos casos de uso passam.
- **Relacionado:** RF01–RF16 · Seção 2.4
- **Commit:** `feat: implementa casos de uso com testes`

## Data

### [x] T09 — API Retrofit e DTOs
- **Descrição:** `WgerApi`, DTOs de `exerciseinfo`, OkHttp/Retrofit no `dataModule`.
- **Pronto quando:** compila; desserialização testada com JSON real reduzido.
- **Relacionado:** RF07 · Seção 3
- **Commit:** `feat: adiciona cliente Retrofit da API wger`

### [x] T10 — Room: entidades, DAOs e banco
- **Descrição:** entidades e DAOs da seção 5, `Converters`, `AppDatabase`, substituição transacional do grupo.
- **Pronto quando:** compila (KSP gera o código) e o banco é registrado no Koin.
- **Relacionado:** RNF01 · Seção 5
- **Commit:** `feat: adiciona banco Room com cache, usuarios e treino`

### [x] T11 — Mappers + testes
- **Descrição:** DTO → entidade (idioma, imagem principal, vídeo, HTML → texto), entidade → domain, treino ↔ entidade.
- **Pronto quando:** testes dos mappers passam.
- **Relacionado:** RN06 · Seção 2.4
- **Commit:** `feat: implementa mappers de exercicios e treino com testes`

### [x] T12 — Repositórios de exercícios e treino + testes
- **Descrição:** `safeApiCall`/`safeDbCall`, `ExerciseRepositoryImpl` (paginação tudo-ou-nada), `WorkoutRepositoryImpl`.
- **Pronto quando:** testes de cache, falhas e duplicados passam.
- **Relacionado:** RNF01, RNF06, RN11 · Seções 6 e 7
- **Commit:** `feat: implementa repositorios offline first com testes`

### [x] T13 — Autenticação local + testes
- **Descrição:** `PasswordHasher` (PBKDF2) e `AuthRepositoryImpl` (cadastro, login, sessão, logout).
- **Pronto quando:** testes de hash e do repositório passam.
- **Relacionado:** RF01–RF04, RNF03 · Seção 13
- **Commit:** `feat: implementa autenticacao local com hash de senha`

## Presentation

### [x] T14 — Navegação, raiz e componentes compartilhados
- **Descrição:** rotas, `PowerGabsRoot` + `MainViewModel` (sessão e conectividade), `OfflineBanner`, `StateViews`, `Logo`, `ErrorMessageMapper`, mapeadores de rótulos, ícones vetoriais (haltere e nuvem cortada).
- **Pronto quando:** o app abre no login ou no objetivo conforme a sessão; testes do `MainViewModel` passam.
- **Relacionado:** RF03, RF16 · Seção 9
- **Commit:** `feat: adiciona navegacao, faixa offline e componentes de estado`

### [x] T15 — Telas de login e cadastro
- **Descrição:** `LoginViewModel`, `RegisterViewModel` + telas + testes.
- **Relacionado:** RF01, RF02, RF04 · CA01–CA04
- **Commit:** `feat: implementa telas de login e cadastro`

### [ ] T16 — Tela de objetivo
- **Descrição:** grade 2×2 de objetivos, ações (Meu treino, Sobre, Sair), pedido de permissão de notificação (API 33+).
- **Relacionado:** RF05, RF04 · CA06
- **Commit:** `feat: implementa tela de selecao de objetivo`

### [ ] T17 — Tela de grupo muscular
- **Descrição:** `MuscleGroupViewModel` + tela com chips e aviso de descanso + testes.
- **Relacionado:** RF06, RF09 · CA16
- **Commit:** `feat: implementa tela de grupo muscular com aviso de descanso`

### [ ] T18 — Lista de exercícios sugeridos
- **Descrição:** `ExerciseListViewModel` + tela com prescrição, pull-to-refresh, "atualizado há…", Snackbar, estados + testes.
- **Relacionado:** RF07, RF08, RF09 · CA07–CA15
- **Commit:** `feat: implementa lista de exercicios sugeridos`

### [ ] T19 — Detalhes do exercício e vídeo
- **Descrição:** `ExerciseDetailsViewModel` + tela com imagem, chips, prescrição, vídeo (ExoPlayer) e "Adicionar ao treino" + testes.
- **Relacionado:** RF10, RF11, RF12 · CA17–CA19
- **Commit:** `feat: implementa detalhes do exercicio com video`

### [ ] T20 — Meu treino
- **Descrição:** `WorkoutViewModel` + tela com filtro, "Marcar como feito" e esforço 1–5 + testes.
- **Relacionado:** RF13–RF15 · CA20–CA22
- **Commit:** `feat: implementa tela meu treino`

### [ ] T21 — Tela Sobre
- **Descrição:** logo, crédito à wger (link), licença CC-BY-SA 4.0, fontes (OFL) e aviso de saúde.
- **Relacionado:** RF18 · CA25
- **Commit:** `feat: implementa tela sobre com atribuicao a wger`

### [ ] T22 — Notificação diária
- **Descrição:** canal (API 26+), `DailyReminderWorker`, `ReminderScheduler` com `KEEP`.
- **Relacionado:** RF17, RN16 · CA24
- **Commit:** `feat: adiciona lembrete diario com WorkManager`

## Validação

### [ ] T23 — Testes finais, evidências e README
- **Descrição:** rodar a suíte completa, registrar no `TEST_RESULTS.md` com a tabela CA → verificação, capturar telas no emulador (inclusive sem internet) em `docs/evidencias/`, README completo.
- **Relacionado:** todos
- **Commit:** `docs: registra validacao final, evidencias e README`

### [ ] T24 — APK de depuração
- **Descrição:** `./gradlew assembleDebug` e cópia para `entrega/PowerGabs-debug.apk`.
- **Commit:** `chore: adiciona APK de depuracao para entrega`
