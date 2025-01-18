package com.example.domain.ports

import com.example.application.req.ActualizarClienteReq
import com.example.application.req.UsuarioReq
import com.example.domain.entities.Usuario
import io.ktor.server.application.*
import org.bson.types.ObjectId

interface RepoUsuarios {

    suspend fun crearUsuario(call: ApplicationCall, usuario: UsuarioReq)

    suspend fun obtenerUsuario(idUsuario: ObjectId): Usuario?

    suspend fun cambiarContrasena(email: String, nuevaContrasena: String)

    suspend fun obtenerUsuario(email: String): Usuario?

    suspend fun crearCliente(call: ApplicationCall, usuario: UsuarioReq)

    suspend fun actualizarCliente(call: ApplicationCall, idCliente: String, actualizarCliente: ActualizarClienteReq)

    suspend fun eliminarCliente(idAdmin: String, idCliente: String)

    suspend fun listarClientes(call: ApplicationCall)

    suspend fun crearIndicesDeColeccion()
}