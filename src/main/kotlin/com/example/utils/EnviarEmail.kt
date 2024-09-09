package com.example.utils

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

private val emailDelSistema = System.getenv("EMAIL_DEL_SISTEMA")
private val contrasenaAppGmail = System.getenv("CONTRASENA_APP_GMAIL")

fun enviarEmail(emailDelDestinatario: String, contenido: String) {
    val host = "smtp.gmail.com"

    val properties: Properties = System.getProperties()

    properties.setProperty("mail.host", host)
    properties["mail.smtp.host"] = host
    properties["mail.smtp.port"] = "465"
    properties["mail.smtp.socketFactory.fallback"] = "false"
    properties.setProperty("mail.smtp.quitwait", "false")
    properties["mail.smtp.socketFactory.port"] = "465"
    properties["mail.smtp.starttls.enable"] = "true"
    properties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
    properties["mail.smtp.ssl.enable"] = "true"
    properties["mail.smtp.auth"] = "true"

    val sesion = Session.getInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication() = PasswordAuthentication(
            emailDelSistema,
            contrasenaAppGmail
        )
    })

    val mensaje = MimeMessage(sesion).apply {
        addRecipient(Message.RecipientType.TO, InternetAddress(emailDelDestinatario))
        subject = "Su código para iniciar sesión"
        setText(contenido)
    }

    Transport.send(mensaje)
}