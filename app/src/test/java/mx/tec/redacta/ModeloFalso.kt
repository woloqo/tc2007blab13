package mx.tec.redacta

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import mx.tec.redacta.data.ModeloLocal
import mx.tec.redacta.domain.EstadoModelo
import mx.tec.redacta.domain.Pedido

/**
 * Un modelo de mentira: contesta al instante lo que le digas, y anota lo que
 * le pidieron. Implementa la misma interfaz que Gemma, así que el ViewModel
 * no nota la diferencia.
 */
class ModeloFalso(
    /** Lo que `cargar()` deja como estado: Listo, o Falta para probar sin archivo. */
    private val alCargar: EstadoModelo = EstadoModelo.Listo
) : ModeloLocal {

    private val _estado = MutableStateFlow<EstadoModelo>(EstadoModelo.Cargando)
    override val estado: StateFlow<EstadoModelo> = _estado

    /** Los pedazos de la próxima respuesta. */
    var pedazos: List<String> = listOf("Hola", ", ", "mundo")

    /** true: después del último pedazo se queda generando, hasta que lo cancelen. */
    var seQuedaGenerando = false

    /** Si no es null, falla con este error después de los pedazos. */
    var falla: Exception? = null

    val pedidos = mutableListOf<Pedido>()

    override suspend fun cargar() {
        _estado.value = alCargar
    }

    override fun generar(pedido: Pedido): Flow<String> = flow {
        pedidos += pedido
        pedazos.forEach { emit(it) }
        falla?.let { throw it }
        if (seQuedaGenerando) awaitCancellation()
    }
}