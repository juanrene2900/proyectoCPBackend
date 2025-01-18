package com.example.domain.ports

import com.example.domain.entities.JsonWebToken
import org.bson.types.ObjectId

interface RepoJwt {

    suspend fun generarJwt(idUsuario: ObjectId): JsonWebToken

    suspend fun jwtExiste(idUsuario: ObjectId, jwt: String): Boolean
}