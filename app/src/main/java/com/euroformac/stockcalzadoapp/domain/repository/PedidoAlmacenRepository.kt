package com.euroformac.stockcalzadoapp.domain.repository

import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import kotlinx.coroutines.flow.Flow

interface PedidoAlmacenRepository {
    fun obtenerPedidosPendientes(): Flow<List<PedidoAlmacen>>
    fun obtenerTodosLosPedidos(): Flow<List<PedidoAlmacen>>
    suspend fun crearPedido(pedido: PedidoAlmacen)
    suspend fun completarPedidoPorCodigo(codigoBarras: String)
    suspend fun actualizarEstadoPedido(id: Long, nuevoEstado: String, fecha: Long)
}
