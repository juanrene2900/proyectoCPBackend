package com.example.infrastructure.repository

import com.example.application.req.ValidarCodigoReq
import com.example.application.req.ValidarRostroReq
import com.example.domain.entities.CodigoUsuario
import com.example.domain.entities.convertirARespuesta
import com.example.domain.ports.RepoJwt
import com.example.domain.ports.RepoUsuarios
import com.example.domain.ports.RepoValidaciones
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.RespuestaEnvioCodigo
import com.example.utils.DetectorDeRostros
import com.example.utils.enviarEmail
import com.example.utils.enviarSms
import com.example.utils.serialName
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.bson.BsonDateTime
import org.bson.types.ObjectId
import java.time.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class ImplRepoValidaciones(
    db: MongoDatabase,
    private val repoUsuarios: RepoUsuarios,
    private val repoJwt: RepoJwt,
) : RepoValidaciones {

    private val codigos = db.getCollection<CodigoUsuario>("codigos")

    override suspend fun validarCodigo(call: ApplicationCall, validarCodigo: ValidarCodigoReq) {
        val idUsuario = ObjectId(call.principal<JWTPrincipal>()!!.subject)

        val usuario = repoUsuarios.obtenerUsuario(idUsuario)
            ?: return call.respond(HttpStatusCode.Unauthorized)

        val cincoMinutosAntesDeLaHoraActual = Instant.now().minusSeconds(5.minutes.inWholeSeconds)

        val filtroCodigo = Filters.and(
            Filters.eq(CodigoUsuario::codigo.serialName, validarCodigo.codigo),
            Filters.eq(CodigoUsuario::usuario.serialName, idUsuario),
            Filters.gte(
                CodigoUsuario::fechaDeCreacion.serialName,
                BsonDateTime(cincoMinutosAntesDeLaHoraActual.toEpochMilli())
            )
        )

        // Obtenemos el código y a la vez lo eliminamos para que no vuelva a ser usado.
        val codigo = codigos.findOneAndDelete(filtroCodigo)

        if (codigo != null) {
            val respuesta = usuario.convertirARespuesta(
                token = repoJwt.generarJwt(idUsuario = usuario.id).jwt
            )
            return call.respond(respuesta)
        }
        call.respond(HttpStatusCode.Unauthorized)
    }

    override suspend fun validarCodigoPorEmail(email: String, validarCodigo: ValidarCodigoReq) {
        val cincoMinutosAntesDeLaHoraActual = Instant.now().minusSeconds(5.minutes.inWholeSeconds)

        // Buscamos el código en sí.
        val filtroCodigo = Filters.and(
            Filters.eq(CodigoUsuario::codigo.serialName, validarCodigo.codigo),
            Filters.gte(
                CodigoUsuario::fechaDeCreacion.serialName,
                BsonDateTime(cincoMinutosAntesDeLaHoraActual.toEpochMilli())
            )
        )
        val codigo = codigos.findOneAndDelete(filtroCodigo)

        if (codigo != null) {
            // Buscamos el email con id del usuario
            val usuario = repoUsuarios.obtenerUsuario(codigo.usuario)
                ?: throw Exception("Usuario no encontrado")

            if (usuario.email != email) {
                throw Exception("El email no coincide con el usuario")
            }
            // Tod0 fue bien, continuar con el proceso
            return
        }
        throw Exception("Código inválido")
    }

    override suspend fun validarRostro(call: ApplicationCall, validarRostro: ValidarRostroReq) {
        val idUsuario = ObjectId(call.principal<JWTPrincipal>()!!.subject)

        val usuario = repoUsuarios.obtenerUsuario(idUsuario)
            ?: return call.respond(HttpStatusCode.Unauthorized)

        val rostroUsuarioEnBase64 = usuario.imagenDelRostroEnBase64
            ?: return call.respond(HttpStatusCode.Unauthorized)

        val sonRostrosIguales = DetectorDeRostros.comparar(
            rostroGuardado = rostroUsuarioEnBase64,
            rostroAComparar = validarRostro.imagenDelRostroEnBase64
        )

        if (sonRostrosIguales) {
            val respuesta = usuario.convertirARespuesta(
                token = repoJwt.generarJwt(idUsuario = usuario.id).jwt
            )
            return call.respond(respuesta)
        }
        call.respond(HttpStatusCode.Unauthorized)
    }

    override suspend fun enviarCodigoAleatorio(
        idUsuario: ObjectId,
        metodoDeAutenticacion: MetodoDeAutenticacion,
    ): RespuestaEnvioCodigo {
        try {
            val usuario = repoUsuarios.obtenerUsuario(idUsuario)
                ?: return RespuestaEnvioCodigo.ERROR

            // Eliminamos todos los códigos generados anteriormente.
            val filtroCodigos = Filters.eq(CodigoUsuario::usuario.serialName, usuario.id)
            val eliminacion = codigos.deleteMany(filtroCodigos)
            if (!eliminacion.wasAcknowledged()) {
                return RespuestaEnvioCodigo.ERROR
            }

            val codigo = generarCodigoAleatorio(longitud = 6)

            val codigoUsuario = CodigoUsuario(codigo = codigo, usuario = usuario.id)
            val insercion = codigos.insertOne(codigoUsuario)

            if (insercion.wasAcknowledged() && insercion.insertedId != null) {
                val contenido = """
                    Buen día, ${usuario.nombresYApellidos}
                    
                    Use el siguiente código para continuar la operación.
                    El código es válido por 5 minutos. Por favor no lo comparta con nadie.
                    
                    $codigo
                """.trimIndent()

                // El token automático envía los dos tokens...

                try {
                    enviarEmail(emailDelDestinatario = usuario.email, contenido)
                } catch (e: Exception) {
                    // Ignorar si da cualquier error...
                }

                try {
                    val celularEcuatoriano = "+593${usuario.celular}"
                    enviarSms(celularDelDestinatario = celularEcuatoriano, contenido)
                } catch (e: Exception) {
                    // Ignorar si da cualquier error...
                }

                return RespuestaEnvioCodigo.ENVIADO
            }
        } catch (_: Exception) {
        }
        return RespuestaEnvioCodigo.ERROR
    }
}

@Suppress("SameParameterValue")
private fun generarCodigoAleatorio(longitud: Int): String {
    return (1..longitud)
        .map { Random.nextInt(0, 10) }
        .joinToString("")
}