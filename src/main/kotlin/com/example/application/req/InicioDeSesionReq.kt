package com.example.application.req

import com.example.MetodosDeAutenticacionSerializer
import com.example.enums.MetodoDeAutenticacion
import com.example.utils.esEmailValido
import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.Serializable

@Serializable
data class InicioDeSesionReq(
    val email: String,
    val contrasena: String,

    @Serializable(with = MetodosDeAutenticacionSerializer::class)
    val metodoDeAutenticacion: MetodoDeAutenticacion,
)

fun InicioDeSesionReq.validarFormato(): ValidationResult {
    if (!esEmailValido(email)) {
        return ValidationResult.Invalid("Email inválido")
    }
    if (contrasena.isEmpty()) {
        return ValidationResult.Invalid("La contraseña no puede estar vacía")
    }
    return ValidationResult.Valid
}