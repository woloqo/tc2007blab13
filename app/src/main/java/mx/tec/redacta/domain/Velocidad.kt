package mx.tec.redacta.domain

/**
 * Tokens por segundo, contados desde que llegó el PRIMERO.
 *
 * Lo de antes —leer tu texto y la instrucción— es otra fase, y en un
 * teléfono es mucho más rápida. Con un solo token todavía no hay intervalo
 * que medir: null.
 */
fun tokensPorSegundo(tokens: Int, milisDesdeElPrimero: Long): Double? =
    if (tokens < 2 || milisDesdeElPrimero <= 0) null
    else (tokens - 1) * 1000.0 / milisDesdeElPrimero