package com.example.utils

import kotlinx.serialization.SerialName
import kotlin.reflect.KProperty

val KProperty<*>.serialName: String
    get() {
        val serialName = annotations.firstOrNull { it is SerialName } as SerialName?
        return serialName?.value ?: name
    }