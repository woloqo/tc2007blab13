package mx.tec.redacta.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TareaTest {

    @Test
    fun el_texto_va_enmarcado_despues_del_pedido() {
        val mensaje = Tarea.Breve.enmarcar("  Mañana no hay clase.  ")

        assertTrue(mensaje.startsWith(Tarea.Breve.pedido))
        assertTrue(mensaje.endsWith("Texto: «Mañana no hay clase.»"))
    }

    @Test
    fun responder_enmarca_un_mensaje() {
        assertTrue(Tarea.Responder.enmarcar("¿Vienes?").endsWith("Mensaje: «¿Vienes?»"))
    }

    @Test
    fun el_ejemplo_se_enmarca_igual_que_el_texto() {
        val ejemplo = Tarea.Amable.pedidoPara("hola").ejemplo

        assertNotNull(ejemplo)
        assertTrue(ejemplo!!.pregunta.startsWith(Tarea.Amable.pedido))
    }

    @Test
    fun sin_instruccion_de_sistema() {
        // Todo va en el mensaje: Gemma no tiene un rol de sistema propio.
        Tarea.entries.forEach { assertNull(it.pedidoPara("hola").instruccion) }
    }

    @Test
    fun responder_se_arriesga_mas_que_las_demas() {
        assertEquals(0.2, Tarea.Amable.pedidoPara("x").temperatura!!, 0.0)
        assertTrue(Tarea.Responder.temperatura > Tarea.Breve.temperatura)
    }
}