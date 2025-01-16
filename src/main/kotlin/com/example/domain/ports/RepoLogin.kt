package com.example.domain.ports

import com.example.application.req.InicioDeSesionReq
import io.ktor.server.application.*

interface RepoLogin {

    suspend fun loginUsuario(call: ApplicationCall, inicioDeSesion: InicioDeSesionReq)
}