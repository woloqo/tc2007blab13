package mx.tec.redacta.ui.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import mx.tec.redacta.ModeloFalso
import mx.tec.redacta.domain.EstadoModelo
import mx.tec.redacta.domain.Tarea
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * El ViewModel, con un modelo de mentira. Sin emulador, sin Gemma y sin Hilt:
 * el ViewModel se construye aquí, y por eso se le puede dar el modelo que sea.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RedactaViewModelTest {

    // viewModelScope corre en Dispatchers.Main. En la JVM no hay hilo principal de
    // Android: se cambia por uno que ejecuta todo en cuanto se pide (Práctica 11).
    @Before
    fun cambiarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private val modelo = ModeloFalso()

    private fun listoConTexto(texto: String = "mañana no hay clase") =
        RedactaViewModel(modelo).apply { alCambiarTexto(texto) }

    @Test
    fun al_crearse_busca_el_modelo() {
        val viewModel = RedactaViewModel(modelo)

        assertEquals(EstadoModelo.Listo, viewModel.ui.value.modelo)
    }

    @Test
    fun sin_modelo_no_se_puede_redactar() {
        val sinArchivo = ModeloFalso(alCargar = EstadoModelo.Falta("/ruta"))
        val viewModel = RedactaViewModel(sinArchivo)
        viewModel.alCambiarTexto("hola")

        viewModel.redactar()

        assertFalse(viewModel.ui.value.puedeRedactar)
        assertEquals(null, viewModel.ui.value.respuesta)
        assertTrue(sinArchivo.pedidos.isEmpty())
    }

    @Test
    fun la_respuesta_se_arma_con_los_pedazos_en_orden() {
        val viewModel = listoConTexto()

        viewModel.redactar()

        val respuesta = viewModel.ui.value.respuesta!!
        assertEquals("Hola, mundo", respuesta.texto)
        assertEquals(3, respuesta.tokens)
        assertFalse(respuesta.generando)
    }

    @Test
    fun al_terminar_se_quitan_los_espacios_de_mas() {
        modelo.pedazos = listOf("Les aviso.", "  Mañana", "  no hay clase.  ")
        val viewModel = listoConTexto()

        viewModel.redactar()

        assertEquals("Les aviso. Mañana no hay clase.", viewModel.ui.value.respuesta?.texto)
    }

    @Test
    fun al_modelo_le_llega_el_texto_enmarcado() {
        val viewModel = listoConTexto("  mañana no hay clase  ")
        viewModel.elegirTarea(Tarea.Breve)

        viewModel.redactar()

        assertEquals(Tarea.Breve.enmarcar("mañana no hay clase"), modelo.pedidos.single().mensaje)
    }

    @Test
    fun detener_corta_la_respuesta_y_deja_lo_que_llego() {
        modelo.seQuedaGenerando = true
        val viewModel = listoConTexto()
        viewModel.redactar()
        assertTrue(viewModel.ui.value.generando)

        viewModel.detener()

        assertFalse(viewModel.ui.value.generando)
        assertEquals("Hola, mundo", viewModel.ui.value.respuesta?.texto)
    }

    @Test
    fun mientras_genera_no_se_puede_pedir_otra() {
        modelo.seQuedaGenerando = true
        val viewModel = listoConTexto()

        viewModel.redactar()
        viewModel.redactar()

        assertEquals(1, modelo.pedidos.size)
    }

    @Test
    fun tres_respuestas_se_separan_al_terminar() {
        modelo.pedazos = listOf("1. ¡Claro!\n", "2. Hoy no puedo.\n", "3. Te los paso mañana.")
        val viewModel = listoConTexto()
        viewModel.elegirTarea(Tarea.Responder)

        viewModel.redactar()

        assertEquals(
            listOf("¡Claro!", "Hoy no puedo.", "Te los paso mañana."),
            viewModel.ui.value.respuesta?.opciones
        )
    }

    @Test
    fun si_el_modelo_falla_se_muestra_el_error() {
        modelo.falla = IllegalStateException("Sin memoria")
        val viewModel = listoConTexto()

        viewModel.redactar()

        val respuesta = viewModel.ui.value.respuesta!!
        assertEquals("Sin memoria", respuesta.error)
        assertFalse(respuesta.generando)
    }

    @Test
    fun usar_la_respuesta_la_vuelve_tu_texto() {
        val viewModel = listoConTexto()
        viewModel.redactar()

        viewModel.usarRespuesta("Hola, mundo")

        assertEquals("Hola, mundo", viewModel.ui.value.texto)
        assertEquals(null, viewModel.ui.value.respuesta)
    }
}