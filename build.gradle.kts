// Arquivo de build raiz: declara os plugins usados pelo módulo :app, sem aplicá-los aqui.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
