package com.example.utils

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber

private val telefonoDeTwilioParaElSistema = System.getenv("TELEFONO_DE_TWILIO_PARA_EL_SISTEMA")
private val sidDeCuentaDeTwilio = System.getenv("SID_DE_CUENTA_DE_TWILIO")
private val authTokenDeCuentaDeTwilio = System.getenv("AUTH_TOKEN_DE_CUENTA_DE_TWILIO")

fun enviarSms(celularDelDestinatario: String, contenido: String): Boolean {
    Twilio.init(sidDeCuentaDeTwilio, authTokenDeCuentaDeTwilio)

    val mensaje = Message.creator(
        PhoneNumber(celularDelDestinatario),
        PhoneNumber(telefonoDeTwilioParaElSistema),
        contenido
    ).create()

    // Verificamos que el estado de la respuesta sea 'queued'
    // Eso quiere decir que el sms se ha puesto en cola y será enviado en unos momentos
    return mensaje.status == Message.Status.QUEUED
}