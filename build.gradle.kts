@file:Suppress("PropertyName")

val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"

    kotlin("plugin.serialization") version "2.0.20" // Para usar kotlinx.serialization
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // Usamos las siguientes librerías

    // Para usar kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")

    // Para usar MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.2")
    implementation("org.mongodb:bson-kotlinx:5.1.2")

    // Para validar formatos de modelos procedentes del cliente
    implementation("io.ktor:ktor-server-request-validation:$ktor_version")

    // Para hashear contraseñas
    implementation("com.password4j:password4j:1.8.2")

    // Para usar inyección de dependencias
    implementation("io.insert-koin:koin-ktor:4.0.0-RC1")

    // Para autenticar usuarios y generar un token
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    // Para recibir cuerpo de solicitudes en JSON
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // Para personalizar mensajes de error al cliente
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")

    // Para usar CORS (necesario para solicitudes desde la web)
    implementation("io.ktor:ktor-server-cors:$ktor_version")

    // Para enviar emails
    implementation("com.sun.mail:javax.mail:1.6.2")

    // Para enviar SMS
    implementation("com.twilio.sdk:twilio:10.4.2")

    // Para usar opencv
    implementation(files("libs/opencv-310.jar"))
    //implementation("org.openpnp:opencv:4.9.0-0")
}