package mx.tec.redacta.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** Cada caso es un formato que el modelo de verdad ha escrito. */
class OpcionesTest {

    @Test
    fun tres_renglones_numerados() {
        assertEquals(
            listOf("¡Claro!", "Hoy no puedo.", "Mañana te digo."),
            separarOpciones("1. ¡Claro!\n2. Hoy no puedo.\n3. Mañana te digo.")
        )
    }

    @Test
    fun con_parentesis_y_negritas() {
        assertEquals(listOf("Sí", "No"), separarOpciones("**1)** Sí\n**2)** No"))
    }

    @Test
    fun ignora_la_introduccion() {
        assertEquals(listOf("Sí", "No"), separarOpciones("Aquí tienes tres opciones:\n1. Sí\n2. No"))
    }

    @Test
    fun quita_la_coma_que_copio_del_pedido() {
        // Se le pidió «Numéralas 1., 2. y 3.» y escribió «1., Sí, por favor.»
        assertEquals(listOf("Sí, por favor."), separarOpciones("1., Sí, por favor."))
    }

    @Test
    fun quita_las_comillas() {
        assertEquals(listOf("Te los paso hoy"), separarOpciones("1. «Te los paso hoy»"))
    }

    @Test
    fun sin_numeros_no_hay_opciones() {
        assertEquals(emptyList<String>(), separarOpciones("Claro, te los paso hoy en la noche."))
    }
}