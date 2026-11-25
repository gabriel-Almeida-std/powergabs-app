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
