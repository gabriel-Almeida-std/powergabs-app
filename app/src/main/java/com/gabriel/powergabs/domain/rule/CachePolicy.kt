package com.gabriel.powergabs.domain.rule

import java.time.Duration
import java.time.Instant

/** Validade do cache de exercícios de cada grupo muscular (RN09). */
object CachePolicy {

    val VALIDITY: Duration = Duration.ofHours(24)

    /**
     * O cache está expirado se nunca foi baixado (`null`) ou se já se passaram
     * 24 horas ou mais desde a última atualização. Exatamente 24 h conta como expirado.
     */
    fun isExpired(lastUpdated: Instant?, now: Instant): Boolean =
        lastUpdated == null || !now.isBefore(lastUpdated.plus(VALIDITY))
}
