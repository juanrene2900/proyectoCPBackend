package com.example.infrastructure.repository

import com.example.application.req.CambiarContrasenaReq
import com.example.application.req.InicioDeSesionReq
import com.example.application.req.RecuperarContrasenaReq
import com.example.application.req.ValidarCodigoReq
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
        suspend fun enviarTokenAlCliente(call: ApplicationCall, idUsuario: ObjectId) {
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
                    return call.respond(HttpStatusCode.Forbidden)
                }
            }

            if (inicioDeSesion.metodoDeAutenticacion == MetodoDeAutenticacion.RECONOCIMIENTO_FACIAL) {
                enviarTokenAlCliente(call, idUsuario = usuario.id)
            } else {
                val respuesta = repoValidaciones.enviarCodigoAleatorio(
                    metodoDeAutenticacion = inicioDeSesion.metodoDeAutenticacion,
                    idUsuario = usuario.id
                )

                if (respuesta == RespuestaEnvioCodigo.ENVIADO) {
                    enviarTokenAlCliente(call, idUsuario = usuario.id)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    override suspend fun solicitarRecuperarContrasena(
        call: ApplicationCall,
        recuperarContrasena: RecuperarContrasenaReq
    ) {
        // Para este ejemplo, usamos el siguiente flujo:

        // 1. Buscamos el usuario por el email.

        val usuario = repoUsuarios.obtenerUsuario(email = recuperarContrasena.email)
            ?: return call.respond(HttpStatusCode.Unauthorized)

        // 2. Si el usuario existe, generamos un código de recuperación y lo guardamos en la base de datos.

        if (usuario.estado == EstadoDeCuenta.INACTIVO) {
            // Usamos Forbidden para indicar que el usuario no está activo. Es personalizable.
            return call.respond(HttpStatusCode.Forbidden)
        }

        // 3. Enviamos el código de recuperación al email del usuario.

        val respuesta = repoValidaciones.enviarCodigoAleatorio(
            metodoDeAutenticacion = MetodoDeAutenticacion.TOKEN_AUTOMATICO,
            idUsuario = usuario.id
        )

        if (respuesta == RespuestaEnvioCodigo.ENVIADO) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    override suspend fun cambiarContrasena(call: ApplicationCall, cambiarContrasena: CambiarContrasenaReq) {
        // 4. El usuario ingresa el código de recuperación junto a la nueva contraseña.

        // -> cambiarContrasena

        // 5. Si el código de recuperación es correcto, el usuario puede cambiar su contraseña.

        repoValidaciones.validarCodigoPorEmail(
            email = cambiarContrasena.email,
            validarCodigo = ValidarCodigoReq(codigo = cambiarContrasena.codigo)
        )

        // En este punto tod0 fue OK, caso contrario se lanzará una Exception al main.

        // 6. El usuario cambia su contraseña.

        val contrasenaEncriptada = Password.hash(cambiarContrasena.nuevoContrasena).withArgon2()
        repoUsuarios.cambiarContrasena(email = cambiarContrasena.email, nuevaContrasena = contrasenaEncriptada.result)

        // 7. El usuario inicia sesión con su nueva contraseña.

        call.respond(HttpStatusCode.OK)
    }
}