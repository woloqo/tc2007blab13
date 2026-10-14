package mx.tec.redacta.domain

/**
 * Lo que Redacta sabe hacer con tu texto, y cómo se le pide al modelo.
 *
 * Cuatro reglas que salieron de probar Gemma 3 1B, no de la teoría:
 *  1. El texto va ENMARCADO dentro del pedido («Texto: «…»»). Suelto, el
 *     modelo lo toma como un mensaje para él, y lo contesta.
 *  2. El pedido dice qué NO hacer: inventar, agregar introducción.
 *  3. Si el formato importa, un ejemplo resuelto vale más que explicarlo.
 *  4. Temperatura baja para no cambiar el sentido; alta para no repetirse.
 */
enum class Tarea(
    val etiqueta: String,
    val pedido: String,
    val rotulo: String = "Texto",
    val ejemplo: Ejemplo? = null,
    val temperatura: Double = 0.2
) {
    Amable(
        "Más amable",
        "Reescribe este texto con un tono amable y respetuoso. Conserva todos los datos y no inventes nada. " +
                "Escribe solo el texto nuevo.",
        ejemplo = Ejemplo(
            "Entreguen el proyecto el viernes. No voy a aceptar nada tarde, ni me escriban para pedir prórroga.",
            "Les recuerdo que el proyecto se entrega el viernes. Por favor, planéenlo con tiempo: esta vez " +
                    "no podré aceptar entregas tarde ni dar prórrogas. ¡Gracias!"
        )
    ),
    Breve(
        "Más corto",
        "Resume este texto en una sola oración. Usa solo lo que dice el texto. Escribe solo el resumen."
    ),
    Responder(
        "3 respuestas",
        "Recibí este mensaje. Escribe tres respuestas cortas que yo podría enviar. " +
                "Numéralas 1., 2. y 3., una por renglón, sin nada más.",
        rotulo = "Mensaje",
        temperatura = 0.8
    );

    /** El texto del usuario, enmarcado en el pedido de esta tarea. */
    fun enmarcar(texto: String): String = "$pedido\n\n$rotulo: «${texto.trim()}»"

    fun pedidoPara(texto: String): Pedido = Pedido(
        mensaje = enmarcar(texto),
        ejemplo = ejemplo?.let { Ejemplo(enmarcar(it.pregunta), it.respuesta) },
        temperatura = temperatura
    )
}