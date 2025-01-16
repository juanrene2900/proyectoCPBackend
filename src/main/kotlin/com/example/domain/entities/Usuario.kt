package com.example.domain.entities

import com.example.EstadosDeCuentaSerializer
import com.example.MetodosDeAutenticacionSerializer
import com.example.RolesSerializer
import com.example.application.res.UsuarioRes
import com.example.enums.EstadoDeCuenta
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.Rol
import com.example.utils.fechaActual
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.BsonDateTime
import org.bson.types.ObjectId

@Serializable
data class Usuario(

    @Contextual
    @SerialName("_id")
    val id: ObjectId,

    @SerialName("nombres_y_apellidos")
    val nombresYApellidos: String,

    val email: String,
    val celular: String,
    val direccion: String,
    val cedula: String,
    val contrasena: String,

    @SerialName("imagen_del_rostro_en_base64")
    val imagenDelRostroEnBase64: String?,

    @Serializable(with = RolesSerializer::class)
    val rol: Rol,

    @Serializable(with = MetodosDeAutenticacionSerializer::class)
    val metodoDeAutenticacion: MetodoDeAutenticacion?,

    @Serializable(with = EstadosDeCuentaSerializer::class)
    val estado: EstadoDeCuenta,

    @Contextual
    @SerialName("fecha_de_creacion")
    val fechaDeCreacion: BsonDateTime = fechaActual(),

    @Contextual
    @SerialName("fecha_de_actualizacion")
    val fechaDeActualizacion: BsonDateTime = fechaActual(),

    // Define a qué usuario le pertenece este usuario.
    // En otras palabras, a qué Admin pertenece este Usuario.
    @Contextual
    val admin: ObjectId? = null,
)

fun Usuario.convertirARespuesta(token: String) = UsuarioRes(
    nombresYApellidos,
    rol,
    token,
    email
)