package com.euroformac.stockcalzadoapp.presentation.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.model.TallaDisponible
import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import com.euroformac.stockcalzadoapp.domain.usecase.ActualizarStockUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetAllProductosUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetStockByModeloUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.ObtenerPedidosPendientesUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.ObtenerTodosLosPedidosUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.CompletarPedidoAlmacenUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.ActualizarEstadoPedidoUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetTallaByBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlmacenViewModel @Inject constructor(
    private val getAllProductosUseCase: GetAllProductosUseCase,
    private val getStockByModeloUseCase: GetStockByModeloUseCase,
    private val actualizarStockUseCase: ActualizarStockUseCase,
    private val obtenerPedidosPendientesUseCase: ObtenerPedidosPendientesUseCase,
    private val obtenerTodosLosPedidosUseCase: ObtenerTodosLosPedidosUseCase,
    private val completarPedidoAlmacenUseCase: CompletarPedidoAlmacenUseCase,
    private val actualizarEstadoPedidoUseCase: ActualizarEstadoPedidoUseCase,
    private val getTallaByBarcodeUseCase: GetTallaByBarcodeUseCase,
    private val insertProductoUseCase: com.euroformac.stockcalzadoapp.domain.usecase.InsertProductoUseCase,
    private val insertTallaUseCase: com.euroformac.stockcalzadoapp.domain.usecase.InsertTallaUseCase,
    private val eliminarProductoUseCase: com.euroformac.stockcalzadoapp.domain.usecase.EliminarProductoUseCase,
    private val eliminarTallaUseCase: com.euroformac.stockcalzadoapp.domain.usecase.EliminarTallaUseCase,
    private val checkPendingOrdersUseCase: com.euroformac.stockcalzadoapp.domain.usecase.CheckPendingOrdersUseCase
) : ViewModel() {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _tallasProductoSeleccionado = MutableStateFlow<List<TallaDisponible>>(emptyList())
    val tallasProductoSeleccionado: StateFlow<List<TallaDisponible>> = _tallasProductoSeleccionado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            getAllProductosUseCase().collect { lista ->
                _productos.value = lista
                _isLoading.value = false
            }
        }
    }

    fun cargarTallasDeProducto(productoId: Long) {
        viewModelScope.launch {
            getStockByModeloUseCase(productoId).collect { tallas ->
                _tallasProductoSeleccionado.value = tallas
            }
        }
    }

    fun aumentarStockAlmacen(talla: TallaDisponible) {
        viewModelScope.launch {
            actualizarStockUseCase(talla.copy(stockAlmacen = talla.stockAlmacen + 1))
        }
    }

    fun disminuirStockAlmacen(talla: TallaDisponible) {
        if (talla.stockAlmacen > 0) {
            viewModelScope.launch {
                actualizarStockUseCase(talla.copy(stockAlmacen = talla.stockAlmacen - 1))
            }
        }
    }

    fun aumentarStockDePrimeraTalla(productoId: Long) {
        viewModelScope.launch {
            val tallas = getStockByModeloUseCase(productoId).firstOrNull()
            if (!tallas.isNullOrEmpty()) {
                val primeraTalla = tallas.first()
                actualizarStockUseCase(primeraTalla.copy(stockAlmacen = primeraTalla.stockAlmacen + 1))
            }
        }
    }

    fun disminuirStockDePrimeraTalla(productoId: Long) {
        viewModelScope.launch {
            val tallas = getStockByModeloUseCase(productoId).firstOrNull()
            if (!tallas.isNullOrEmpty()) {
                val primeraTalla = tallas.first()
                if (primeraTalla.stockAlmacen > 0) {
                    actualizarStockUseCase(primeraTalla.copy(stockAlmacen = primeraTalla.stockAlmacen - 1))
                }
            }
        }
    }

    fun aumentarStockCantidadDePrimeraTalla(productoId: Long, cantidad: Int) {
        viewModelScope.launch {
            val tallas = getStockByModeloUseCase(productoId).firstOrNull()
            if (!tallas.isNullOrEmpty()) {
                val primeraTalla = tallas.first()
                actualizarStockUseCase(primeraTalla.copy(stockAlmacen = primeraTalla.stockAlmacen + cantidad))
            }
        }
    }

    /**
     * Devuelve un Flow con el stock total del almacén para un producto,
     * sumando el stockAlmacen de todas sus tallas.
     */
    fun getStockTotalFlow(productoId: Long): Flow<Int> {
        return getStockByModeloUseCase(productoId).map { tallas ->
            tallas.sumOf { it.stockAlmacen }
        }
    }

    suspend fun getTallas(productoId: Long): List<TallaDisponible> {
        return getStockByModeloUseCase(productoId).firstOrNull() ?: emptyList()
    }

    val pedidosPendientes: StateFlow<List<PedidoAlmacen>> = obtenerPedidosPendientesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosPedidos: StateFlow<List<PedidoAlmacen>> = obtenerTodosLosPedidosUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun completarPedido(codigoBarras: String) {
        viewModelScope.launch {
            completarPedidoAlmacenUseCase(codigoBarras)
        }
    }

    fun completarPedidoPorId(id: Long) {
        viewModelScope.launch {
            actualizarEstadoPedidoUseCase(id, "COMPLETADO", System.currentTimeMillis())
        }
    }

    fun completarPedidoConStock(pedido: PedidoAlmacen, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val talla = getTallaByBarcodeUseCase(pedido.codigoBarras).firstOrNull()
            if (talla != null) {
                if (talla.stockAlmacen > 0) {
                    actualizarStockUseCase(talla.copy(stockAlmacen = talla.stockAlmacen - 1))
                    actualizarEstadoPedidoUseCase(pedido.id, "COMPLETADO", System.currentTimeMillis())
                    onResult(true, "Pedido completado y stock actualizado")
                } else {
                    onResult(false, "No hay stock disponible para esta talla")
                }
            } else {
                onResult(false, "Talla/Producto no encontrado")
            }
        }
    }

    fun marcarPedidoComoFallido(id: Long) {
        viewModelScope.launch {
            actualizarEstadoPedidoUseCase(id, "FALLIDO", System.currentTimeMillis())
        }
    }

    fun registrarEntradaConCodigo(codigoBarras: String, cantidad: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val talla = getTallaByBarcodeUseCase(codigoBarras).firstOrNull()
            if (talla != null) {
                actualizarStockUseCase(talla.copy(stockAlmacen = talla.stockAlmacen + cantidad))
                onResult(true, "Entrada registrada")
            } else {
                onResult(false, "Producto no encontrado")
            }
        }
    }

    fun registrarProductoConRangoTallas(
        nombre: String, marca: String, color: String, genero: String, precio: Double,
        tallaInicial: Double, tallaFinal: Double, codigo: String, stockInicial: Int,
        imagenUrl: String = "producto_placeholder",
        onResult: (Boolean, String) -> Unit
    ) {
        if (tallaInicial > tallaFinal) {
            onResult(false, "La talla inicial no puede ser mayor que la final")
            return
        }

        // 1. Validar que el código base sea numérico
        val baseLong = codigo.toLongOrNull()
        if (baseLong == null) {
            onResult(false, "Código inválido")
            return
        }

        viewModelScope.launch {
            val numTallas = ((tallaFinal - tallaInicial).toInt() + 1)
            val barcodes = mutableListOf<String>()
            val length = codigo.length

            // 2. Generar y verificar códigos para evitar duplicados
            for (i in 0 until numTallas) {
                val nextVal = baseLong + i
                val nextBarcode = nextVal.toString().padStart(length, '0')
                
                val tallaExistente = getTallaByBarcodeUseCase(nextBarcode).firstOrNull()
                if (tallaExistente != null) {
                    onResult(false, "Ya existe un código generado")
                    return@launch
                }
                barcodes.add(nextBarcode)
            }

            // 3. Buscar/Crear producto
            val todos = productos.value
            val productoExistente = todos.find { 
                it.modelo.equals(nombre, true) && it.marca.equals(marca, true) && it.color.equals(color, true)
            }

            val targetProductoId = if (productoExistente != null) {
                productoExistente.id
            } else {
                val nuevoProducto = Producto(
                    id = 0L,
                    barcode = barcodes.first(), // Usar el primer código como base del producto
                    marca = marca,
                    modelo = nombre,
                    color = color,
                    genero = genero,
                    descripcion = "Registrado manualmente",
                    precio = precio,
                    imagenUrl = imagenUrl
                )
                insertProductoUseCase(nuevoProducto)
            }

            // 4. Crear rango de tallas con sus respectivos códigos
            val tallasExistentes = getTallas(targetProductoId)
            var currentTalla = tallaInicial
            var index = 0
            
            while (currentTalla <= tallaFinal) {
                val yaExiste = tallasExistentes.any { it.talla == currentTalla }
                if (!yaExiste) {
                    val esLaPrimera = index == 0
                    val nuevaTalla = TallaDisponible(
                        id = 0L,
                        productoId = targetProductoId,
                        talla = currentTalla,
                        stockAlmacen = if (esLaPrimera) stockInicial else 0,
                        stockTienda = 0,
                        codigoBarras = barcodes[index]
                    )
                    insertTallaUseCase(nuevaTalla)
                }
                currentTalla += 1.0
                index++
            }
            onResult(true, "Producto y rango de tallas registrados")
        }
    }

    fun eliminarProducto(productoId: Long, onConfirmExtra: (Int, () -> Unit) -> Unit, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val pendingCount = checkPendingOrdersUseCase.forProducto(productoId)
            val action = {
                viewModelScope.launch {
                    eliminarProductoUseCase(productoId)
                    onResult(true, "Producto eliminado")
                }
                Unit
            }
            
            if (pendingCount > 0) {
                onConfirmExtra(pendingCount, action)
            } else {
                action()
            }
        }
    }

    fun eliminarTalla(productoId: Long, talla: Double, onConfirmExtra: (Int, () -> Unit) -> Unit, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val tallas = getTallas(productoId)
            val tallaObj = tallas.find { it.talla == talla }
            if (tallaObj == null) {
                onResult(false, "Talla no encontrada")
                return@launch
            }

            val pendingCount = checkPendingOrdersUseCase.forTalla(productoId, talla)
            val action = {
                viewModelScope.launch {
                    eliminarTallaUseCase(tallaObj.id)
                    onResult(true, "Talla eliminada")
                }
                Unit
            }

            if (pendingCount > 0) {
                onConfirmExtra(pendingCount, action)
            } else {
                action()
            }
        }
    }
}
