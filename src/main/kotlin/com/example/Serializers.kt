package com.example

import com.example.enums.EstadoDeCuenta
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.Rol
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

object EstadosDeCuentaSerializer : KSerializer<EstadoDeCuenta> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(EstadosDeCuentaSerializer::class.simpleName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): EstadoDeCuenta {
        val id = decoder.decodeString()
        return EstadoDeCuenta.entries.first { it.id == id }
    }

    override fun serialize(encoder: Encoder, value: EstadoDeCuenta) {
        encoder.encodeString(value.id)
    }
}

object RolesSerializer : KSerializer<Rol> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(RolesSerializer::class.simpleName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Rol {
        val id = decoder.decodeString()
        return Rol.entries.first { it.id == id }
    }

    override fun serialize(encoder: Encoder, value: Rol) {
        encoder.encodeString(value.id)
    }
}

object MetodosDeAutenticacionSerializer : KSerializer<MetodoDeAutenticacion> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(MetodosDeAutenticacionSerializer::class.simpleName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MetodoDeAutenticacion {
        val id = decoder.decodeString()
        return MetodoDeAutenticacion.entries.first { it.id == id }
    }

    override fun serialize(encoder: Encoder, value: MetodoDeAutenticacion) {
        encoder.encodeString(value.id)
    }
}

object ObjectIdSerializer : KSerializer<ObjectId> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(ObjectIdSerializer::class.simpleName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ObjectId {
        return ObjectId(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: ObjectId) {
        encoder.encodeString(value.toHexString())
    }
}