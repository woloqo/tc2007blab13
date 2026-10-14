package mx.tec.redacta.data

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mx.tec.redacta.domain.EstadoModelo
import mx.tec.redacta.domain.Pedido

/**
 * Gemma 3 1B, con LiteRT-LM, en el CPU del teléfono.
 *
 * `@Singleton`: el modelo ocupa más de 1 GB de memoria y tarda segundos en
 * cargar. Se carga una vez y lo usa toda la app.
 */
@Singleton
class GemmaLocal @Inject constructor(
    @ApplicationContext private val contexto: Context
) : ModeloLocal {

    private val _estado = MutableStateFlow<EstadoModelo>(EstadoModelo.Cargando)
    override val estado: StateFlow<EstadoModelo> = _estado.asStateFlow()

    private var motor: Engine? = null
    private val candado = Mutex()

    override suspend fun cargar() = candado.withLock {
        if (motor != null) return@withLock
        // Donde lo deja `adb push`: la carpeta de la app en el almacenamiento compartido.
        val archivo = File(contexto.getExternalFilesDir(null), ARCHIVO)
        if (!archivo.exists()) {
            _estado.value = EstadoModelo.Falta(archivo.absolutePath)
            return@withLock
        }
        _estado.value = EstadoModelo.Cargando
        val nuevo = Engine(
            EngineConfig(
                modelPath = archivo.absolutePath,
                backend = Backend.CPU(),
                cacheDir = contexto.cacheDir.absolutePath
            )
        )
        runCatching { withContext(Dispatchers.Default) { nuevo.initialize() } }
            .onSuccess {
                motor = nuevo
                _estado.value = EstadoModelo.Listo
            }
            .onFailure { e ->
                // Si lo cancelaron, el motor sí llegó a cargar: se libera. Si falló, no hay nada que cerrar.
                if (nuevo.isInitialized()) nuevo.close()
                if (e is CancellationException) throw e
                _estado.value = EstadoModelo.Error(e.message ?: "No se pudo cargar el modelo")
            }
    }

    override fun generar(pedido: Pedido): Flow<String> = flow {
        val motor = checkNotNull(motor) { "El modelo todavía no está cargado" }
        val config = ConversationConfig(
            systemInstruction = pedido.instruccion?.let { Contents.of(it) },
            maxOutputToken = MAX_TOKENS
        )
        // Una conversación por pedido: sin historia, cada texto se trata solo.
        motor.createConversation(config).use { conversacion ->
            try {
                conversacion.sendMessageAsync(pedido.mensaje).collect { emit(it.texto()) }
            } catch (e: CancellationException) {
                conversacion.cancelProcess()   // que el CPU deje de generar lo que ya nadie va a leer
                throw e
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun Message.texto(): String =
        contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }

    companion object {
        const val ARCHIVO = "gemma3-1b-it-int4.litertlm"

        /** Un tope de seguridad. Que conteste corto lo pide la instrucción, no este número. */
        const val MAX_TOKENS = 256
    }
}