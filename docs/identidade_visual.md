# Identidade visual — PowerGabs

Este documento define o tema do app. Siga-o em toda tela e componente.
A referência visual completa (canvas de design) é mantida pelo responsável do projeto; em caso de dúvida sobre aparência, pergunte.

## Princípios

- Sensação: força e intensidade, "anilha e giz".
- Base de cor: vermelho-coral (energia, esforço) + preto-carvão (peso, ferro), com grafite como secundária e âmbar como terciária (destaques e esforço percebido).
- Formas mais retas que o padrão do Material 3, para transmitir firmeza.
- Temas claro e escuro são obrigatórios e seguem o sistema (`isSystemInDarkTheme()`). O tema escuro é o "rosto" do app: é nele que o coral sobre o preto aparece com mais força.
- Todos os pares de texto e fundo têm contraste mínimo de 4,5:1 (WCAG AA).

## Regras para o agente

1. **Cores somente via `MaterialTheme.colorScheme`.** Nunca use `Color(0xFF...)` em telas ou componentes. Valores hexadecimais existem apenas em `Color.kt`.
2. **Não use cor dinâmica (Material You).** O tema não tem o parâmetro `dynamicColor`; não o adicione.
3. **`PowerGabsBrand` é só para ilustração** (ícone, imagens decorativas). Nunca use essas cores como fundo de texto, botão ou superfície.
4. **Tipografia somente via `MaterialTheme.typography`.** Não defina `fontSize` ou `fontFamily` diretamente em telas.
5. **Não crie cores, fontes ou formas novas.** Se algo parecer faltar, pergunte.
6. Todo `@Composable` de tela deve ter preview nos dois temas com `@PreviewLightDark`.
7. **Erro nunca depende só da cor.** Como a cor primária (coral) e a de erro (vermelho) são próximas, toda mensagem de erro usa `errorContainer` **e** um ícone de alerta **e** texto explicativo. Nunca use `primary` para indicar erro, nem `error` para destacar ação.
8. Não force caixa alta com `uppercase()` nos textos. A força visual vem da fonte condensada, não de letras maiúsculas.

## Arquivos do tema

Os arquivos ficam em `presentation/theme/`. O template do Android Studio os cria em `ui/theme/`: mova-os para `presentation/theme/` e ajuste o `package`, conforme a arquitetura do `CLAUDE.md`.

### Color.kt

```kotlin
package com.seunome.powergabs.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Cores exclusivas de ilustração (ícone e imagens decorativas).
 * NUNCA usar como fundo de texto, botão ou superfície.
 */
object PowerGabsBrand {
    val Coral = Color(0xFFFF6B4A)
    val Brasa = Color(0xFFFFB59E)
    val Carvao = Color(0xFF1A1A1A)
}

// ---------- Tema claro ----------
internal val PrimaryLight = Color(0xFFB8402A)
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val PrimaryContainerLight = Color(0xFFFFDAD1)
internal val OnPrimaryContainerLight = Color(0xFF3D0A02)
internal val InversePrimaryLight = Color(0xFFFF8A6E)

internal val SecondaryLight = Color(0xFF52525A)
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val SecondaryContainerLight = Color(0xFFE2E1E6)
internal val OnSecondaryContainerLight = Color(0xFF1B1B21)

internal val TertiaryLight = Color(0xFF7D5700)
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val TertiaryContainerLight = Color(0xFFFFDEA8)
internal val OnTertiaryContainerLight = Color(0xFF271900)

internal val BackgroundLight = Color(0xFFFCF8F6)
internal val OnBackgroundLight = Color(0xFF1C1B1B)
internal val SurfaceVariantLight = Color(0xFFEDE0DC)
internal val OnSurfaceVariantLight = Color(0xFF53433F)
internal val SurfaceDimLight = Color(0xFFDDD8D6)
internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFF7F2F0)
internal val SurfaceContainerLight = Color(0xFFF1ECEA)
internal val SurfaceContainerHighLight = Color(0xFFEBE6E4)
internal val SurfaceContainerHighestLight = Color(0xFFE5E1DF)
internal val InverseSurfaceLight = Color(0xFF313030)
internal val InverseOnSurfaceLight = Color(0xFFF4F0EE)

internal val OutlineLight = Color(0xFF857370)
internal val OutlineVariantLight = Color(0xFFD8C2BD)
internal val ErrorLight = Color(0xFFBA1A1A)
internal val OnErrorLight = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFFFDAD6)
internal val OnErrorContainerLight = Color(0xFF410002)

// ---------- Tema escuro ----------
internal val PrimaryDark = Color(0xFFFF8A6E)
internal val OnPrimaryDark = Color(0xFF5C1305)
internal val PrimaryContainerDark = Color(0xFF8C2A16)
internal val OnPrimaryContainerDark = Color(0xFFFFDAD1)
internal val InversePrimaryDark = Color(0xFFB8402A)

internal val SecondaryDark = Color(0xFFC7C6CD)
internal val OnSecondaryDark = Color(0xFF2F2F35)
internal val SecondaryContainerDark = Color(0xFF45454C)
internal val OnSecondaryContainerDark = Color(0xFFE3E2E9)

internal val TertiaryDark = Color(0xFFF5BD48)
internal val OnTertiaryDark = Color(0xFF422C00)
internal val TertiaryContainerDark = Color(0xFF5F4100)
internal val OnTertiaryContainerDark = Color(0xFFFFDEA8)

internal val BackgroundDark = Color(0xFF111111)
internal val OnBackgroundDark = Color(0xFFE6E1E0)
internal val SurfaceVariantDark = Color(0xFF3B3B3F)
internal val OnSurfaceVariantDark = Color(0xFFC9C5CA)
internal val SurfaceBrightDark = Color(0xFF37373A)
internal val SurfaceContainerLowestDark = Color(0xFF0B0B0C)
internal val SurfaceContainerLowDark = Color(0xFF1A1A1B)
internal val SurfaceContainerDark = Color(0xFF1E1E20)
internal val SurfaceContainerHighDark = Color(0xFF28282A)
internal val SurfaceContainerHighestDark = Color(0xFF333335)
internal val InverseSurfaceDark = Color(0xFFE6E1E0)
internal val InverseOnSurfaceDark = Color(0xFF313030)

internal val OutlineDark = Color(0xFF938F94)
internal val OutlineVariantDark = Color(0xFF48464A)
internal val ErrorDark = Color(0xFFFFB4AB)
internal val OnErrorDark = Color(0xFF690005)
internal val ErrorContainerDark = Color(0xFF93000A)
internal val OnErrorContainerDark = Color(0xFFFFDAD6)
```

### Type.kt

As fontes ficam embutidas no app (não usar "downloadable fonts"), porque o app é Offline First. Família escolhida: **Barlow** (Google Fonts, licença OFL). A variante *Condensed* dá a cara "esportiva" aos títulos; a variante normal garante leitura confortável nos textos corridos. Por serem da mesma família, combinam sem conflito.

Os arquivos `.ttf` são adicionados manualmente em `app/src/main/res/font/` com estes nomes exatos:

- `barlow_condensed_bold.ttf`
- `barlow_condensed_extrabold.ttf`
- `barlow_regular.ttf`
- `barlow_semibold.ttf`
- `barlow_bold.ttf`

**Se os arquivos não existirem, pare e me peça.** Não baixe, não substitua por outra fonte e não remova a referência. O texto da licença (OFL) fica em `docs/licencas/` e as fontes são citadas no README.

```kotlin
package com.seunome.powergabs.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.seunome.powergabs.R

private val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
    Font(R.font.barlow_condensed_extrabold, FontWeight.ExtraBold),
)

private val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
)

internal val PowerGabsTypography = Typography(
    displaySmall = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp),
    headlineSmall = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    labelMedium = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
)
```

### Theme.kt

```kotlin
package com.seunome.powergabs.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = PrimaryLight, onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight, onPrimaryContainer = OnPrimaryContainerLight,
    inversePrimary = InversePrimaryLight,
    secondary = SecondaryLight, onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight, onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight, onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight, onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight, onBackground = OnBackgroundLight,
    surface = BackgroundLight, onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight, onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = PrimaryLight,
    surfaceDim = SurfaceDimLight, surfaceBright = BackgroundLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    inverseSurface = InverseSurfaceLight, inverseOnSurface = InverseOnSurfaceLight,
    outline = OutlineLight, outlineVariant = OutlineVariantLight,
    error = ErrorLight, onError = OnErrorLight,
    errorContainer = ErrorContainerLight, onErrorContainer = OnErrorContainerLight,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark, onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark, onPrimaryContainer = OnPrimaryContainerDark,
    inversePrimary = InversePrimaryDark,
    secondary = SecondaryDark, onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark, onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark, onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark, onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark, onBackground = OnBackgroundDark,
    surface = BackgroundDark, onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark, onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = PrimaryDark,
    surfaceDim = BackgroundDark, surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = InverseSurfaceDark, inverseOnSurface = InverseOnSurfaceDark,
    outline = OutlineDark, outlineVariant = OutlineVariantDark,
    error = ErrorDark, onError = OnErrorDark,
    errorContainer = ErrorContainerDark, onErrorContainer = OnErrorContainerDark,
)

private val PowerGabsShapes = Shapes(
    small = RoundedCornerShape(8.dp),    // imagens de exercício, chips e badges de prescrição
    medium = RoundedCornerShape(12.dp),  // cards de objetivo, de grupo muscular e itens de lista
    large = RoundedCornerShape(20.dp),   // painéis, banners e bottom sheets
)

@Composable
fun PowerGabsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PowerGabsTypography,
        shapes = PowerGabsShapes,
        content = content,
    )
}
```

Na `MainActivity`, chame `enableEdgeToEdge()` antes de `setContent { PowerGabsTheme { ... } }` e trate os insets com `Scaffold` ou `WindowInsets`.

## Onde usar cada cor

| Elemento | Papel |
|---|---|
| Fundo das telas | `background` |
| Botão principal (ex.: "Entrar", "Montar treino", "Adicionar ao treino") | `Button` padrão → `primary` / `onPrimary` |
| Botão secundário (ex.: "Criar conta", "Marcar como feito") | `OutlinedButton` → borda `outline`, texto `primary` |
| Campos de login e cadastro | `OutlinedTextField` padrão (borda `outline`, foco `primary`) |
| Card de objetivo não selecionado | `surfaceContainerLowest` com borda `outlineVariant` |
| Card de objetivo selecionado | `primaryContainer` com borda `primary` (2dp) |
| Chip de grupo muscular não selecionado | `FilterChip` padrão (borda `outline`) |
| Chip de grupo muscular selecionado | `secondaryContainer` / `onSecondaryContainer` |
| Badge de prescrição (ex.: "4 × 8–12 · 90 s") | `secondaryContainer` / `onSecondaryContainer` |
| Chips de músculo e equipamento (detalhes) | `surfaceContainerHigh` / `onSurfaceVariant` |
| Indicador de esforço (1 a 5) | `tertiary` (nível vazio: `outline`) |
| Aviso de descanso ("Você treinou Pernas ontem") | `tertiaryContainer` / `onTertiaryContainer`, com ícone de informação |
| Banner "sem internet" | `inverseSurface` / `inverseOnSurface`, com ícone de nuvem cortada |
| Itens do "Meu treino" | `surfaceContainer` |
| Item do "Meu treino" concluído | `surfaceContainerLow`, com ícone de check em `primary` |
| Imagem de exercício indisponível | `surfaceContainer` com borda `outline` e ícone de haltere em `outline` |
| Área de vídeo indisponível ou offline | `surfaceContainerHigh` com ícone e texto em `onSurfaceVariant` |
| Textos secundários (equipamento, "atualizado há…") | `onSurfaceVariant` |
| Mensagens de erro | `errorContainer` / `onErrorContainer`, sempre com ícone de alerta |

## Onde usar cada estilo de texto

| Uso | Estilo |
|---|---|
| "Qual é o seu objetivo?" e título do login | `displaySmall` |
| Título de tela (nome do grupo muscular, "Meu treino", "Sobre") | `headlineSmall` |
| Nome do exercício (lista e detalhes) | `titleLarge` |
| Nome do objetivo no card | `titleMedium` |
| Títulos de seção nos detalhes ("Músculos", "Como executar") | `titleSmall` |
| Descrição do exercício e textos corridos | `bodyLarge` |
| Subtítulos e informações secundárias | `bodyMedium` |
| Equipamento, legendas, descrição curta do card de objetivo | `bodySmall` |
| Botões | `labelLarge` (padrão dos botões M3) |
| Chips e badge de prescrição | `labelMedium` |

## Espaçamento e toque

- Margem lateral das telas: 20dp.
- Espaço entre itens de lista: 12dp; entre seções: 24dp.
- Alvo de toque mínimo: 48dp (botões de ícone usam `IconButton`, que já garante isso).
- Imagem do exercício na lista: 72×72dp, forma `small`. Na tela de detalhes, ocupa a largura total com 240dp de altura.
- Vídeo: proporção 16:9, largura total, forma `medium`.
- Cards de objetivo: grade de 2 colunas, altura mínima de 120dp.

## Ícones

- Use os ícones do conjunto core (`Icons.Rounded`) quando existirem.
- Para ícones que não estão no core (ex.: haltere, nuvem cortada, play), importe vetores individuais pelo *Vector Asset* do Android Studio para `res/drawable`. Não adicione a dependência `material-icons-extended` sem perguntar.
- Botões só com ícone devem ter `contentDescription` em português (ex.: "Voltar", "Adicionar ao treino", "Reproduzir vídeo").

## Ícone do app e logo

**Símbolo:** um kettlebell desenhado em contorno grosso, com a letra G no corpo. O kettlebell remete à academia; o G, ao nome. O símbolo é coral (`PowerGabsBrand.Coral`) sobre carvão (`PowerGabsBrand.Carvao`).

Os arquivos são fornecidos prontos pelo responsável do projeto. **Não os redesenhe, não altere cores, espessuras ou proporções e não gere versões novas.** Se algum estiver faltando, pare e pergunte.

O projeto usa `minSdk = 24`. Por isso existem duas formas do ícone:

| Arquivo | Uso |
|---|---|
| `res/mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml` | Ícone adaptativo (Android 8.0+), que junta as três camadas abaixo |
| `res/drawable/ic_launcher_foreground.xml` | Camada da frente: o símbolo em coral, dentro do círculo seguro de 66dp |
| `res/values/ic_launcher_background.xml` | Camada de fundo: a cor `ic_launcher_background` (carvão) |
| `res/drawable/ic_launcher_monochrome.xml` | Camada monocromática para ícones temáticos (Android 13+) |
| `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png` e `ic_launcher_round.png` | Ícones em imagem para Android 7.x (API 24 e 25), que não têm ícone adaptativo |
| `res/drawable/ic_logo_symbol.xml` | Só o símbolo, desenhado em branco, para usar nas telas |
| `docs/assets/powergabs_icon_512.png` | Ícone em alta resolução para o README |
| `docs/assets/powergabs_logo_escuro.png` e `powergabs_logo_claro.png` | Logo completo (símbolo + nome) para o README |

Os arquivos XML do ícone contêm valores hexadecimais. Esta é a única exceção à regra 1: eles fazem parte da marca, não do tema.

### Logo nas telas

Nas telas, o logo **não** é uma imagem. Ele é montado em Compose, para seguir o tema claro/escuro e ser lido por leitores de tela:

```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        painter = painterResource(R.drawable.ic_logo_symbol),
        contentDescription = null, // o texto ao lado já nomeia o app
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(56.dp),
    )
    Spacer(Modifier.width(12.dp))
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) { append("Power") }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Gabs") }
        },
        style = MaterialTheme.typography.displaySmall,
    )
}
```

- Use o logo completo nas telas de login, cadastro e "Sobre". Nas demais telas, o título da tela basta.
- O símbolo sozinho nunca recebe `tint` fora de `primary`, `onSurfaceVariant` (estados desativados) ou `inverseOnSurface` (sobre `inverseSurface`).
- Não aplique sombra, gradiente, rotação ou contorno extra ao símbolo.

## Estados das telas

- **Carregando:** `CircularProgressIndicator` centralizado em `primary`.
- **Vazio:** frase curta explicando o motivo e uma ação (ex.: "Seu treino está vazio." + botão "Escolher um objetivo").
- **Erro:** o que aconteceu e como resolver, sem pedir desculpas (ex.: "Não foi possível carregar os exercícios. Verifique sua conexão." + botão "Tentar novamente").
- **Vídeo indisponível:** informativo, não é erro (ex.: "Este exercício ainda não tem vídeo de execução.").
- **Vídeo offline:** "O vídeo precisa de internet. A descrição e a imagem continuam disponíveis."
