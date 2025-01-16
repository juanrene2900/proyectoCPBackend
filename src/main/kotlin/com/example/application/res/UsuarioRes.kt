package com.example.application.res

import com.example.RolesSerializer
import com.example.enums.Rol
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioRes(

    @SerialName("nombres_y_apellidos")
    val nombresYApellidos: String,

    @Serializable(with = RolesSerializer::class)
    val rol: Rol,

    val token: String,

    val email: String,
)