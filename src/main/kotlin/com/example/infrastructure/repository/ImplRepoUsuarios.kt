package com.example.infrastructure.repository

import com.example.application.req.ActualizarClienteReq
import com.example.application.req.UsuarioReq
import com.example.application.req.convertirADominio
import com.example.application.req.parametrosActualizar
import com.example.application.res.ClienteRes
import com.example.domain.entities.Usuario
import com.example.domain.ports.RepoUsuarios
import com.example.enums.Rol
import com.example.utils.serialName
import com.mongodb.MongoException
import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class ImplRepoUsuarios(db: MongoDatabase) : RepoUsuarios {

    private val usuarios = db.getCollection<Usuario>("usuarios")

    override suspend fun crearUsuario(call: ApplicationCall, usuario: UsuarioReq) {
        try {
            val insercionUsuario = usuarios.insertOne(usuario.convertirADominio())
            if (insercionUsuario.wasAcknowledged() && insercionUsuario.insertedId != null) {
                return call.respond(HttpStatusCode.Created)
            }
        } catch (e: MongoException) {
            // Si el código de error es 11000 quiere decir que hubo un error de duplicado.
            if (e.code == 11_000) {
                return call.respond(HttpStatusCode.Conflict)
            }
        }
        call.respond(HttpStatusCode.InternalServerError)
    }

    override suspend fun obtenerUsuario(idUsuario: ObjectId): Usuario? {
        val filtroUsuario = Filters.eq(Usuario::id.serialName, idUsuario)
        return usuarios.find(filtroUsuario).firstOrNull()
    }

    override suspend fun cambiarContrasena(email: String, nuevaContrasena: String) {
        val filtroUsuario = Filters.eq(Usuario::email.serialName, email)
        val actualizacion = usuarios.updateOne(
            filtroUsuario,
            Updates.set(Usuario::contrasena.serialName, nuevaContrasena)
        )

        if (actualizacion.wasAcknowledged() && actualizacion.modifiedCount == 1L) {
            return
        }
        throw Exception("No se pudo cambiar la contraseña.")
    }

    override suspend fun obtenerUsuario(email: String): Usuario? {
        val filtroUsuario = Filters.eq(Usuario::email.serialName, email)
        return usuarios.find(filtroUsuario).singleOrNull()
    }

    override suspend fun crearCliente(call: ApplicationCall, usuario: UsuarioReq) {
        val idAdmin = ObjectId(call.principal<JWTPrincipal>()!!.subject)

        try {
            val resultado = usuarios.insertOne(usuario.convertirADominio(idAdmin))
            if (resultado.wasAcknowledged() && resultado.insertedId != null) {
                return call.respond(HttpStatusCode.Created)
            }
        } catch (e: MongoException) {
            // Si el código de error es 11000 quiere decir que hubo un error de duplicado.
            if (e.code == 11_000) {
                return call.respond(HttpStatusCode.Conflict)
            }
        }
        call.respond(HttpStatusCode.InternalServerError)
    }

    override suspend fun actualizarCliente(
        call: ApplicationCall,
        idCliente: String,
        actualizarCliente: ActualizarClienteReq
    ) {
        val idAdmin = ObjectId(call.principal<JWTPrincipal>()!!.subject)

        val filtroCliente = Filters.and(
            Filters.eq(Usuario::id.serialName, ObjectId(idCliente)),
            Filters.eq(Usuario::admin.serialName, idAdmin),
            Filters.eq(Usuario::rol.serialName, Rol.CLIENTE.id) // Por si acaso.
        )

        val actualizacion = usuarios.updateOne(filtroCliente, actualizarCliente.parametrosActualizar())

        if (actualizacion.wasAcknowledged() && actualizacion.matchedCount == 1L) {
            return call.respond(HttpStatusCode.OK)
        }
        call.respond(HttpStatusCode.InternalServerError)
    }

    override suspend fun eliminarCliente(idAdmin: String, idCliente: String) {
        val filtroCliente = Filters.and(
            Filters.eq(Usuario::id.serialName, ObjectId(idCliente)),
            Filters.eq(Usuario::admin.serialName, ObjectId(idAdmin)),
            Filters.eq(Usuario::rol.serialName, Rol.CLIENTE.id) // Por si acaso.
        )
        val eliminacion = usuarios.deleteOne(filtroCliente)
        if (eliminacion.wasAcknowledged() && eliminacion.deletedCount == 1L) {
            return
        }
        throw Exception("No se pudo eliminar el cliente.")
    }

    override suspend fun listarClientes(call: ApplicationCall) {
        val idAdmin = ObjectId(call.principal<JWTPrincipal>()!!.subject)

        val filtroClientes = Filters.and(
            Filters.eq(Usuario::admin.serialName, idAdmin),
            Filters.eq(Usuario::rol.serialName, Rol.CLIENTE.id), // Por si acaso.
        )
        val usuarios = usuarios.withDocumentClass<ClienteRes>().find(filtroClientes).toList()

        call.respond(usuarios)
    }

    // Con IndexOptions().unique(true) hacemos que los campos sean únicos.
    override suspend fun crearIndicesDeColeccion() {
        usuarios.createIndexes(
            listOf(
                IndexModel(
                    Indexes.ascending(Usuario::email.serialName),
                    IndexOptions().unique(true)
                ),
                IndexModel(
                    Indexes.ascending(Usuario::cedula.serialName),
                    IndexOptions().unique(true)
                )
            )
        ).collect()
    }
}