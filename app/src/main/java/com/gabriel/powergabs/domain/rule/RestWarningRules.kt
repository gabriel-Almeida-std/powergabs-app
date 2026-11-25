package com.gabriel.powergabs.domain.rule

import java.time.Instant
import java.time.ZoneId

/** Aviso de descanso (RN10). */
object RestWarningRules {

    /**
     * `true` se alguma conclusão ([doneDates]) do grupo caiu no dia anterior ao de [now],
     * considerando a data local do fuso [zone]. Treinos de hoje ou de anteontem não geram aviso.
     */
    fun shouldWarn(doneDates: List<Instant>, now: Instant, zone: ZoneId): Boolean {
        val yesterday = now.atZone(zone).toLocalDate().minusDays(1)
        return doneDates.any { it.atZone(zone).toLocalDate() == yesterday }
    }
}
