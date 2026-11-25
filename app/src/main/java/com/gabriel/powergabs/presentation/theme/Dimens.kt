package com.gabriel.powergabs.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Medidas da identidade visual (seção "Espaçamento e toque").
 * Centralizar aqui evita números mágicos espalhados pelas telas.
 */
object Dimens {
    /** Margem lateral das telas. */
    val ScreenPadding = 20.dp

    /** Espaço entre itens de lista. */
    val ItemSpacing = 12.dp

    /** Espaço entre seções. */
    val SectionSpacing = 24.dp

    /** Espaços pequenos dentro de um componente (ícone + texto, chips). */
    val SmallSpacing = 8.dp

    /** Imagem do exercício na lista: 72×72dp. */
    val ListImageSize = 72.dp

    /** Imagem do exercício nos detalhes: largura total × 240dp. */
    val DetailImageHeight = 240.dp

    /** Altura mínima dos cards de objetivo. */
    val GoalCardMinHeight = 120.dp

    /** Símbolo do logo nas telas de login, cadastro e Sobre. */
    val LogoSymbolSize = 56.dp

    /** Borda do card de objetivo selecionado. */
    val SelectedBorderWidth = 2.dp
}
