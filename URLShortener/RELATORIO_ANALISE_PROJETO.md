# Relatório de Análise do Projeto URL Shortener

## Índice
1. [Avaliação da Arquitetura](#1-avaliação-da-arquitetura)
2. [Padrões de Projeto Identificados](#2-padrões-de-projeto-identificados)
3. [Análise do UrlShortenerViewModel](#3-análise-do-urlshortenerviewmodel)
4. [Plano de Refatoração com Navigation Component](#4-plano-de-refatoração-com-navigation-component)
5. [Pontos Positivos](#5-pontos-positivos)
6. [Pontos Negativos](#6-pontos-negativos)
7. [Plano de Refatoração Geral](#7-plano-de-refatoração-geral)

---

## 1. Avaliação da Arquitetura

### 1.1 Padrão Arquitetural: MVVM (Model-View-ViewModel)

O projeto utiliza a arquitetura **MVVM** com Jetpack Compose, organizada em camadas:

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (View)                         │
│  ui.screen, ui.component, ui.state, ui.event, ui.theme       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   ViewModel Layer                            │
│  UrlShortenerViewModel                                       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Domain Layer                               │
│  domain.model, domain.repository                             │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Data Layer                                 │
│  data.remote (UrlShortenerClient), data.remote.model (DTOs)  │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Fluxo de Dados

- **Unidirecional (parcial)**: Eventos (`UrlShortenerUIEvent`) → ViewModel → Estado (`UrlShortenerUIState`) → UI
- **Comunicação**: `StateFlow` e `collectAsState()` para reatividade
- **Navegação**: Navigation Compose com `NavHost`, `NavRoute` e `rememberNavController()`

### 1.3 Avaliação da Arquitetura

| Aspecto | Avaliação | Observação |
|---------|-----------|------------|
| **Separação de responsabilidades** | ✅ Boa | Camadas bem definidas (UI, ViewModel, Domain, Data) |
| **Testabilidade** | ⚠️ Parcial | Repository e Client injetáveis; ViewModel acoplado à factory |
| **Escalabilidade** | ⚠️ Média | Sem DI (Hilt/Koin); ViewModel compartilhado entre rotas |
| **Consistência** | ⚠️ Variável | Mistura de `mutableStateOf` e `StateFlow`; estado reutilizado para diferentes fluxos |

---

## 2. Padrões de Projeto Identificados

### 2.1 Repository Pattern

**Localização**: `domain.repository.UrlShortenerRepository` e `UrlShortenerRepositoryDefault`

**Descrição**: abstração da fonte de dados (API REST) atrás de uma interface.

```kotlin
// UrlShortenerRepository.kt
interface UrlShortenerRepository {
    suspend fun postUrl(urlShortener: UrlShortener): UrlResult?
    suspend fun getUrlShortener(id: String): UrlShortener?
}
```

**Uso**: O ViewModel depende da interface, permitindo testes com implementações mock.

---

### 2.2 Factory Pattern

**Localização**: `UrlShortenerViewModel.companion object FACTORY` e `viewModelFactory`

**Descrição**: criação do ViewModel com suas dependências (Client, Repository) encapsulada.

```kotlin
val FACTORY = viewModelFactory {
    initializer {
        val client = HttpClientBuilder.createService<UrlShortenerClient>(BuildConfig.BASE_URL)
        val repository = UrlShortenerRepositoryDefault(client)
        UrlShortenerViewModel(repository)
    }
}
```

**Uso**: Permite instanciar o ViewModel via Compose sem Activity/Fragment.

---

### 2.3 Builder Pattern

**Localização**: `HttpClientBuilder`

**Descrição**: construção configurável do cliente HTTP (OkHttp + Retrofit).

```kotlin
object HttpClientBuilder {
    inline fun <reified T> createService(url: String, ...): T {
        val okHttpClient = OkHttpClient.Builder().apply { ... }.build()
        return Retrofit.Builder().baseUrl(url).client(okHttpClient)...create(T::class.java)
    }
}
```

---

### 2.4 Sealed Class (State/Object Pattern)

**Localização**: `UrlShortenerUIState`, `UrlShortenerUIEvent`

**Descrição**: modelagem de estados e eventos como hierarquias fechadas.

```kotlin
sealed class UrlShortenerUIState {
    object Idle : UrlShortenerUIState()
    object Loading : UrlShortenerUIState()
    data class Success<T>(val data: T) : UrlShortenerUIState()
    data class Error(val message: String) : UrlShortenerUIState()
}
```

**Uso**: `when` exaustivo e type-safety para UI e ViewModel.

---

### 2.5 Value Class (Kotlin)

**Localização**: `domain.model.UrlShortener`

**Descrição**: wrapper leve com validação centralizada.

```kotlin
@JvmInline
value class UrlShortener private constructor(val url: String) {
    companion object { fun createToPostUrl(url: String): UrlShortener { ... } }
}
```

---

### 2.6 Single Activity

**Localização**: `MainActivity`

**Descrição**: uma única Activity hospeda toda a UI em Compose.

**Uso**: Telas em `NavHost`; transições via `navController.navigate()` e `popBackStack()`.

---

### 2.7 Interpreter / Command Pattern (parcial)

**Localização**: `UrlShortenerViewModel.interpreter(action: UrlShortenerUIEvent)`

**Descrição**: centralização do tratamento de eventos em um único método.

```kotlin
fun interpreter(action: UrlShortenerUIEvent) {
    when (action) {
        is UrlShortenerUIEvent.PostShortUrlEvent -> postUrl(...)
        is UrlShortenerUIEvent.GetShortUrlEvent -> getUrlShortener(action.id)
    }
}
```

**Uso**: Dispatcher de ações da UI para lógica de negócio no ViewModel.

---

## 3. Análise do UrlShortenerViewModel

### 3.1 Pontos Positivos

- Uso de `StateFlow` para estado reativo
- Método `interpreter()` centraliza eventos
- Injeção de `CoroutineContext` facilita testes
- `viewModelScope` evita vazamento de coroutines

### 3.2 Pontos de Melhoria para Eventos e Transições de Estado

#### 3.2.1 Mistura de mecanismos de estado

**Problema**: Uso simultâneo de `StateFlow` e `mutableStateOf`:

```kotlin
private val _urlShortener = mutableStateOf<UrlShortener?>(null)
val urlShortener: State<UrlShortener?> = _urlShortener
```

**Sugestão**: Padronizar em `StateFlow` para manter consistência e facilitar testes.

---

#### 3.2.2 Estado genérico `Success<T>` para contextos distintos

**Problema**: `Success<UrlResult>` (post) e `Success<UrlShortener>` (get) compartilham o mesmo estado. A UI precisa fazer cast e `is` para diferenciar:

```kotlin
// ShortenerUrlScreen.kt
is UrlShortenerUIState.Success<*> -> {
    val s = (uiState as UrlShortenerUIState.Success<*>).data
    if (s is UrlShortener && !s.url.isEmpty()) {
        UrlDetailScreen(s.url, onBackPressed)
    } else { ... }
}
```

**Sugestão**: Separar estados ou usar eventos de navegação (veja 3.2.5).

---

#### 3.2.3 Loading sempre aplicado em qualquer ação

**Problema**: `interpreter()` coloca o estado em `Loading` antes do `when`, inclusive para `GetShortUrlEvent`:

```kotlin
fun interpreter(action: UrlShortenerUIEvent) {
    mutableUiSate.update { UrlShortenerUIState.Loading }  // Sempre
    when (action) { ... }
}
```

**Sugestão**: Aplicar `Loading` apenas onde faz sentido (ex.: `PostShortUrlEvent`), ou ter um `Loading` parametrizado (ex.: por tipo de operação).

---

#### 3.2.4 Ausência de one-shot events

**Problema**: Navegação e mensagens efêmeras (ex.: Snackbar) são tratadas como estado persistente. Ex.: `Success` permanece após exibição, exigindo `putUiOnIdle()` manual.

**Sugestão**: Introduzir **eventos one-shot** (`Channel` ou `SharedFlow` com `replay = 0`):

```kotlin
// Exemplo conceitual
private val _events = MutableSharedFlow<UrlShortenerEvent>(replay = 0)
val events: SharedFlow<UrlShortenerEvent> = _events.asSharedFlow()

sealed class UrlShortenerEvent {
    data class NavigateToDetail(val url: String) : UrlShortenerEvent()
    data class ShowError(val message: String) : UrlShortenerEvent()
    data object ShowSuccess : UrlShortenerEvent()
}
```

A UI coleta os eventos uma vez, consome e não os reexibe.

---

#### 3.2.5 Navegação acoplada ao estado

**Problema**: A decisão de navegar para `UrlDetailScreen` está na UI (`UrlShortenerScreen`), baseada em `Success<UrlShortener>`. Isso mistura estado de negócio com decisão de navegação.

**Sugestão**: O ViewModel emite um evento de navegação; a UI reage ao evento e chama `navController.navigate()`. Isso torna a navegação testável e desacoplada do estado.

---

#### 3.2.6 Tratamento de erro pouco estruturado

**Problema**: Mensagens de erro genéricas e `Exception` com `message` opcional:

```kotlin
val message = exception.message ?: "Failed to post shorten URL"
```

**Sugestão**: Usar um domínio de erros (ex.: sealed class) e mapear exceções para mensagens amigáveis de forma centralizada.

---

#### 3.2.7 Falta de reset de estado ao navegar

**Problema**: Ao voltar do detalhe para a lista, é necessário `putUiOnIdle()`. Se o usuário navegar rapidamente, estados antigos podem persistir.

**Sugestão**: Ao navegar para detalhe, emitir um evento/reset específico para limpar estado de sucesso/detalhe, em vez de depender de `Idle` genérico.

---

### 3.3 Resumo das Melhorias Sugeridas

| Prioridade | Melhoria |
|------------|----------|
| Alta | Separar eventos one-shot de estado persistente |
| Alta | Padronizar em `StateFlow` (remover `mutableStateOf` para `urlShortener`) |
| Média | Especificar estados de sucesso ou usar eventos de navegação |
| Média | Aplicar `Loading` apenas onde for necessário |
| Média | Centralizar tratamento de erros com domínio de erros |
| Baixa | Resetar estado ao navegar e em transições de tela |

---

## 4. Plano de Refatoração com Navigation Component

O projeto já utiliza Navigation Compose. O plano abaixo amplia e organiza melhor o uso do Navigation Component.

### 4.1 Estado Atual

- `NavRoute` enum com rotas: Splash, ShortenerUrl, UrlDetail
- `NavHost` em `UrlShortenerApp` com `composable()` para cada rota
- ViewModel compartilhado entre `ShortenerUrlScreen` e `UrlDetailScreen`
- Parâmetros de navegação: nenhum (dado passa pelo `urlShortener` do ViewModel)

### 4.2 Problemas Identificados

1. **Parâmetros via ViewModel**: O `id` da URL não vai na rota; o detalhe depende de `viewModel.urlShortener.value`.
2. **Deep link e estado**: Ao rotacionar ou restaurar, o detalhe pode perder dados.
3. **Type-safety**: Rotas são `String`; sem garantia de parâmetros obrigatórios.

### 4.3 Plano de Implementação

#### Fase 1: Rotas tipadas e parâmetros na URL

**Objetivo**: Passar o `id` da URL (ou a URL completa) como argumento de rota.

**Passos**:

1. Definir rotas com argumentos:

```kotlin
// Exemplo com typed navigation (Kotlin DSL ou navigation-compose)
sealed class NavRoute {
    data object Splash : NavRoute()
    data object ShortenerList : NavRoute()
    data class UrlDetail(val id: String) : NavRoute()
}
```

2. Configurar `NavHost` com argumentos:

```kotlin
composable(
    route = "detail/{urlId}",
    arguments = listOf(navArgument("urlId") { type = NavType.StringType })
) { backStackEntry ->
    val urlId = backStackEntry.arguments?.getString("urlId") ?: return@composable
    UrlDetailScreen(urlId = urlId, viewModel = viewModel)
}
```

3. Navegar com parâmetro:

```kotlin
navController.navigate("detail/${urlResult.alias}")
```

#### Fase 2: ViewModels por destino (opcional)

**Objetivo**: Evitar que `UrlDetailScreen` dependa do ViewModel compartilhado apenas para ler dados.

**Passos**:

1. Criar `UrlDetailViewModel` que recebe `urlId` e busca os dados.
2. Usar `SavedStateHandle` para obter `urlId` e permitir restauração.
3. Manter `UrlShortenerViewModel` na tela da lista; detalhe usa apenas o `UrlDetailViewModel`.

#### Fase 3: Safe Args (opcional)

**Objetivo**: Tipagem e segurança em tempo de compilação.

**Passos**:

1. Adicionar plugin `navigation-safe-args-gradle-plugin` (ou equivalente para Compose).
2. Gerar classes de direções e argumentos.
3. Usar `NavController.navigate(NavDirections)` em vez de strings.

#### Fase 4: Animações e transições

**Objetivo**: Transições visuais consistentes.

**Passos**:

1. Definir `enterTransition` e `exitTransition` para cada `composable()`:

```kotlin
composable(
    route = "detail/{urlId}",
    enterTransition = { slideInHorizontally { it } },
    exitTransition = { slideOutHorizontally { -it } }
) { ... }
```

2. Avaliar `AnimatedContent` ou transições compartilhadas para cases específicos.

#### Fase 5: Deep links

**Objetivo**: Suportar links externos (ex.: `myapp://url/abc123`).

**Passos**:

1. Declarar deep link no `composable()`:

```kotlin
composable(
    route = "detail/{urlId}",
    deepLinks = listOf(navDeepLink { uriPattern = "myapp://url/{urlId}" })
) { ... }
```

2. Configurar intent filters no `AndroidManifest` se necessário.

### 4.4 Cronograma sugerido

| Fase | Descrição | Esforço estimado |
|------|-----------|------------------|
| 1 | Parâmetros na rota (urlId) | 2–4 horas |
| 2 | ViewModel por tela + SavedStateHandle | 4–6 horas |
| 3 | Safe Args (opcional) | 2–3 horas |
| 4 | Animações | 2–4 horas |
| 5 | Deep links | 2–3 horas |

---

## 5. Pontos Positivos

1. **Arquitetura MVVM clara**: Separação entre UI, ViewModel e dados.
2. **Jetpack Compose**: UI moderna e declarativa.
3. **Navigation Compose**: Navegação integrada com Compose.
4. **Sealed classes**: `UrlShortenerUIState` e `UrlShortenerUIEvent` bem utilizados.
5. **Value class**: `UrlShortener` com validação e encapsulamento.
6. **Repository Pattern**: Abstração da API favorece testes.
7. **Qualidade de código**: Detekt, ktlint e JaCoCo configurados.
8. **Material 3**: Uso de tema e componentes atualizados.
9. **Coroutines e StateFlow**: Abordagem reativa e assíncrona adequada.
10. **Componentização**: Telas e componentes (`UrlShortenerFormComponent`, `UrlShortenerListComponent`) relativamente bem separados.

---

## 6. Pontos Negativos

1. **Ausência de DI**: Dependências criadas manualmente na factory do ViewModel.
2. **Estado e eventos misturados**: Navegação e mensagens efêmeras tratadas como estado persistente.
3. **ViewModel compartilhado**: Um único ViewModel para lista e detalhe, dificultando responsabilidades claras.
4. **Parâmetros de navegação no ViewModel**: Dado do detalhe não está na rota; perda de estado em rotação/restore.
5. **Código morto**: `SafeRequest` e `OperationResult` não utilizados.
6. **MainActivity com código legado**: `UrlShortenerScreen` deprecado e composables duplicados.
7. **Bug em `UrlShortenerListComponent`**: Lógica de `onClickItem()` dentro de `when (uiState)` dispara em `Success<UrlShortener>`, fazendo `onClickItem()` ser chamado em contextos inesperados (ex.: após post com sucesso).
8. **Typos**: `mutableUiSate` (deveria ser `mutableUiState`), comentário "DO NOTHINH".
9. **Tratamento de erros da API**: `null` em falhas no Repository sem propagar motivo do erro.
10. **Testes**: Pouca evidência de testes unitários ou de UI para ViewModel e fluxos principais.

---

## 7. Plano de Refatoração Geral

### 7.1 Curto prazo (1–2 sprints)

1. Corrigir typos e bug em `UrlShortenerListComponent` (separar clique na lista de `Success` no post).
2. Implementar one-shot events no ViewModel para navegação e Snackbars.
3. Padronizar estado em `StateFlow` e remover `mutableStateOf` onde não for necessário.
4. Passar `id` (ou URL) como parâmetro de rota para o detalhe.
5. Remover ou integrar `SafeRequest` no fluxo de chamadas da API.

### 7.2 Médio prazo (2–4 sprints)

1. Introduzir Hilt ou Koin para injeção de dependências.
2. Separar ViewModels por tela (lista vs. detalhe).
3. Usar `SavedStateHandle` para restaurar parâmetros em rotação/process death.
4. Melhorar tratamento de erros com domínio de erros e mapeamento de exceções.
5. Remover código deprecado e duplicado da `MainActivity`.

### 7.3 Longo prazo

1. Adotar MVI ou fluxo unidirecional mais estrito (se o produto crescer).
2. Implementar testes unitários e de UI para ViewModels e fluxos críticos.
3. Suportar deep links e compartilhamento.
4. Avaliar cache local (Room) para URLs encurtadas.

---

*Relatório gerado em março de 2025.*
