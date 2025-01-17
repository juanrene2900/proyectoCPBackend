package com.example.application.req

import com.example.utils.esEmailValido
import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CambiarContrasenaReq(
    val email: String,

    @SerialName("nueva_contrasena")
    val nuevoContrasena: String,

    val codigo: String
)

fun CambiarContrasenaReq.validate(): ValidationResult {
    if (!esEmailValido(email)) {
        return ValidationResult.Invalid("Email inválido")
    }
    return ValidationResult.Valid
}