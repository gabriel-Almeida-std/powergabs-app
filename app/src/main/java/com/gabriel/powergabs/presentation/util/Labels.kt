package com.gabriel.powergabs.presentation.util

import androidx.annotation.StringRes
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.model.Equipment
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.Muscle
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.rule.CredentialsError

/*
 * Rótulos em português dos tipos do domain. O domain não conhece `R.string`;
 * a tradução para texto de tela é responsabilidade da presentation.
 */

@StringRes
fun Goal.labelRes(): Int = when (this) {
    Goal.HYPERTROPHY -> R.string.goal_hypertrophy
    Goal.STRENGTH -> R.string.goal_strength
    Goal.ENDURANCE -> R.string.goal_endurance
    Goal.CONDITIONING -> R.string.goal_conditioning
}

@StringRes
fun Goal.descriptionRes(): Int = when (this) {
    Goal.HYPERTROPHY -> R.string.goal_hypertrophy_description
    Goal.STRENGTH -> R.string.goal_strength_description
    Goal.ENDURANCE -> R.string.goal_endurance_description
    Goal.CONDITIONING -> R.string.goal_conditioning_description
}

@StringRes
fun MuscleGroup.labelRes(): Int = when (this) {
    MuscleGroup.ABS -> R.string.group_abs
    MuscleGroup.ARMS -> R.string.group_arms
    MuscleGroup.BACK -> R.string.group_back
    MuscleGroup.CALVES -> R.string.group_calves
    MuscleGroup.CARDIO -> R.string.group_cardio
    MuscleGroup.CHEST -> R.string.group_chest
    MuscleGroup.LEGS -> R.string.group_legs
    MuscleGroup.SHOULDERS -> R.string.group_shoulders
}

/** Nome do músculo em português pelo ID da wger; `null` se o app não tiver tradução (usa o nome da API). */
@StringRes
fun Muscle.labelResOrNull(): Int? = when (id) {
    1 -> R.string.muscle_1
    2 -> R.string.muscle_2
    3 -> R.string.muscle_3
    4 -> R.string.muscle_4
    5 -> R.string.muscle_5
    6 -> R.string.muscle_6
    7 -> R.string.muscle_7
    8 -> R.string.muscle_8
    9 -> R.string.muscle_9
    10 -> R.string.muscle_10
    11 -> R.string.muscle_11
    12 -> R.string.muscle_12
    13 -> R.string.muscle_13
    14 -> R.string.muscle_14
    15 -> R.string.muscle_15
    else -> null
}

/** Nome do equipamento em português pelo ID da wger; `null` se o app não tiver tradução. */
@StringRes
fun Equipment.labelResOrNull(): Int? = when (id) {
    1 -> R.string.equipment_1
    2 -> R.string.equipment_2
    3 -> R.string.equipment_3
    4 -> R.string.equipment_4
    5 -> R.string.equipment_5
    6 -> R.string.equipment_6
    7 -> R.string.equipment_7
    8 -> R.string.equipment_8
    9 -> R.string.equipment_9
    10 -> R.string.equipment_10
    11 -> R.string.equipment_11
    12 -> R.string.equipment_12
    else -> null
}

@StringRes
fun CredentialsError.messageRes(): Int = when (this) {
    CredentialsError.EMAIL_EMPTY -> R.string.error_email_empty
    CredentialsError.EMAIL_INVALID -> R.string.error_email_invalid
    CredentialsError.PASSWORD_EMPTY -> R.string.error_password_empty
    CredentialsError.PASSWORD_TOO_SHORT -> R.string.error_password_too_short
    CredentialsError.CONFIRMATION_MISMATCH -> R.string.error_confirmation_mismatch
}
