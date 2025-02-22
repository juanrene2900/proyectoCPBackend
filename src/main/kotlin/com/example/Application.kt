package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.application.req.*
import com.example.domain.ports.RepoJwt
import com.example.domain.ports.RepoLogin
import com.example.domain.ports.RepoUsuarios
import com.example.domain.ports.RepoValidaciones
import com.example.enums.EstadoDeCuenta
import com.example.infrastructure.repository.ImplRepoLogin
import com.example.infrastructure.repository.ImplRepoUsuarios
import com.example.infrastructure.repository.ImplRepoValidaciones
import com.example.infrastructure.repository.ImplRepositorioJwt
import com.example.plugins.configurarRutas
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.bson.types.ObjectId
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.opencv.core.Core

fun main() {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    embeddedServer(
        Netty,
        port = 8081,
        host = "localhost",
        module = Application::modulo
    ).start(wait = true)
}

fun Application.modulo() {
    configurarCORS()
    configurarInyeccionDeDependencias()
    crearIndicesDeColecciones()
    configurarErroresEnRespuestas()
    configurarAutenticacion()
    configurarProcesadorDeDatosEnSolicitudes()
    configurarValidacionesModelosEnSolicitudes()
    configurarRutas()
}

private fun Application.configurarCORS() {
    val modoDesarrollo = environment.developmentMode

    install(CORS) {
        if (modoDesarrollo) anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Get)
    }
}

private fun Application.configurarInyeccionDeDependencias() {
    install(Koin) {
        modules(
            module {
                single<MongoClient> {
                    val url = System.getenv("URL_DE_CONEXION_MONGO_DB")
                    MongoClient.create(url)
                }
                single<MongoDatabase> {
                    val db = System.getenv("BASE_DE_DATOS")
                    get<MongoClient>().getDatabase(db)
                }
            },
            module {
                single<RepoUsuarios> {
                    val db = get<MongoDatabase>()
                    ImplRepoUsuarios(db)
                }
                single<RepoJwt> {
                    val db = get<MongoDatabase>()
                    ImplRepositorioJwt(db)
                }
                single<RepoValidaciones> {
                    val db = get<MongoDatabase>()
                    val repoUsuarios = get<RepoUsuarios>()
                    val repoJwt = get<RepoJwt>()

                    ImplRepoValidaciones(db, repoUsuarios, repoJwt)
                }
                single<RepoLogin> {
                    val repoUsuarios = get<RepoUsuarios>()
                    val repoJwt = get<RepoJwt>()
                    val repoValidaciones = get<RepoValidaciones>()

                    ImplRepoLogin(
                        repoUsuarios,
                        repoJwt,
                        repoValidaciones,
                    )
                }
            }
        )
    }
}

private fun Application.crearIndicesDeColecciones() {
    val repoUsuarios by inject<RepoUsuarios>()

    runBlocking {
        repoUsuarios.crearIndicesDeColeccion()
    }
}

private fun Application.configurarErroresEnRespuestas() {
    install(StatusPages) {
        exception<RequestValidationException> { call, validacion ->
            call.respondText(
                text = "El formato del cuerpo es incorrecto porque:\n${validacion.reasons}",
                status = HttpStatusCode.BadRequest
            )
        }
        exception<BadRequestException> { call, _ ->
            call.respondText(
                text = "El formato del cuerpo es incorrecto porque faltan llaves o los tipos de datos son inválidos.",
                status = HttpStatusCode.BadRequest
            )
        }
        exception<Throwable> { call, _ ->
            call.respondText(
                text = "Ocurrió un error desconocido.",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}

private fun Application.configurarAutenticacion() {
    val repoUsuarios by inject<RepoUsuarios>()
    val repoJwt by inject<RepoJwt>()

    install(Authentication) {
        jwt {
            realm = System.getenv("JWT_REINO")
            verifier(
                JWT
                    .require(Algorithm.HMAC256(System.getenv("JWT_SECRETO")))
                    .withAudience(System.getenv("JWT_AUDIENCIA"))
                    .withIssuer(System.getenv("JWT_EMISOR"))
                    .build()
            )
            validate { credencial ->
                // El jwt es válido, pero vamos a hacer validaciones extras.

                val idUsuario = ObjectId(credencial.subject!!)

                // 1. Verificar si existe en nuestra db (recordemos que solo se puede tener un jwt a la vez).
                val jwt = this.request.headers[HttpHeaders.Authorization]!!
                val jwtExiste = repoJwt.jwtExiste(idUsuario, jwt)

                if (!jwtExiste) {
                    return@validate null
                }

                // 2. Verificar si el dueño de este jwt (usuario) existe y está activo.
                val usuario = repoUsuarios.obtenerUsuario(idUsuario)
                val usuarioEsValido = usuario?.estado == EstadoDeCuenta.ACTIVO

                if (!usuarioEsValido) {
                    return@validate null
                }

                JWTPrincipal(credencial.payload)
            }
            challenge { _, _ ->
                call.respondText(
                    text = "El token no es válido o ha caducado.",
                    status = HttpStatusCode.Unauthorized
                )
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun Application.configurarProcesadorDeDatosEnSolicitudes() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            namingStrategy = JsonNamingStrategy.SnakeCase
            serializersModule = SerializersModule {
                contextual(ObjectIdSerializer)
            }
        })
    }
}

private fun Application.configurarValidacionesModelosEnSolicitudes() {
    install(RequestValidation) {
        validate<UsuarioReq> { it.validarFormato() }
        validate<InicioDeSesionReq> { it.validarFormato() }
        validate<ValidarCodigoReq> { it.validarFormato() }
        validate<ValidarRostroReq> { it.validarFormato() }
        validate<ActualizarClienteReq> { it.validarFormato() }
        validate<RecuperarContrasenaReq> { it.validate() }
        validate<CambiarContrasenaReq> { it.validate() }
    }
}