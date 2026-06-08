package com.euroformac.stockcalzadoapp.data.repository

import com.euroformac.stockcalzadoapp.data.local.dao.MovimientoDao
import com.euroformac.stockcalzadoapp.data.local.entity.MovimientoEntity
import com.euroformac.stockcalzadoapp.domain.model.Movimiento
import com.euroformac.stockcalzadoapp.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovimientoRepositoryImpl(
    private val movimientoDao: MovimientoDao
) : MovimientoRepository {

    override fun getAllMovimientos(): Flow<List<Movimiento>> {
        return movimientoDao.getAllMovimientos().map { list ->
            list.map { mapToDomain(it) }
        }
    }

    override fun getMovimientosByProducto(productoId: Long): Flow<List<Movimiento>> {
        return movimientoDao.getMovimientosByProducto(productoId).map { list ->
            list.map { mapToDomain(it) }
        }
    }

    override suspend fun registrarMovimiento(movimiento: Movimiento) {
        val entity = MovimientoEntity(
            productoId = movimiento.productoId,
            talla = movimiento.talla,
            cantidad = movimiento.cantidad,
            tipo = movimiento.tipo,
            fecha = movimiento.fecha,
            origen = movimiento.origen
        )
        movimientoDao.insertMovimiento(entity)
    }

    private fun mapToDomain(entity: MovimientoEntity): Movimiento {
        return Movimiento(
            id = entity.id,
            productoId = entity.productoId,
            talla = entity.talla,
            cantidad = entity.cantidad,
            tipo = entity.tipo,
            fecha = entity.fecha,
            origen = entity.origen
        )
    }
}
