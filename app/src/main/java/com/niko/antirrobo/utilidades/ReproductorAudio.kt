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
        // ✨ CANDADO ANTI-AMNESIA: Evita que un doble-clic rápido sobreescriba tu volumen original
        if (EstadoAlarma.estaSonando) return
        if (reproductor?.isPlaying == true) return

        try {
            // Guardamos la foto del volumen exacto que tenías
            volumenOriginalAlarma = gestorAudio.getStreamVolume(AudioManager.STREAM_ALARM)
            EstadoAlarma.estaSonando = true

            val filtroVolumen = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            contexto.registerReceiver(vigiaVolumen, filtroVolumen)

            forzarVolumenAlarma()

            // Construimos el reproductor PIEZA POR PIEZA (Tu código que sí funciona)
            reproductor = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                val archivoSonido = contexto.resources.openRawResourceFd(R.raw.alarma)
                setDataSource(archivoSonido.fileDescriptor, archivoSonido.startOffset, archivoSonido.length)
                archivoSonido.close()

                setVolume(1.0f, 1.0f)
                isLooping = true

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
            EstadoAlarma.estaSonando = false
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

        // ✨ EL SEGURO DE DESPERTADOR ✨
        // Si el volumen que tenías guardado era 0 (te iba a dejar sin alarma), lo forzamos al 70%
        val volumenSeguro = if (volumenOriginalAlarma <= 0) {
            val maxAlarm = gestorAudio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            (maxAlarm * 0.7).toInt()
        } else {
            volumenOriginalAlarma
        }

        gestorAudio.setStreamVolume(AudioManager.STREAM_ALARM, volumenSeguro, 0)
    }
}