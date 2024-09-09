package com.example.utils

class ValidadorDeCedulas private constructor() {
    companion object {

        fun esValida(id: String): Boolean {
            if (!contieneSoloNumeros(id)) {
                return false
            }

            var suma = 0

            if (id.length != 10) {
                return false
            } else {
                val a = IntArray(id.length / 2)
                val b = IntArray(id.length / 2)
                var c = 0
                var d = 1

                for (i in 0 until id.length / 2) {
                    a[i] = id[c].toString().toInt()
                    c += 2

                    if (i < id.length / 2 - 1) {
                        b[i] = id[d].toString().toInt()
                        d += 2
                    }
                }

                for (i in a.indices) {
                    a[i] = a[i] * 2
                    if (a[i] > 9) {
                        a[i] = a[i] - 9
                    }
                    suma += a[i] + b[i]
                }

                val aux = suma / 10
                val dec = (aux + 1) * 10

                return if (dec - suma == id[id.length - 1].toString().toInt())
                    true
                else if (suma % 10 == 0 && id[id.length - 1] == '0') {
                    true
                } else {
                    false
                }
            }
        }

        private fun contieneSoloNumeros(str: String) = Regex("^[0-9]+$").matches(str)
    }
}