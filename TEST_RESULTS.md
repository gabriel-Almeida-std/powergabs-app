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
