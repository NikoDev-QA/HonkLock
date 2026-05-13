# 🦆🔒 HonkLock

<p align="center">
  <i>"El guardián digital que grita cuando el ladrón actúa."</i>
</p>

## 📖 La Filosofía / The Philosophy (¿Por qué un Ganso?)
Históricamente, los gansos han sido utilizados como los mejores animales guardianes en granjas y fortalezas. Son hiper-vigilantes, territoriales y, ante el primer patrón inusual, lanzan un graznido ensordecedor. **HonkLock** es un "ganso digital". Vigila los sensores del dispositivo en segundo plano y, ante una anomalía de seguridad (robo en transporte público, descuido), lanza una alerta visual y auditiva imposible de esquivar.

## ✨ Características Principales / Features
*   ✈️ **Detector de Modo Avión:** La alarma se dispara si se activa el modo avión con la pantalla bloqueada.
*   📱 **Detector de SIM:** Reacciona instantáneamente si la bandeja de la tarjeta SIM es extraída.
*   🦹‍♂️ **Interceptación de Apagado (Jefe Final):** Usando una capa de Accesibilidad, anula el menú de apagado del sistema y sobrepone una pantalla de seguridad inesquivable.
*   🔊 **Candado de Volumen (Nivel Dios):** Secuestra el canal de audio del despertador y neutraliza los botones físicos. Si el ladrón intenta bajar el volumen, la app lo fuerza al 100% en 0.5 milisegundos.
*   🔐 **Cierre Biométrico:** La alerta solo se desactiva cuando el propietario autentica su huella dactilar o PIN (`ACTION_USER_PRESENT`).

## 🛠️ Tecnologías / Tech Stack
Este proyecto fue desarrollado utilizando arquitectura limpia y tecnologías modernas de Android:
*   **Lenguaje:** Kotlin 100% Nativo.
*   **UI:** Jetpack Compose (Declarative UI).
*   **Background:** Foreground Services & BroadcastReceivers (Bajo consumo de batería).
*   **Asincronismo:** Kotlin Coroutines (`Dispatchers.Default`) para los bucles de seguridad.
*   **Seguridad:** AccesibilityService API & WindowManager Overlays.

## 🤝 Transparencia y Open Source
HonkLock solicita permisos sensibles (Accesibilidad, Superposición de pantalla, Modificar Audio). Al ser Open Source, garantizamos que no existen "cajas negras", envíos de datos a servidores de terceros, ni anuncios. Tu privacidad no sale de tu teléfono.

---
*Desarrollado con ❤️ por Niko | Open Source Project*
