package com.example.data.importer

import android.content.Context
import android.net.Uri
import com.example.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object SampleAudioGenerator {

    /**
     * Generates 3 pleasant demo WAV audio files (Acoustic Chill, Midnight Groove, Horizon Breeze)
     * so the user can test playback and Spotify UI immediately on first launch.
     */
    suspend fun createDemoTracks(context: Context): List<TrackEntity> = withContext(Dispatchers.IO) {
        val sampleDir = File(context.filesDir, "demo_music")
        if (!sampleDir.exists()) sampleDir.mkdirs()

        val sample1 = File(sampleDir, "Acoustic_Melody_Demo.wav")
        if (!sample1.exists()) {
            generateSynthesizedWav(sample1, durationSeconds = 8, baseFreq = 440.0) // A4 Chord
        }

        val sample2 = File(sampleDir, "Midnight_Groove_Demo.wav")
        if (!sample2.exists()) {
            generateSynthesizedWav(sample2, durationSeconds = 10, baseFreq = 329.63) // E4 Chord
        }

        val sample3 = File(sampleDir, "LoFi_Sunset_Demo.wav")
        if (!sample3.exists()) {
            generateSynthesizedWav(sample3, durationSeconds = 9, baseFreq = 261.63) // C4 Chord
        }

        val track1Title = "Atmósfera Acústica (Muestra)"
        val track1Artist = "SpotLocal Demo Studio"
        val cover1 = com.example.data.storage.LocalStorageManager.generateSeedWebpCover(
            context, track1Title, track1Artist, sample1.name
        )

        val track2Title = "Ritmo Nocturno (Muestra)"
        val track2Artist = "SpotLocal Demo Studio"
        val cover2 = com.example.data.storage.LocalStorageManager.generateSeedWebpCover(
            context, track2Title, track2Artist, sample2.name
        )

        val track3Title = "Brisa de Atardecer (Muestra)"
        val track3Artist = "SpotLocal Demo Studio"
        val cover3 = com.example.data.storage.LocalStorageManager.generateSeedWebpCover(
            context, track3Title, track3Artist, sample3.name
        )

        listOf(
            TrackEntity(
                uriString = Uri.fromFile(sample1).toString(),
                title = track1Title,
                artist = track1Artist,
                album = "Bienvenido a SpotLocal",
                durationMs = 8000L,
                fileSizeBytes = sample1.length(),
                folderName = "Música de Muestra",
                dateImported = System.currentTimeMillis() - 2000,
                coverArtPath = cover1,
                isFavorite = true,
                isSample = true,
                lyrics = """
                    [00:00.50] Sintonizando frecuencia acústica SpotLocal...
                    [00:02.00] ♪ Sonido de alta fidelidad procesado en NDK ♪
                    [00:04.50] Disfruta la música sin conexiones ni anuncios
                    [00:06.50] SpotLocal: Tu reproductor definitivo
                """.trimIndent()
            ),
            TrackEntity(
                uriString = Uri.fromFile(sample2).toString(),
                title = track2Title,
                artist = track2Artist,
                album = "Bienvenido a SpotLocal",
                durationMs = 10000L,
                fileSizeBytes = sample2.length(),
                folderName = "Música de Muestra",
                dateImported = System.currentTimeMillis() - 1000,
                coverArtPath = cover2,
                isFavorite = false,
                isSample = true,
                lyrics = """
                    [00:01.00] Ritmos nocturnos para concentrarse y crear
                    [00:03.50] ♪ Ajusta la velocidad y el tono en tiempo real ♪
                    [00:06.00] La experiencia Spotify en código nativo
                    [00:08.00] Creado exclusivamente para ti
                """.trimIndent()
            ),
            TrackEntity(
                uriString = Uri.fromFile(sample3).toString(),
                title = track3Title,
                artist = track3Artist,
                album = "Bienvenido a SpotLocal",
                durationMs = 9000L,
                fileSizeBytes = sample3.length(),
                folderName = "Música de Muestra",
                dateImported = System.currentTimeMillis(),
                coverArtPath = cover3,
                isFavorite = true,
                isSample = true,
                lyrics = """
                    [00:00.80] Brisa de atardecer lo-fi
                    [00:03.00] ♪ Separación de Voces e Instrumental por IA ♪
                    [00:05.50] Guarda y edita letras en formato sincronizado LRC
                    [00:07.50] ¡Que disfrutes la música!
                """.trimIndent()
            )
        )
    }

    private fun generateSynthesizedWav(outputFile: File, durationSeconds: Int, baseFreq: Double) {
        val sampleRate = 44100
        val numSamples = sampleRate * durationSeconds
        val pcmData = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            
            // Envelope for smooth fade in / fade out
            val envelope = when {
                t < 0.5 -> t / 0.5
                t > durationSeconds - 0.5 -> (durationSeconds - t) / 0.5
                else -> 1.0
            }

            // Harmony triad: baseFreq, major 3rd, 5th
            val val1 = sin(2.0 * Math.PI * baseFreq * t)
            val val2 = sin(2.0 * Math.PI * (baseFreq * 1.25) * t) * 0.6
            val val3 = sin(2.0 * Math.PI * (baseFreq * 1.5) * t) * 0.4
            
            val sampleVal = ((val1 + val2 + val3) / 2.0 * envelope * 24000).toInt().coerceIn(-32000, 32000)

            val byteBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.putShort(sampleVal.toShort())
            val bytes = byteBuffer.array()

            pcmData[i * 2] = bytes[0]
            pcmData[i * 2 + 1] = bytes[1]
        }

        val totalDataLen = pcmData.size + 36
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size for PCM
            putShort(1.toShort()) // AudioFormat 1 = PCM
            putShort(1.toShort()) // NumChannels = Mono
            putInt(sampleRate)
            putInt(sampleRate * 2) // ByteRate
            putShort(2.toShort()) // BlockAlign
            putShort(16.toShort()) // BitsPerSample
            put("data".toByteArray())
            putInt(pcmData.size)
        }.array()

        FileOutputStream(outputFile).use { out ->
            out.write(header)
            out.write(pcmData)
        }
    }
}
