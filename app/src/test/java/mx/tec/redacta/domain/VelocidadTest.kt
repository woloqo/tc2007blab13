package mx.tec.redacta.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VelocidadTest {

    @Test
    fun once_tokens_en_un_segundo_son_diez_por_segundo() {
        // Entre el primero y el último hay 10 intervalos, no 11.
        assertEquals(10.0, tokensPorSegundo(11, 1000)!!, 0.001)
    }

    @Test
    fun con_un_solo_token_no_hay_velocidad() {
        assertNull(tokensPorSegundo(1, 500))
    }

    @Test
    fun sin_tiempo_no_hay_velocidad() {
        assertNull(tokensPorSegundo(5, 0))
    }
}