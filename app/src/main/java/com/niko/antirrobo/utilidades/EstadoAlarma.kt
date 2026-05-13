package com.niko.antirrobo.utilidades

/**
 * Esta es una variable global (Singleton) que toda la app puede ver.
 * Nos sirve para saber si el celular está gritando o no.
 */
object EstadoAlarma {
    var estaSonando: Boolean = false
}