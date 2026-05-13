package com.niko.antirrobo

import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PantallaAlertaActivity : ComponentActivity() {

    // Vigía interno para cerrarse solita cuando pongas la huella
    private val detectorDesbloqueo = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                finish() // Se auto-destruye
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // El Pase VIP para estar por encima del bloqueo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        super.onCreate(savedInstanceState)
        registerReceiver(detectorDesbloqueo, IntentFilter(Intent.ACTION_USER_PRESENT))

        setContent {
            val contextoActual = LocalContext.current as Activity
            val gestorBloqueo = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = "🚫 INTENTO DE APAGADO BLOQUEADO 🚫\n\nDISPOSITIVO RASTREADO",
                        color = Color.Red,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // ✨ EL BOTÓN SALVAVIDAS ✨
                    Button(
                        onClick = {
                            // Le pedimos a Android que nos deje ver el lector de huellas/PIN
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                gestorBloqueo.requestDismissKeyguard(contextoActual, null)
                            } else {
                                contextoActual.finish() // En teléfonos viejos, cerrar la app basta
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(text = "PONER HUELLA PARA DESACTIVAR", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(detectorDesbloqueo)
    }
}