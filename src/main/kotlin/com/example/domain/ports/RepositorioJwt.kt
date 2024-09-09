package com.example.domain.ports

import com.example.domain.entities.JsonWebToken
import org.bson.types.ObjectId

interface RepositorioJwt {

    suspend fun generarJwt(idUsuario: ObjectId): JsonWebToken

    suspend fun jwtExiste(idUsuario: ObjectId, jwt: String): Boolean
}