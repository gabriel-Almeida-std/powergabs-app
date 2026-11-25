package com.gabriel.powergabs.domain.rule

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/** Horário do lembrete diário (RN16). */
object ReminderRules {

    val REMINDER_TIME: LocalTime = LocalTime.of(18, 0)

    /**
     * Tempo até o próximo lembrete: hoje às 18:00 se ainda não chegou essa hora;
     * caso contrário (inclusive exatamente às 18:00), amanhã às 18:00.
     */
    fun delayUntilNextReminder(now: Instant, zone: ZoneId): Duration {
        val localNow = now.atZone(zone)
        var next = localNow.toLocalDate().atTime(REMINDER_TIME).atZone(zone)
        if (!next.isAfter(localNow)) next = next.plusDays(1)
        return Duration.between(now, next.toInstant())
    }
}
