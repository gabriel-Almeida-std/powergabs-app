# 01 — Especificação do PowerGabs

> Fase 1 do SDD: **o quê** o app faz e **por quê**. O "como" fica em `02-planejamento.md`.

## 1. Proposta

O PowerGabs é um app Android que monta sugestões de treino a partir de duas escolhas do usuário: o **objetivo** (Hipertrofia, Força, Resistência ou Condicionamento) e o **grupo muscular do dia**. O app busca os exercícios na API pública da [wger](https://wger.de), filtra os que combinam com o objetivo e mostra cada um com uma **prescrição** de séries, repetições e descanso. O usuário monta o próprio "Meu treino", marca os exercícios feitos e registra o esforço percebido. As funcionalidades principais funcionam sem internet.

## 2. Problema que resolve

Quem treina sozinho costuma chegar à academia sem saber **quais exercícios** fazer para um grupo muscular nem **quantas séries e repetições** combinam com o próprio objetivo. A informação existe, mas está espalhada em vídeos, sites e planilhas, e muitas vezes não está disponível sem internet (academias em subsolo, plano de dados limitado). O PowerGabs junta essas respostas em poucos toques e guarda tudo no aparelho.

## 3. Objetivos

**Geral:** sugerir um treino coerente com o objetivo e o grupo muscular escolhidos, com prescrição de referência, funcionando também sem internet.

**Específicos:**
1. Consumir a API da wger e manter um cache local válido por 24 horas por grupo muscular.
2. Aplicar no domain as regras de seleção e de prescrição por objetivo, cobertas por testes.
3. Permitir que o usuário monte, acompanhe e avalie o próprio treino, sem depender de rede.
4. Aplicar Clean Architecture, Offline First e a identidade visual do projeto.

## 4. Público-alvo

Praticantes de musculação iniciantes e intermediários que treinam sem acompanhamento constante e querem uma referência rápida de exercícios e volume de treino.

## 5. Funcionalidades (visão geral)

1. Cadastro, login e logout **locais** (e-mail e senha guardados no aparelho).
2. Seleção do objetivo (primeira tela após o login).
3. Seleção do grupo muscular do dia, com aviso de descanso.
4. Lista de exercícios sugeridos, com a prescrição, pull-to-refresh, "atualizado há…" e aviso de descanso.
5. Detalhes do exercício: imagem, nome, descrição, músculos, equipamentos, prescrição e vídeo (quando existir).
6. "Meu treino": filtro por status, marcação como feito e registro do esforço percebido (1 a 5).
7. Indicador de modo offline.
8. Notificação diária lembrando de treinar.
9. Tela "Sobre", com a atribuição à wger (CC-BY-SA) e o aviso de saúde.

## 6. Requisitos funcionais (RF)

| Código | Requisito | Prioridade |
|---|---|---|
| RF01 | O app deve permitir criar uma conta local com e-mail e senha (com confirmação de senha). | Alta |
| RF02 | O app deve permitir entrar com e-mail e senha de uma conta local já criada. | Alta |
| RF03 | O app deve manter a sessão: depois do login, abrir direto na seleção de objetivo, com ou sem internet. | Alta |
| RF04 | O app deve permitir sair da conta (logout), voltando para a tela de login. | Alta |
| RF05 | O app deve permitir escolher um objetivo: Hipertrofia, Força, Resistência ou Condicionamento. | Alta |
| RF06 | O app deve permitir escolher o grupo muscular do dia entre as categorias da wger. | Alta |
| RF07 | O app deve listar os exercícios sugeridos do grupo, filtrados pelo objetivo e com a prescrição aplicada. | Alta |
| RF08 | A lista deve permitir atualizar com pull-to-refresh e mostrar quando foi atualizada ("atualizado há…"). | Média |
| RF09 | O app deve avisar quando o usuário concluiu exercícios do mesmo grupo muscular no dia anterior, sugerindo descanso, sem impedir a escolha. | Média |
| RF10 | O app deve exibir os detalhes do exercício: imagem, nome, descrição, músculos, equipamentos e prescrição. | Alta |
| RF11 | O app deve exibir o vídeo de execução (fonte: wger) apenas quando o exercício tiver vídeo. | Média |
| RF12 | O app deve permitir adicionar um exercício ao "Meu treino", salvando a prescrição do objetivo daquele momento. | Alta |
| RF13 | O "Meu treino" deve listar os exercícios do usuário, com filtro por status (todos, planejados, feitos). | Alta |
| RF14 | O app deve permitir marcar um exercício do "Meu treino" como feito. | Alta |
| RF15 | O app deve permitir registrar o esforço percebido (1 a 5) de um exercício feito. | Média |
| RF16 | O app deve indicar quando está sem internet. | Alta |
| RF17 | O app deve enviar uma notificação diária lembrando o usuário de treinar. | Média |
| RF18 | O app deve ter a tela "Sobre", com o crédito à wger, a licença CC-BY-SA e o aviso de saúde. | Alta |

## 7. Requisitos não funcionais (RNF)

| Código | Requisito |
|---|---|
| RNF01 | **Offline First:** o Room é a única fonte de verdade; a UI observa o banco e a rede só atualiza o banco. Login, objetivo, grupo, lista e detalhes já carregados e "Meu treino" funcionam sem internet. |
| RNF02 | **Arquitetura:** Clean Architecture em um módulo (`presentation → domain ← data`); o domain é Kotlin puro (sem `android.*`, Retrofit, Room, Media3 ou Koin). |
| RNF03 | **Segurança:** a senha nunca é gravada em texto puro; usa-se hash PBKDF2WithHmacSHA256 com sal aleatório por usuário. |
| RNF04 | **Stack:** Kotlin, Compose + Material 3, Navigation type-safe, Koin, Coroutines/Flow, Retrofit + OkHttp + kotlinx.serialization, Room (KSP), Coil, Media3, WorkManager. Sem Hilt, RxJava, LiveData, XML de layout, Gson ou Firebase. |
| RNF05 | **Compatibilidade:** `minSdk 24` (Android 7.0); `compileSdk`/`targetSdk` 37. Canal de notificação só na API 26+ e permissão `POST_NOTIFICATIONS` só na API 33+. |
| RNF06 | **Erros:** toda exceção previsível (rede, timeout, HTTP, JSON, banco, autenticação) vira `AppError` dentro de `AppResult`; mensagens ao usuário em português, em `strings.xml`. |
| RNF07 | **Estados de tela:** todo ViewModel expõe um `StateFlow<UiState>` imutável com carregando, sucesso, vazio e erro. |
| RNF08 | **Desempenho e memória:** I/O em `Dispatchers.IO` injetado; ViewModels sem `Context`; ExoPlayer liberado com `DisposableEffect`; coleta com `collectAsStateWithLifecycle`; `key` estável em listas. |
| RNF09 | **Identidade visual:** cores, tipografia e formas apenas do tema (`docs/identidade_visual.md`), temas claro e escuro, contraste AA, previews `@PreviewLightDark`. |
| RNF10 | **Testes:** regras, casos de uso, mappers, repositórios e ViewModels com testes unitários JVM (JUnit 4, MockK, coroutines-test, Turbine). |
| RNF11 | **Licença:** o conteúdo de exercícios (CC-BY-SA 4.0) é creditado à wger na tela "Sobre" e no README. |

### Limitações conhecidas (aceitas nesta especificação)

- **Conta só no aparelho:** como a autenticação é local, a conta e o "Meu treino" não passam para outro aparelho nem sobrevivem à desinstalação. Não há recuperação de senha.
- **Primeiro acesso a um grupo exige internet:** sem cache, o grupo mostra o estado de erro com "Tentar novamente".
- **Idioma do conteúdo:** nem todo exercício tem tradução em português; nesses casos o nome e a descrição aparecem em inglês. Os nomes de músculos e equipamentos são traduzidos pelo app (lista fixa abaixo).
- **Vídeos:** poucos exercícios da wger têm vídeo (ex.: 5 de 90 em Peito; nenhum em Cardio), e o vídeo exige internet.
- **Prescrição geral:** as faixas são referências gerais, não prescrição individual.

## 8. Dados de referência da API wger (consultados em 24/11/2026)

Consultas feitas na API real (`https://wger.de/api/v2/`), sem autenticação.

### 8.1 Categorias (`exercisecategory/`) → grupos musculares

| ID | Nome na API | Nome no app | Exercícios (24/11/2026) |
|---|---|---|---|
| 10 | Abs | Abdômen | 118 |
| 8 | Arms | Braços | 168 |
| 12 | Back | Costas | 175 |
| 14 | Calves | Panturrilhas | 15 |
| 15 | Cardio | Cardio | 53 |
| 11 | Chest | Peito | 90 |
| 9 | Legs | Pernas | 235 |
| 13 | Shoulders | Ombros | 125 |

### 8.2 Equipamentos (`equipment/`)

| ID | Nome na API | Nome no app |
|---|---|---|
| 1 | Barbell | Barra |
| 2 | SZ-Bar | Barra W |
| 3 | Dumbbell | Halteres |
| 4 | Gym mat | Colchonete |
| 5 | Swiss Ball | Bola suíça |
| 6 | Pull-up bar | Barra fixa |
| 7 | none (bodyweight exercise) | Peso corporal |
| 8 | Bench | Banco |
| 9 | Incline bench | Banco inclinado |
| 10 | Kettlebell | Kettlebell |
| 11 | Resistance band | Elástico |
| 12 | Cable machine | Polia |

### 8.3 Idiomas (`language/`)

| ID | Código | Uso |
|---|---|---|
| 7 | pt | Idioma preferido para nome e descrição |
| 2 | en | Idioma de reserva |

### 8.4 Músculos (`muscle/`) — tradução exibida no app

| ID | Nome científico | Nome no app |
|---|---|---|
| 1 | Biceps brachii | Bíceps |
| 2 | Anterior deltoid | Deltoide anterior |
| 3 | Serratus anterior | Serrátil anterior |
| 4 | Pectoralis major | Peitoral maior |
| 5 | Triceps brachii | Tríceps |
| 6 | Rectus abdominis | Reto abdominal |
| 7 | Gastrocnemius | Gastrocnêmio |
| 8 | Gluteus maximus | Glúteo máximo |
| 9 | Trapezius | Trapézio |
| 10 | Quadriceps femoris | Quadríceps |
| 11 | Biceps femoris | Posterior de coxa |
| 12 | Latissimus dorsi | Latíssimo do dorso |
| 13 | Brachialis | Braquial |
| 14 | Obliquus externus abdominis | Oblíquo externo |
| 15 | Soleus | Sóleo |

Um músculo com ID fora da tabela aparece com o nome vindo da API.

### 8.5 Estrutura relevante de `exerciseinfo/`

Filtro `?category={id}&limit=100&offset=n`. Campos usados: `id`, `category.id`, `muscles[]`, `muscles_secondary[]`, `equipment[]`, `images[]` (`image`, `is_main`, `thumbnails`), `videos[]` (`video`, `is_main`), `translations[]` (`language`, `name`, `description` em HTML). A descrição vem em HTML e é convertida para texto simples.

## 9. Regras de negócio (RN)

| Código | Regra |
|---|---|
| RN01 | **Prescrição por objetivo** (faixas de referência): Hipertrofia 3–4 séries × 8–12 repetições, 60–90 s de descanso; Força 3–5 × 3–6, 120–180 s; Resistência 2–3 × 15–20, 30–60 s; Condicionamento 2–3 × 12–15, 30 s. |
| RN02 | **Seleção — Hipertrofia:** todos os exercícios do grupo escolhido, com qualquer equipamento (inclusive sem equipamento). |
| RN03 | **Seleção — Força:** apenas exercícios do grupo que usam **Barra (ID 1)** ou **Halteres (ID 3)**. |
| RN04 | **Seleção — Resistência:** todos os exercícios do grupo, incluindo os de peso corporal (equipamento ID 7 ou sem equipamento informado). |
| RN05 | **Seleção — Condicionamento:** exercícios do grupo escolhido **mais** os da categoria **Cardio (ID 15)**. |
| RN06 | **Nome por idioma:** nome e descrição em português (idioma 7) quando houver; senão, em inglês (idioma 2). Exercício sem nome em português nem em inglês é descartado. |
| RN07 | **Ordenação:** exercícios com imagem vêm antes dos sem imagem; dentro de cada bloco, ordem alfabética do nome. |
| RN08 | **Sem duplicados:** um exercício aparece uma vez só na lista (mesmo ID ou mesmo nome, ignorando maiúsculas, acentos e espaços extras; fica o primeiro após a ordenação). |
| RN09 | **Validade do cache:** o cache de cada grupo vale 24 horas a partir da última atualização bem-sucedida. Exatamente 24 h já é expirado. |
| RN10 | **Aviso de descanso:** se o usuário concluiu (`DONE`) algum exercício do mesmo grupo muscular no **dia anterior** (data local do aparelho), o app sugere descanso. O aviso não impede a escolha. |
| RN11 | **Sem repetição no treino:** um exercício não pode ser adicionado duas vezes ao "Meu treino" do mesmo usuário. |
| RN12 | **Prescrição congelada:** ao adicionar ao "Meu treino", a prescrição do objetivo daquele momento é salva junto e não muda depois. |
| RN13 | **Status:** `PLANNED` → `DONE`. Não há volta de `DONE` para `PLANNED`. Ao marcar como feito, grava-se a data/hora da conclusão. |
| RN14 | **Esforço percebido:** valor inteiro de 1 a 5, permitido apenas para exercício `DONE`; fora do intervalo é rejeitado. Pode ser alterado depois. |
| RN15 | **Credenciais:** e-mail em formato válido (normalizado para minúsculas, sem espaços nas pontas); senha com no mínimo 6 caracteres; no cadastro, a confirmação deve ser igual à senha; não pode haver duas contas com o mesmo e-mail. |
| RN16 | **Lembrete diário:** a notificação é agendada para as **18:00** (horário local). Se já passou das 18:00, o próximo lembrete é no dia seguinte. |
| RN17 | **Tempo injetável:** regras que dependem de data/hora recebem o horário por um `TimeProvider`, para que os testes controlem o tempo. |

## 10. Critérios de aceite (CA)

- **CA01 (RF01, RN15):** Dado um e-mail novo e senha válida com confirmação igual, quando o usuário toca em "Criar conta", então a conta é criada e ele entra no app.
- **CA02 (RF01, RN15):** Dado um e-mail já cadastrado, quando o usuário tenta criar a conta, então aparece "Já existe uma conta com este e-mail."
- **CA03 (RF01, RN15):** Dado senha com menos de 6 caracteres ou confirmação diferente, quando tenta criar a conta, então o campo mostra o erro e nada é gravado.
- **CA04 (RF02):** Dado e-mail ou senha incorretos, quando toca em "Entrar", então aparece "E-mail ou senha incorretos." sem informar qual dos dois.
- **CA05 (RF03):** Dado um usuário logado, quando o app é fechado e aberto sem internet, então ele abre na seleção de objetivo.
- **CA06 (RF04):** Dado um usuário logado, quando toca em "Sair", então volta ao login e a sessão é apagada.
- **CA07 (RF07, RN01):** Dado o objetivo Força, quando a lista abre, então cada item mostra "3–5 × 3–6 · 120–180 s".
- **CA08 (RF07, RN03):** Dado o objetivo Força, então só aparecem exercícios com Barra ou Halteres.
- **CA09 (RF07, RN05):** Dado o objetivo Condicionamento e o grupo Peito, então a lista inclui exercícios de Peito e de Cardio.
- **CA10 (RN06):** Dado um exercício com tradução em português, então o nome aparece em português; sem ela, em inglês; sem nenhuma das duas, ele não aparece.
- **CA11 (RN07, RN08):** Dada uma lista com e sem imagens e com nomes repetidos, então os com imagem vêm primeiro e não há repetição.
- **CA12 (RN09, RNF01):** Dado um cache com menos de 24 h, quando o grupo é aberto, então não há requisição; com 24 h ou mais e internet, os dados são buscados e o Room é atualizado.
- **CA13 (RNF01):** Dado um cache existente e a requisição falha, então os dados continuam na tela e aparece um Snackbar com o erro.
- **CA14 (RNF01):** Dado nenhum cache e a requisição falha, então aparece o estado de erro com "Tentar novamente".
- **CA15 (RF08):** Dado internet, quando o usuário puxa a lista, então a API é consultada mesmo com cache válido; sem internet, aparece "Sem internet: não foi possível atualizar." e o cache é mantido.
- **CA16 (RF09, RN10):** Dado um exercício de Pernas concluído ontem, quando o usuário escolhe Pernas, então aparece o aviso de descanso, e ele ainda consegue continuar.
- **CA17 (RF10):** Dado um exercício já carregado, quando os detalhes são abertos sem internet, então nome, descrição, músculos, equipamentos e prescrição aparecem; a imagem aparece se estiver no cache do Coil ou mostra o espaço reservado.
- **CA18 (RF11):** Dado um exercício sem vídeo, então aparece "Este exercício ainda não tem vídeo de execução."; com vídeo e sem internet, aparece "O vídeo precisa de internet. A descrição e a imagem continuam disponíveis."
- **CA19 (RF12, RN11, RN12):** Dado um exercício fora do treino, quando o usuário toca em "Adicionar ao treino", então ele aparece no "Meu treino" com a prescrição do objetivo atual; tocar de novo não duplica (o botão indica "Já está no treino").
- **CA20 (RF13):** Dado o filtro "Feitos", então só aparecem exercícios `DONE`.
- **CA21 (RF14, RN13):** Dado um exercício planejado, quando toca em "Marcar como feito", então ele passa a `DONE` e não volta a `PLANNED`.
- **CA22 (RF15, RN14):** Dado um exercício `DONE`, quando o usuário escolhe um esforço de 1 a 5, então o valor é salvo; para `PLANNED`, o seletor não aparece e o domain rejeita a operação.
- **CA23 (RF16):** Dado o aparelho sem internet, então aparece a faixa "Você está sem internet" com o ícone de nuvem cortada.
- **CA24 (RF17, RN16):** Dado o app instalado e a permissão concedida (API 33+), então um lembrete é agendado diariamente às 18:00.
- **CA25 (RF18):** Dado a tela "Sobre", então aparecem o crédito à wger (https://wger.de), a licença CC-BY-SA 4.0 e o aviso de saúde.

## 11. Fora do escopo

Contas e treinos da wger, sincronização na nuvem, Firebase, recuperação de senha, planos de dieta, busca livre por texto, login social e prescrição individual.

## 12. Decisões registradas

| Data | Decisão | Motivo |
|---|---|---|
| 24/11/2026 | Firebase removido; autenticação local com Room + PBKDF2. | Decisão do responsável: o app não deve depender de serviço externo para login. |
| 24/11/2026 | Força considera apenas Barra (1) e Halteres (3); Barra W (2) fica de fora. | Seguir literalmente a regra "barra ou halteres" do enunciado; a Barra W é um equipamento específico para isolamento. |
| 24/11/2026 | Exercício sem equipamento informado conta como peso corporal. | Na API, 28 de 53 exercícios de Cardio vêm com `equipment` vazio. |
| 24/11/2026 | Duplicado = mesmo ID ou mesmo nome normalizado. | A wger tem variações cadastradas com o mesmo nome. |
| 24/11/2026 | Lembrete diário fixo às 18:00. | O escopo não prevê tela de configuração de horário. |
| 24/11/2026 | Nomes de músculos e equipamentos traduzidos pelo app por ID. | A API só traz esses nomes em inglês/latim. |
