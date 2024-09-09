package com.example.infrastructure.repository

import com.example.application.req.UsuarioReq
import com.example.application.req.convertirADominio
import com.example.domain.entities.Usuario
import com.example.domain.ports.RepositorioUsuarios
import com.example.utils.serialName
import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.singleOrNull
import org.bson.types.ObjectId

class ImplementacionRepositorioUsuarios(db: MongoDatabase) : RepositorioUsuarios {

    private val usuarios = db.getCollection<Usuario>("usuarios")

    override suspend fun crearUsuario(call: ApplicationCall, usuario: UsuarioReq) {
        try {
            val insercionUsuario = usuarios.insertOne(usuario.convertirADominio())
            if (insercionUsuario.wasAcknowledged() && insercionUsuario.insertedId != null) {
                return call.respond(HttpStatusCode.Created)
            }
        } catch (e: MongoException) {
            // Si el código de error es 11000 quiere decir que hubo un error de duplicado
            if (e.code == 11_000) {
                return call.respond(HttpStatusCode.Conflict)
            }
        }
        call.respond(HttpStatusCode.InternalServerError)
    }

    override suspend fun obtenerUsuario(idUsuario: ObjectId): Usuario? {
        val filtroUsuario = Filters.eq(Usuario::id.serialName, idUsuario)
        return usuarios.find(filtroUsuario).singleOrNull()
    }

    override suspend fun obtenerUsuario(email: String): Usuario? {
        val filtroUsuario = Filters.eq(Usuario::email.serialName, email)
        return usuarios.find(filtroUsuario).singleOrNull()
    }

    // Con IndexOptions().unique(true) hacemos que los campos sean únicos
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