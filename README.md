<p align="center">
  <img src="docs/assets/powergabs_logo_escuro.png" alt="PowerGabs" width="360">
</p>

# PowerGabs

App Android que monta sugestões de treino de acordo com o **objetivo** do usuário (Hipertrofia, Força, Resistência ou Condicionamento) e o **grupo muscular do dia**, usando os exercícios da API pública [wger](https://wger.de).

> 🚧 Em desenvolvimento. Este README descreve apenas o que já está implementado e é atualizado a cada tarefa concluída (ver `docs/03-tarefas.md`).

## Estado atual

- Projeto configurado com a stack definida em `docs/02-planejamento.md` (Compose, Koin, Room, Retrofit, Coil, Media3, WorkManager).

## Documentação (Spec-Driven Development)

| Documento | Conteúdo |
|---|---|
| [`docs/01-especificacao.md`](docs/01-especificacao.md) | Proposta, requisitos, regras de negócio e critérios de aceite |
| [`docs/02-planejamento.md`](docs/02-planejamento.md) | Arquitetura, stack, banco, Offline First e testes |
| [`docs/03-tarefas.md`](docs/03-tarefas.md) | Tarefas em ordem, com o andamento |
| [`docs/identidade_visual.md`](docs/identidade_visual.md) | Tema, cores, tipografia e ícones |

## Como executar

Requisitos: Android Studio recente (JDK 21+ embutido), SDK Android 37.

```bash
./gradlew assembleDebug        # gera o APK de depuração
./gradlew testDebugUnitTest    # roda os testes unitários
```

Não é preciso chave de API: a wger permite leitura pública.
