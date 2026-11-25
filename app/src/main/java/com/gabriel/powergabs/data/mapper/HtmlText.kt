package com.gabriel.powergabs.data.mapper

/**
 * A wger entrega a descrição em HTML (`<p>`, `<ul><li>`, `&nbsp;`...). O `Text` do Compose não
 * interpreta HTML, então a descrição é convertida aqui, uma vez, para texto simples com quebras de linha.
 */
object HtmlText {

    private val LINE_BREAKS = Regex("(?i)<br\\s*/?>|</p>|</div>|</h[1-6]>|</li>")
    private val LIST_ITEM = Regex("(?i)<li[^>]*>")
    private val TAGS = Regex("<[^>]+>")
    private val NUMERIC_ENTITY = Regex("&#(\\d+);")
    private val HORIZONTAL_SPACES = Regex("[ \\t\\u00A0]+")
    private val EXTRA_BLANK_LINES = Regex("\\n{3,}")

    private val NAMED_ENTITIES = mapOf(
        "&nbsp;" to " ",
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&quot;" to "\"",
        "&apos;" to "'",
    )

    fun toPlainText(html: String): String {
        var text = html
            .replace("\r\n", "\n")
            .replace(LINE_BREAKS, "\n")
            .replace(LIST_ITEM, "• ")
            .replace(TAGS, "")
        NAMED_ENTITIES.forEach { (entity, value) -> text = text.replace(entity, value) }
        text = text.replace(NUMERIC_ENTITY) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: ""
        }
        return text
            .lines()
            .joinToString("\n") { it.replace(HORIZONTAL_SPACES, " ").trim() }
            .replace(EXTRA_BLANK_LINES, "\n\n")
            .trim()
    }
}
