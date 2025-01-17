package com.example.plugins

import com.example.application.req.*
import com.example.domain.ports.RepoLogin
import com.example.domain.ports.RepoUsuarios
import com.example.domain.ports.RepoValidaciones
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configurarRutas() {

    val repoUsuarios by inject<RepoUsuarios>()
    val repoValidaciones by inject<RepoValidaciones>()
    val repoLogin by inject<RepoLogin>()

    routing {
        post("/usuarios") {
            val usuario = call.receive<UsuarioReq>()
            repoUsuarios.crearUsuario(call, usuario)
        }
        post("/usuarios/login") {
            val inicioDeSesion = call.receive<InicioDeSesionReq>()
            repoLogin.loginUsuario(call, inicioDeSesion)
        }
        post("/usuarios/solicitar-recuperar-contrasena") {
            val solicitarRecuperarContrasena = call.receive<RecuperarContrasenaReq>()
            repoLogin.solicitarRecuperarContrasena(call, solicitarRecuperarContrasena)
        }
        post("/usuarios/cambiar-contrasena") {
            val cambiarContrasena = call.receive<CambiarContrasenaReq>()
            repoLogin.cambiarContrasena(call, cambiarContrasena)
        }

        authenticate {
            post("/usuarios/validar-codigo") {
                val validarCodigo = call.receive<ValidarCodigoReq>()
                repoValidaciones.validarCodigo(call, validarCodigo)
            }
            post("/usuarios/validar-rostro") {
                val validarRostro = call.receive<ValidarRostroReq>()
                repoValidaciones.validarRostro(call, validarRostro)
            }

            post("/clientes") {
                val usuario = call.receive<UsuarioReq>()
                repoUsuarios.crearCliente(call, usuario)
            }

            get("/clientes") {
                repoUsuarios.listarClientes(call)
            }

            delete("/clientes/{id_cliente}") {
                val idAdmin = call.principal<JWTPrincipal>()!!.subject!!
                val idCliente = call.parameters["id_cliente"]!!

                repoUsuarios.eliminarCliente(idAdmin, idCliente)
                call.respond(HttpStatusCode.OK)
            }

            patch("/clientes/{id_cliente}") {
                val idCliente = call.parameters["id_cliente"]!!
                val usuario = call.receive<ActualizarClienteReq>()

                repoUsuarios.actualizarCliente(call, idCliente, usuario)
            }
        }
    }
}