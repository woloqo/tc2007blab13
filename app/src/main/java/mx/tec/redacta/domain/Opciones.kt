package mx.tec.redacta.domain

/**
 * Las respuestas de la tarea «3 respuestas», cada una por separado.
 *
 * El modelo no siempre respeta el formato al pie de la letra: a veces escribe
 * «1)», a veces pone negritas, a veces agrega un renglón antes, y si le pides
 * «1., 2. y 3.» puede copiar hasta la coma: «1., Sí». Aquí se toman solo los
 * renglones numerados, y se limpian.
 */
fun separarOpciones(texto: String): List<String> =
    texto.lines()
        .mapNotNull { renglon -> NUMERADO.matchEntire(renglon.replace("**", "").trim())?.groupValues?.get(1) }
        .map { it.trim().trim('"', '«', '»', '“', '”').trim() }
        .filter { it.isNotEmpty() }

private val NUMERADO = Regex("""^\d+\s*[.)]\s*[,;:\-]?\s*(.+)$""")