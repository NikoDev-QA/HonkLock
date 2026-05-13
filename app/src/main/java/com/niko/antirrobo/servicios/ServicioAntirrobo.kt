package com.niko.antirrobo.servicios

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.niko.antirrobo.PantallaAlertaActivity
import com.niko.antirrobo.R
import com.niko.antirrobo.detectores.DetectorChip
import com.niko.antirrobo.detectores.DetectorModoAvion
import com.niko.antirrobo.utilidades.ReproductorAudio

class ServicioAntirrobo : Service() {

    // Nuestras herramientas
    private lateinit var reproductorAudio: ReproductorAudio
    private lateinit var detectorModoAvion: DetectorModoAvion
    private lateinit var detectorChip: DetectorChip

    // 1. Vigía para el desbloqueo (Huella o PIN)
    private val detectorDesbloqueo = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                // ¡El dueño desbloqueó el teléfono! Apagamos todo.
                reproductorAudio.detenerGrito()
                Toast.makeText(context, "Antirrobo: Alarma desactivada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. Vigía que escucha el chisme de nuestro Servicio de Accesibilidad (Apagado falso)
    private val detectorApagado = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.niko.antirrobo.ALERTA_APAGADO") {
                evaluarRobo() // ¡Llamamos a la misma función de gritar!
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        reproductorAudio = ReproductorAudio(this)
        detectorModoAvion = DetectorModoAvion { evaluarRobo() }
        detectorChip = DetectorChip { evaluarRobo() }
        iniciarModoInmortal()
        estaCorriendo = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filtroAvion = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(detectorModoAvion, filtroAvion)

        val filtroSim = IntentFilter("android.intent.action.SIM_STATE_CHANGED")
        registerReceiver(detectorChip, filtroSim)

        val filtroDesbloqueo = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(detectorDesbloqueo, filtroDesbloqueo)

        val filtroApagado = IntentFilter("com.niko.antirrobo.ALERTA_APAGADO")
        ContextCompat.registerReceiver(
            this,
            detectorApagado,
            filtroApagado,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(detectorModoAvion)
        unregisterReceiver(detectorChip)
        unregisterReceiver(detectorDesbloqueo)
        unregisterReceiver(detectorApagado)

        reproductorAudio.detenerGrito()
        estaCorriendo = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Esta es la función principal. Se llama cuando sacan el chip, ponen modo avión o intentan apagar.
     */
    private fun evaluarRobo() {
        val gestorBloqueo = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (gestorBloqueo.isKeyguardLocked) {

            // 1. Encendemos la pantalla físicamente
            encenderPantalla()

            // 2. ✨ LLAMAMOS A LA PANTALLA VIP (Que aplastará a la pantalla de bloqueo) ✨
            val intentPantalla = Intent(this, PantallaAlertaActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intentPantalla)

            // 3. ¡A gritar se ha dicho!
            reproductorAudio.iniciarGrito()
            Toast.makeText(this, "¡ROBO DETECTADO! Pon tu huella.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Magia negra de Android para encender la pantalla si estaba apagada
     */
    @Suppress("DEPRECATION")
    private fun encenderPantalla() {
        val gestorEnergia = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = gestorEnergia.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "Antirrobo::AlarmaWakeLock"
        )
        wakeLock.acquire(5000)
    }

    private fun iniciarModoInmortal() {
        val canalId = "canal_antirrobo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "Servicio Antirrobo", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
        val notificacion = NotificationCompat.Builder(this, canalId)
            .setContentTitle("Antirrobo Activado")
            .setContentText("Tu teléfono está protegido en segundo plano.")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .build()
        startForeground(1, notificacion)
    }
    // Esto es como una variable global que toda la app puede leer para saber si el servicio vive
    companion object {
        var estaCorriendo = false
    }
}