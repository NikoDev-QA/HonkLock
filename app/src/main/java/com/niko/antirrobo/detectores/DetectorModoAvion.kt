package com.niko.antirrobo.detectores

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Vigía que se despierta cuando el usuario (o el ladrón) toca el botón de Modo Avión.
 */
class DetectorModoAvion(private val alDetectarRobo: () -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Verificamos que el aviso sea efectivamente del Modo Avión
        if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {

            // Android nos manda un extra llamado "state" que es true si se encendió, o false si se apagó
            val activado = intent.getBooleanExtra("state", false)

            if (activado) {
                // ¡Pusieron modo avión! Llamamos a la función que disparará nuestra alarma
                alDetectarRobo()
            }
        }
    }
}