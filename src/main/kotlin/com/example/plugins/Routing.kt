package com.example.plugins

import com.example.application.req.InicioDeSesionReq
import com.example.application.req.UsuarioReq
import com.example.application.req.ValidarCodigoReq
import com.example.application.req.ValidarRostroReq
import com.example.domain.ports.RepoLogin
import com.example.domain.ports.RepoUsuarios
import com.example.domain.ports.RepoValidaciones
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
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
        }
    }
}