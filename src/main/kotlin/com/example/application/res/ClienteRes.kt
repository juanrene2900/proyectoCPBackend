package com.example.application.res

import com.example.EstadosDeCuentaSerializer
import com.example.enums.EstadoDeCuenta
import com.example.enums.MetodoDeAutenticacion
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
    val estado: EstadoDeCuenta,

    @Serializable(with = MetodoDeAutenticacionSerializer::class)
    @SerialName("metodo_de_autenticacion")
    val metodoDeAutenticacion: MetodoDeAutenticacion
)

private object MetodoDeAutenticacionSerializer : KSerializer<MetodoDeAutenticacion> {
    override val descriptor = PrimitiveSerialDescriptor("MetodoDeAutenticacion", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MetodoDeAutenticacion {
        val id = decoder.decodeString()
        return MetodoDeAutenticacion.entries.first { it.id == id }
    }

    override fun serialize(encoder: Encoder, value: MetodoDeAutenticacion) {
        encoder.encodeString(value.id)
    }
}