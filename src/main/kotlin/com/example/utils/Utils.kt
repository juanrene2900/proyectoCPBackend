package com.example.utils

import org.bson.BsonDateTime
import java.time.Instant
import java.time.ZoneOffset

fun esEmailValido(email: String): Boolean {
    val patronDeEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(patronDeEmail.toRegex())
}

fun fechaActual() = BsonDateTime(
    Instant.now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
)