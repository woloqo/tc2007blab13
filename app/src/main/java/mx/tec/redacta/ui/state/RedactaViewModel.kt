package mx.tec.redacta.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.redacta.data.ModeloLocal
import mx.tec.redacta.domain.Tarea

@HiltViewModel
class RedactaViewModel @Inject constructor(
    private val modelo: ModeloLocal
) : ViewModel() {

    private val _ui = MutableStateFlow(RedactaUiState())
    val ui: StateFlow<RedactaUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            modelo.estado.collect { estado -> _ui.update { it.copy(modelo = estado) } }
        }
        buscarModelo()
    }

    /** Busca el archivo y lo carga. Es también el botón «Ya lo copié: buscar otra vez». */
    fun buscarModelo() {
        viewModelScope.launch { modelo.cargar() }
    }

    fun alCambiarTexto(texto: String) = _ui.update { it.copy(texto = texto) }

    fun elegirTarea(tarea: Tarea) = _ui.update { it.copy(tarea = tarea) }

    /** «Usar este texto»: la respuesta pasa a ser tu texto, para pedirle otra cosa. */
    fun usarRespuesta(texto: String) = _ui.update { it.copy(texto = texto, respuesta = null) }

    fun redactar() {
        // B2
    }

    fun detener() {
        // B2
    }
}