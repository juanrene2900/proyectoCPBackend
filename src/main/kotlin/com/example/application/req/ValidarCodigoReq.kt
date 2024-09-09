package com.example.application.req

import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.Serializable

@Serializable
data class ValidarCodigoReq(
    val codigo: String,
)

fun ValidarCodigoReq.validarFormato(): ValidationResult {
    if (codigo.length != 6 || !codigo.all { it.isDigit() }) {
        return ValidationResult.Invalid("Código inválido")
    }
    return ValidationResult.Valid
}