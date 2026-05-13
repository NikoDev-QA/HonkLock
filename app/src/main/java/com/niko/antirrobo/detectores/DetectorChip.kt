package com.niko.antirrobo.detectores

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Vigía que revisa si alguien retira la bandeja de la tarjeta SIM.
 */
class DetectorChip(private val alDetectarRobo: () -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // La acción secreta de Android cuando algo pasa con la SIM
        if (intent?.action == "android.intent.action.SIM_STATE_CHANGED") {

            // Android envía el estado bajo la llave "ss" (SIM State)
            val estadoSim = intent.getStringExtra("ss")

            // ABSENT significa que la tarjeta SIM fue retirada físicamente del celular
            if (estadoSim == "ABSENT") {
                // ¡Sacaron el chip! A gritar se ha dicho.
                alDetectarRobo()
            }
        }
    }
}