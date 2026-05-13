package com.niko.antirrobo.detectores

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat // ✨ Nuestra herramienta mágica de compatibilidad
import com.niko.antirrobo.servicios.ServicioAntirrobo

/**
 * Este vigía se despierta una sola vez: justo cuando el celular termina de prenderse.
 */
class DetectorArranque : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Verificamos si la acción es que el dispositivo arrancó
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {

            // ¡El celular despertó! Le decimos a nuestro Servicio Antirrobo que inicie su guardia.
            val intentServicio = Intent(context, ServicioAntirrobo::class.java)

            // Usamos ContextCompat: Si es Android 8+ usará startForegroundService,
            // y si es más antiguo, usará el startService normal. ¡A prueba de fallos!
            ContextCompat.startForegroundService(context, intentServicio)
        }
    }
}