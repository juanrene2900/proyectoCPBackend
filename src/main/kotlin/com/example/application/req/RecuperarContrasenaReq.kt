package com.example.application.req

import com.example.utils.esEmailValido
import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.Serializable

@Serializable
data class RecuperarContrasenaReq(
    val email: String
)

fun RecuperarContrasenaReq.validate(): ValidationResult {
    if (!esEmailValido(email)) {
        return ValidationResult.Invalid("Email inválido")
    }
    return ValidationResult.Valid
}