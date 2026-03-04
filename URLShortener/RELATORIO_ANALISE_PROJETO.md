# Relatório de Análise do Projeto URL Shortener

## Índice
1. [Avaliação da Arquitetura](#1-avaliação-da-arquitetura)
2. [Padrões de Projeto Identificados](#2-padrões-de-projeto-identificados)
3. [Avaliação de Componentes (ViewModel, Repository, HttpClientBuilder)](#3-avaliação-de-componentes-viewmodel-repository-httpclientbuilder)
4. [MutableStateFlow e StateFlow no ViewModel](#4-mutablestateflow-e-stateflow-no-viewmodel)
5. [Value Class e Algoritmo de Hash](#5-value-class-e-algoritmo-de-hash)
6. [Plano de Refatoração com Navigation Component](#6-plano-de-refatoração-com-navigation-component)
7. [Melhorias para UrlShortenerListComponent](#7-melhorias-para-urlshortenerlistcomponent)
8. [Pontos Positivos](#8-pontos-positivos)
9. [Pontos Negativos](#9-pontos-negativos)
10. [Plano de Refatoração Geral](#10-plano-de-refatoração-geral)
11. [Reavaliação do Código (Atualização)](#11-reavaliação-do-código-atualização)

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

## 3. Avaliação de Componentes (ViewModel, Repository, HttpClientBuilder)

### 3.1 UrlShortenerViewModel

#### Pontos positivos
- Uso de `StateFlow` para estado reativo
- Método `interpreter()` centraliza eventos
- Injeção de `CoroutineContext` facilita testes
- `viewModelScope` evita vazamento de coroutines

#### Pontos de melhoria
- **Mistura de mecanismos**: `mutableStateOf` para `urlShortener` vs `StateFlow` para o resto — padronizar em `StateFlow`
- **Estado genérico `Success<T>`**: Post e Get compartilham o mesmo estado; a UI precisa de cast — separar ou usar eventos de navegação
- **Loading indiscriminado**: `Loading` aplicado em todas as ações — aplicar apenas onde faz sentido
- **Ausência de one-shot events**: Navegação/Snackbar tratados como estado — usar `SharedFlow`/`Channel`
- **Tratamento de erro**: Mensagens genéricas — criar domínio de erros (sealed class)
- **Reset de estado**: Dependência de `putUiOnIdle()` manual — emitir reset ao navegar

---

### 3.2 UrlShortenerRepositoryDefault

#### Pontos positivos
- Implementa a interface `UrlShortenerRepository`, permitindo mocks em testes
- Mapeamento DTO → domínio centralizado
- Código enxuto e legível

#### Pontos de melhoria
- **Retorno `null` em falhas**: Não propaga motivo do erro (HTTP 4xx/5xx, body de erro)
- **Comentários vagos**: "Handle successful response if needed" / "Handle error response if needed" — remover ou substituir por lógica concreta
- **Ausência de `SafeRequest`**: O projeto tem `SafeRequest`/`OperationResult` não utilizados — integrar para tratamento padronizado de erros
- **Exceções não tratadas**: Chamadas à API podem lançar exceções de rede; o Repository não as encapsula em um tipo de resultado
- **Sugestão de assinatura**:

```kotlin
// Opção: Result ou sealed class
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String, val code: Int? = null) : RepositoryResult<Nothing>()
}
```

---

### 3.3 HttpClientBuilder

#### Pontos positivos
- Uso de `inline reified` para criação genérica de serviços
- Logging condicional em debug
- Parâmetros configuráveis (URL, converter, isDebug)

#### Pontos de melhoria
- **Singleton global**: `object` dificulta testes e múltiplas instâncias (ex.: APIs diferentes)
- **Sem timeout configurável**: OkHttp usa timeouts padrão — expor `connectTimeout`, `readTimeout`, `writeTimeout`
- **Sem interceptors customizáveis**: Não permite adicionar auth, retry, etc.
- **Acoplamento a BuildConfig**: `isDebug` poderia ser parâmetro ou injeção
- **Sugestão**: Converter em classe com `Builder` interno ou factory para maior flexibilidade:

```kotlin
class HttpClientBuilder private constructor(
    private val baseUrl: String,
    private val converterFactory: Converter.Factory,
    private val isDebug: Boolean,
    private val connectTimeout: Long = 30L,
    private val readTimeout: Long = 30L
) {
    fun <T : Any> createService(serviceClass: KClass<T>): T { ... }
    class Builder { ... }
}
```

---

## 4. MutableStateFlow e StateFlow no ViewModel

### 4.1 Situação atual

O `UrlShortenerViewModel` usa **ambos** os mecanismos:

| Mecanismo | Uso | Exposição |
|-----------|-----|-----------|
| `MutableStateFlow` + `StateFlow` | `textFieldContent`, `urls`, `uiState` | `asStateFlow()` |
| `mutableStateOf` + `State` | `urlShortener` | `State<UrlShortener?>` |

### 4.2 Pontos negativos

1. **Inconsistência**: Duas formas de estado reativo na mesma classe dificultam manutenção e onboarding.
2. **APIs diferentes na UI**: `collectAsState()` para `StateFlow` vs `by`/`.value` para `State` — a UI precisa saber qual usar.
3. **Testabilidade**: `State` do Compose é mais difícil de testar em ViewModels; `StateFlow` pode ser coletado em testes com `turbine` ou `first()`.
4. **Hot vs Cold**: `StateFlow` é hot e mantém o último valor; `mutableStateOf` também, mas a semântica de recomposição do Compose pode divergir em edge cases.
5. **Thread-safety**: `StateFlow.update {}` é atômico; `_urlShortener.value = x` em coroutines pode ter race conditions se não for sincronizado.

### 4.3 Sugestões de mudança

**Opção 1: Padronizar em StateFlow (recomendado)**

```kotlin
private val _urlShortener = MutableStateFlow<UrlShortener?>(null)
val urlShortener: StateFlow<UrlShortener?> = _urlShortener.asStateFlow()
```

Na UI: `val urlShortener by viewModel.urlShortener.collectAsState()`.

**Opção 2: Estado unificado**

Unificar todo o estado em um único `data class`:

```kotlin
data class UrlShortenerState(
    val textFieldContent: String = "",
    val urls: List<UrlResult> = emptyList(),
    val uiState: UrlShortenerUIState = Idle,
    val urlShortener: UrlShortener? = null
)
private val _state = MutableStateFlow(UrlShortenerState())
val state: StateFlow<UrlShortenerState> = _state.asStateFlow()
```

**Opção 3: Manter `mutableStateOf` apenas para estado de UI puro**

Se houver um caso específico que se beneficie de `mutableStateOf` (ex.: animação que precisa de recomposição imediata), documentar o motivo e manter apenas esse caso; o resto em `StateFlow`.

---

## 5. Value Class e Algoritmo de Hash

### 5.1 O que está bem implementado

- **`@JvmInline value class`**: Evita alocação extra em runtime; boa escolha para wrapper de `String`
- **Construtor privado**: Força criação via `createToPostUrl` e `createFromGetResult`, garantindo validação
- **Validação de URL**: Regex para `https?|ftp` e estrutura de domínio
- **Dois pontos de entrada**: `createToPostUrl` (com validação e hash) e `createFromGetResult` (apenas wrap) — separação correta de responsabilidades

### 5.2 O que poderia melhorar

- **`shortenerUrl` no domínio**: O método `shortenerUrl` gera uma URL local que **não é usada pela API** — a API é quem encurta. O método parece redundante ou mal posicionado; `createToPostUrl` deveria apenas validar e passar a URL original.
- **Validação de `tinyUrl`**: `isValidUrl(tinyUrl)` valida uma URL gerada localmente com formato `scheme://domain.com/path`; o regex pode não cobrir todos os casos.
- **Exceção genérica**: `IllegalArgumentException("Invalid URL format")` — considerar um tipo de erro mais específico (ex.: `InvalidUrlException`).
- **Regex complexo**: Manutenção difícil; considerar `android.net.Uri.parse()` ou `java.net.URL` para validação (com cuidado para edge cases).

### 5.3 Algoritmo `shortenerUrl` e alternativas

O método atual usa **SHA-256** para gerar um hash e extrair `domain` (8 chars) + `path` (8 chars):

```kotlin
private fun shortenerUrl(url: String): String {
    val scheme = runCatching { URI(url).scheme }.getOrNull()?.lowercase() ?: "https"
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(url.toByteArray(Charsets.UTF_8))
    val hashHex = hashBytes.joinToString("") { "%02x".format(it) }
    val domain = hashHex.substring(0, 8)
    val path = hashHex.substring(8, 16)
    return "$scheme://$domain.com/$path"
}
```

**Observação**: Esse método gera uma URL *local* que não corresponde ao que a API retorna. Se a API é a fonte da verdade, esse algoritmo pode ser removido ou movido para um utilitário de preview/demonstração.

#### Alternativas de implementação de hash (para encurtamento local ou offline)

| Algoritmo | Prós | Contras |
|-----------|------|---------|
| **SHA-256** (atual) | Criptograficamente seguro, colisões raras | Hash longo (64 hex chars), lento para grandes volumes |
| **MD5** | Mais rápido, 32 chars | Colisões conhecidas, não recomendado para segurança |
| **Base62/Base64** | URLs curtas e legíveis | Precisa de contador ou hash; implementação extra |
| **MurmurHash3** | Rápido, bom para não-críptico | Colisões possíveis, não é criptográfico |
| **xxHash** | Muito rápido | Não criptográfico |
| **CRC32** | Simples, rápido | Colisões mais frequentes |

**Exemplo com Base62 (IDs curtos e legíveis)**:

```kotlin
private const val BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

fun shortenerUrlBase62(url: String, length: Int = 8): String {
    val hash = url.hashCode().toLong().and(0xFFFFFFFFL)
    return (0 until length).map { BASE62[(hash shr (it * 6)) and 0x3F].code.toChar() }.joinToString("")
}
```

**Exemplo com MurmurHash3 (Kotlin multiplatform)**:

```kotlin
// Requer dependência: org.jetbrains.kotlinx:kotlinx-hash
// ou implementação manual
fun shortenerUrlMurmur(url: String): String {
    val hash = MurmurHash3.hash32(url.toByteArray())
    return Integer.toHexString(hash).take(8)
}
```

**Recomendação**: Se a API faz o encurtamento, remover `shortenerUrl` de `createToPostUrl` e usar apenas validação. Se for necessário encurtamento local (ex.: cache, fallback), documentar o propósito e escolher um algoritmo adequado ao caso (ex.: Base62 para legibilidade, SHA-256 para unicidade).

---

## 6. Plano de Refatoração com Navigation Component

O projeto já utiliza Navigation Compose. O plano abaixo amplia e organiza melhor o uso do Navigation Component.

### 6.1 Estado Atual

- `NavRoute` enum com rotas: Splash, ShortenerUrl, UrlDetail
- `NavHost` em `UrlShortenerApp` com `composable()` para cada rota
- ViewModel compartilhado entre `ShortenerUrlScreen` e `UrlDetailScreen`
- Parâmetros de navegação: nenhum (dado passa pelo `urlShortener` do ViewModel)

### 6.2 Problemas Identificados

1. **Parâmetros via ViewModel**: O `id` da URL não vai na rota; o detalhe depende de `viewModel.urlShortener.value`.
2. **Deep link e estado**: Ao rotacionar ou restaurar, o detalhe pode perder dados.
3. **Type-safety**: Rotas são `String`; sem garantia de parâmetros obrigatórios.

### 6.3 Plano de Implementação

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

### 6.4 Cronograma sugerido

| Fase | Descrição | Esforço estimado |
|------|-----------|------------------|
| 1 | Parâmetros na rota (urlId) | 2–4 horas |
| 2 | ViewModel por tela + SavedStateHandle | 4–6 horas |
| 3 | Safe Args (opcional) | 2–3 horas |
| 4 | Animações | 2–4 horas |
| 5 | Deep links | 2–3 horas |

---

## 7. Melhorias para UrlShortenerListComponent

### 7.1 Problemas atuais

1. **Bug de lógica**: O bloco `when (uiState)` chama `onClickItem()` quando `Success<UrlShortener>` — isso dispara navegação **sempre que o estado é Success**, inclusive após post, e não apenas no clique do item. O efeito colateral está no lugar errado.
2. **Acoplamento ao ViewModel**: O componente recebe o ViewModel inteiro; deveria receber apenas dados e callbacks.
3. **Labels invertidos**: `link.self` é URL original, `link.short` é URL encurtada; o código mostra "Shorted URL: self" e "Original URL: short" — invertido.
4. **Uso de `items(urls.size)`**: Preferir `items(urls)` ou `items(urls, key = { it.alias })` para keys estáveis e melhor performance.
5. **Comentário "DO NOTHINH"**: Typo e lógica vazia — o bloco não deveria existir ou deveria ter propósito claro.
6. **Responsabilidade mista**: O componente mistura (a) renderização da lista, (b) reação a `uiState` para navegação e (c) disparo de eventos — separar responsabilidades.

### 7.2 Plano de implementação melhorada

#### Princípio: componente "burro" (apresentacional)

O componente deve receber **dados** e **callbacks**, não o ViewModel:

```kotlin
@Composable
fun UrlShortenerListComponent(
    modifier: Modifier = Modifier,
    urls: List<UrlResult>,
    onItemClick: (String) -> Unit  // alias do item clicado
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(
            items = urls,
            key = { it.alias }
        ) { url ->
            UrlShortenerListItem(
                url = url,
                onClick = { onItemClick(url.alias) }
            )
        }
    }
}
```

#### Correção dos labels

```kotlin
Text(text = "Original URL: ${url.link.self}")
Text(text = "Short URL: ${url.link.short}")
```

#### Orquestração na tela (ou no ViewModel)

A decisão de navegar deve ficar na **tela** ou em um **evento do ViewModel**:

```kotlin
// Na tela ShortenerUrlScreen ou UrlShortenerApp
UrlShortenerListComponent(
    urls = urls,
    onItemClick = { alias ->
        viewModel.interpreter(UrlShortenerUIEvent.GetShortUrlEvent(alias))
        // Navegação via evento one-shot do ViewModel, ou:
        navController.navigate("detail/$alias")
    }
)
```

Se usar parâmetros na rota (seção 6), a navegação pode ser direta: `navController.navigate("detail/$alias")`, e o `UrlDetailScreen` busca os dados via `urlId`.

#### Estrutura sugerida final

```kotlin
// Componente puro - apenas UI
@Composable
fun UrlShortenerListComponent(
    modifier: Modifier = Modifier,
    urls: List<UrlResult>,
    onItemClick: (String) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items = urls, key = { it.alias }) { url ->
            Card(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(4.dp),
                onClick = { onItemClick(url.alias) },
                shape = RectangleShape
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Original: ${url.link.self}", style = MaterialTheme.typography.bodyMedium)
                    Text("Short: ${url.link.short}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
```

#### Resumo das mudanças

| Mudança | Benefício |
|---------|-----------|
| Remover `when (uiState)` do componente | Elimina bug de navegação indevida |
| Receber `urls` e `onItemClick` em vez do ViewModel | Componente testável, reutilizável |
| Corrigir labels (self vs short) | Exibição correta |
| `items(urls, key = { it.alias })` | Keys estáveis, melhor recomposição |
| Navegação na tela ou via evento | Responsabilidades claras |

---

## 8. Pontos Positivos

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

## 9. Pontos Negativos

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

## 10. Plano de Refatoração Geral

### 10.1 Curto prazo (1–2 sprints)

1. Refatorar `UrlShortenerListComponent` conforme seção 7 (componente apresentacional, corrigir labels, remover bug de `onClickItem`).
2. Implementar one-shot events no ViewModel para navegação e Snackbars.
3. Padronizar estado em `StateFlow` e remover `mutableStateOf` onde não for necessário.
4. Passar `id` (ou URL) como parâmetro de rota para o detalhe.
5. Remover ou integrar `SafeRequest` no fluxo de chamadas da API.

### 10.2 Médio prazo (2–4 sprints)

1. Introduzir Hilt ou Koin para injeção de dependências.
2. Separar ViewModels por tela (lista vs. detalhe).
3. Usar `SavedStateHandle` para restaurar parâmetros em rotação/process death.
4. Melhorar tratamento de erros com domínio de erros e mapeamento de exceções.
5. Remover código deprecado e duplicado da `MainActivity`.

### 10.3 Longo prazo

1. Adotar MVI ou fluxo unidirecional mais estrito (se o produto crescer).
2. Implementar testes unitários e de UI para ViewModels e fluxos críticos.
3. Suportar deep links e compartilhamento.
4. Avaliar cache local (Room) para URLs encurtadas.

---

## 11. Reavaliação do Código (Atualização)

Esta seção compara o estado atual do código com as recomendações anteriores e indica se as mudanças estão no caminho correto.

### 11.1 Mudanças Implementadas

| Área | Mudança | Avaliação |
|------|---------|-----------|
| **ViewModel** | `mutableStateOf` substituído por `MutableStateFlow` para `urlShortener` | ✅ Correto — padronização em StateFlow |
| **ViewModel** | Repository retorna `RepositoryResult` em vez de `null` | ✅ Correto — tratamento de erros estruturado |
| **Repository** | `UrlShortenerRepositoryDefault` usa `SafeRepository.remoteCall` | ✅ Correto — encapsulamento de chamadas e erros |
| **Repository** | Interface retorna `RepositoryResult<UrlResult>` e `RepositoryResult<UrlShortener>` | ✅ Correto — tipo explícito de resultado |
| **HttpClient** | `HttpClientBuilder` → `HttpClient` com Builder pattern | ✅ Correto — timeouts configuráveis |
| **HttpClient** | `withConnectionTimeout`, `withReadTimeout`, `isDebugMode` | ✅ Correto — flexibilidade aumentada |
| **Domain** | `RepositoryResult` e `SafeRepository` criados | ✅ Correto — domínio de erros centralizado |

### 11.2 Mudanças Parcialmente Implementadas ou com Problemas

| Área | Situação | Observação |
|------|----------|-------------|
| **ViewModel** | Código comentado (TODO remover) em `postUrl` e `getUrlShortener` | ⚠️ Remover trechos comentados |
| **ViewModel** | `getUrlShortener`: `UrlShortenerUIState.Success(urlShortener.value)` | ⚠️ Preferir `result.data` para clareza e evitar race |
| **ShortenerUrlScreen** | `putUiOnIdle()` chamado nos branches Loading e Error | ❌ Bug — reseta estado imediatamente, escondendo loading/erro |
| **UrlShortenerListComponent** | Sem alterações | ❌ Bug de `onClickItem()` em `Success<UrlShortener>` permanece |
| **UrlShortenerListComponent** | Labels invertidos (Shorted/Original) | ❌ Permanece incorreto |
| **UrlShortenerListComponent** | Ainda acoplado ao ViewModel | ❌ Não refatorado para componente apresentacional |
| **Navigation** | Parâmetros ainda não passados na rota | ⚠️ `urlId` não está na URL; dados via ViewModel |
| **SafeRepository** | `Gson().fromJson(errorBody, String::class.java)` | ⚠️ API pode retornar JSON de erro, não String pura |

### 11.3 Direção das Mudanças

**Resumo**: As mudanças estão **em grande parte no caminho correto**. Os ajustes em ViewModel (StateFlow), Repository (RepositoryResult, SafeRepository) e HttpClient (Builder, timeouts) seguem as recomendações. Porém:

- **ShortenerUrlScreen**: A chamada de `putUiOnIdle()` em Loading e Error é um **regressão** — o loading e o erro somem imediatamente.
- **UrlShortenerListComponent**: Nenhuma refatoração aplicada; bugs e acoplamento continuam.
- **Código morto**: Trechos comentados devem ser removidos.

### 11.4 Nota Final: **6,5 / 10**

| Critério | Nota | Justificativa |
|----------|------|---------------|
| Arquitetura | 7/10 | MVVM bem aplicado; falta separação de ViewModels por tela |
| Qualidade de código | 6/10 | Melhorias em Repository/HttpClient; código comentado e bugs na UI |
| Tratamento de erros | 8/10 | RepositoryResult e SafeRepository bem implementados |
| Consistência de estado | 7/10 | StateFlow padronizado; Loading/Error com reset incorreto |
| Componentização | 5/10 | UrlShortenerListComponent com bugs e acoplamento |
| Navegação | 5/10 | Sem parâmetros na rota; dados via ViewModel |
| Testabilidade | 6/10 | Repository e HttpClient mais testáveis; ViewModel ainda acoplado à factory |

**Média ponderada**: ~6,5/10

### 11.5 Plano de Refatoração Atualizado (Priorizado)

#### Prioridade 1 — Crítico (1–2 dias)

1. **Remover `putUiOnIdle()` dos branches Loading e Error** em `ShortenerUrlScreen` — o estado não deve ser resetado enquanto loading ou erro estão sendo exibidos.
2. **Remover código comentado** em `UrlShortenerViewModel` (blocos TODO).
3. **Corrigir labels** em `UrlShortenerListComponent`: "Original URL: \${url.link.self}", "Short URL: \${url.link.short}".

#### Prioridade 2 — Alta (3–5 dias)

4. **Refatorar `UrlShortenerListComponent`** para componente apresentacional: receber `urls` e `onItemClick(id)`, remover `when(uiState)` e acoplamento ao ViewModel.
5. **Passar `urlId` como parâmetro de rota** para `UrlDetailScreen`; navegar com `navController.navigate("detail/$alias")`.
6. **Corrigir `getUrlShortener`** no ViewModel: usar `UrlShortenerUIState.Success(result.data)` em vez de `urlShortener.value`.

#### Prioridade 3 — Média (1–2 semanas)

7. **Implementar eventos one-shot** para navegação (SharedFlow/Channel) e desacoplar decisão de navegação do estado.
8. **Criar `UrlDetailViewModel`** com `SavedStateHandle` para `urlId`; remover dependência do ViewModel compartilhado no detalhe.
9. **Revisar `SafeRepository`**: tratar `errorBody` como JSON quando a API retornar objeto de erro.

#### Prioridade 4 — Baixa

10. Introduzir Hilt/Koin para DI.
11. Corrigir typo `mutableUiSate` → `mutableUiState`.
12. Aplicar `Loading` apenas em `PostShortUrlEvent` (opcional).

---

*Relatório gerado em março de 2025. Reavaliação atualizada.*
