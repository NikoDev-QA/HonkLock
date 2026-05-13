package com.niko.antirrobo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.niko.antirrobo.servicios.ServicioAntirrobo

class MainActivity : ComponentActivity() {

    private val estadoAccesibilidad = mutableStateOf(false)

    private val pedirPermisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val preferencias = getSharedPreferences("AjustesAntirrobo", Context.MODE_PRIVATE)
        preferencias.edit().putBoolean("ya_pregunte_notificaciones", true).apply()

        if (!concedido) {
            Toast.makeText(this, "Aviso: Sin notificaciones, el sistema podría cerrar la app.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferencias = getSharedPreferences("AjustesAntirrobo", Context.MODE_PRIVATE)
        val yaLePregunte = preferencias.getBoolean("ya_pregunte_notificaciones", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permisoOtorgado = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!permisoOtorgado && !yaLePregunte) {
                pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PantallaPrincipal(estadoAccesibilidad)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        estadoAccesibilidad.value = isAccesibilidadActivada(this)
    }

    private fun isAccesibilidadActivada(context: Context): Boolean {
        val serviciosActivados = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val separador = TextUtils.SimpleStringSplitter(':')
        separador.setString(serviciosActivados)

        while (separador.hasNext()) {
            val componente = separador.next()
            if (componente.contains(context.packageName)) {
                return true
            }
        }
        return false
    }
}

@Composable
fun PantallaPrincipal(estadoAccesibilidad: State<Boolean>) {
    val contexto = LocalContext.current
    var estaActivado by remember { mutableStateOf(ServicioAntirrobo.estaCorriendo) }
    var mostrarDialogoInfo by remember { mutableStateOf(false) }

    // ✨ La herramienta mágica AHORA ADENTRO de la función Composable ✨
    val manejadorEnlaces = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Empujador invisible para centrar el contenido principal
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Escudo",
            modifier = Modifier.size(120.dp),
            tint = if (estaActivado) Color(0xFF4CAF50) else Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (estaActivado) "PROTECCIÓN ACTIVADA" else "PROTECCIÓN DESACTIVADA",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (estaActivado) Color(0xFF4CAF50) else Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Tu teléfono vigila el Modo Avión, Chip y Apagados.", textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val intentServicio = Intent(contexto, ServicioAntirrobo::class.java)
                if (estaActivado) {
                    contexto.stopService(intentServicio)
                    estaActivado = false
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        contexto.startForegroundService(intentServicio)
                    } else {
                        contexto.startService(intentServicio)
                    }
                    estaActivado = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (estaActivado) Color.Red else Color(0xFF2196F3)
            )
        ) {
            Text(
                text = if (estaActivado) "DESACTIVAR ANTIRROBO" else "ACTIVAR ANTIRROBO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!estadoAccesibilidad.value) {
            OutlinedButton(
                onClick = {
                    val intentAccesibilidad = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    contexto.startActivity(intentAccesibilidad)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.SettingsAccessibility, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Habilitar Seguridad de Sistema", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { mostrarDialogoInfo = true }) {
            Icon(Icons.Default.Info, contentDescription = "Info")
            Spacer(modifier = Modifier.width(8.dp))
            Text("¿Por qué piden permisos sensibles?")
        }

        // Empujador invisible para mandar la firma hasta el fondo
        Spacer(modifier = Modifier.weight(1f))

        // ✨ LA FIRMA DE LA CREADORA ✨
        Text(
            text = "Desarrollado con ❤️ por Nicole D.J.", // Tu nombre aquí
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ✨ EL ENLACE A GITHUB ✨
        Text(
            text = "Ver código fuente en GitHub (Open Source)",
            fontSize = 12.sp,
            color = Color(0xFF2196F3),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                manejadorEnlaces.openUri("https://github.com/tu_usuario/antirrobo")
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    } // <-- ✨ AQUÍ SE CIERRA LA CAJA COLUMN (Llave recuperada) ✨

    // El Cuadro de Diálogo se dibuja por fuera de la columna
    if (mostrarDialogoInfo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoInfo = false },
            title = {
                Text("Transparencia y Permisos", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Para que este antirrobo funcione, necesita tomar el control y evitar que el ladrón se salga con la suya.\n\n" +
                            "🔒 Accesibilidad: Se usa ÚNICAMENTE para detectar si intentan apagar tu teléfono o bajar el volumen, bloqueando esa acción.\n\n" +
                            "📱 Mostrar sobre otras apps: Sirve para lanzar la pantalla roja de alerta por encima de todo.\n\n" +
                            "Android te mostrará advertencias de seguridad asustadizas (¡es normal!). Como somos Open Source, tu privacidad y tus datos están 100% a salvo y no salen de tu celular."
                )
            },
            confirmButton = {
                Button(onClick = { mostrarDialogoInfo = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}