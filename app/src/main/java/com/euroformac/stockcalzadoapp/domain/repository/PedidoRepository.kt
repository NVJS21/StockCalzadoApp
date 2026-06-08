package com.euroformac.stockcalzadoapp.domain.repository

import com.euroformac.stockcalzadoapp.domain.model.LineaPedido
import com.euroformac.stockcalzadoapp.domain.model.Pedido
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    fun getAllPedidos(): Flow<List<Pedido>>
    fun getLineasByPedido(pedidoId: Long): Flow<List<LineaPedido>>
    suspend fun crearPedido(pedido: Pedido, lineas: List<LineaPedido>)
    suspend fun countPendingOrdersForProducto(productoId: Long): Int
    suspend fun countPendingOrdersForTalla(productoId: Long, talla: Double): Int
}
