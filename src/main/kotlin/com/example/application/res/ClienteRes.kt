package com.example.application.res

import com.example.EstadosDeCuentaSerializer
import com.example.enums.EstadoDeCuenta
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class ClienteRes(

    @Contextual
    @SerialName("_id")
    val id: ObjectId,

    @SerialName("nombres_y_apellidos")
    val nombresYApellidos: String,

    val email: String,
    val celular: String,
    val direccion: String,
    val cedula: String,

    @Serializable(with = EstadosDeCuentaSerializer::class)
    val estado: EstadoDeCuenta
)