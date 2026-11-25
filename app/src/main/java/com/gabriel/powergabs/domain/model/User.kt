package com.gabriel.powergabs.domain.model

/** Usuário logado (RF02, RF03). A senha nunca sai da camada data. */
data class User(val id: Long, val email: String)
