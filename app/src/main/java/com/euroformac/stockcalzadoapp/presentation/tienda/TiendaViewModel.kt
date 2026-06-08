package com.euroformac.stockcalzadoapp.presentation.tienda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import com.euroformac.stockcalzadoapp.domain.usecase.CrearPedidoAlmacenUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetTallaByBarcodeUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetProductoByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TiendaViewModel @Inject constructor(
    private val crearPedidoAlmacenUseCase: CrearPedidoAlmacenUseCase,
    private val getTallaByBarcodeUseCase: GetTallaByBarcodeUseCase,
    private val getProductoByIdUseCase: GetProductoByIdUseCase
) : ViewModel() {

    fun crearSolicitudPedido(codigoBarras: String) {
        viewModelScope.launch {
            val talla = getTallaByBarcodeUseCase(codigoBarras).firstOrNull()
            val producto = if (talla != null) {
                getProductoByIdUseCase(talla.productoId).firstOrNull()
            } else {
                null
            }

            val pedido = if (producto != null && talla != null) {
                PedidoAlmacen(
                    codigoBarras = codigoBarras,
                    nombreProducto = "${producto.marca} ${producto.modelo}",
                    talla = talla.talla.toString(),
                    color = producto.color,
                    estado = "PENDIENTE",
                    fechaCreacion = System.currentTimeMillis()
                )
            } else {
                PedidoAlmacen(
                    codigoBarras = codigoBarras,
                    nombreProducto = "Desconocido",
                    talla = "N/A",
                    color = "N/A",
                    estado = "PENDIENTE",
                    fechaCreacion = System.currentTimeMillis()
                )
            }
            crearPedidoAlmacenUseCase(pedido)
        }
    }
}
