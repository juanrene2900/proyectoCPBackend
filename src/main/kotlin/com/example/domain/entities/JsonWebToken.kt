package com.example.domain.entities

import com.example.utils.fechaActual
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.BsonDateTime
import org.bson.types.ObjectId

@Serializable
data class JsonWebToken(
    val jwt: String,

    @Contextual
    val usuario: ObjectId,

    @Contextual
    @SerialName("fecha_de_creacion")
    val fechaDeCreacion: BsonDateTime = fechaActual(),
)