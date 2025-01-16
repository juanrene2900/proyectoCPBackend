package com.example.application.req

import com.example.MetodosDeAutenticacionSerializer
import com.example.RolesSerializer
import com.example.domain.entities.Usuario
import com.example.enums.EstadoDeCuenta
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.Rol
import com.example.utils.ValidadorDeCedulas
import com.example.utils.esEmailValido
import com.password4j.Password
import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class UsuarioReq(

    @SerialName("nombres_y_apellidos")
    val nombresYApellidos: String,

    val email: String,
    val celular: String,
    val direccion: String,
    val cedula: String,
    val contrasena: String,

    @Serializable(with = RolesSerializer::class)
    val rol: Rol,

    @SerialName("metodo_de_autenticacion")
    @Serializable(with = MetodosDeAutenticacionSerializer::class)
    val metodoDeAutenticacion: MetodoDeAutenticacion?,

    @SerialName("imagen_del_rostro_en_base_64")
    val imagenDelRostroEnBase64: String?,
)

fun UsuarioReq.validarFormato(): ValidationResult {
    if (nombresYApellidos.length !in 1..200) {
        return ValidationResult.Invalid("La longitud de nombres y apellidos debe ser entre 1 y 200")
    }
    if (!esEmailValido(email)) {
        return ValidationResult.Invalid("Email inválido")
    }
    if (celular.length !in 1..20) {
        return ValidationResult.Invalid("La longitud de teléfono debe ser entre 1 y 20")
    }
    if (direccion.length !in 1..200) {
        return ValidationResult.Invalid("La longitud de dirección debe ser entre 1 y 200")
    }
    if (!ValidadorDeCedulas.esValida(cedula)) {
        return ValidationResult.Invalid("Cédula inválida")
    }
    if (contrasena.length !in 8..20) {
        return ValidationResult.Invalid("La longitud de la contraseña debe ser entre 8 y 20")
    }
    if (imagenDelRostroEnBase64 != null && imagenDelRostroEnBase64.isEmpty()) {
        return ValidationResult.Invalid("La imagen del rostro en base64 no puede estar vacía")
    }
    if (metodoDeAutenticacion == null && rol != Rol.ADMIN) {
        return ValidationResult.Invalid("Solo un administrador puede usar cualquier método de autenticación")
    }
    return ValidationResult.Valid
}

fun UsuarioReq.convertirADominio(usuario: ObjectId? = null): Usuario {
    val contrasenaHasheada = Password.hash(contrasena).withArgon2().result

    return Usuario(
        id = ObjectId(),
        nombresYApellidos,
        email,
        celular,
        direccion,
        cedula,
        contrasena = contrasenaHasheada,
        imagenDelRostroEnBase64,
        rol,
        metodoDeAutenticacion,
        estado = EstadoDeCuenta.ACTIVO,
        admin = usuario
    )
}