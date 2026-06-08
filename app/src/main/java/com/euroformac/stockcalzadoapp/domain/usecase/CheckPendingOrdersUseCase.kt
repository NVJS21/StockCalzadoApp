package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.repository.PedidoRepository

class CheckPendingOrdersUseCase(
    private val repository: PedidoRepository
) {
    suspend fun forProducto(productoId: Long): Int {
        return repository.countPendingOrdersForProducto(productoId)
    }

    suspend fun forTalla(productoId: Long, talla: Double): Int {
        return repository.countPendingOrdersForTalla(productoId, talla)
    }
}
