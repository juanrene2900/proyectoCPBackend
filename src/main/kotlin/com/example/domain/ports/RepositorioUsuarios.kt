package com.example.domain.ports

import com.example.application.req.UsuarioReq
import com.example.domain.entities.Usuario
import io.ktor.server.application.*
import org.bson.types.ObjectId

interface RepositorioUsuarios {

    suspend fun crearUsuario(call: ApplicationCall, usuario: UsuarioReq)

    suspend fun obtenerUsuario(idUsuario: ObjectId): Usuario?

    suspend fun obtenerUsuario(email: String): Usuario?

    suspend fun crearIndicesDeColeccion()
}