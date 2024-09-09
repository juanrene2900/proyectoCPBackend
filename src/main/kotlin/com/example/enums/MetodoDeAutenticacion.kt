package com.example.enums

enum class MetodoDeAutenticacion(val id: String) {
    CODIGO_POR_EMAIL("codigo_por_email"),
    CODIGO_POR_SMS("codigo_por_sms"),
    RECONOCIMIENTO_FACIAL("reconocimiento_facial"),
}