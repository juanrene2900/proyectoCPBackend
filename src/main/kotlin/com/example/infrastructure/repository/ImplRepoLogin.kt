package com.example.infrastructure.repository

import com.example.application.req.InicioDeSesionReq
import com.example.domain.ports.RepoJwt
import com.example.domain.ports.RepoLogin
import com.example.domain.ports.RepoUsuarios
import com.example.domain.ports.RepoValidaciones
import com.example.enums.EstadoDeCuenta
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.RespuestaEnvioCodigo
import com.password4j.Password
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.bson.types.ObjectId

class ImplRepoLogin(
    private val repoUsuarios: RepoUsuarios,
    private val repoJwt: RepoJwt,
    private val repoValidaciones: RepoValidaciones,
) : RepoLogin {

    override suspend fun loginUsuario(call: ApplicationCall, inicioDeSesion: InicioDeSesionReq) {
        suspend fun enviarTokenAlCliente(idUsuario: ObjectId) {
            call.respond(
                mapOf("token" to repoJwt.generarJwt(idUsuario).jwt)
            )
        }

        val usuario = repoUsuarios.obtenerUsuario(email = inicioDeSesion.email)
            ?: return call.respond(HttpStatusCode.Unauthorized)

        if (usuario.estado == EstadoDeCuenta.INACTIVO) {
            // Usamos Forbidden para indicar que el usuario no está activo. Es personalizable.
            return call.respond(HttpStatusCode.Forbidden)
        }

        val contrasenasCoinciden = Password.check(inicioDeSesion.contrasena, usuario.contrasena).withArgon2()

        if (contrasenasCoinciden) {
            if (usuario.metodoDeAutenticacion != null) {
                if (usuario.metodoDeAutenticacion != inicioDeSesion.metodoDeAutenticacion) {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }

            if (inicioDeSesion.metodoDeAutenticacion == MetodoDeAutenticacion.RECONOCIMIENTO_FACIAL) {
                enviarTokenAlCliente(idUsuario = usuario.id)
            } else {
                val respuesta = repoValidaciones.enviarCodigoAleatorio(
                    metodoDeAutenticacion = inicioDeSesion.metodoDeAutenticacion,
                    idUsuario = usuario.id
                )

                if (respuesta == RespuestaEnvioCodigo.ENVIADO) {
                    enviarTokenAlCliente(idUsuario = usuario.id)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}