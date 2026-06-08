package com.euroformac.stockcalzadoapp.data.repository

import com.euroformac.stockcalzadoapp.data.local.dao.PedidoDao
import com.euroformac.stockcalzadoapp.data.local.entity.LineaPedidoEntity
import com.euroformac.stockcalzadoapp.data.local.entity.PedidoEntity
import com.euroformac.stockcalzadoapp.domain.model.LineaPedido
import com.euroformac.stockcalzadoapp.domain.model.Pedido
import com.euroformac.stockcalzadoapp.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PedidoRepositoryImpl(
    private val pedidoDao: PedidoDao
) : PedidoRepository {

    override fun getAllPedidos(): Flow<List<Pedido>> {
        return pedidoDao.getAllPedidos().map { list ->
            list.map {
                Pedido(
                    id = it.id,
                    tipo = it.tipo,
                    estado = it.estado,
                    fechaCreacion = it.fechaCreacion,
                    notas = it.notas
                )
            }
        }
    }

    override fun getLineasByPedido(pedidoId: Long): Flow<List<LineaPedido>> {
        return pedidoDao.getLineasByPedido(pedidoId).map { list ->
            list.map {
                LineaPedido(
                    id = it.id,
                    pedidoId = it.pedidoId,
                    productoId = it.productoId,
                    talla = it.talla,
                    cantidad = it.cantidad
                )
            }
        }
    }

    override suspend fun crearPedido(pedido: Pedido, lineas: List<LineaPedido>) {
        val pedidoEntity = PedidoEntity(
            tipo = pedido.tipo,
            estado = pedido.estado,
            fechaCreacion = pedido.fechaCreacion,
            notas = pedido.notas
        )
        val pedidoId = pedidoDao.insertPedido(pedidoEntity)

        val lineasEntities = lineas.map {
            LineaPedidoEntity(
                pedidoId = pedidoId,
                productoId = it.productoId,
                talla = it.talla,
                cantidad = it.cantidad
            )
        }
        pedidoDao.insertLineasPedido(lineasEntities)
    }

    override suspend fun countPendingOrdersForProducto(productoId: Long): Int {
        return pedidoDao.countPendingOrdersForProducto(productoId)
    }

    override suspend fun countPendingOrdersForTalla(productoId: Long, talla: Double): Int {
        return pedidoDao.countPendingOrdersForTalla(productoId, talla)
    }
}
