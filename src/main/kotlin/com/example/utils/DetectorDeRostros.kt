package com.example.utils

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.face.Face
import org.opencv.face.LBPHFaceRecognizer
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.*

class DetectorDeRostros private constructor() {
    companion object {

        fun comparar(rostroGuardado: String, rostroAComparar: String): Boolean {
            val rostroGuardadoMap = convertirBase64AMat(rostroGuardado)
            val rostroACompararMap = convertirBase64AMat(rostroAComparar)

            return compararRostros(rostroGuardadoMap, rostroACompararMap)
        }

        private fun convertirBase64AMat(base64: String): Mat {
            val decodificador = Base64.getDecoder()
            val bytes = decodificador.decode(base64)
            val mat = MatOfByte(*bytes)

            return Imgcodecs.imdecode(mat, Imgcodecs.IMREAD_COLOR)
        }

        private fun compararRostros(rostroGuardado: Mat, rostroAComparar: Mat): Boolean {
            val reconocimientoFacial = Face.createLBPHFaceRecognizer()

            // Convertimos las imágenes a escala de grises.
            val rostroGuardadoGris = Mat()
            val rostroACompararGris = Mat()
            Imgproc.cvtColor(rostroGuardado, rostroGuardadoGris, Imgproc.COLOR_BGR2GRAY)
            Imgproc.cvtColor(rostroAComparar, rostroACompararGris, Imgproc.COLOR_BGR2GRAY)

            // Entrenamos el rostro guardado.
            val rostrosAEntrenar = listOf(rostroGuardadoGris)
            val etiquetasDeEntrenamiento = MatOfInt(1)
            reconocimientoFacial.train(rostrosAEntrenar, etiquetasDeEntrenamiento)

            // Ejecutamos el reconocimiento facial.
            val resultadoEtiquetas = IntArray(1)
            val resultadoConfianzas = DoubleArray(1)
            reconocimientoFacial.predict(rostroACompararGris, resultadoEtiquetas, resultadoConfianzas)

            val confianza = resultadoConfianzas[0]

            return confianza < UMBRAL_CONFIANZA
        }
    }
}

// Entre mayor sea el valor más diferentes serán los rostros.
private const val UMBRAL_CONFIANZA = 25