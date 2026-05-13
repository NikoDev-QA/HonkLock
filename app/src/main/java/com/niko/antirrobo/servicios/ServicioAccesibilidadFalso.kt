package com.niko.antirrobo.servicios

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.KeyEvent // Para detectar botones
import android.util.Log
import com.niko.antirrobo.utilidades.EstadoAlarma // Nuestro comunicador

class ServicioAccesibilidadFalso : AccessibilityService() {

    // FUNCIÓN 1: Detectar ventanas (Menú de apagado)
    override fun onAccessibilityEvent(evento: AccessibilityEvent?) {
        if (evento?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            val nombreClase = evento.className?.toString()?.lowercase() ?: return
            Log.d("Antirrobo", "Ventana detectada: $nombreClase")

            if (nombreClase.contains("globalactions") ||
                nombreClase.contains("power") ||
                nombreClase.contains("shutdown") ||
                nombreClase.contains("reboot")) {

                val gestorBloqueo = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

                if (gestorBloqueo.isKeyguardLocked) {
                    Log.d("Antirrobo", "¡Intento de apagado bloqueado detectado!")
                    performGlobalAction(GLOBAL_ACTION_BACK)

                    val aviso = Intent("com.niko.antirrobo.ALERTA_APAGADO")
                    aviso.setPackage(packageName)
                    sendBroadcast(aviso)
                }
            }
        }
    }

    // FUNCIÓN 2: Detectar y secuestrar botones físicos
    override fun onKeyEvent(evento: KeyEvent?): Boolean {
        // Si la alarma ESTÁ SONANDO...
        if (EstadoAlarma.estaSonando) {
            val tecla = evento?.keyCode

            // Si el ladrón presiona Bajar Volumen, Subir Volumen o Silencio...
            if (tecla == KeyEvent.KEYCODE_VOLUME_DOWN ||
                tecla == KeyEvent.KEYCODE_VOLUME_UP ||
                tecla == KeyEvent.KEYCODE_VOLUME_MUTE) {

                Log.d("Antirrobo", "¡Botón de volumen bloqueado al ladrón!")
                // Retornar TRUE evita que Android procese el botón
                return true
            }
        }

        // Si no está sonando, los botones funcionan normal
        return super.onKeyEvent(evento)
    }

    // FUNCIÓN 3: Obligatoria de Android (No la usamos)
    override fun onInterrupt() {
    }
}