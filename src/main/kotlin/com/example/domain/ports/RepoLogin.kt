package com.example.domain.ports

import com.example.application.req.CambiarContrasenaReq
import com.example.application.req.InicioDeSesionReq
import com.example.application.req.RecuperarContrasenaReq
import io.ktor.server.application.*

interface RepoLogin {

    suspend fun loginUsuario(call: ApplicationCall, inicioDeSesion: InicioDeSesionReq)

    suspend fun solicitarRecuperarContrasena(call: ApplicationCall, recuperarContrasena: RecuperarContrasenaReq)

    suspend fun cambiarContrasena(call: ApplicationCall, cambiarContrasena: CambiarContrasenaReq)
}