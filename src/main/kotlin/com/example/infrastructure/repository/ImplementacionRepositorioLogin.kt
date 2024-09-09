package com.example.infrastructure.repository

import com.example.application.req.InicioDeSesionReq
import com.example.domain.ports.RepositorioJwt
import com.example.domain.ports.RepositorioLogin
import com.example.domain.ports.RepositorioUsuarios
import com.example.domain.ports.RepositorioValidaciones
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.RespuestaEnvioCodigo
import com.password4j.Password
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.bson.types.ObjectId

class ImplementacionRepositorioLogin(
    private val repositorioUsuarios: RepositorioUsuarios,
    private val repositorioJwt: RepositorioJwt,
    private val repositorioValidaciones: RepositorioValidaciones,
) : RepositorioLogin {

    override suspend fun loginUsuario(call: ApplicationCall, inicioDeSesion: InicioDeSesionReq) {
        suspend fun enviarTokenAlCliente(idUsuario: ObjectId) {
            call.respond(
                mapOf("token" to repositorioJwt.generarJwt(idUsuario).jwt)
            )
        }

        val usuario = repositorioUsuarios.obtenerUsuario(email = inicioDeSesion.email)
            ?: return call.respond(HttpStatusCode.Unauthorized)

        val contrasenasCoinciden = Password.check(inicioDeSesion.contrasena, usuario.contrasena).withArgon2()
        if (contrasenasCoinciden) {
            if (inicioDeSesion.metodoDeAutenticacion == MetodoDeAutenticacion.RECONOCIMIENTO_FACIAL) {
                enviarTokenAlCliente(idUsuario = usuario.id)
            } else {
                val respuesta = repositorioValidaciones.enviarCodigo(
                    metodoDeAutenticacion = inicioDeSesion.metodoDeAutenticacion,
                    idUsuario = usuario.id
                )

                return if (respuesta == RespuestaEnvioCodigo.ENVIADO) {
                    enviarTokenAlCliente(idUsuario = usuario.id)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
        call.respond(HttpStatusCode.Unauthorized)
    }
}