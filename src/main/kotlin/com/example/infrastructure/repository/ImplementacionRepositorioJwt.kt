package com.example.infrastructure.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.entities.JsonWebToken
import com.example.domain.ports.RepositorioJwt
import com.example.utils.serialName
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import org.bson.types.ObjectId
import java.util.*
import kotlin.time.Duration.Companion.hours

class ImplementacionRepositorioJwt(db: MongoDatabase) : RepositorioJwt {

    private val jwts = db.getCollection<JsonWebToken>("jwts")

    override suspend fun generarJwt(idUsuario: ObjectId): JsonWebToken {
        // Eliminamos todos los jwts anteriores
        val filtro = Filters.eq(JsonWebToken::usuario.serialName, idUsuario)
        val eliminacion = jwts.deleteMany(filtro)
        check(eliminacion.wasAcknowledged())

        // Creamos el nuevo token

        // El token dura hasta 1 hora
        val duracion = Date(System.currentTimeMillis() + 1.hours.inWholeMilliseconds)

        val jwt = JWT.create()
            .withSubject(idUsuario.toHexString()) // En los argumentos agregamos el id del usuario
            .withAudience(System.getenv("JWT_AUDIENCIA"))
            .withIssuer(System.getenv("JWT_EMISOR"))
            .withExpiresAt(duracion)
            .sign(Algorithm.HMAC256(System.getenv("JWT_SECRETO")))

        // Insertamos el nuevo token

        val jsonWebToken = JsonWebToken(jwt, usuario = idUsuario)
        val insercion = jwts.insertOne(jsonWebToken)
        check(insercion.wasAcknowledged() && insercion.insertedId != null)

        return jsonWebToken
    }

    override suspend fun jwtExiste(idUsuario: ObjectId, jwt: String): Boolean {
        val filtroJwt = Filters.and(
            Filters.eq(JsonWebToken::jwt.serialName, eliminarBearer(jwt)),
            Filters.eq(JsonWebToken::usuario.serialName, idUsuario),
        )
        val jsonWebToken = jwts.find(filtroJwt).singleOrNull()

        return jsonWebToken != null
    }
}

private fun eliminarBearer(jwt: String): String {
    val patron = "Bearer "
    if (jwt.startsWith(patron)) {
        return jwt.substring(patron.length)
    }
    return jwt
}