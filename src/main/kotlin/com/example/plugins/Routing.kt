package com.example.plugins

import com.example.application.req.InicioDeSesionReq
import com.example.application.req.UsuarioReq
import com.example.application.req.ValidarCodigoReq
import com.example.application.req.ValidarRostroReq
import com.example.domain.ports.RepositorioLogin
import com.example.domain.ports.RepositorioUsuarios
import com.example.domain.ports.RepositorioValidaciones
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configurarRutas() {

    val repositorioUsuarios by inject<RepositorioUsuarios>()
    val repositorioValidaciones by inject<RepositorioValidaciones>()
    val repositorioLogin by inject<RepositorioLogin>()

    routing {
        post("/usuarios") {
            val usuario = call.receive<UsuarioReq>()
            repositorioUsuarios.crearUsuario(call, usuario)
        }
        post("/usuarios/login") {
            val inicioDeSesion = call.receive<InicioDeSesionReq>()
            repositorioLogin.loginUsuario(call, inicioDeSesion)
        }

        authenticate {
            post("/usuarios/validar-codigo") {
                val validarCodigo = call.receive<ValidarCodigoReq>()
                repositorioValidaciones.validarCodigo(call, validarCodigo)
            }
            post("/usuarios/validar-rostro") {
                val validarRostro = call.receive<ValidarRostroReq>()
                repositorioValidaciones.validarRostro(call, validarRostro)
            }
        }
    }
}