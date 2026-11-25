package com.gabriel.powergabs.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Configuração de rede centralizada (usada pelo `dataModule` do Koin). */
object NetworkFactory {

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 30L

    /** Lê só o que o app usa e tolera campos ausentes ou nulos. */
    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    /** OkHttp com timeouts; o log HTTP só é ligado em depuração, para não expor tráfego em produção. */
    fun okHttpClient(enableLogging: Boolean): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (enableLogging) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

    fun wgerApi(client: OkHttpClient, json: Json): WgerApi =
        Retrofit.Builder()
            .baseUrl(WgerApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WgerApi::class.java)
}
