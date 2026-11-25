# Resultados dos testes — PowerGabs

- **Comando:** `./gradlew testDebugUnitTest`
- **Relatório HTML:** `app/build/reports/tests/testDebugUnitTest/index.html`
- Cada entrada abaixo é uma execução real, registrada ao fim da tarefa correspondente.

---

## 2026-11-24 20:35 — T07: regras de negócio do domain

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 48 |
| Aprovados | 48 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Classes: `PrescriptionRulesTest` (RN01), `ExerciseSelectionRulesTest` (RN02–RN05), `ExerciseCatalogRulesTest` (RN06–RN08), `CachePolicyTest` (RN09), `RestWarningRulesTest` (RN10), `WorkoutRulesTest` (RN11–RN14), `CredentialsRulesTest` (RN15), `ReminderRulesTest` (RN16).

## 2026-11-24 20:37 — T08: casos de uso do domain

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 76 |
| Aprovados | 76 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Novas classes: `AuthUseCasesTest` (RF01, RF02, RN15), `ExerciseUseCasesTest` (RF07, RF08, RF10, RN09), `WorkoutUseCasesTest` (RF09, RF12–RF15, RN10–RN14).

## 2026-11-24 20:38 — T09: API Retrofit e DTOs

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 79 |
| Aprovados | 79 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Nova classe: `ExerciseInfoDtoTest` (desserialização de JSON real reduzido, campos ausentes e JSON inválido).

## 2026-11-24 20:41 — T11: mappers

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 97 |
| Aprovados | 97 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Novas classes: `ExerciseMapperTest` (RN06, imagem/vídeo principal), `HtmlTextTest` (HTML → texto), `WorkoutMapperTest` (ida e volta da entidade).

## 2026-11-24 20:42 — T12: repositórios Offline First

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 110 |
| Aprovados | 110 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Novas classes: `ExerciseRepositoryImplTest` (paginação tudo-ou-nada, transação, conversão de IOException/HttpException/SerializationException/banco), `WorkoutRepositoryImplTest` (inserção, duplicado, leitura e datas de conclusão).

## 2026-11-24 20:43 — T13: autenticação local

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 124 |
| Aprovados | 124 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Novas classes: `PasswordHasherTest` (RNF03), `AuthRepositoryImplTest` (RF01–RF04, CA02, CA04, CA06).

## 2026-11-24 20:47 — T14: navegação, raiz e componentes

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 132 |
| Aprovados | 132 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Novas classes: `MainViewModelTest` (RF03, RF04, RF16), `FormattersTest` (badge de prescrição CA07, "atualizado há…", mensagens de erro).

## 2026-11-24 20:53 — T15: telas de login e cadastro

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 139 |
| Aprovados | 139 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Nova classe: `AuthViewModelsTest` (CA01–CA04). Verificado também no emulador: erros de campo no cadastro e entrada no app após criar a conta.

## 2026-11-24 20:57 — T16: tela de objetivo

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 142 |
| Aprovados | 142 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Nova classe: `GoalViewModelTest` (RF04, RF05). Verificado no emulador: grade 2×2, card selecionado com borda `primary` e pedido de permissão de notificação (API 37).

## 2026-11-24 21:00 — T17: tela de grupo muscular

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 146 |
| Aprovados | 146 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Nova classe: `MuscleGroupViewModelTest` (RF06, RF09, CA16).

## 2026-11-24 21:04 — T18: lista de exercícios sugeridos

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 154 |
| Aprovados | 154 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Dois testes novos falharam na primeira execução por sincronização do próprio teste (`expectMostRecentItem` sem `advanceUntilIdle`); corrigidos no teste, sem mudança no código de produção. |

Nova classe: `ExerciseListViewModelTest` (CA12–CA16). Verificado no emulador com a API real: Força + Peito = 25 exercícios, nomes em português, imagem primeiro e badge "3–5 × 3–6 · 120–180 s".

## 2026-11-24 21:13 — T19: detalhes do exercício e vídeo

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 160 |
| Aprovados | 160 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | No emulador, o vídeo falhou com `c2.goldfish.h264.decoder` (erro de MediaCodec); corrigido com um `MediaCodecSelector` que prefere o decodificador de software. Após a correção, o vídeo do Supino reto reproduziu. |

Nova classe: `ExerciseDetailsViewModelTest` (CA17–CA19). Verificado no emulador: detalhes em português, chips de músculos/equipamentos, vídeo e "Adicionar ao treino" → "Já está no treino".

## 2026-11-24 21:15 — T20: Meu treino

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 167 |
| Aprovados | 167 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Nenhuma |

Nova classe: `WorkoutViewModelTest` (CA20–CA22). Verificado no emulador: marcar como feito e registrar esforço 4 de 5.

## 2026-11-24 21:24 — T22: lembrete diário

| Item | Valor |
|---|---|
| Comando | `./gradlew testDebugUnitTest` |
| Total | 168 |
| Aprovados | 168 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Teste manual no emulador: o WorkManager recusou a execução forçada antes do horário (comportamento esperado). Para ver a notificação, um build temporário com atraso zero (não commitado) foi instalado; a notificação "Hora de treinar!" apareceu e o código voltou ao atraso calculado. |

Nova classe: `ReminderSchedulerTest` (CA24: KEEP, intervalo de 1 dia e atraso até as 18:00).

---

## 2026-11-24 21:31 — T23: validação final (execução limpa)

| Item | Valor |
|---|---|
| Comando | `./gradlew clean testDebugUnitTest` |
| Total | 168 |
| Aprovados | 168 |
| Falhas | 0 |
| Ignorados | 0 |
| Correções aplicadas | Durante a validação no emulador: ícones da barra de status ilegíveis sobre a faixa offline (commit `fix: ajusta cor dos icones da barra de status com a faixa offline`) e crédito das fontes ajustado ao texto da OFL. |

### Testes por classe

| Camada | Classe | Testes |
|---|---|---|
| domain/rule | `PrescriptionRulesTest` | 4 |
| domain/rule | `ExerciseSelectionRulesTest` | 8 |
| domain/rule | `ExerciseCatalogRulesTest` | 9 |
| domain/rule | `CachePolicyTest` | 4 |
| domain/rule | `RestWarningRulesTest` | 5 |
| domain/rule | `WorkoutRulesTest` | 8 |
| domain/rule | `CredentialsRulesTest` | 7 |
| domain/rule | `ReminderRulesTest` | 3 |
| domain/usecase | `AuthUseCasesTest` | 6 |
| domain/usecase | `ExerciseUseCasesTest` | 9 |
| domain/usecase | `WorkoutUseCasesTest` | 13 |
| data | `ExerciseInfoDtoTest` | 3 |
| data | `ExerciseMapperTest` | 10 |
| data | `HtmlTextTest` | 5 |
| data | `WorkoutMapperTest` | 3 |
| data | `ExerciseRepositoryImplTest` | 8 |
| data | `WorkoutRepositoryImplTest` | 5 |
| data | `PasswordHasherTest` | 7 |
| data | `AuthRepositoryImplTest` | 7 |
| data | `ReminderSchedulerTest` | 1 |
| presentation | `MainViewModelTest` | 2 |
| presentation | `FormattersTest` | 6 |
| presentation | `AuthViewModelsTest` | 7 |
| presentation | `GoalViewModelTest` | 3 |
| presentation | `MuscleGroupViewModelTest` | 4 |
| presentation | `ExerciseListViewModelTest` | 8 |
| presentation | `ExerciseDetailsViewModelTest` | 6 |
| presentation | `WorkoutViewModelTest` | 7 |
| **Total** | | **168** |

### Critérios de aceite → verificação

Legenda: **T** = teste unitário · **E** = evidência (captura em `docs/evidencias/`) · **M** = verificação manual no emulador (Medium Phone, API 37).

| CA | Descrição resumida | Verificação |
|---|---|---|
| CA01 | Cadastro com dados válidos entra no app | T (`AuthUseCasesTest`, `AuthRepositoryImplTest`) · M |
| CA02 | E-mail já cadastrado é recusado | T (`AuthRepositoryImplTest`, `AuthViewModelsTest`) |
| CA03 | Senha curta ou confirmação diferente | T (`CredentialsRulesTest`, `AuthViewModelsTest`) · E 02 |
| CA04 | Credenciais erradas → mensagem genérica | T (`AuthRepositoryImplTest`, `AuthViewModelsTest`) · E 19 |
| CA05 | Sessão mantida, abre sem internet | T (`MainViewModelTest`) · E 11 |
| CA06 | Logout volta ao login | T (`GoalViewModelTest`, `AuthRepositoryImplTest`) · E 19 |
| CA07 | Força mostra "3–5 × 3–6 · 120–180 s" | T (`PrescriptionRulesTest`, `FormattersTest`) · E 05 |
| CA08 | Força só com Barra/Halteres | T (`ExerciseSelectionRulesTest`, `ExerciseUseCasesTest`) · E 05 |
| CA09 | Condicionamento inclui Cardio | T (`ExerciseSelectionRulesTest`, `ExerciseUseCasesTest`) |
| CA10 | Nome em português, senão inglês, senão descartado | T (`ExerciseCatalogRulesTest`, `ExerciseMapperTest`) · E 05 |
| CA11 | Imagem primeiro e sem duplicados | T (`ExerciseCatalogRulesTest`) · E 05 |
| CA12 | Cache < 24 h não faz requisição; expirado é renovado | T (`CachePolicyTest`, `ExerciseUseCasesTest`) · M (troca de objetivo sem nova busca) · E 21, 22 |
| CA13 | Falha com cache → dados + Snackbar | T (`ExerciseListViewModelTest`) |
| CA14 | Falha sem cache → erro + "Tentar novamente" | T (`ExerciseListViewModelTest`) · E 15 |
| CA15 | Pull-to-refresh força; offline avisa e mantém cache | T (`ExerciseUseCasesTest`, `ExerciseListViewModelTest`) · E 13 |
| CA16 | Aviso de descanso (treinou ontem) sem bloquear | T (`RestWarningRulesTest`, `WorkoutUseCasesTest`, `MuscleGroupViewModelTest`, `ExerciseListViewModelTest`) · E 20, 21 |
| CA17 | Detalhes offline | T (`ExerciseDetailsViewModelTest`) · E 14 |
| CA18 | Vídeo indisponível / offline | T (`ExerciseDetailsViewModelTest`) · E 07, 14 |
| CA19 | Adicionar ao treino sem duplicar, com prescrição | T (`WorkoutRulesTest`, `WorkoutUseCasesTest`, `ExerciseDetailsViewModelTest`) · M |
| CA20 | Filtro "Feitos" | T (`WorkoutRulesTest`, `WorkoutUseCasesTest`, `WorkoutViewModelTest`) |
| CA21 | Marcar como feito, sem volta | T (`WorkoutRulesTest`, `WorkoutUseCasesTest`) · E 08 |
| CA22 | Esforço 1–5 só para feitos | T (`WorkoutRulesTest`, `WorkoutUseCasesTest`, `WorkoutViewModelTest`) · E 08 |
| CA23 | Faixa "sem internet" | T (`MainViewModelTest`) · E 11–16 |
| CA24 | Lembrete diário às 18:00 | T (`ReminderRulesTest`, `ReminderSchedulerTest`) · E 10 · M (job agendado no JobScheduler) |
| CA25 | Tela "Sobre" com wger, licença e aviso de saúde | E 09 |

### Observações da verificação manual

- A API real da wger foi consultada no emulador: Força + Peito = 25 exercícios; Hipertrofia + Peito = 89 (90 na API, 1 duplicado removido); Hipertrofia + Pernas = 235 (3 páginas).
- Aviso de descanso (CA16) e cache expirado (CA12): o relógio do emulador foi adiantado 1 dia (`settings put global auto_time 0` + `cmd alarm set-time`). Ao escolher Peito, apareceu "Você treinou Peito ontem…" sem bloquear o botão; a lista mostrou primeiro o cache ("Atualizado há 1 dia") e, em seguida, o renovou ("Atualizado agora"). O horário automático foi restaurado depois.
- A notificação foi exibida com um build temporário de atraso zero (não commitado), porque o WorkManager recusa execução forçada antes do horário agendado.
