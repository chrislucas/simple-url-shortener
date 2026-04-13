# Relatório de Análise do Projeto URL Shortener

## Índice

1. [Avaliação da Arquitetura](#1-avaliação-da-arquitetura)
2. [Padrões de Projeto Identificados](#2-padrões-de-projeto-identificados)
3. [Avaliação de Componentes (ViewModel, Repository, HttpClient, SafeRepository)](#3-avaliação-de-componentes-viewmodel-repository-httpclient-saferepository)
4. [Múltiplos StateFlows, alternativas e MutableStateFlow vs StateFlow](#4-múltiplos-stateflows-alternativas-e-mutablestateflow-vs-stateflow)
5. [Refatoração: modelo único de estado (UrlShortenerViewModel)](#5-refatoração-modelo-único-de-estado-urlshortenerviewmodel)
6. [Value Class e Algoritmo de Hash](#6-value-class-e-algoritmo-de-hash)
7. [Plano de Refatoração com Navigation Component](#7-plano-de-refatoração-com-navigation-component)
8. [Melhorias para UrlShortenerListComponent](#8-melhorias-para-urlshortenerlistcomponent)
9. [Pontos Positivos](#9-pontos-positivos)
10. [Pontos Negativos e Como Resolver](#10-pontos-negativos-e-como-resolver)
11. [Plano de Refatoração Geral](#11-plano-de-refatoração-geral)
12. [Reavaliação do Código (Atualização)](#12-reavaliação-do-código-atualização)
13. [Nota final do código (0–10)](#13-nota-final-do-código-010)

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

- **Eventos de UI**: `UrlShortenerUIEvent` → `uiEventInterpreter` no ViewModel
- **Estado**: `StateFlow` (`textFieldContent`, `urls`, `uiState`, `urlShortener`) + `collectAsState()` na UI
- **Efeitos one-shot**: `SharedFlow<UrlShortenerEvent>` (`navigationEvent`) para navegação, Snackbar e mensagens de erro — consumidos uma vez em `LaunchedEffect`
- **Navegação**: Navigation Compose com `NavHost`, `NavRoute` e `rememberNavController()`

### 1.3 Avaliação da Arquitetura


| Aspecto                            | Avaliação   | Observação                                                                           |
| ---------------------------------- | ----------- | ------------------------------------------------------------------------------------ |
| **Separação de responsabilidades** | ✅ Boa       | Camadas bem definidas (UI, ViewModel, Domain, Data)                                  |
| **Testabilidade**                  | ⚠️ Parcial  | Repository e Client injetáveis; ViewModel acoplado à factory                         |
| **Escalabilidade**                 | ⚠️ Média    | Sem DI (Hilt/Koin); ViewModel compartilhado entre rotas                              |
| **Consistência**                   | ✅ Melhorada | Padronização em `StateFlow`; `urls` como `Set` evita duplicatas                    |


---

## 2. Padrões de Projeto Identificados

### 2.1 Repository Pattern

**Localização**: `domain.repository.UrlShortenerRepository` e `UrlShortenerRepositoryDefault`

**Descrição**: abstração da fonte de dados (API REST) atrás de uma interface.

```kotlin
// UrlShortenerRepository.kt
interface UrlShortenerRepository {
    suspend fun postUrl(urlShortener: UrlShortener): RepositoryResult<UrlResult>
    suspend fun getUrlShortener(id: String): RepositoryResult<UrlShortener>
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
        val httpClient = HttpClient.Builder(BuildConfig.BASE_URL)
            .withConnectionTimeout(20L)
            .withReadTimeout(20L)
            .isDebugMode(BuildConfig.DEBUG)
            .build()
        val client = httpClient.createService(UrlShortenerClient::class)
        val repository = UrlShortenerRepositoryDefault(client)
        UrlShortenerViewModel(repository)
    }
}
```

**Uso**: Permite instanciar o ViewModel via Compose sem Activity/Fragment.

---

### 2.3 Builder Pattern

**Localização**: `HttpClient`

**Descrição**: construção configurável do cliente HTTP (OkHttp + Retrofit) via Builder pattern.

```kotlin
class HttpClient private constructor(...) {
    class Builder(...) {
        fun withConnectionTimeout(seconds: Long): Builder
        fun withReadTimeout(seconds: Long): Builder
        fun isDebugMode(isDebug: Boolean): Builder
        fun build(): HttpClient
    }
    fun <T : Any> createService(serviceClass: KClass<T>): T
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

**Localização**: `UrlShortenerViewModel.uiEventInterpreter(uiEvent: UrlShortenerUIEvent)`

**Descrição**: centralização do tratamento de eventos de UI em um único método.

```kotlin
fun uiEventInterpreter(uiEvent: UrlShortenerUIEvent) {
    when (uiEvent) {
        is UrlShortenerUIEvent.PostShortUrlEvent -> { ... }
        is UrlShortenerUIEvent.GetShortUrlEvent -> { ... }
    }
}
```

**Uso**: Dispatcher de ações da UI para lógica de negócio no ViewModel.

---

### 2.8 Observer / Eventos one-shot (SharedFlow)

**Localização**: `MutableSharedFlow<UrlShortenerEvent>` exposto como `navigationEvent` (nome histórico; cobre Snackbar, erro e navegação).

**Descrição**: eventos consumidos uma vez pela UI (`NavigateToDetail`, `ShowSnackBar`, `ShowError`).

---

## 3. Avaliação de Componentes (ViewModel, Repository, HttpClient, SafeRepository)

### 3.1 UrlShortenerViewModel

#### Pontos positivos

- Uso de `StateFlow` para estado reativo; `urls` como `Set<UrlResult>` evita duplicatas
- `uiEventInterpreter` centraliza eventos de UI
- `MutableSharedFlow` para eventos one-shot (navegação, Snackbar, erro)
- `Loading` apenas em post; get não força overlay global desnecessário
- Injeção de `CoroutineContext` facilita testes
- `viewModelScope` e `RepositoryResult` para erros estruturados
- `clearUrlShortener()` encapsula reset de `urlShortener`

#### Pontos de melhoria

- **Vários fluxos de estado** (ver seção 4): quatro `StateFlow` + um `SharedFlow` aumentam superfície de atualização e testes
- **`putUiOnIdle()` manual**: ainda necessário na navegação; poderia integrar ao fluxo de eventos
- **ViewModel compartilhado** entre lista e detalhe: considerar `UrlDetailViewModel` + argumento de rota
- **Nome `navigationEvent`**: o fluxo também emite Snackbar/erro — renomear para `uiEvents` ou `oneShotEvents` melhora legibilidade

---

### 3.2 UrlShortenerRepositoryDefault

#### Pontos positivos

- Implementa `UrlShortenerRepository`; mapeamento DTO → domínio claro
- Delega chamadas HTTP e erros a `SafeRepository.remoteCall`
- Retorna `RepositoryResult` tipado

#### Pontos de melhoria

- **`responseBody.let { ... }`** em `postUrl` é redundante com o bloco `onSuccess` — pode simplificar para mapeamento direto
- Erros de rede já cobertos por `SafeRepository`; manter consistência de mensagens entre métodos

---

### 3.3 HttpClient

*(O projeto usa a classe `HttpClient` com `Builder`; não há mais `HttpClientBuilder` como nome de tipo.)*

#### Pontos positivos

- Builder com `withConnectionTimeout`, `withReadTimeout`, `isDebugMode`
- Instância por factory (não singleton global), testável
- Timeouts aplicados ao `OkHttpClient`

#### Pontos de melhoria

- Expor `writeTimeout` se houver upload de corpo grande
- Permitir interceptors (auth, logging condicional avançado) via Builder

---

### 3.4 SafeRepository

#### Pontos positivos

- Função genérica `remoteCall` reutilizável para qualquer `Response<T>`
- Trata sucesso, corpo nulo, HTTP de erro e exceções em um único lugar
- Retorna `RepositoryResult` com código HTTP quando disponível

#### Pontos de melhoria

- **`Gson().fromJson(errorBody, String::class.java)`**: assume que o corpo de erro é JSON string; APIs frequentemente retornam `{"message":"..."}` ou `{"error":"..."}` — usar DTO de erro ou `JsonObject`/`JsonParser` para extrair mensagem com segurança
- **`empty_message` como string**: se `errorBody` for vazio, o Gson pode falhar — tratar `null`/string vazia antes do parse
- **Centralizar mensagens**: constantes ou resource strings para textos genéricos ("null_body", etc.)

---

## 4. Múltiplos StateFlows, alternativas e MutableStateFlow vs StateFlow

### 4.1 Quantidade de StateFlows no UrlShortenerViewModel

No estado atual, o ViewModel expõe **quatro** `StateFlow` derivados de `MutableStateFlow`:

| Flow | Conteúdo | Papel |
|------|----------|--------|
| `textFieldContent` | `String` | Texto do campo de URL |
| `urls` | `Set<UrlResult>` | Lista acumulada de URLs encurtadas |
| `uiState` | `UrlShortenerUIState` | Idle / Loading (principalmente post) |
| `urlShortener` | `UrlShortener?` | Detalhe selecionado para a tela de detalhe |

Além disso, há **`navigationEvent`**: `SharedFlow<UrlShortenerEvent>` para eventos one-shot.

**Por que pode ser “excessivo”?**

- Várias fontes de verdade precisam ser atualizadas em conjunto (`putUiOnIdle` + `clearUrlShortener`), com risco de inconsistência se um fluxo for esquecido
- Testes do ViewModel exigem observar ou assertar vários flows
- Recomposição: mais `collectAsState()` na árvore aumenta pontos de subscrição (ainda aceitável neste app pequeno)

**Alternativas para simplificar**

1. **Um único `StateFlow<UrlShortenerScreenState>`** (ver seção 5) — um `data class` imutável com todos os campos; uma única `update { }` por transição
2. **`combine` de flows** — menos comum para UI state; útil se quiser derivar estado somente leitura
3. **Reduzir campos**: por exemplo, `urlShortener` só é necessário até migrar para rota com `urlId` + `UrlDetailViewModel`

### 4.2 MutableStateFlow e StateFlow na mesma ViewModel

Na prática, o padrão é: **`MutableStateFlow` privado** + **`StateFlow` público** via `asStateFlow()` — não há mistura com `mutableStateOf` do Compose no ViewModel (bom).

**Pontos negativos possíveis**

- Muitos `MutableStateFlow` independentes: atualizações não atômicas entre eles (ex.: `urlShortener` e `uiState` em momentos ligeiramente diferentes)
- Leitores precisam saber qual flow observar para cada parte da UI

**Sugestões**

- Unificar em um único estado (seção 5), ou documentar claramente o contrato de cada flow
- Preferir `update { }` em vez de `.value =` quando a atualização depender do valor anterior

---

## 5. Refatoração: modelo único de estado (UrlShortenerViewModel)

### 5.1 Objetivo

Representar **todo** o estado da tela principal em um único tipo imutável, exposto por **um** `StateFlow`, reduzindo bugs de sincronização e simplificando testes.

### 5.2 Exemplo de modelo

```kotlin
data class UrlShortenerScreenState(
    val textFieldContent: String = "",
    val urls: Set<UrlResult> = emptySet(),
    val uiStatus: UiStatus = UiStatus.Idle,
    val selectedDetail: UrlShortener? = null,
)

sealed class UiStatus {
    data object Idle : UiStatus()
    data object LoadingPost : UiStatus()
}
```

- Eventos one-shot (`NavigateToDetail`, Snackbar) podem permanecer em `SharedFlow` ou serem derivados de transições explícitas no reducer.

### 5.3 Passos de refatoração sugeridos

1. Introduzir `UrlShortenerScreenState` e `MutableStateFlow(UrlShortenerScreenState())`
2. Substituir atualizações diretas a `textFieldContent`, `shortUrls`, `mutableUiState`, `mutableUrlShortener` por `state.update { it.copy(...) }`
3. Expor `val state: StateFlow<UrlShortenerScreenState>`
4. Migrar composables para `viewModel.state.collectAsState()` e um único `when` ou decomposição por propriedade
5. Remover flows antigos quando não houver mais referências
6. Manter `SharedFlow` de eventos apenas se ainda forem necessários após a unificação (muitos times colocam “efeitos” só no canal de eventos)

### 5.4 Benefícios

- Snapshot único do estado — mais fácil de logar, serializar (SavedStateHandle) e testar
- Transições explícitas — menos `putUiOnIdle` espalhado

---

## 6. Value Class e Algoritmo de Hash

### 6.1 O que está bem implementado

- `**@JvmInline value class`**: Evita alocação extra em runtime; boa escolha para wrapper de `String`
- **Construtor privado**: Força criação via `createToPostUrl` e `createFromGetResult`, garantindo validação
- **Validação de URL**: Regex para `https?|ftp` e estrutura de domínio
- **Dois pontos de entrada**: `createToPostUrl` (com validação e hash) e `createFromGetResult` (apenas wrap) — separação correta de responsabilidades

### 6.2 O que poderia melhorar

- `**shortenerUrl` no domínio**: O método `shortenerUrl` gera uma URL local que **não é usada pela API** — a API é quem encurta. O método parece redundante ou mal posicionado; `createToPostUrl` deveria apenas validar e passar a URL original.
- **Validação de `tinyUrl`**: `isValidUrl(tinyUrl)` valida uma URL gerada localmente com formato `scheme://domain.com/path`; o regex pode não cobrir todos os casos.
- **Exceção genérica**: `IllegalArgumentException("Invalid URL format")` — considerar um tipo de erro mais específico (ex.: `InvalidUrlException`).
- **Regex complexo**: Manutenção difícil; considerar `android.net.Uri.parse()` ou `java.net.URL` para validação (com cuidado para edge cases).

### 6.3 Algoritmo `shortenerUrl` e alternativas

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


| Algoritmo           | Prós                                      | Contras                                               |
| ------------------- | ----------------------------------------- | ----------------------------------------------------- |
| **SHA-256** (atual) | Criptograficamente seguro, colisões raras | Hash longo (64 hex chars), lento para grandes volumes |
| **MD5**             | Mais rápido, 32 chars                     | Colisões conhecidas, não recomendado para segurança   |
| **Base62/Base64**   | URLs curtas e legíveis                    | Precisa de contador ou hash; implementação extra      |
| **MurmurHash3**     | Rápido, bom para não-críptico             | Colisões possíveis, não é criptográfico               |
| **xxHash**          | Muito rápido                              | Não criptográfico                                     |
| **CRC32**           | Simples, rápido                           | Colisões mais frequentes                              |


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

## 7. Plano de Refatoração com Navigation Component

O projeto já utiliza Navigation Compose. O plano abaixo amplia e organiza melhor o uso do Navigation Component.

### 7.1 Estado Atual

- `NavRoute` enum com rotas: Splash, ShortenerUrl, UrlDetail
- `NavHost` em `UrlShortenerApp` com `composable()` para cada rota
- ViewModel compartilhado entre `ShortenerUrlScreen` e `UrlDetailScreen`
- Parâmetros de navegação: nenhum (dado passa pelo `urlShortener` do ViewModel)

### 7.2 Problemas Identificados

1. **Parâmetros via ViewModel**: O `id` da URL não vai na rota; o detalhe depende de `viewModel.urlShortener.value`.
2. **Deep link e estado**: Ao rotacionar ou restaurar, o detalhe pode perder dados.
3. **Type-safety**: Rotas são `String`; sem garantia de parâmetros obrigatórios.

### 7.3 Plano de Implementação

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

1. Configurar `NavHost` com argumentos:

```kotlin
composable(
    route = "detail/{urlId}",
    arguments = listOf(navArgument("urlId") { type = NavType.StringType })
) { backStackEntry ->
    val urlId = backStackEntry.arguments?.getString("urlId") ?: return@composable
    UrlDetailScreen(urlId = urlId, viewModel = viewModel)
}
```

1. Navegar com parâmetro:

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

1. Avaliar `AnimatedContent` ou transições compartilhadas para cases específicos.

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

1. Configurar intent filters no `AndroidManifest` se necessário.

### 7.4 Cronograma sugerido


| Fase | Descrição                             | Esforço estimado |
| ---- | ------------------------------------- | ---------------- |
| 1    | Parâmetros na rota (urlId)            | 2–4 horas        |
| 2    | ViewModel por tela + SavedStateHandle | 4–6 horas        |
| 3    | Safe Args (opcional)                  | 2–3 horas        |
| 4    | Animações                             | 2–4 horas        |
| 5    | Deep links                            | 2–3 horas        |


---

## 8. Melhorias para UrlShortenerListComponent

### 8.1 Situação atual (refatoração aplicada)

O componente foi **refatorado** conforme as recomendações:

- **Componente apresentacional**: Recebe `urls: List<UrlResult>` e `onClickItem: (String) -> Unit` — sem acoplamento ao ViewModel
- **Labels corretos**: "Shorted URL: \${url.link.short}", "Original URL: \${url.link.self}"
- **`items(urls, key = { url.alias })`**: Keys estáveis para melhor recomposição
- **Sem `when(uiState)`**: Navegação apenas no clique do item

### 8.2 Pontos de atenção

1. **Orquestração na tela**: O callback `{ pathId -> interpreter(GetShortUrlEvent(pathId)); onClickItem() }` chama `interpreter` (assíncrono) e `onClickItem` (navega) imediatamente — a navegação ocorre antes do fetch completar. O detalhe pode exibir estado vazio brevemente até `urlShortener` ser atualizado. **Sugestão**: Navegar com `urlId` na rota e deixar o detalhe buscar os dados; ou usar evento one-shot para navegar apenas após sucesso.
2. **Typo "Shorted"**: Considerar "Short URL" em vez de "Shorted URL" (gramática).

### 8.3 Referência (implementação atual)

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

Se usar parâmetros na rota (seção 7), a navegação pode ser direta: `navController.navigate("detail/$alias")`, e o `UrlDetailScreen` busca os dados via `urlId`.

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


| Mudança                                            | Benefício                          |
| -------------------------------------------------- | ---------------------------------- |
| Remover `when (uiState)` do componente             | Elimina bug de navegação indevida  |
| Receber `urls` e `onItemClick` em vez do ViewModel | Componente testável, reutilizável  |
| Corrigir labels (self vs short)                    | Exibição correta                   |
| `items(urls, key = { it.alias })`                  | Keys estáveis, melhor recomposição |
| Navegação na tela ou via evento                    | Responsabilidades claras           |


---

## 9. Pontos Positivos

1. **Arquitetura MVVM clara**: Separação entre UI, ViewModel e dados.
2. **Jetpack Compose**: UI moderna e declarativa.
3. **Navigation Compose**: Navegação integrada com Compose.
4. **Sealed classes**: `UrlShortenerUIState` e `UrlShortenerUIEvent` bem utilizados.
5. **Value class**: `UrlShortener` com validação e encapsulamento.
6. **Repository Pattern**: Abstração da API favorece testes; `RepositoryResult` e `SafeRepository` para erros.
7. **Qualidade de código**: Detekt, ktlint e JaCoCo configurados.
8. **Material 3**: Uso de tema e componentes atualizados.
9. **Coroutines e StateFlow**: Abordagem reativa; padronização em `StateFlow`; `urls` como `Set` evita duplicatas.
10. **Componentização**: `UrlShortenerListComponent` apresentacional (urls + callback); `UrlShortenerFormComponent` com separação de concerns.
11. **HttpClient**: Builder pattern com timeouts configuráveis.
12. **MainActivity**: Código legado removido; estrutura limpa.

---

## 10. Pontos Negativos e Como Resolver

### 10.1 Ausência de DI

**Problema**: Dependências criadas manualmente na factory do ViewModel.

**Como resolver**:
- Introduzir **Hilt** ou **Koin** como framework de injeção de dependências.
- Criar módulos para `HttpClient`, `UrlShortenerClient`, `UrlShortenerRepository` e `UrlShortenerViewModel`.
- Anotar a `Application` com `@HiltAndroidApp` (Hilt) ou configurar `startKoin` (Koin).
- Remover a factory manual do ViewModel; o framework injeta as dependências via construtor.
- Benefício: testes mais simples com `@Inject` e mocks, e configuração centralizada.

---

### 10.2 Estado e eventos misturados

**Problema**: Navegação e mensagens efêmeras (ex.: Snackbar) tratadas como estado persistente.

**Como resolver**:
- Criar um canal de **eventos one-shot** com `SharedFlow(replay = 0)` ou `Channel`.
- Definir `sealed class UrlShortenerEvent` com variantes como `NavigateToDetail(id)`, `ShowSnackbar(message)`, `ShowError(message)`.
- O ViewModel emite eventos; a UI coleta com `collectAsState` ou `LaunchedEffect` e consome uma vez (não reexibe).
- Após consumir, não é necessário `putUiOnIdle()` — o estado de sucesso/erro não precisa persistir para efeitos colaterais.

---

### 10.3 ViewModel compartilhado

**Problema**: Um único ViewModel para lista e detalhe, dificultando responsabilidades claras.

**Como resolver**:
- Criar `UrlDetailViewModel` dedicado à tela de detalhe.
- O `UrlDetailViewModel` recebe `urlId` via `SavedStateHandle` (argumento de rota).
- Busca os dados no `init` ou em um `LaunchedEffect`; a tela de detalhe observa apenas esse ViewModel.
- Manter `UrlShortenerViewModel` apenas na tela da lista; cada tela tem seu próprio escopo de estado.

---

### 10.4 Parâmetros de navegação no ViewModel

**Problema**: Dado do detalhe não está na rota; perda de estado em rotação/restore.

**Como resolver**:
- Definir rota com argumento: `"detail/{urlId}"` e `navArgument("urlId") { type = NavType.StringType }`.
- Navegar com `navController.navigate("detail/${urlResult.alias}")` ao clicar no item.
- No `composable` do detalhe, obter `urlId` de `backStackEntry.arguments?.getString("urlId")`.
- O `UrlDetailViewModel` usa `SavedStateHandle.get<String>("urlId")` para restaurar após rotação/process death.

---

### 10.5 Código morto

**Problema**: `SafeRequest` e `OperationResult` não utilizados (ou substituídos por `SafeRepository`/`RepositoryResult`).

**Como resolver**:
- Se `SafeRepository` e `RepositoryResult` já cobrem o caso: **remover** `SafeRequest` e `OperationResult` para evitar confusão.
- Se ainda houver uso planejado: documentar o propósito ou integrar ao fluxo existente.
- Executar busca por referências; remover arquivos ou classes não referenciadas.

---

### 10.6 MainActivity com código legado *(Resolvido)*

**Problema** (anterior): `UrlShortenerScreen` deprecado e composables duplicados.

**Status**: Código legado removido; `MainActivity` limpa, chamando apenas `UrlShortenerApp`.

---

### 10.7 Bug em UrlShortenerListComponent *(Resolvido)*

**Problema** (anterior): `onClickItem()` chamado dentro de `when (uiState)` em `Success<UrlShortener>`, disparando navegação em contextos indevidos.

**Status**: Refatorado para componente apresentacional com `urls` e `onItemClick`. Resta ajustar a orquestração (navegação antes do fetch) — ver seção 8.2.

---

### 10.8 Typos

**Problema**: `mutableUiSate` (deveria ser `mutableUiState`), comentário "DO NOTHINH".

**Como resolver**:
- Renomear `mutableUiSate` para `mutableUiState` em todo o ViewModel (refactor do IDE).
- Remover ou corrigir o comentário "DO NOTHINH"; se o bloco `else` for vazio, removê-lo ou documentar o motivo.

---

### 10.9 Tratamento de erros da API *(Mitigado)*

**Problema** (anterior): `null` em falhas no Repository sem propagar motivo do erro.

**Status**: Resolvido com `RepositoryResult` e `SafeRepository`. Resta revisar parsing de `errorBody` quando a API retornar JSON objeto.
- Garantir que o Repository retorne sempre `RepositoryResult.Success` ou `RepositoryResult.Error`.
- Em `RepositoryResult.Error`, incluir `message` e `code` (HTTP status) quando disponível.
- No `SafeRepository`, tratar `errorBody` como JSON quando a API retornar objeto de erro (ex.: `{"message": "..."}`) em vez de assumir String pura.

---

### 10.10 Testes

**Problema**: Pouca evidência de testes unitários ou de UI para ViewModel e fluxos principais.

**Como resolver**:
- **ViewModel**: testes com `runTest`, `StateFlow` (ex.: `turbine` ou `first()`), e Repository mockado.
- **Repository**: testes com `UrlShortenerClient` mockado (MockK) e respostas de sucesso/erro.
- **Componentes**: testes de Compose com `composeTestRule` para interações e estado exibido.
- **Navegação**: usar `TestNavHostController` para validar rotas e argumentos.
- Configurar cobertura mínima no JaCoCo e integrar ao pipeline de CI.

---

## 11. Plano de Refatoração Geral

### 11.1 Curto prazo (1–2 sprints) — itens pendentes

1. ~~Refatorar `UrlShortenerListComponent`~~ *(concluído)*
2. ~~Padronizar estado em `StateFlow`~~ *(concluído)*
3. ~~Remover código deprecado da `MainActivity`~~ *(concluído)*
4. Corrigir `getUrlShortener`: usar `result.data` em vez de `urlShortener.value`.
5. Corrigir typo `mutableUiSate` → `mutableUiState`.
6. Passar `id` como parâmetro de rota para o detalhe.
7. ~~Implementar one-shot events para navegação~~ *(implementado — `UrlShortenerEvent` + `navigationEvent`)*

### 11.2 Médio prazo (2–4 sprints)

1. Introduzir Hilt ou Koin para injeção de dependências.
2. Criar `UrlDetailViewModel` com `SavedStateHandle`; separar ViewModels por tela.
3. Revisar `SafeRepository`: tratar `errorBody` como JSON objeto quando aplicável.
4. Corrigir "Shorted" → "Short" em `UrlShortenerListComponent`.

### 11.3 Longo prazo

1. Adotar MVI ou fluxo unidirecional mais estrito (se o produto crescer).
2. Implementar testes unitários e de UI para ViewModels e fluxos críticos.
3. Suportar deep links e compartilhamento.
4. Avaliar cache local (Room) para URLs encurtadas.

---

## 12. Reavaliação do Código (Atualização)

Esta seção apresenta a avaliação completa atual do código, seguindo os mesmos critérios e nível de detalhe das análises anteriores.

### 12.1 Mudanças Implementadas desde a Última Avaliação

| Área | Mudança | Avaliação |
|------|---------|-----------|
| **UrlShortenerListComponent** | Refatorado para componente apresentacional | ✅ Correto — recebe `urls` e `onClickItem` |
| **UrlShortenerListComponent** | Labels corrigidos (Short/Original) | ✅ Correto |
| **UrlShortenerListComponent** | `items(urls, key = { url.alias })` | ✅ Correto — keys estáveis |
| **ShortenerUrlScreen** | `putUiOnIdle()` removido de Loading e Error | ✅ Correto — bug corrigido |
| **MainActivity** | Código legado e deprecado removido | ✅ Correto — estrutura limpa |
| **ViewModel** | Código comentado removido | ✅ Correto |
| **ViewModel** | `urls` como `Set<UrlResult>` | ✅ Correto — evita duplicatas |

### 12.2 Pontos que Permanecem ou Surgiram

| Área | Situação | Observação |
|------|----------|------------|
| **ViewModel** | `getUrlShortener`: `Success(urlShortener.value)` | ⚠️ Preferir `result.data` — `urlShortener` é o StateFlow exposto; usar `mutableUrlShortener.value` ou `result.data` |
| **ViewModel** | Typo `mutableUiSate` | ⚠️ Renomear para `mutableUiState` |
| **Navegação** | Parâmetros não passados na rota | ⚠️ `urlId` não está na URL; dados via ViewModel; perda de estado em rotação |
| **Orquestração** | Navegação imediata no clique | ⚠️ `onClickItem()` chamado antes do fetch; detalhe pode exibir vazio brevemente |
| **SafeRepository** | `Gson().fromJson(errorBody, String::class.java)` | ⚠️ Pode falhar se API retornar JSON objeto |

### 12.3 Avaliação por Critério

| Critério | Nota | Justificativa |
|----------|------|---------------|
| **Arquitetura** | 7,5/10 | MVVM bem aplicado; falta separação de ViewModels por tela |
| **Qualidade de código** | 7/10 | Componentização melhorada; typo e pequenos ajustes restantes |
| **Tratamento de erros** | 8/10 | RepositoryResult e SafeRepository bem implementados |
| **Consistência de estado** | 8/10 | StateFlow padronizado; `urls` como Set; sem putUiOnIdle incorreto |
| **Componentização** | 8/10 | UrlShortenerListComponent apresentacional; orquestração pode melhorar |
| **Navegação** | 5,5/10 | Sem parâmetros na rota; dados via ViewModel; race no clique |
| **Testabilidade** | 6,5/10 | Repository e HttpClient testáveis; ViewModel acoplado à factory |

### 12.4 Nota consolidada (histórico): **7,2 / 10**

**Evolução**: 6,5 → 7,2 (melhoria de +0,7)

As mudanças estão **no caminho correto**. A refatoração do `UrlShortenerListComponent`, a remoção do bug de `putUiOnIdle`, a limpeza do `MainActivity` e a padronização em `StateFlow` elevam a qualidade. Os próximos passos focam em navegação com parâmetros na rota e eventos one-shot.

### 12.5 Plano de Refatoração Atualizado (Priorizado)

#### Prioridade 1 — Crítico (1–2 dias)

1. **Corrigir `getUrlShortener`**: Usar `UrlShortenerUIState.Success(result.data)` em vez de `urlShortener.value`.
2. **Corrigir typo**: `mutableUiSate` → `mutableUiState`.

#### Prioridade 2 — Alta (3–5 dias)

3. **Passar `urlId` como parâmetro de rota**: `"detail/{urlId}"`; navegar com `navController.navigate("detail/$alias")`.
4. **Criar `UrlDetailViewModel`** com `SavedStateHandle` para buscar dados por `urlId`; eliminar dependência do ViewModel compartilhado e race no clique.
5. **Revisar `SafeRepository`**: Tratar `errorBody` como JSON objeto quando aplicável.

#### Prioridade 3 — Média (1–2 semanas)

6. **Implementar eventos one-shot** para navegação (SharedFlow/Channel).
7. **Corrigir "Shorted"** → "Short" em `UrlShortenerListComponent`.

#### Prioridade 4 — Baixa

8. Introduzir Hilt/Koin para DI.
9. Aplicar `Loading` apenas em `PostShortUrlEvent` (opcional).
10. Implementar testes unitários e de UI.

---

## 13. Nota final do código (0–10)

### Nota: **7,5 / 10**

| Critério | Nota | Comentário breve |
|----------|------|-------------------|
| Arquitetura MVVM e camadas | 8/10 | Separação clara; falta DI e ViewModel dedicado ao detalhe |
| Padrões (Repository, Builder, sealed, value class, eventos) | 8/10 | Uso consistente; `SafeRepository` centraliza rede |
| UrlShortenerViewModel | 7/10 | Vários `StateFlow` + `SharedFlow`; `uiEventInterpreter` e eventos one-shot bem encaminhados; ainda há espaço para estado unificado |
| Dados e rede | 8/10 | `RepositoryResult`, timeouts no `HttpClient`; parsing de erro HTTP frágil |
| UI Compose | 7,5/10 | Lista apresentacional; Snackbar + navegação por eventos |
| Navegação | 6/10 | Sem argumentos na rota; estado de detalhe no ViewModel compartilhado |
| Testes e manutenção | 5,5/10 | Pouca cobertura aparente; factory manual |

### Justificativa

O projeto está **acima da média** para um app de porte reduzido: MVVM coerente, Compose, Navigation, tratamento de resultado de rede tipado, `HttpClient` configurável, `SafeRepository` reutilizável e evolução recente com **eventos one-shot** (`UrlShortenerEvent` + `SharedFlow`), o que endereça parte do acoplamento navegação/estado.

A nota não chega a **8–9** porque ainda há **múltiplos fluxos de estado** sem modelo único, **navegação sem parâmetros na rota**, **ViewModel compartilhado** entre telas, **sem framework de DI**, **testes pouco visíveis** e **melhorias pendentes** em `SafeRepository` (corpo de erro JSON) e no domínio `UrlShortener` (hash local vs API).

### Evolução sugerida para subir a nota

- Unificar estado da tela (seção 5) e/ou `UrlDetailViewModel` + `SavedStateHandle`
- Hilt/Koin e testes de ViewModel com `Turbine`
- Rotas com `urlId` e deep links opcionais

---

*Relatório gerado em março de 2025. Atualizado com arquitetura, componentes (incl. SafeRepository), múltiplos StateFlows, modelo único proposto, value class, nota 7,5/10.*