package com.gabriel.powergabs.presentation.util

import com.gabriel.powergabs.domain.model.Prescription
import java.time.Duration
import java.time.Instant

/** Textos curtos calculados na presentation, em Kotlin puro para poderem ser testados. */
object Formatters {

    private const val EN_DASH = "–"

    /** `8..12` → "8–12"; `30..30` → "30". */
    fun range(range: IntRange): String =
        if (range.first == range.last) "${range.first}" else "${range.first}$EN_DASH${range.last}"

    /** Badge de prescrição da identidade visual: "3–4 × 8–12 · 60–90 s". */
    fun prescriptionBadge(prescription: Prescription): String =
        "${range(prescription.sets)} × ${range(prescription.reps)} · ${range(prescription.restSeconds)} s"

    /** Unidade usada no "atualizado há…" (RF08). */
    enum class ElapsedUnit { JUST_NOW, MINUTES, HOURS, DAYS }

    data class Elapsed(val unit: ElapsedUnit, val value: Long)

    /** Tempo desde a última atualização, na maior unidade inteira: menos de 1 min → "agora". */
    fun elapsedSince(lastUpdated: Instant, now: Instant): Elapsed {
        val duration = Duration.between(lastUpdated, now).coerceAtLeast(Duration.ZERO)
        return when {
            duration.toMinutes() < 1 -> Elapsed(ElapsedUnit.JUST_NOW, 0)
            duration.toHours() < 1 -> Elapsed(ElapsedUnit.MINUTES, duration.toMinutes())
            duration.toDays() < 1 -> Elapsed(ElapsedUnit.HOURS, duration.toHours())
            else -> Elapsed(ElapsedUnit.DAYS, duration.toDays())
        }
    }

    private fun Duration.coerceAtLeast(min: Duration): Duration = if (this < min) min else this
}
