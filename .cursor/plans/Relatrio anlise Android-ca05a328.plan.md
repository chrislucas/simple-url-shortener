<!-- ca05a328-d8a7-41a5-890b-170fee4777cc -->
---
todos:
  - id: "decide-doc-target"
    content: "Confirmar com o utilizador: atualizar RELATORIO_ANALISE_PROJETO.md vs novo .md em docs/ (sem apagar ficheiros)."
    status: pending
  - id: "draft-sections"
    content: "Redigir todas as secções do prompt com evidências do código (VM, Screen, Nav, tests, value class)."
    status: pending
  - id: "refactor-test-plan"
    content: "Incluir plano de refactoring e plano paralelo de alteração de testes unitários/instrumentados."
    status: pending
  - id: "score-rationale"
    content: "Fechar nota 0–10 com critérios e justificativa objetiva."
    status: pending
isProject: false
---
# Plano: relatório de análise Android (só `docs/*.md`)

## Contexto do repositório

- **App**: módulo único `app/`, Compose + MVVM.
- **ViewModel**: [`app/src/main/java/com/br/urlshortener/viewmodel/UrlShortenerViewModel.kt`](app/src/main/java/com/br/urlshortener/viewmodel/UrlShortenerViewModel.kt) — expõe **vários** `StateFlow` (`textFieldContent`, `urls`, `uiState`, `urlShortener`) e um `SharedFlow` (`navigationEvent`); `MutableStateFlow` privado + `StateFlow` público por propriedade.
- **Tela principal**: [`app/src/main/java/com/br/urlshortener/ui/screen/ShortenerUrlScreen.kt`](app/src/main/java/com/br/urlshortener/ui/screen/ShortenerUrlScreen.kt) — composable **`UrlShortenerScreen`** (nome usado em [`UrlShortenerApp.kt`](app/src/main/java/com/br/urlshortener/ui/screen/UrlShortenerApp.kt)); overlay de loading/erro via `when (uiState)`; formulário/lista sempre compostos em `UrlShortenerForm`.
- **Estado selado**: [`app/src/main/java/com/br/urlshortener/ui/state/UrlShortenerUIState.kt`](app/src/main/java/com/br/urlshortener/ui/state/UrlShortenerUIState.kt) — inclui `Success<T>` **quase não usado** no fluxo principal do ViewModel (apenas em testes).
- **Navegação**: [`UrlShortenerApp.kt`](app/src/main/java/com/br/urlshortener/ui/screen/UrlShortenerApp.kt) — `NavHost`, `NavRoute` enum, `LaunchedEffect` a consumir `navigationEvent` para navegação/snackbar.
- **Value class**: [`app/src/main/java/com/br/urlshortener/domain/model/UrlShortener.kt`](app/src/main/java/com/br/urlshortener/domain/model/UrlShortener.kt) — `@JvmInline value class UrlShortener` com factory `createToPostUrl` / `createFromGetResult`.
- **Testes**: unitários em `app/src/test` (ViewModel, UI state, HttpClient, `UrlShortener`); instrumentados em `app/src/androidTest` (telas e componentes).
- **Documentação existente**: já existe [`docs/RELATORIO_ANALISE_PROJETO.md`](docs/RELATORIO_ANALISE_PROJETO.md) com estrutura semelhante. **Não apagar ficheiros** — decidir contigo se o entregável é **atualizar** esse ficheiro ou **criar** um novo (ex.: `docs/ANALISE_ANDROID_PROMPT.md`) para cumprir o prompt sem sobrescrever histórico.

## Estrutura obrigatória do relatório (conteúdo a redigir)

1. **Arquitetura** — MVVM + camadas `ui` / `viewmodel` / `domain` / `data.remote`; fluxo evento → ViewModel → `RepositoryResult`; efeitos via `SharedFlow`.
2. **Padrões de projeto e onde aparecem** — Repository (`UrlShortenerRepository` + `UrlShortenerRepositoryDefault` + [`SafeRepository`](app/src/main/java/com/br/urlshortener/domain/repository/SafeRepository.kt)); Factory do ViewModel (`FACTORY`); sealed classes para estado/eventos; possível menção a Strategy/Adapter conforme uso em HTTP/DTOs.
3. **Secção dedicada: ViewModels, Repositories, UI** — pontos fortes/fracos e sugestões (ex.: fábrica acoplada a `HttpClient`/`BuildConfig`; lista como `Set<UrlResult>`; overlays vs. form sempre visível).
4. **Pontos negativos e sugestões de mudança** — lista acionável (incluir, se relevante: `UrlShortenerUIState.Success` não usado no VM; possível duplicação de imports `delay` em `UrlShortenerViewModel` — linhas 18–28 — como risco de manutenção).
5. **Navegação** — avaliar `NavRoute` por nome de enum como rota, partilha do mesmo `ViewModel` no `UrlShortenerApp`, `LaunchedEffect(Unit)` + `collect` único, tratamento de `urlShortener` na rota de detalhe, `popBackStack` + `putUiOnIdle`.
6. **Refactoring de ViewModel: modelo único de estado** — propor um `data class` (ou sealed único) que una texto, lista, detalhe opcional, flags de loading/erro e mensagens, reduzindo leituras paralelas; discutir migração de `navigationEvent` (manter `SharedFlow` ou mapear efeitos a partir do estado).
7. **Secção específica: muitos `StateFlow` em `UrlShortenerViewModel`** — contar e explicar papéis (`textFieldContent`, `urls`, `uiState`, `urlShortener`); alternativas: estado único, `combine` para derivados, menos fontes de verdade; **MutableStateFlow vs StateFlow na mesma VM**: padrão recomendado (privado mutável / público imutável) e quando isso se multiplica demais.
8. **Secção específica: `UrlShortenerScreen` e visibilidade por `uiState`** — descrever o `when` atual e o facto de `Idle`/`Success` não controlarem o formulário; sugerir `Box` + `AnimatedVisibility`, ou estado único com campos `showLoading` / `errorMessage`, ou derivar UI de um único `collectAsState()`.
9. **Testes** — cobertura atual (ficheiros em `test` e `androidTest`); lacunas (repository isolado, navegação, `UrlShortenerApp`, testes de snapshot/semântica); **plano de modificação de testes** alinhado ao refactoring (ex.: asserts passando de múltiplos flows para um único estado; testes de tela com novo mock de estado).
10. **Value class** — `UrlShortener`: construtor privado, factories, validação; o que está bem e o que melhorar (ex.: regex/local de validação, semântica “URL encurtada local” vs API).
11. **Nota 0–10** — critérios explícitos (arquitetura, consistência de estado, testes, navegação, qualidade Compose) e justificativa em texto.
12. **Plano de execução de refactoring** — fases curtas (estado único → ajustar UI → ajustar testes); **subsecção obrigatória**: plano de atualização dos testes unitários e instrumentados.

## Entregável e permissões

- **Local**: apenas [`docs/`](docs/) para criar/editar `.md`.
- **Não** alterar `.kt` nem outros tipos de ficheiro nesta fase; **não** apagar `RELATORIO_ANALISE_PROJETO.md` (ou qualquer outro) sem a tua permissão explícita.
- Se optares por **novo** ficheiro, o relatório pode referenciar o existente para evitar duplicação total; se optares por **atualizar**, incorporar as secções do prompt que ainda não estão explícitas (especialmente navegação, testes, `UrlShortenerScreen`/uiState, plano de testes para refactor).

## Diagrama sugerido no relatório (opcional)

Um fluxo mermaid simples: `UIEvent` → `UrlShortenerViewModel` → `Repository` / `StateFlow`+`SharedFlow` → `Compose` / `NavHost` — útil para a secção de arquitetura.
