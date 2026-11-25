# CLAUDE.md — PowerGabs

## Contexto do projeto

Este repositório contém o **PowerGabs**, um aplicativo Android que monta sugestões de treino de acordo com o **objetivo** do usuário e o **grupo muscular do dia**. É um trabalho acadêmico, e os critérios de avaliação estão refletidos nestas instruções. Siga-as em todas as sessões.

O usuário faz login, escolhe o objetivo (Hipertrofia, Força, Resistência ou Condicionamento) e o grupo muscular que quer treinar. O app consulta a API da wger, seleciona os exercícios adequados e aplica uma **prescrição** (séries, repetições e descanso) definida pelo objetivo. O usuário pode ver os detalhes e o vídeo de execução (quando existir), adicionar exercícios ao "Meu treino", marcá-los como feitos e registrar o esforço percebido. As funcionalidades principais devem funcionar sem internet.

## Regras de trabalho (leia antes de qualquer ação)

1. Siga o Spec-Driven Development (SDD), nesta ordem: Especificação → Planejamento → Decomposição → Implementação → Validação. Não escreva código de produção antes de os documentos de especificação, planejamento e tarefas estarem aprovados por mim.
2. Ao final de cada fase do SDD, pare e peça minha aprovação antes de seguir. *(Em 24/11/2026 o responsável concedeu autonomia total para seguir todas as fases até o fim, registrando cada fase em commit.)*
3. Antes de qualquer ação que crie ou altere arquivos, explique o que vai fazer e por quê, e aguarde minha confirmação.
4. Trabalhe uma tarefa por vez, na ordem de `docs/03-tarefas.md`. Ao concluir, marque a tarefa como concluída no arquivo.
5. Não adicione funcionalidades, bibliotecas ou telas fora da especificação. Se achar que algo é necessário, pergunte.
6. Em caso de dúvida ou ambiguidade, pergunte em vez de supor.
7. Explique as decisões técnicas de forma didática: este é um projeto de aprendizado e preciso entender e conseguir defender cada parte do código.
8. Escreva respostas e documentação em português do Brasil. Nomes de classes, funções e variáveis em inglês.
9. Ao concluir cada tarefa, sugira a mensagem de commit (ver "Versionamento") e aguarde que eu faça o commit. Nunca acumule várias funcionalidades em um único commit.
10. A identidade visual está em `docs/identidade_visual.md`. Siga-a em toda tela e componente.

## Documentos do SDD

Ficam em `docs/`, em Markdown:

| Fase | Arquivo |
|---|---|
| Especificação | `docs/01-especificacao.md` (proposta, objetivos, requisitos funcionais e não funcionais) |
| Planejamento | `docs/02-planejamento.md` (arquitetura, tecnologias, estrutura e estratégia de implementação) |
| Decomposição | `docs/03-tarefas.md` (tarefas pequenas, em sequência lógica, com caixa de seleção) |
| Identidade visual | `docs/identidade_visual.md` |
| Validação | `TEST_RESULTS.md` (na raiz) |

## Stack obrigatória

| Área | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegação | Navigation Compose com rotas type-safe (kotlinx.serialization) |
| Injeção de dependências | Koin (koin-android, koin-androidx-compose) |
| Assíncrono | Kotlin Coroutines, Flow e StateFlow |
| Ciclo de vida | `collectAsStateWithLifecycle` (lifecycle-runtime-compose) |
| Rede | Retrofit + OkHttp + conversor kotlinx.serialization |
| Persistência | Room (com KSP) |
| Imagens | Coil (com cache em disco) |
| Vídeo | Media3 ExoPlayer |
| Autenticação | Local (e-mail e senha), com usuários e sessão no Room e senha com hash PBKDF2 (`javax.crypto`) |
| Notificações | WorkManager + NotificationCompat |
| Testes | JUnit 4, MockK, kotlinx-coroutines-test, Turbine |
| Build | Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`) |
| SDK | `minSdk = 24` (Android 7.0); `compileSdk` e `targetSdk` na versão estável mais recente |

Use as versões estáveis mais recentes e compatíveis entre si. Não use Hilt, Dagger, RxJava, LiveData, layouts XML nem Gson. Não use Firebase nem outros serviços de nuvem (decisão do responsável em 24/11/2026).

## API: wger

- Documentação: https://wger.de/api/v2/ (navegável) e https://wger.readthedocs.io
- Base URL: `https://wger.de/api/v2/`
- Endpoints de leitura pública (não exigem autenticação):
  - `exercisecategory/`: grupos musculares (categorias).
  - `equipment/`: equipamentos.
  - `exerciseinfo/`: exercícios com traduções, categoria, músculos, equipamentos, imagens e vídeos.
  - `language/`: idiomas disponíveis.
- Paginação por `limit` e `offset`.
- **Os IDs de categorias, equipamentos e idiomas devem ser confirmados consultando a API real durante a fase de Especificação** e registrados em `docs/01-especificacao.md`. Não escreva IDs de memória.
- Idioma: exibir o nome e a descrição em português quando houver tradução; caso contrário, em inglês.
- A wger não exige chave de API para leitura. Mesmo assim, toda a configuração de rede fica centralizada no módulo Koin de dados (OkHttp com timeouts e, em build de depuração, `HttpLoggingInterceptor`).
- Atribuição obrigatória: o conteúdo de exercícios é licenciado sob CC-BY-SA. A tela "Sobre" deve creditar a wger (https://wger.de) e citar a licença.

## Autenticação local

> Decisão do responsável (24/11/2026): o Firebase foi removido do projeto. A autenticação é feita localmente, no aparelho.

- Usuários (e-mail + hash da senha com sal, PBKDF2WithHmacSHA256) ficam no Room. A senha nunca é gravada em texto puro.
- O domain não conhece a implementação: existe uma interface `AuthRepository` no domain, implementada no data.
- A sessão fica salva no Room. Depois do primeiro login, o app abre direto, com ou sem internet.
- O "Meu treino" pertence ao usuário logado.

## Regras de negócio (camada Domain)

Estas regras ficam na camada Domain, em Kotlin puro, e devem ser cobertas por testes unitários.

### Prescrição por objetivo

| Objetivo | Séries | Repetições | Descanso | Regra de seleção |
|---|---|---|---|---|
| Hipertrofia | 3 a 4 | 8 a 12 | 60 a 90 s | Exercícios do grupo escolhido, qualquer equipamento |
| Força | 3 a 5 | 3 a 6 | 120 a 180 s | Apenas exercícios com barra ou halteres |
| Resistência | 2 a 3 | 15 a 20 | 30 a 60 s | Exercícios do grupo escolhido, incluindo peso corporal |
| Condicionamento | 2 a 3 | 12 a 15 | 30 s | Exercícios do grupo escolhido + exercícios da categoria Cardio |

Os valores são faixas gerais de referência, exibidos como sugestão. O app não faz prescrição individual (ver aviso de saúde no escopo).

### Demais regras

- Descartar exercícios sem nome em português nem em inglês.
- Ordenar exercícios com imagem antes dos que não têm imagem.
- Não exibir exercícios duplicados.
- O cache de exercícios de cada grupo muscular vale 24 horas. A prescrição é calculada no domain sobre os dados em cache, por isso trocar de objetivo não exige nova requisição.
- Aviso de descanso: se o usuário concluiu (`DONE`) algum exercício do mesmo grupo muscular no dia anterior, exibir um aviso sugerindo descanso. O aviso não impede a escolha.
- Um exercício não pode ser adicionado duas vezes ao "Meu treino".
- Ao adicionar ao "Meu treino", a prescrição do objetivo daquele momento é salva junto.
- Status do exercício salvo: `PLANNED` → `DONE`. Não há volta de `DONE` para `PLANNED`.
- Esforço percebido (1 a 5) só é permitido para exercícios `DONE`; valores fora do intervalo são rejeitados.
- Regras que dependem de data ("dia anterior", "24 horas") recebem o horário por uma abstração injetável (ex.: `Clock` ou `TimeProvider`), para que os testes controlem o tempo.

## Escopo

Dentro do escopo:
1. Login e cadastro locais (e-mail e senha, gravados no aparelho), incluindo logout.
2. Seleção de objetivo (tela inicial após o login).
3. Seleção do grupo muscular do dia.
4. Lista de exercícios sugeridos, com a prescrição aplicada, pull-to-refresh e aviso de descanso.
5. Detalhes do exercício: imagem, nome, descrição, músculos, equipamentos e prescrição.
6. Vídeo de execução com a wger como fonte, exibido apenas quando o exercício tem vídeo.
7. "Meu treino", com filtro por status, marcação como feito e registro de esforço.
8. Indicador de modo offline.
9. Notificação diária lembrando o usuário de treinar. Como o `minSdk` é 24, o canal de notificação só é criado a partir da API 26 e a permissão `POST_NOTIFICATIONS` só é pedida a partir da API 33, ambos com verificação de versão.
10. Tela "Sobre" com a atribuição à wger e o aviso de saúde: "As sugestões são gerais e não substituem a orientação de um profissional de educação física."

Fora do escopo: contas e treinos da wger, sincronização do "Meu treino" na nuvem, planos de dieta, busca livre por texto e login social (Google, Facebook etc.).

## Arquitetura

Clean Architecture em um único módulo `:app`, com separação por pacotes e regra de dependência rígida:

```
presentation  →  domain  ←  data
```

- **domain**: modelos, interfaces de repositório, casos de uso e regras de negócio. Kotlin puro: é **proibido** importar `android.*`, Retrofit, Room, Media3 ou Koin.
- **data**: implementa os repositórios do domain. Contém DTOs, entidades Room, DAOs, API Retrofit, a autenticação local, o Worker de notificação e os mappers. DTOs e entidades nunca saem desta camada.
- **presentation**: telas Compose, ViewModels, UiState e navegação. Acessa apenas casos de uso.
- **di**: módulos Koin; é o único lugar que conhece todas as camadas.

```
com.<seunome>.powergabs/
├── PowerGabsApp.kt        # Application: inicializa o Koin e o canal de notificação
├── MainActivity.kt
├── di/                    # dataModule, domainModule, presentationModule
├── core/                  # AppResult, AppError, dispatchers, monitor de conectividade, TimeProvider
├── domain/
│   ├── model/             # Goal, MuscleGroup, Exercise, ExerciseDetails, Prescription, PlannedExercise, WorkoutStatus, User
│   ├── repository/        # interfaces (ExerciseRepository, WorkoutRepository, AuthRepository)
│   └── usecase/
├── data/
│   ├── remote/            # WgerApi, DTOs
│   ├── local/             # AppDatabase, DAOs, entidades
│   ├── auth/              # autenticação local (hash de senha + sessão)
│   ├── notification/      # Worker do lembrete diário
│   ├── mapper/
│   └── repository/        # implementações
└── presentation/
    ├── navigation/
    ├── theme/
    ├── components/
    └── screens/           # auth/, goal/, musclegroup/, exercises/, details/, workout/, about/
```

## Offline First e sincronização

- O **Room é a única fonte de verdade**. A UI observa `Flow` vindos do Room; a rede apenas atualiza o banco.
- Ao abrir um grupo muscular:
  1. Emitir imediatamente o que estiver em cache.
  2. Se o cache estiver vazio ou tiver mais de 24 h e houver conexão, buscar na API e gravar no Room (o Flow emite os dados novos sozinho).
  3. Se a requisição falhar e houver cache, manter os dados na tela e mostrar o erro de forma não bloqueante (Snackbar).
  4. Se a requisição falhar e não houver cache, exibir o estado de erro com o botão "Tentar novamente".
- O pull-to-refresh força a busca na API, ignorando a validade de 24 h. Sem conexão, informa que não é possível atualizar e mantém o cache.
- Cada grupo muscular guarda a data da última atualização, usada na regra das 24 h e exibida como "atualizado há…".
- Detalhes do exercício já carregados ficam disponíveis offline. Imagens usam o cache em disco do Coil; sem cache, exibir o espaço reservado definido na identidade visual.
- O vídeo exige conexão. Offline, a área do vídeo informa isso em vez de tentar reproduzir.
- O "Meu treino" é totalmente local e funciona sem internet.
- O monitor de conectividade expõe um `Flow<Boolean>` (via `ConnectivityManager.NetworkCallback`) e alimenta o indicador de modo offline.
- Gravações no Room substituem os dados do grupo em uma transação, para não deixar o cache pela metade.

## Qualidade e confiabilidade

- **Erros:** o data converte exceções (sem rede, timeout, HTTP, JSON inválido, erros de banco e de autenticação) em `AppError`, devolvido dentro de `AppResult`. Nenhuma exceção previsível chega à UI sem tratamento. Mensagens para o usuário ficam em `strings.xml`, em português.
- **Estados de tela:** todo ViewModel expõe um `StateFlow<UiState>` que cobre carregando, sucesso, vazio e erro.
- **Memory leaks:** ViewModels não guardam `Context`, `Activity` nem `View`; coroutines rodam em `viewModelScope`; o ExoPlayer é criado e liberado com `DisposableEffect`; Flows são coletados com `collectAsStateWithLifecycle`.
- **Thread principal:** I/O sempre em `Dispatchers.IO`, injetado via `core` para poder ser trocado nos testes.
- **Recomposição:** UiState imutável (`data class` com `val`; listas como `List`), `key` estável em `LazyColumn`/`LazyVerticalGrid`, lambdas estáveis e `remember`/`derivedStateOf` quando houver cálculo na UI. Explique cada uso.

## Testes

- Cobrir obrigatoriamente: todas as regras de negócio do domain, os casos de uso, os mappers, os repositórios (com API e DAO falsos via MockK) e os ViewModels (com Turbine e `StandardTestDispatcher`).
- Cada teste segue o padrão Arrange / Act / Assert, com nome descritivo em inglês usando crases (ex.: `` `strength goal keeps only barbell and dumbbell exercises` ``).
- Comando: `./gradlew testDebugUnitTest`.
- Os resultados são registrados em `TEST_RESULTS.md`: data, comando, total de testes, aprovados, falhas e correções feitas.

## Versionamento

- Commits pequenos e frequentes, um por tarefa concluída, seguindo Conventional Commits em português:
  - `docs:` documentação; `feat:` funcionalidade; `fix:` correção; `test:` testes; `refactor:` refatoração sem mudança de comportamento; `chore:` configuração e build.
  - Exemplos: `docs: adiciona especificação inicial do projeto`, `feat: implementa cache de exercícios com Room`, `test: adiciona testes da prescrição por objetivo`.
- Os documentos do SDD são commitados antes de qualquer código de produção.
- Antes de cada commit, confirmar que `local.properties` está no `.gitignore`.
- Os commits não levam a linha `Co-Authored-By` (decisão do responsável).

## Entregáveis

Ao longo do desenvolvimento, mantenha atualizados:

1. **README.md** na raiz, com: nome e descrição; objetivo e problema resolvido; funcionalidades implementadas; tecnologias e bibliotecas; a API wger, com link e **justificativa da escolha**; arquitetura e camadas; estratégia Offline First; configuração e execução; execução dos testes; evidências dos testes; limitações conhecidas e funcionalidades não implementadas; créditos das fontes e da wger.
2. **Documentação SDD** em `docs/`.
3. **TEST_RESULTS.md** na raiz.
4. **APK de depuração** (`./gradlew assembleDebug`), sem assinatura de distribuição, copiado para `entrega/`.
5. **Evidências**: capturas de tela das principais funcionalidades, incluindo o funcionamento sem internet, em `docs/evidencias/`.

O README deve refletir o estado real do app: não descreva como pronto algo que ainda não foi implementado.
