package com.example.application.req

import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidarRostroReq(

    @SerialName("imagen_del_rostro_en_base_64")
    val imagenDelRostroEnBase64: String,
)

fun ValidarRostroReq.validarFormato(): ValidationResult {
    if (imagenDelRostroEnBase64.isEmpty()) {
        return ValidationResult.Invalid("La imagen en base64 no puede estar vacía")
    }
    return ValidationResult.Valid
}