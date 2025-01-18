package com.example.domain.ports

import com.example.application.req.ValidarCodigoReq
import com.example.application.req.ValidarRostroReq
import com.example.enums.MetodoDeAutenticacion
import com.example.enums.RespuestaEnvioCodigo
import io.ktor.server.application.*
import org.bson.types.ObjectId

interface RepoValidaciones {

    suspend fun validarCodigo(call: ApplicationCall, validarCodigo: ValidarCodigoReq)

    suspend fun validarCodigoPorEmail(email: String, validarCodigo: ValidarCodigoReq)

    suspend fun validarRostro(call: ApplicationCall, validarRostro: ValidarRostroReq)

    suspend fun enviarCodigoAleatorio(
        idUsuario: ObjectId,
        metodoDeAutenticacion: MetodoDeAutenticacion
    ): RespuestaEnvioCodigo
}