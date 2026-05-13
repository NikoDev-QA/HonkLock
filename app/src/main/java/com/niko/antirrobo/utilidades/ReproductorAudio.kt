package com.niko.antirrobo.utilidades

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.niko.antirrobo.R
import kotlinx.coroutines.*

class ReproductorAudio(private val contexto: Context) {

    private var reproductor: MediaPlayer? = null
    private val gestorAudio = contexto.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var volumenOriginalAlarma = 0
    private var trabajoRespaldo: Job? = null

    // El vigía que ataca si el ladrón toca el botón
    private val vigiaVolumen = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION" && EstadoAlarma.estaSonando) {
                forzarVolumenAlarma()
            }
        }
    }

    private fun forzarVolumenAlarma() {
        try {
            val maxAlarm = gestorAudio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            gestorAudio.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0)
        } catch (_: Exception) {
        }
    }

    fun iniciarGrito() {
        if (reproductor?.isPlaying == true) return

        try {
            EstadoAlarma.estaSonando = true
            volumenOriginalAlarma = gestorAudio.getStreamVolume(AudioManager.STREAM_ALARM)

            val filtroVolumen = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            contexto.registerReceiver(vigiaVolumen, filtroVolumen)

            forzarVolumenAlarma()

            // 4. ✨ EL ARREGLO: Construimos el reproductor PIEZA POR PIEZA ✨
            reproductor = MediaPlayer().apply {
                // PRIMERO: Le decimos estrictamente que es una ALARMA DE RELOJ
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                // SEGUNDO: Buscamos tu archivo mp3
                val archivoSonido = contexto.resources.openRawResourceFd(R.raw.alarma)
                setDataSource(archivoSonido.fileDescriptor, archivoSonido.startOffset, archivoSonido.length)
                archivoSonido.close()

                // TERCERO: Forzamos volumen interno
                setVolume(1.0f, 1.0f)
                isLooping = true

                // CUARTO: Lo preparamos y lo arrancamos
                prepare()
                start()
            }

            trabajoRespaldo = CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    forzarVolumenAlarma()
                    delay(500)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detenerGrito() {
        EstadoAlarma.estaSonando = false
        trabajoRespaldo?.cancel()
        trabajoRespaldo = null

        try {
            contexto.unregisterReceiver(vigiaVolumen)
        } catch (_: Exception) {}

        reproductor?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        reproductor = null

        gestorAudio.setStreamVolume(AudioManager.STREAM_ALARM, volumenOriginalAlarma, 0)
    }
}