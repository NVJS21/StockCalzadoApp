package com.euroformac.stockcalzadoapp.data.repository

import com.euroformac.stockcalzadoapp.data.local.dao.PedidoAlmacenDao
import com.euroformac.stockcalzadoapp.data.local.entity.PedidoAlmacenEntity
import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PedidoAlmacenRepositoryImpl(
    private val dao: PedidoAlmacenDao
) : PedidoAlmacenRepository {

    override fun obtenerPedidosPendientes(): Flow<List<PedidoAlmacen>> {
        return dao.obtenerPedidosPendientes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun obtenerTodosLosPedidos(): Flow<List<PedidoAlmacen>> {
        return dao.obtenerTodosLosPedidos().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun crearPedido(pedido: PedidoAlmacen) {
        dao.insertarPedido(pedido.toEntity())
    }

    override suspend fun completarPedidoPorCodigo(codigoBarras: String) {
        dao.marcarPedidoCompletado(codigoBarras)
    }

    override suspend fun actualizarEstadoPedido(id: Long, nuevoEstado: String, fecha: Long) {
        dao.actualizarEstadoPedido(id, nuevoEstado, fecha)
    }

    private fun PedidoAlmacenEntity.toDomainModel(): PedidoAlmacen {
        return PedidoAlmacen(
            id = id,
            codigoBarras = codigoBarras,
            nombreProducto = nombreProducto,
            talla = talla,
            color = color,
            estado = estado,
            fechaCreacion = fechaCreacion,
            fechaFinalizacion = fechaFinalizacion
        )
    }

    private fun PedidoAlmacen.toEntity(): PedidoAlmacenEntity {
        return PedidoAlmacenEntity(
            id = id,
            codigoBarras = codigoBarras,
            nombreProducto = nombreProducto,
            talla = talla,
            color = color,
            estado = estado,
            fechaCreacion = fechaCreacion,
            fechaFinalizacion = fechaFinalizacion
        )
    }
}
