package com.gabriel.powergabs.core.time

import java.time.Instant
import java.time.ZoneId

/**
 * Fonte do "agora" (RN17).
 *
 * Regras como "dia anterior" (RN10) e "24 horas" (RN09) pedem o horário a esta interface,
 * em vez de chamar `Instant.now()` diretamente. Nos testes, um provedor fixo controla o tempo.
 */
interface TimeProvider {
    fun now(): Instant
    fun zone(): ZoneId
}

/** Implementação real: relógio e fuso horário do aparelho. */
class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
