package com.example.application.req

import com.example.EstadosDeCuentaSerializer
import com.example.MetodosDeAutenticacionSerializer
import com.example.domain.entities.Usuario
import com.example.enums.EstadoDeCuenta
import com.example.enums.MetodoDeAutenticacion
import com.example.utils.serialName
import com.mongodb.client.model.Updates
import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson

@Serializable
data class ActualizarClienteReq(
    @SerialName("nombres_y_apellidos")
    val nombresYApellidos: String,

    val celular: String,
    val direccion: String,

    @SerialName("metodo_de_autenticacion")
    @Serializable(with = MetodosDeAutenticacionSerializer::class)
    val metodoDeAutenticacion: MetodoDeAutenticacion?,

    @SerialName("imagen_del_rostro_en_base_64")
    val imagenDelRostroEnBase64: String?,

    @Serializable(with = EstadosDeCuentaSerializer::class)
    val estado: EstadoDeCuenta,
)

fun ActualizarClienteReq.validarFormato(): ValidationResult {
    if (nombresYApellidos.length !in 1..200) {
        return ValidationResult.Invalid("La longitud de nombres y apellidos debe ser entre 1 y 200")
    }
    if (celular.length !in 1..20) {
        return ValidationResult.Invalid("La longitud de teléfono debe ser entre 1 y 20")
    }
    if (direccion.length !in 1..200) {
        return ValidationResult.Invalid("La longitud de dirección debe ser entre 1 y 200")
    }
    if (imagenDelRostroEnBase64 != null && imagenDelRostroEnBase64.isEmpty()) {
        return ValidationResult.Invalid("La imagen del rostro en base64 no puede estar vacía")
    }
    if (metodoDeAutenticacion == null) {
        return ValidationResult.Invalid("Solo un administrador puede usar cualquier método de autenticación")
    }
    return ValidationResult.Valid
}

fun ActualizarClienteReq.parametrosActualizar(): Bson = Updates.combine(
    listOfNotNull(
        Updates.set(
            Usuario::nombresYApellidos.serialName,
            nombresYApellidos
        ),
        Updates.set(
            Usuario::celular.serialName,
            celular
        ),
        Updates.set(
            Usuario::direccion.serialName,
            direccion
        ),
        Updates.set(
            Usuario::estado.serialName,
            estado.id
        ),
        Updates.set(
            Usuario::metodoDeAutenticacion.serialName,
            metodoDeAutenticacion?.id
        ),
        if (imagenDelRostroEnBase64 != null)
            Updates.set(
                Usuario::imagenDelRostroEnBase64.serialName,
                imagenDelRostroEnBase64
            )
        else
            null
    )
)