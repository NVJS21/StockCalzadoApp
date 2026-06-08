package com.euroformac.stockcalzadoapp.domain.repository

import com.euroformac.stockcalzadoapp.domain.model.Movimiento
import kotlinx.coroutines.flow.Flow

interface MovimientoRepository {
    fun getAllMovimientos(): Flow<List<Movimiento>>
    fun getMovimientosByProducto(productoId: Long): Flow<List<Movimiento>>
    suspend fun registrarMovimiento(movimiento: Movimiento)
}
