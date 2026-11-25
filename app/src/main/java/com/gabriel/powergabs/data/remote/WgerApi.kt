package com.gabriel.powergabs.data.remote

import com.gabriel.powergabs.data.remote.dto.ExerciseInfoDto
import com.gabriel.powergabs.data.remote.dto.PageDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API pública da wger (leitura sem autenticação).
 * O Retrofit gera a implementação desta interface; funções `suspend` rodam em coroutines.
 */
interface WgerApi {

    /** Exercícios de uma categoria (grupo muscular), paginados por `limit` e `offset`. */
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("category") categoryId: Int,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("offset") offset: Int = 0,
    ): PageDto<ExerciseInfoDto>

    companion object {
        const val BASE_URL = "https://wger.de/api/v2/"
        const val PAGE_SIZE = 100

        /** Trava de segurança: no máximo 10 páginas (1 000 exercícios) por grupo. */
        const val MAX_PAGES = 10
    }
}
