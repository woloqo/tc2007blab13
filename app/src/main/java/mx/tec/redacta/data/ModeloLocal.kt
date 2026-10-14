package mx.tec.redacta.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import mx.tec.redacta.domain.EstadoModelo
import mx.tec.redacta.domain.Pedido

/**
 * Un modelo de lenguaje que corre en el teléfono. El ViewModel solo conoce
 * esta interfaz: en la app es Gemma (GemmaLocal); en las pruebas, uno de
 * mentira que contesta al instante.
 */
interface ModeloLocal {
    val estado: StateFlow<EstadoModelo>

    /** Busca el archivo y lo sube a memoria. Tarda segundos: se llama desde una corrutina. */
    suspend fun cargar()

    /**
     * La respuesta, en pedazos, conforme se genera. Cada pedazo suele ser un token.
     * Cancelar la corrutina que la recolecta detiene la generación.
     */
    fun generar(pedido: Pedido): Flow<String>
}