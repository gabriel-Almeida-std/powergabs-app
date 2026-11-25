package com.gabriel.powergabs.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class HtmlTextTest {

    @Test
    fun `paragraphs become separate lines`() {
        val text = HtmlText.toPlainText("<p>Primeiro passo.</p>\n<p>Segundo passo.</p>")

        assertEquals("Primeiro passo.\n\nSegundo passo.", text)
    }

    @Test
    fun `list items become bullets`() {
        val text = HtmlText.toPlainText("<ul><li>Um</li><li>Dois</li></ul>")

        assertEquals("• Um\n• Dois", text)
    }

    @Test
    fun `entities are decoded and extra spaces removed`() {
        val text = HtmlText.toPlainText("Peso&nbsp;&nbsp;corporal &amp; banco &#233;")

        assertEquals("Peso corporal & banco é", text)
    }

    @Test
    fun `plain text is kept`() {
        assertEquals("Sem HTML", HtmlText.toPlainText("  Sem HTML  "))
    }

    @Test
    fun `line breaks are respected`() {
        assertEquals("A\nB", HtmlText.toPlainText("A<br>B"))
    }
}
